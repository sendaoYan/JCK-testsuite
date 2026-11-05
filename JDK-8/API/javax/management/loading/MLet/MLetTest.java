/*
 * @test
 * @summary javax.management.loading.MLet conformance: concrete class reflection.
 * @run main MLetTest
 */

import java.io.InputStream;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;
import javax.management.loading.ClassLoaderRepository;
import javax.management.loading.MLet;

public class MLetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // Class name used for the cross-loader closed loop: its .class file is copied
    // into a temp directory so only a URL-based loader over that directory can
    // define it (the system classpath copy has the same name but a different
    // defining loader when a bootstrap-only parent is used).
    static final String DUMMY_NAME = "MLetTest$LoadedDummy";

    public static class LoadedDummy {
        public static final String TAG = "mlet-dummy";
    }

    // Bootstrap-only parent: cannot see application classes, so loadClass for
    // DUMMY_NAME must fall through to the MLet's own URL search path.
    static ClassLoader bootOnlyParent() {
        return new ClassLoader(null) { };
    }

    static Path copyDummyClass() throws Exception {
        Path dir = Files.createTempDirectory("jckmlet");
        try (InputStream in = MLetTest.class.getResourceAsStream(DUMMY_NAME + ".class")) {
            check(in != null, "dummy class resource found on classpath");
            Files.copy(in, dir.resolve(DUMMY_NAME + ".class"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return dir;
    }

    static void deleteRecursively(Path dir) throws Exception {
        Files.walk(dir, FileVisitOption.values())
             .sorted(Comparator.reverseOrder())
             .forEach(p -> { try { Files.delete(p); } catch (Exception e) { } });
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.MLet");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.net.URLClassLoader".equals(c.getSuperclass().getName()), "superclass java.net.URLClassLoader");
        check(Class.forName("javax.management.loading.MLetMBean").isAssignableFrom(c), "implements/extends javax.management.loading.MLetMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(Class.forName("java.io.Externalizable").isAssignableFrom(c), "implements/extends java.io.Externalizable");
        check(hasMethod(c, "addURL"), "declares addURL");
        check(hasMethod(c, "getLibraryDirectory"), "declares getLibraryDirectory");
        check(hasMethod(c, "getMBeansFromURL"), "declares getMBeansFromURL");
        // ---- deepened: constructor matrix / URL management ----
        testConstructorsAndURLs();
        // ---- deepened: class + resource loading semantics ----
        testClassAndResourceLoading();
        // ---- deepened: library directory + Externalizable refusal ----
        testLibraryDirectoryAndExternalizable();
        // ---- deepened: getMBeansFromURL state machine ----
        testGetMBeansFromURL();
        // ---- deepened: MBeanRegistration lifecycle + standard MBean view ----
        testRegistrationLifecycle();
        // ---- deepened: class loader repository participation closed loop ----
        testRepositoryClosedLoop();
        // ---- deepened: loadClass(String, ClassLoaderRepository) delegation ----
        testLoadClassWithCLR();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: constructor matrix / URL management ----
    static void testConstructorsAndURLs() throws Exception {
        Path dir = copyDummyClass();
        try {
            URL dirUrl = dir.toUri().toURL();
            URL sub = new URL(dirUrl, "sub/");

            // no-arg: empty URL list, library directory pre-initialized
            MLet m0 = new MLet();
            check(m0.getURLs() != null && m0.getURLs().length == 0, "no-arg getURLs empty");
            check(m0.getLibraryDirectory() != null, "no-arg library directory initialized");

            // (URL[]): exact read-back
            MLet m1 = new MLet(new URL[]{sub});
            URL[] got = m1.getURLs();
            check(got.length == 1 && sub.equals(got[0]), "(URL[]) getURLs read-back");
            check(m1.getURLs().length == 1, "(URL[]) repeated getURLs length");

            // (URL[], ClassLoader): parent delegation for system classes
            MLet m2 = new MLet(new URL[]{sub}, MLetTest.class.getClassLoader());
            check(m2.loadClass("java.lang.String") == String.class,
                    "(URL[],ClassLoader) delegates java.lang.String to parent singleton");
            check(m2.loadClass(MLetTest.class.getName()) == MLetTest.class,
                    "(URL[],ClassLoader) delegates app class to parent");

            // (URL[], boolean delegateToCLR=false): form constructs and reads back
            MLet m3 = new MLet(new URL[]{sub}, false);
            check(m3.getURLs().length == 1 && sub.equals(m3.getURLs()[0]),
                    "(URL[],boolean) getURLs read-back");

            // (URL[], ClassLoader, boolean)
            MLet m4 = new MLet(new URL[]{sub}, MLetTest.class.getClassLoader(), false);
            check(m4.getURLs().length == 1, "(URL[],ClassLoader,boolean) getURLs read-back");

            // (URL[], ClassLoader, URLStreamHandlerFactory, boolean): null factory allowed
            MLet m5 = new MLet(new URL[]{sub}, MLetTest.class.getClassLoader(), null, true);
            check(m5.getURLs().length == 1, "(URL[],ClassLoader,factory,boolean) getURLs read-back");

            // (URL[], ClassLoader, URLStreamHandlerFactory)
            MLet m6 = new MLet(new URL[]{sub}, MLetTest.class.getClassLoader(), null);
            check(m6.getURLs().length == 1, "(URL[],ClassLoader,factory) getURLs read-back");

            // addURL(URL): appended, and duplicate-suppressing (unlike raw URLClassLoader)
            m0.addURL(sub);
            check(m0.getURLs().length == 1, "addURL(URL) appends");
            m0.addURL(sub);
            check(m0.getURLs().length == 1, "addURL(URL) duplicate suppressed");
            URL other = new URL(dirUrl, "other/");
            m0.addURL(other);
            check(m0.getURLs().length == 2 && other.equals(m0.getURLs()[1]),
                    "addURL(URL) second distinct URL appended in order");

            // addURL(String): absolute spec accepted, duplicate suppressed
            m0.addURL(dirUrl.toExternalForm() + "str/");
            check(m0.getURLs().length == 3, "addURL(String absolute) appends");
            m0.addURL(dirUrl.toExternalForm() + "str/");
            check(m0.getURLs().length == 3, "addURL(String) duplicate suppressed");

            // addURL(String) with a relative spec: no URL context -> MalformedURLException
            // wrapped into ServiceNotFoundException with fixed message (verified on JDK-8)
            try {
                m0.addURL("relative/path");
                check(false, "addURL(relative) should throw");
            } catch (ServiceNotFoundException e) {
                check("The specified URL is malformed".equals(e.getMessage()),
                        "addURL(relative) message exact: " + e.getMessage());
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- deepened: class + resource loading semantics ----
    static void testClassAndResourceLoading() throws Exception {
        MLet m = new MLet();
        // delegation to parent for bootstrap-visible class
        check(m.loadClass("java.lang.String") == String.class, "loadClass java.lang.String");
        check(m.loadClass("java.util.ArrayList") == java.util.ArrayList.class,
                "loadClass java.util.ArrayList");
        // unknown class: ClassNotFoundException, message is the class name
        try {
            m.loadClass("no.such.Clazz");
            check(false, "loadClass unknown should throw");
        } catch (ClassNotFoundException e) {
            check("no.such.Clazz".equals(e.getMessage()),
                    "loadClass unknown message is class name: " + e.getMessage());
        }
        // resources: parent-first lookup
        URL u = m.getResource("java/lang/String.class");
        check(u != null, "getResource finds bootstrap resource");
        check(m.getResource("no/such/Resource.txt") == null, "getResource unknown null");
        InputStream is = m.getResourceAsStream("java/lang/String.class");
        check(is != null, "getResourceAsStream non-null");
        try {
            check(is.read() == 0xCA && is.read() == 0xFE && is.read() == 0xBA && is.read() == 0xBE,
                    "getResourceAsStream yields class-file magic");
        } finally {
            is.close();
        }
        Enumeration<URL> en = m.getResources("java/lang/String.class");
        check(en != null, "getResources never returns null");
        check(en.hasMoreElements(), "getResources has bootstrap element");
        check(m.getResources("no/such/Resource.txt") != null
                && !m.getResources("no/such/Resource.txt").hasMoreElements(),
                "getResources unknown yields empty non-null enumeration");
    }

    // ---- deepened: library directory + Externalizable refusal ----
    static void testLibraryDirectoryAndExternalizable() throws Exception {
        MLet m = new MLet();
        check(m.getLibraryDirectory() != null, "library directory defaults non-null");
        m.setLibraryDirectory("/jck/lib/dir");
        check("/jck/lib/dir".equals(m.getLibraryDirectory()),
                "setLibraryDirectory round-trip");
        m.setLibraryDirectory(null);
        check(m.getLibraryDirectory() == null, "setLibraryDirectory(null) accepted");
        // Externalizable is declared but serialization is refused
        try {
            m.writeExternal(null);
            check(false, "writeExternal should refuse");
        } catch (UnsupportedOperationException e) {
            check("MLet.writeExternal".equals(e.getMessage()),
                    "writeExternal message exact: " + e.getMessage());
        }
        try {
            m.readExternal(null);
            check(false, "readExternal should refuse");
        } catch (UnsupportedOperationException e) {
            check("MLet.readExternal".equals(e.getMessage()),
                    "readExternal message exact: " + e.getMessage());
        }
    }

    // ---- deepened: getMBeansFromURL state machine ----
    static void testGetMBeansFromURL() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        MLet unregistered = new MLet();

        // (URL) overload checks null BEFORE server registration state
        try {
            unregistered.getMBeansFromURL((URL) null);
            check(false, "getMBeansFromURL((URL)null) should throw");
        } catch (ServiceNotFoundException e) {
            check("The specified URL is null".equals(e.getMessage()),
                    "getMBeansFromURL((URL)null) message exact: " + e.getMessage());
        }
        // (String) overload checks server state FIRST
        try {
            unregistered.getMBeansFromURL((String) null);
            check(false, "getMBeansFromURL((String)null) unregistered should throw");
        } catch (IllegalStateException e) {
            check("This MLet MBean is not registered with an MBeanServer.".equals(e.getMessage()),
                    "unregistered (String)null message exact: " + e.getMessage());
        }
        try {
            unregistered.getMBeansFromURL("file:/no/such/file.mlet");
            check(false, "getMBeansFromURL(String) unregistered should throw");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().contains("not registered"),
                    "unregistered String URL message contains 'not registered'");
        }
        try {
            unregistered.getMBeansFromURL(new URL("file:/no/such/f2.mlet"));
            check(false, "getMBeansFromURL(URL) unregistered should throw");
        } catch (IllegalStateException e) {
            check(e.getMessage() != null && e.getMessage().contains("not registered"),
                    "unregistered URL delegation message contains 'not registered'");
        }

        // registered: file-missing URL -> ServiceNotFoundException naming the URL
        ObjectName on = new ObjectName("jck.mlet:o=mlet1");
        server.registerMBean(unregistered, on);
        try {
            unregistered.getMBeansFromURL("file:/no/such/file3.mlet");
            check(false, "getMBeansFromURL missing file should throw");
        } catch (ServiceNotFoundException e) {
            String msg = e.getMessage();
            check(msg != null && msg.startsWith("Problems while parsing URL ["),
                    "missing file message prefix: " + msg);
            check(msg != null && msg.contains("file:/no/such/file3.mlet"),
                    "missing file message contains URL");
            check(e.getCause() instanceof java.io.FileNotFoundException,
                    "missing file cause FileNotFoundException: " + e.getCause());
        }
        // unknown protocol -> MalformedURLException as cause
        try {
            unregistered.getMBeansFromURL("bogusproto://x/y");
            check(false, "getMBeansFromURL unknown protocol should throw");
        } catch (ServiceNotFoundException e) {
            check(e.getCause() instanceof java.net.MalformedURLException,
                    "unknown protocol cause MalformedURLException");
            check(e.getCause().getMessage() != null
                    && e.getCause().getMessage().contains("unknown protocol"),
                    "unknown protocol cause message contains 'unknown protocol'");
        }
        // registered: (String) null now reported by the null check
        try {
            unregistered.getMBeansFromURL((String) null);
            check(false, "getMBeansFromURL((String)null) registered should throw");
        } catch (ServiceNotFoundException e) {
            check("The specified URL is null".equals(e.getMessage()),
                    "registered (String)null message exact");
        }
        server.unregisterMBean(on);
    }

    // ---- deepened: MBeanRegistration lifecycle + standard MBean view ----
    static void testRegistrationLifecycle() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        MLet m = new MLet();
        // preRegister echoes a non-null name back (same instance)
        ObjectName given = new ObjectName("jck.mlet:lifecycle=given");
        check(m.preRegister(server, given) == given, "preRegister returns same ObjectName");
        // preRegister(null server, null name) -> NPE from default-name construction
        try {
            m.preRegister(null, null);
            check(false, "preRegister(null,null) should throw NPE");
        } catch (NullPointerException e) {
            passed++;
        }
        // preRegister(server, null) -> <defaultDomain>:type=MLet
        ObjectName def = m.preRegister(server, null);
        check(def.equals(new ObjectName(server.getDefaultDomain() + ":type=MLet")),
                "preRegister default name is <default>:type=MLet, got " + def);
        m.postRegister(Boolean.TRUE);
        m.preDeregister();
        m.postDeregister();
        passed++; // lifecycle no-ops callable

        // registerMBean with null name uses preRegister default name
        MLet m2 = new MLet();
        ObjectInstance oi = server.registerMBean(m2, null);
        check(oi.getObjectName().equals(
                new ObjectName(server.getDefaultDomain() + ":type=MLet")),
                "registerMBean(null) name from preRegister: " + oi.getObjectName());
        check("javax.management.loading.MLet".equals(oi.getClassName()),
                "ObjectInstance class name");

        // standard MBean view: attributes LibraryDirectory (rw) and URLs (ro)
        javax.management.MBeanInfo info = server.getMBeanInfo(oi.getObjectName());
        boolean lib = false, urls = false;
        for (MBeanAttributeInfo a : info.getAttributes()) {
            if ("LibraryDirectory".equals(a.getName())) {
                lib = true;
                check(a.isReadable() && a.isWritable() && !a.isIs(),
                        "LibraryDirectory read-write non-is");
                check("java.lang.String".equals(a.getType()), "LibraryDirectory type");
            }
            if ("URLs".equals(a.getName())) {
                urls = true;
                check(a.isReadable() && !a.isWritable(), "URLs read-only");
                check("[Ljava.net.URL;".equals(a.getType()), "URLs type URL[]");
            }
        }
        check(lib, "attribute LibraryDirectory exposed");
        check(urls, "attribute URLs exposed");

        // attribute round-trip through the server
        check(((URL[]) server.getAttribute(oi.getObjectName(), "URLs")).length == 0,
                "getAttribute URLs empty initially");
        server.setAttribute(oi.getObjectName(), new Attribute("LibraryDirectory", "/jck/x"));
        check("/jck/x".equals(server.getAttribute(oi.getObjectName(), "LibraryDirectory")),
                "setAttribute/getAttribute LibraryDirectory round-trip");

        check(server.isRegistered(oi.getObjectName()), "registered");
        server.unregisterMBean(oi.getObjectName());
        check(!server.isRegistered(oi.getObjectName()), "unregistered");
    }

    // ---- deepened: class loader repository participation closed loop ----
    static void testRepositoryClosedLoop() throws Exception {
        Path dir = copyDummyClass();
        try {
            URL dirUrl = dir.toUri().toURL();
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            ClassLoaderRepository repo = server.getClassLoaderRepository();
            ClassLoader sys = ClassLoader.getSystemClassLoader();

            // before: with the system loader excluded nobody can define DUMMY_NAME
            try {
                repo.loadClassWithout(sys, DUMMY_NAME);
                check(false, "repo.loadClassWithout pre-registration should fail");
            } catch (ClassNotFoundException e) {
                check(DUMMY_NAME.equals(e.getMessage()), "pre CNFE message is class name");
            }

            MLet mlet = new MLet(new URL[]{dirUrl}, bootOnlyParent());
            ObjectName on = new ObjectName("jck.mlet:o=repoMlet");
            server.registerMBean(mlet, on);

            // direct load: the MLet itself defines the class from its URL
            Class<?> direct = mlet.loadClass(DUMMY_NAME);
            check(direct != MLetTest.LoadedDummy.class,
                    "MLet defines its own Class instance (bootstrap-only parent)");
            check(direct.getClassLoader() == mlet, "defining loader is the MLet");
            check(MLetTest.LoadedDummy.TAG.equals(direct.getField("TAG").get(null)),
                    "same constant value across loaders");

            // via repository: the registered MLet is consulted
            Class<?> viaRepo = repo.loadClassWithout(sys, DUMMY_NAME);
            check(viaRepo.getClassLoader() == mlet,
                    "repo resolves through registered MLet (same defining loader)");
            // plain loadClass finds the classpath copy through the seeded system loader
            check(repo.loadClass(DUMMY_NAME) == MLetTest.LoadedDummy.class,
                    "plain repo.loadClass resolves classpath copy");
            check(server.getClassLoader(on) == mlet, "server.getClassLoader returns the MLet");

            server.unregisterMBean(on);
            try {
                repo.loadClassWithout(sys, DUMMY_NAME);
                check(false, "repo.loadClassWithout post-unregistration should fail");
            } catch (ClassNotFoundException e) {
                passed++;
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- deepened: loadClass(String, ClassLoaderRepository) delegation ----
    static void testLoadClassWithCLR() throws Exception {
        Path dir = copyDummyClass();
        try {
            URL dirUrl = dir.toUri().toURL();
            final MLet defining = new MLet(new URL[]{dirUrl}, bootOnlyParent());
            // fake repository: loadClassBefore defines through the 'defining' loader,
            // everything else fails -> proves which CLR entry MLet consults
            ClassLoaderRepository fake = new ClassLoaderRepository() {
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    throw new ClassNotFoundException(name);
                }
                public Class<?> loadClassWithout(ClassLoader exclude, String name)
                        throws ClassNotFoundException {
                    throw new ClassNotFoundException(name);
                }
                public Class<?> loadClassBefore(ClassLoader stop, String name)
                        throws ClassNotFoundException {
                    return Class.forName(name, false, defining);
                }
            };
            // parent delegation wins before the CLR is consulted
            MLet empty = new MLet(new URL[0], bootOnlyParent());
            check(empty.loadClass("java.lang.String", fake) == String.class,
                    "loadClass(name,clr) parent-first for bootstrap class");
            // URLs empty + parent blind + fake CLR -> resolved by loadClassBefore
            Class<?> viaClr = empty.loadClass(DUMMY_NAME, fake);
            check(viaClr.getClassLoader() == defining,
                    "loadClass(name,clr) falls through to clr.loadClassBefore(mlet, name)");
            // failure propagates unchanged
            try {
                empty.loadClass("NoExistQ", fake);
                check(false, "loadClass(unknown,clr) should throw");
            } catch (ClassNotFoundException e) {
                check("NoExistQ".equals(e.getMessage()),
                        "loadClass(unknown,clr) CNFE message is class name");
            }
        } finally {
            deleteRecursively(dir);
        }
    }
}

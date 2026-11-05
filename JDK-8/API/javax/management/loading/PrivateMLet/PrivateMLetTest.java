/*
 * @test
 * @summary javax.management.loading.PrivateMLet conformance: concrete class reflection.
 * @run main PrivateMLetTest
 */

import java.io.InputStream;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.loading.ClassLoaderRepository;
import javax.management.loading.MLet;
import javax.management.loading.MLetMBean;
import javax.management.loading.PrivateClassLoader;
import javax.management.loading.PrivateMLet;

public class PrivateMLetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static final String DUMMY_NAME = "PrivateMLetTest$LoadedDummy";

    public static class LoadedDummy {
        public static final String TAG = "priv-mlet-dummy";
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.PrivateMLet");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.loading.MLet".equals(c.getSuperclass().getName()), "superclass javax.management.loading.MLet");
        check(Class.forName("javax.management.loading.PrivateClassLoader").isAssignableFrom(c), "implements/extends javax.management.loading.PrivateClassLoader");
        // ---- deepened: constructor matrix + inheritance surface ----
        testConstructorsAndSurface();
        // ---- deepened: private semantics vs repository (closed loop) ----
        testPrivateSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: constructor matrix + inheritance surface ----
    static void testConstructorsAndSurface() throws Exception {
        check(PrivateMLet.class.getConstructors().length == 3,
                "exactly 3 public constructors");
        // (URL[], boolean)
        URL u = new URL("file:/jck/priv/dir/");
        PrivateMLet p1 = new PrivateMLet(new URL[]{u}, true);
        check(p1.getURLs().length == 1 && u.equals(p1.getURLs()[0]),
                "(URL[],boolean) getURLs read-back");
        check(p1.getLibraryDirectory() != null, "(URL[],boolean) library dir initialized");
        PrivateMLet p0 = new PrivateMLet(new URL[0], false);
        check(p0.getURLs().length == 0, "(URL[],boolean=false) empty URLs");
        // (URL[], ClassLoader, boolean): parent delegation for system classes
        PrivateMLet p2 = new PrivateMLet(new URL[]{u}, PrivateMLetTest.class.getClassLoader(), true);
        check(p2.loadClass("java.lang.String") == String.class,
                "(URL[],ClassLoader,boolean) delegates java.lang.String to parent");
        check(p2.loadClass(PrivateMLetTest.class.getName()) == PrivateMLetTest.class,
                "(URL[],ClassLoader,boolean) delegates app class to parent");
        // (URL[], ClassLoader, URLStreamHandlerFactory, boolean): null factory allowed,
        // null parent means the bootstrap loader
        PrivateMLet p3 = new PrivateMLet(new URL[]{u}, null, null, true);
        check(p3.getURLs().length == 1, "(URL[],ClassLoader,factory,boolean) read-back");
        check(p3.loadClass("java.lang.Object") == Object.class,
                "bootstrap parent still serves java.lang.Object");
        // inheritance surface: full MLet + marker contracts preserved
        check(p1 instanceof MLet, "PrivateMLet is-an MLet");
        check(p1 instanceof MLetMBean, "PrivateMLet is-an MLetMBean (standard MBean view)");
        check(p1 instanceof PrivateClassLoader, "PrivateMLet is-a PrivateClassLoader");
        // MLet operations inherited and functional
        p1.addURL(new URL("file:/jck/priv/dir2/"));
        check(p1.getURLs().length == 2, "inherited addURL(URL) works");
        p1.setLibraryDirectory("/jck/privlib");
        check("/jck/privlib".equals(p1.getLibraryDirectory()),
                "inherited set/getLibraryDirectory round-trip");
        try {
            p1.loadClass("no.such.Clazz");
            check(false, "inherited loadClass unknown should throw");
        } catch (ClassNotFoundException e) {
            check("no.such.Clazz".equals(e.getMessage()), "CNFE message is class name");
        }
        // no extra public methods of its own: everything comes from MLet
        check(cD().getDeclaredMethods().length == 0,
                "no own declared methods (behavior fully inherited from MLet)");
    }

    static Class<?> cD() throws Exception {
        return Class.forName("javax.management.loading.PrivateMLet");
    }

    // ---- deepened: private semantics vs repository (closed loop) ----
    static void testPrivateSemantics() throws Exception {
        Path dir = Files.createTempDirectory("jckpml");
        try {
            try (InputStream in = PrivateMLetTest.class.getResourceAsStream(DUMMY_NAME + ".class")) {
                check(in != null, "dummy resource found");
                Files.copy(in, dir.resolve(DUMMY_NAME + ".class"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            URL dirUrl = dir.toUri().toURL();
            ClassLoader bootOnly = new ClassLoader(null) { };
            ClassLoader sys = ClassLoader.getSystemClassLoader();

            // counterfactual: a PUBLIC MLet over the same URL IS consulted
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            ClassLoaderRepository repo = server.getClassLoaderRepository();
            MLet pub = new MLet(new URL[]{dirUrl}, bootOnly);
            ObjectName pubName = new ObjectName("jck.pml:o=pub");
            server.registerMBean(pub, pubName);
            Class<?> viaPub = repo.loadClassWithout(sys, DUMMY_NAME);
            check(viaPub.getClassLoader() == pub, "public MLet participates in repository");
            server.unregisterMBean(pubName);
            try {
                repo.loadClassWithout(sys, DUMMY_NAME);
                check(false, "after unregister expected CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }

            // the PrivateMLet: same URL, registered the same way, but excluded
            PrivateMLet priv = new PrivateMLet(new URL[]{dirUrl}, bootOnly, true);
            ObjectName privName = new ObjectName("jck.pml:o=priv");
            server.registerMBean(priv, privName);
            try {
                repo.loadClassWithout(sys, DUMMY_NAME);
                check(false, "private loader must stay out of the repository search list");
            } catch (ClassNotFoundException e) {
                check(DUMMY_NAME.equals(e.getMessage()),
                        "repository exclusion CNFE message is class name");
            }
            // named retrieval still works: PrivateClassLoader loaders are remembered
            check(server.getClassLoader(privName) == priv,
                    "server.getClassLoader(privateName) still resolves the loader");
            check(server.isRegistered(privName), "private MLet is a normal registered MBean");
            // the loader itself works fine outside the repository
            Class<?> direct = priv.loadClass(DUMMY_NAME);
            check(direct.getClassLoader() == priv, "PrivateMLet defines classes itself");
            check(direct != PrivateMLetTest.LoadedDummy.class,
                    "distinct Class instance from classpath copy");
            check(PrivateMLetTest.LoadedDummy.TAG.equals(direct.getField("TAG").get(null)),
                    "same constant value across loaders");
            check(priv.loadClass("java.lang.String") == String.class,
                    "PrivateMLet delegates system classes to parent");
            // registration bookkeeping cleanup
            server.unregisterMBean(privName);
            check(!server.isRegistered(privName), "unregistered");
        } finally {
            Files.walk(dir, FileVisitOption.values())
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> { try { Files.delete(p); } catch (Exception e) { } });
        }
    }
}


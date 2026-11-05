/*
 * @test
 * @summary javax.management.loading.ClassLoaderRepository conformance: white-box structure + black-box behavior.
 * @run main ClassLoaderRepositoryTest
 */

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.loading.ClassLoaderRepository;
import javax.management.loading.PrivateClassLoader;

public class ClassLoaderRepositoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }
    static final String DUMMY_NAME = "ClassLoaderRepositoryTest$LoadedDummy";

    public static class LoadedDummy {
        public static final String TAG = "clr-dummy";
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.ClassLoaderRepository");
        check(c.isInterface(), "is interface");
        Method[] mm = c.getMethods();
        check(has(mm, "loadClass"), "has loadClass");
        check(has(mm, "loadClassWithout"), "has loadClassWithout");
        check(has(mm, "loadClassBefore"), "has loadClassBefore");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation proving the interface contract is callable.
        ClassLoaderRepository repo = new ClassLoaderRepository() {
            public Class<?> loadClass(String className) throws ClassNotFoundException {
                return ClassLoader.getSystemClassLoader().loadClass(className);
            }
            public Class<?> loadClassWithout(ClassLoader exclude, String className)
                    throws ClassNotFoundException {
                return loadClass(className);
            }
            public Class<?> loadClassBefore(ClassLoader stop, String className)
                    throws ClassNotFoundException {
                return loadClass(className);
            }
        };

        check(repo.loadClass("java.lang.String") == String.class, "loadClass java.lang.String");
        check(repo.loadClass("java.lang.Integer") == Integer.class, "loadClass java.lang.Integer");
        check(repo.loadClassWithout(null, "java.util.List") == java.util.List.class,
                "loadClassWithout null exclude");
        check(repo.loadClassWithout(Thread.currentThread().getContextClassLoader(),
                "java.lang.Double") == Double.class, "loadClassWithout real exclude");
        check(repo.loadClassBefore(null, "java.util.ArrayList") == java.util.ArrayList.class,
                "loadClassBefore null stop");

        boolean cnfe = false;
        try {
            repo.loadClass("no.such.Class.Name.Here");
        } catch (ClassNotFoundException e) {
            cnfe = true;
        }
        check(cnfe, "loadClass ClassNotFoundException for unknown class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- deepened: exact interface surface ----
        testExactSurface();
        // ---- deepened: repository from a real MBeanServer (seeded loaders) ----
        testServerRepository();
        // ---- deepened: dynamic loader participation closed loop ----
        testDynamicParticipation();
        // ---- deepened: createMBeanServer tracked path ----
        testTrackedServerPath();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: exact interface surface ----
    static void testExactSurface() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.ClassLoaderRepository");
        Method[] ms = c.getMethods();
        check(ms.length == 3, "exactly 3 methods, got " + ms.length);
        for (Method m : ms) {
            check(m.getReturnType() == Class.class, "all methods return Class: " + m.getName());
            Class<?>[] pt = m.getParameterTypes();
            boolean twoArg = pt.length == 2 && pt[0] == ClassLoader.class && pt[1] == String.class;
            boolean oneArg = pt.length == 1 && pt[0] == String.class;
            check(oneArg || twoArg, "signature (String) or (ClassLoader,String): " + m.getName());
            check(m.getExceptionTypes().length == 1
                    && m.getExceptionTypes()[0] == ClassNotFoundException.class,
                    "declares ClassNotFoundException: " + m.getName());
        }
        check(c.getInterfaces().length == 0, "extends nothing");
        check(c.getMethods().length == 3, "no additional inherited interface members");
    }

    // ---- deepened: repository from a real MBeanServer (seeded loaders) ----
    static void testServerRepository() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ClassLoaderRepository repo = server.getClassLoaderRepository();
        check(repo != null, "server returns a repository");
        check(repo instanceof ClassLoaderRepository, "repository implements the interface");

        ClassLoader sys = ClassLoader.getSystemClassLoader();

        // loadClass: platform class + classpath class through the seeded loaders
        check(repo.loadClass("java.lang.String") == String.class, "loadClass java.lang.String");
        check(repo.loadClass(DUMMY_NAME) == ClassLoaderRepositoryTest.LoadedDummy.class,
                "loadClass app class via seeded system loader");
        check(repo.loadClass("java.util.HashMap") == java.util.HashMap.class,
                "loadClass java.util.HashMap");

        // unknown class: CNFE whose message is exactly the class name
        try {
            repo.loadClass("no.such.Clazz");
            check(false, "loadClass unknown should throw");
        } catch (ClassNotFoundException e) {
            check("no.such.Clazz".equals(e.getMessage()),
                    "CNFE message is class name: " + e.getMessage());
        }

        // loadClassWithout(null, name): null exclusion behaves as plain loadClass
        check(repo.loadClassWithout(null, "java.lang.String") == String.class,
                "loadClassWithout(null,..) as plain loadClass");
        check(repo.loadClassWithout(null, DUMMY_NAME)
                == ClassLoaderRepositoryTest.LoadedDummy.class,
                "loadClassWithout(null,..) finds classpath class");
        // loadClassWithout(sys, app-class): only the system loader can see it -> CNFE
        try {
            repo.loadClassWithout(sys, DUMMY_NAME);
            check(false, "loadClassWithout(sys, appClass) should fail");
        } catch (ClassNotFoundException e) {
            check(DUMMY_NAME.equals(e.getMessage()), "exclusion CNFE message is class name");
        }
        // loadClassWithout(sys, platform class) still fine
        check(repo.loadClassWithout(sys, "java.lang.String") == String.class,
                "loadClassWithout(sys, platform class)");

        // loadClassBefore(sys, platform class): loaders before system serve it
        check(repo.loadClassBefore(sys, "java.lang.String") == String.class,
                "loadClassBefore(sys, platform class)");
        // loadClassBefore(sys, app class): search stops AT the system loader -> CNFE
        try {
            repo.loadClassBefore(sys, DUMMY_NAME);
            check(false, "loadClassBefore(sys, appClass) should fail");
        } catch (ClassNotFoundException e) {
            passed++;
        }
        // loadClassBefore(null, name): null behaves as plain loadClass
        check(repo.loadClassBefore(null, DUMMY_NAME)
                == ClassLoaderRepositoryTest.LoadedDummy.class,
                "loadClassBefore(null,..) as plain loadClass");
    }

    // ---- deepened: dynamic loader participation closed loop ----
    static void testDynamicParticipation() throws Exception {
        Path dir = Files.createTempDirectory("jckclr");
        try {
            try (InputStream in = ClassLoaderRepositoryTest.class
                    .getResourceAsStream(DUMMY_NAME + ".class")) {
                check(in != null, "dummy resource found");
                Files.copy(in, dir.resolve(DUMMY_NAME + ".class"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            URL dirUrl = dir.toUri().toURL();
            ClassLoader bootOnly = new ClassLoader(null) { };
            ClassLoader sys = ClassLoader.getSystemClassLoader();
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            ClassLoaderRepository repo = server.getClassLoaderRepository();

            // no URL loader registered yet: app class invisible when system excluded
            try {
                repo.loadClassWithout(sys, DUMMY_NAME);
                check(false, "pre-registration expected CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }

            // registering a ClassLoader MBean adds it to the search list
            RepoLoader pub = new RepoLoader(dirUrl, bootOnly);
            ObjectName pubName = new ObjectName("jck.clr:o=loader");
            server.registerMBean(pub, pubName);
            Class<?> viaRepo = repo.loadClassWithout(sys, DUMMY_NAME);
            check(viaRepo.getClassLoader() == pub,
                    "registered loader consulted, defines the class itself");
            check(viaRepo != ClassLoaderRepositoryTest.LoadedDummy.class,
                    "distinct Class instance from the classpath copy");
            check(repo.loadClass(DUMMY_NAME) == ClassLoaderRepositoryTest.LoadedDummy.class,
                    "plain loadClass still prefers the seeded system loader");
            check(server.getClassLoader(pubName) == pub,
                    "server.getClassLoader by name returns the registered loader");

            // unregistering removes it again
            server.unregisterMBean(pubName);
            try {
                repo.loadClassWithout(sys, DUMMY_NAME);
                check(false, "post-unregistration expected CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }

            // a PrivateClassLoader loader is registered as MBean but stays out of the list
            PrivateRepoLoader priv = new PrivateRepoLoader(dirUrl, bootOnly);
            ObjectName privName = new ObjectName("jck.clr:o=priv");
            server.registerMBean(priv, privName);
            try {
                repo.loadClassWithout(sys, DUMMY_NAME);
                check(false, "private loader must stay out of the search list");
            } catch (ClassNotFoundException e) {
                check(DUMMY_NAME.equals(e.getMessage()), "private exclusion CNFE message");
            }
            check(server.getClassLoader(privName) == priv,
                    "private loader still retrievable by explicit name");
            check(priv.loadClass(DUMMY_NAME).getClassLoader() == priv,
                    "private loader still loads directly");
            server.unregisterMBean(privName);
        } finally {
            Files.walk(dir, FileVisitOption.values())
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> { try { Files.delete(p); } catch (Exception e) { } });
        }
    }

    // ---- deepened: createMBeanServer tracked path ----
    static void testTrackedServerPath() throws Exception {
        // createMBeanServer registers the server in the factory list; its repository
        // is fully wired and released with releaseMBeanServer
        MBeanServer tracked = MBeanServerFactory.createMBeanServer();
        try {
            check(MBeanServerFactory.findMBeanServer(null).contains(tracked),
                    "tracked server visible in findMBeanServer(null)");
            ClassLoaderRepository repo = tracked.getClassLoaderRepository();
            check(repo.loadClass("java.lang.String") == String.class,
                    "tracked server repository loads platform class");
            check(repo.loadClass(DUMMY_NAME) == ClassLoaderRepositoryTest.LoadedDummy.class,
                    "tracked server repository loads classpath class");
            try {
                repo.loadClass("no.such.Zz");
                check(false, "tracked repository unknown class expected CNFE");
            } catch (ClassNotFoundException e) {
                check("no.such.Zz".equals(e.getMessage()), "tracked CNFE message");
            }
        } finally {
            MBeanServerFactory.releaseMBeanServer(tracked);
        }
        check(!MBeanServerFactory.findMBeanServer(null).contains(tracked),
                "released server no longer tracked");
    }

    public interface RepoLoaderMBean { String getKind(); }
    public static class RepoLoader extends URLClassLoader implements RepoLoaderMBean {
        RepoLoader(URL u, ClassLoader p) { super(new URL[]{u}, p); }
        public String getKind() { return "repo"; }
    }

    public interface PrivateRepoLoaderMBean { String getKind(); }
    public static class PrivateRepoLoader extends URLClassLoader
            implements PrivateRepoLoaderMBean, PrivateClassLoader {
        PrivateRepoLoader(URL u, ClassLoader p) { super(new URL[]{u}, p); }
        public String getKind() { return "private"; }
    }
}


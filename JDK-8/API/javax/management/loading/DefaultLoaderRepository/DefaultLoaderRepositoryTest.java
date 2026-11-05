/*
 * @test
 * @summary javax.management.loading.DefaultLoaderRepository conformance: concrete class reflection.
 * @run main DefaultLoaderRepositoryTest
 */

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

@SuppressWarnings("deprecation")
public class DefaultLoaderRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static final String DUMMY_NAME = "DefaultLoaderRepositoryTest$LoadedDummy";

    public static class LoadedDummy {
        public static final String TAG = "dlr-dummy";
    }

    public interface TrackedLoaderMBean { String getKind(); }
    public static class TrackedLoader extends URLClassLoader implements TrackedLoaderMBean {
        TrackedLoader(URL u, ClassLoader p) { super(new URL[]{u}, p); }
        public String getKind() { return "tracked"; }
    }

    public static void main(String[] args) throws Exception {
        // ---- deepened: empty aggregation (must run before any server is created) ----
        testNoTrackedServer();
        Class<?> c = Class.forName("javax.management.loading.DefaultLoaderRepository");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "loadClass"), "declares loadClass");
        check(hasMethod(c, "loadClassWithout"), "declares loadClassWithout");
        // ---- deepened: deprecation + shape ----
        testDeprecationAndShape();
        // ---- deepened: static matrix over tracked servers (aggregation closed loop) ----
        testTrackedAggregation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: empty aggregation ----
    static void testNoTrackedServer() throws Exception {
        // DefaultLoaderRepository consults ONLY servers from
        // MBeanServerFactory.findMBeanServer(null) (createMBeanServer ones);
        // newMBeanServer() servers are invisible to it. With no tracked server
        // even java.lang.String cannot be loaded (verified on JDK-8).
        boolean anyTracked = !MBeanServerFactory.findMBeanServer(null).isEmpty();
        if (!anyTracked) {
            try {
                javax.management.loading.DefaultLoaderRepository.loadClass("java.lang.String");
                check(false, "no tracked server: loadClass should throw CNFE");
            } catch (ClassNotFoundException e) {
                check("java.lang.String".equals(e.getMessage()),
                        "empty aggregation CNFE message is the class name");
            }
            try {
                javax.management.loading.DefaultLoaderRepository
                        .loadClassWithout(null, "java.lang.String");
                check(false, "no tracked server: loadClassWithout should throw CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }
        } else {
            passed++; // defensive: another component pre-created a tracked server
        }
    }

    // ---- deepened: deprecation + shape ----
    static void testDeprecationAndShape() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.DefaultLoaderRepository");
        check(c.getAnnotation(Deprecated.class) != null, "class annotated @Deprecated");
        check(c.getConstructors().length == 1, "single public no-arg constructor");
        check(new javax.management.loading.DefaultLoaderRepository() != null,
                "public constructor instantiable");
        Method[] ms = c.getMethods();
        int own = 0;
        for (Method m : ms) {
            if (m.getDeclaringClass() != c) continue;
            own++;
            check(Modifier.isStatic(m.getModifiers()), "own method static: " + m.getName());
            check(m.getReturnType() == Class.class, "returns Class: " + m.getName());
            check(m.getExceptionTypes().length == 1
                    && m.getExceptionTypes()[0] == ClassNotFoundException.class,
                    "declares ClassNotFoundException: " + m.getName());
        }
        check(own == 2, "exactly 2 own public methods (loadClass, loadClassWithout)");
        // hierarchy: plain Object subclass, no interfaces
        check(c.getSuperclass() == Object.class, "direct Object subclass");
        check(c.getInterfaces().length == 0, "implements nothing");
    }

    // ---- deepened: static matrix over tracked servers (aggregation closed loop) ----
    static void testTrackedAggregation() throws Exception {
        Path dir = Files.createTempDirectory("jckdlr");
        MBeanServer tracked = MBeanServerFactory.createMBeanServer();
        try {
            try (InputStream in = DefaultLoaderRepositoryTest.class
                    .getResourceAsStream(DUMMY_NAME + ".class")) {
                check(in != null, "dummy resource found");
                Files.copy(in, dir.resolve(DUMMY_NAME + ".class"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            URL dirUrl = dir.toUri().toURL();
            ClassLoader bootOnly = new ClassLoader(null) { };
            ClassLoader sys = ClassLoader.getSystemClassLoader();

            // statics now resolve through the tracked server's repository
            check(javax.management.loading.DefaultLoaderRepository
                    .loadClass("java.lang.String") == String.class,
                    "loadClass java.lang.String via tracked server");
            check(javax.management.loading.DefaultLoaderRepository
                    .loadClass(DUMMY_NAME) == DefaultLoaderRepositoryTest.LoadedDummy.class,
                    "loadClass app class via seeded system loader");
            try {
                javax.management.loading.DefaultLoaderRepository.loadClass("no.such.Clazz");
                check(false, "unknown class expected CNFE");
            } catch (ClassNotFoundException e) {
                check("no.such.Clazz".equals(e.getMessage()),
                        "unknown CNFE message is class name");
            }
            // loadClassWithout(null, ..) behaves as plain loadClass
            check(javax.management.loading.DefaultLoaderRepository
                    .loadClassWithout(null, "java.lang.String") == String.class,
                    "loadClassWithout(null,..) as plain loadClass");

            // aggregation reaches loaders REGISTERED in the tracked server:
            // with the system loader excluded, only the registered URL loader helps
            try {
                javax.management.loading.DefaultLoaderRepository
                        .loadClassWithout(sys, DUMMY_NAME);
                check(false, "no registered loader yet: expected CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }
            TrackedLoader tl = new TrackedLoader(dirUrl, bootOnly);
            ObjectName tlName = new ObjectName("jck.dlr:o=loader");
            tracked.registerMBean(tl, tlName);
            Class<?> viaDlr = javax.management.loading.DefaultLoaderRepository
                    .loadClassWithout(sys, DUMMY_NAME);
            check(viaDlr.getClassLoader() == tl,
                    "aggregated repository consults the registered loader");
            check(viaDlr != DefaultLoaderRepositoryTest.LoadedDummy.class,
                    "distinct Class instance defined by the registered loader");
            tracked.unregisterMBean(tlName);
            try {
                javax.management.loading.DefaultLoaderRepository
                        .loadClassWithout(sys, DUMMY_NAME);
                check(false, "unregistered loader: expected CNFE again");
            } catch (ClassNotFoundException e) {
                passed++;
            }

            // ClassLoaderRepository obtained from the tracked server behaves identically
            ClassLoaderRepository repo = tracked.getClassLoaderRepository();
            check(repo.loadClass("java.lang.String") == String.class,
                    "tracked server repository loadClass");
        } finally {
            MBeanServerFactory.releaseMBeanServer(tracked);
            Files.walk(dir, FileVisitOption.values())
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> { try { Files.delete(p); } catch (Exception e) { } });
        }
    }
}

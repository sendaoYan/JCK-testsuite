/*
 * @test
 * @summary javax.management.loading.DefaultLoaderRepository conformance: white-box structure + black-box behavior.
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
import javax.management.loading.DefaultLoaderRepository;

@SuppressWarnings("deprecation")
public class DefaultLoaderRepositoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.DefaultLoaderRepository");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getAnnotation(Deprecated.class) != null, "has @Deprecated");
        check(c.getConstructors().length >= 1, "has public constructor");
        Method[] mm = c.getMethods();
        check(has(mm, "loadClass"), "has loadClass");
        check(has(mm, "loadClassWithout"), "has loadClassWithout");
        // static methods
        for (Method m : mm) {
            if (m.getName().equals("loadClass"))
                check(Modifier.isStatic(m.getModifiers()), "loadClass is static");
            if (m.getName().equals("loadClassWithout"))
                check(Modifier.isStatic(m.getModifiers()), "loadClassWithout is static");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            // A new empty MBeanServer contributes its own classloader repository.
            check(DefaultLoaderRepository.loadClass("java.lang.String") == String.class,
                    "loadClass java.lang.String");
            check(DefaultLoaderRepository.loadClassWithout(null, "java.lang.Integer") == Integer.class,
                    "loadClassWithout null exclude");

            boolean cnfe = false;
            try {
                DefaultLoaderRepository.loadClass("no.such.Class.Name.Here");
            } catch (ClassNotFoundException e) {
                cnfe = true;
            }
            check(cnfe, "loadClass ClassNotFoundException for unknown class");
        } finally {
            MBeanServerFactory.releaseMBeanServer(mbs);
        }
    }

    public static void main(String[] args) throws Exception {
        // ---- deepened: empty aggregation (must run before any server is created) ----
        testNoTrackedServer();
        testWhiteBox();
        testBlackBox();
        // ---- deepened: exact shape ----
        testExactShape();
        // ---- deepened: aggregation closed loop over a registered URL loader ----
        testTrackedAggregation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: empty aggregation ----
    static void testNoTrackedServer() throws Exception {
        // DefaultLoaderRepository consults ONLY servers from
        // MBeanServerFactory.findMBeanServer(null) (createMBeanServer ones);
        // newMBeanServer() servers are invisible to it. With no tracked server
        // even java.lang.String cannot be loaded (verified on JDK-27).
        boolean anyTracked = !MBeanServerFactory.findMBeanServer(null).isEmpty();
        if (!anyTracked) {
            try {
                DefaultLoaderRepository.loadClass("java.lang.String");
                check(false, "no tracked server: loadClass should throw CNFE");
            } catch (ClassNotFoundException e) {
                check("java.lang.String".equals(e.getMessage()),
                        "empty aggregation CNFE message is the class name");
            }
            try {
                DefaultLoaderRepository.loadClassWithout(null, "java.lang.String");
                check(false, "no tracked server: loadClassWithout should throw CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }
        } else {
            passed++; // defensive: another component pre-created a tracked server
        }
    }

    // ---- deepened: exact shape ----
    static void testExactShape() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.DefaultLoaderRepository");
        check(c.getSuperclass() == Object.class, "direct Object subclass");
        check(c.getInterfaces().length == 0, "implements nothing");
        check(c.getConstructors().length == 1, "single public no-arg constructor");
        check(new DefaultLoaderRepository() != null, "public constructor instantiable");
        int own = 0;
        for (Method m : c.getMethods()) {
            if (m.getDeclaringClass() != c) continue;
            own++;
            check(Modifier.isStatic(m.getModifiers()), "own method static: " + m.getName());
            check(m.getReturnType() == Class.class, "returns Class: " + m.getName());
            check(m.getExceptionTypes().length == 1
                    && m.getExceptionTypes()[0] == ClassNotFoundException.class,
                    "declares ClassNotFoundException: " + m.getName());
        }
        check(own == 2, "exactly 2 own public methods (loadClass, loadClassWithout)");
    }

    // ---- deepened: aggregation closed loop over a registered URL loader ----
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

            check(DefaultLoaderRepository.loadClass(DUMMY_NAME)
                    == DefaultLoaderRepositoryTest.LoadedDummy.class,
                    "static loadClass app class via tracked server");
            try {
                DefaultLoaderRepository.loadClass("no.such.Clazz");
                check(false, "unknown class expected CNFE");
            } catch (ClassNotFoundException e) {
                check("no.such.Clazz".equals(e.getMessage()),
                        "unknown CNFE message is class name");
            }
            // with the system loader excluded nobody can define DUMMY_NAME yet
            try {
                DefaultLoaderRepository.loadClassWithout(sys, DUMMY_NAME);
                check(false, "no registered loader yet: expected CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }
            // aggregation reaches loaders REGISTERED in the tracked server
            TrackedLoader tl = new TrackedLoader(dirUrl, bootOnly);
            ObjectName tlName = new ObjectName("jck.dlr:o=loader");
            tracked.registerMBean(tl, tlName);
            Class<?> viaDlr = DefaultLoaderRepository.loadClassWithout(sys, DUMMY_NAME);
            check(viaDlr.getClassLoader() == tl,
                    "aggregated repository consults the registered loader");
            check(viaDlr != DefaultLoaderRepositoryTest.LoadedDummy.class,
                    "distinct Class instance defined by the registered loader");
            tracked.unregisterMBean(tlName);
            try {
                DefaultLoaderRepository.loadClassWithout(sys, DUMMY_NAME);
                check(false, "unregistered loader: expected CNFE again");
            } catch (ClassNotFoundException e) {
                passed++;
            }
        } finally {
            MBeanServerFactory.releaseMBeanServer(tracked);
            Files.walk(dir, FileVisitOption.values())
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> { try { Files.delete(p); } catch (Exception e) { } });
        }
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
}

/*
 * @test
 * @summary javax.management.loading.PrivateClassLoader conformance: interface reflection.
 * @run main PrivateClassLoaderTest
 */

import java.io.InputStream;
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
import javax.management.loading.MLet;
import javax.management.loading.PrivateClassLoader;
import javax.management.loading.PrivateMLet;

public class PrivateClassLoaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.PrivateClassLoader");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        // ---- deepened: pure marker interface ----
        testMarkerShape();
        // ---- deepened: known implementors + repository exclusion semantics ----
        testImplementorsAndSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Class<?> pc() throws Exception {
        return Class.forName("javax.management.loading.PrivateClassLoader");
    }

    // ---- deepened: pure marker interface ----
    static void testMarkerShape() throws Exception {
        check(pc().getDeclaredMethods().length == 0, "declares no methods (marker)");
        check(pc().getMethods().length == 0,
                "getMethods empty for method-less interface (no Object members reported)");
        check(pc().getDeclaredFields().length == 0, "no fields");
        check(pc().getInterfaces().length == 0, "extends nothing");
        // custom loader can implement it without adding any method
        class Marked extends URLClassLoader implements PrivateClassLoader {
            Marked() { super(new URL[0]); }
        }
        ClassLoader cl = new Marked();
        check(cl instanceof PrivateClassLoader, "custom loader instanceof PrivateClassLoader");
        check(cl instanceof URLClassLoader, "custom loader still a URLClassLoader");
    }

    // ---- deepened: known implementors + repository exclusion semantics ----
    static void testImplementorsAndSemantics() throws Exception {
        // PrivateMLet is the JDK-provided private loader
        check(PrivateClassLoader.class.isAssignableFrom(PrivateMLet.class),
                "PrivateMLet implements PrivateClassLoader");
        // plain MLet is NOT private: it participates in the repository
        check(!PrivateClassLoader.class.isAssignableFrom(MLet.class),
                "MLet is not a PrivateClassLoader");

        // semantic proof: a PrivateClassLoader loader registered as an MBean is
        // excluded from the ClassLoaderRepository search list, while a public
        // loader is consulted (verified against a real MBeanServer).
        Path dir = Files.createTempDirectory("jckpriv");
        try {
            try (InputStream in = PrivateClassLoaderTest.class
                    .getResourceAsStream("PrivateClassLoaderTest$Dummy.class")) {
                check(in != null, "dummy resource found");
                Files.copy(in, dir.resolve("PrivateClassLoaderTest$Dummy.class"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            final String dummyName = "PrivateClassLoaderTest$Dummy";
            URL dirUrl = dir.toUri().toURL();
            ClassLoader bootOnly = new ClassLoader(null) { };
            ClassLoader sys = ClassLoader.getSystemClassLoader();

            // public loader: URLClassLoader subclass (not private) as a standard MBean
            PublicLoader pub = new PublicLoader(dirUrl, bootOnly);
            PrivateLoader priv = new PrivateLoader(dirUrl, bootOnly);

            MBeanServer server = MBeanServerFactory.newMBeanServer();
            ClassLoaderRepository repo = server.getClassLoaderRepository();
            try {
                repo.loadClassWithout(sys, dummyName);
                check(false, "no loader registered yet: expected CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }
            ObjectName pubName = new ObjectName("jck.pcl:o=pub");
            server.registerMBean(pub, pubName);
            Class<?> viaRepo = repo.loadClassWithout(sys, dummyName);
            check(viaRepo.getClassLoader() == pub, "public loader consulted by repository");
            server.unregisterMBean(pubName);

            ObjectName privName = new ObjectName("jck.pcl:o=priv");
            server.registerMBean(priv, privName);
            try {
                repo.loadClassWithout(sys, dummyName);
                check(false, "private loader must not be consulted by repository");
            } catch (ClassNotFoundException e) {
                check(dummyName.equals(e.getMessage()), "private exclusion CNFE names class");
            }
            // but it is still reachable by explicit name
            check(server.getClassLoader(privName) == priv,
                    "server.getClassLoader retrieves private loader by name");
            // and directly usable as a loader
            check(priv.loadClass(dummyName).getClassLoader() == priv,
                    "private loader still loads by itself");
            server.unregisterMBean(privName);
        } finally {
            Files.walk(dir, FileVisitOption.values())
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> { try { Files.delete(p); } catch (Exception e) { } });
        }
    }

    public static class Dummy { }

    // naming convention keeps Standard MBean compliance (class name + "MBean")
    public interface PublicLoaderMBean { String getKind(); }
    public static class PublicLoader extends URLClassLoader implements PublicLoaderMBean {
        PublicLoader(URL u, ClassLoader p) { super(new URL[]{u}, p); }
        public String getKind() { return "public"; }
    }

    public interface PrivateLoaderMBean { String getKind(); }
    public static class PrivateLoader extends URLClassLoader implements PrivateLoaderMBean, PrivateClassLoader {
        PrivateLoader(URL u, ClassLoader p) { super(new URL[]{u}, p); }
        public String getKind() { return "private"; }
    }
}


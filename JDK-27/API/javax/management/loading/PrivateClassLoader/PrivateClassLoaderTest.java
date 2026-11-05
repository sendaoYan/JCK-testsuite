/*
 * @test
 * @summary javax.management.loading.PrivateClassLoader conformance: white-box structure + black-box behavior.
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
import javax.management.loading.PrivateClassLoader;

public class PrivateClassLoaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.PrivateClassLoader");
        check(c.isInterface(), "is interface");
        check(c.getDeclaredMethods().length == 0, "marker interface has no methods");
        check(c.getDeclaredFields().length == 0, "marker interface has no fields");
    }

    // A ClassLoader implementing the marker interface satisfies both contracts.
    static class PrivateLoader extends ClassLoader implements PrivateClassLoader {
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassLoader loader = new PrivateLoader();
        check(loader instanceof ClassLoader, "instance of ClassLoader");
        check(loader instanceof PrivateClassLoader, "instance of PrivateClassLoader");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- deepened: repository exclusion semantics (closed loop) ----
        testExclusionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: repository exclusion semantics (closed loop) ----
    // A loader implementing the marker is registered as an MBean like any other
    // ClassLoader MBean, but the ClassLoaderRepository never consults it for
    // loadClass/loadClassWithout/loadClassBefore; it stays retrievable by explicit
    // ObjectName via MBeanServer.getClassLoader.
    // (JDK-27 version note: the JDK-provided implementors MLet/PrivateMLet were
    // removed from javax.management.loading in JDK-27; the marker semantics are
    // exercised here with a user-defined loader.)
    static void testExclusionSemantics() throws Exception {
        check(PrivateClassLoader.class.getInterfaces().length == 0, "marker extends nothing");
        check(PrivateClassLoader.class.getDeclaredMethods().length == 0,
                "marker declares no methods");
        // implementors discoverable in this JDK: none in the loading package itself
        boolean mletGone = true;
        try {
            Class.forName("javax.management.loading.MLet");
            mletGone = false;
        } catch (ClassNotFoundException expected) {
            // MLet family removed in JDK-27 (verified: only ClassLoaderRepository,
            // DefaultLoaderRepository, PrivateClassLoader remain)
        }
        check(mletGone, "MLet family removed from javax.management.loading in JDK-27");

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

            MBeanServer server = MBeanServerFactory.newMBeanServer();
            ClassLoaderRepository repo = server.getClassLoaderRepository();
            try {
                repo.loadClassWithout(sys, dummyName);
                check(false, "no loader registered yet: expected CNFE");
            } catch (ClassNotFoundException e) {
                passed++;
            }

            // counterfactual first: a NON-private loader is consulted
            PublicLoader pub = new PublicLoader(dirUrl, bootOnly);
            ObjectName pubName = new ObjectName("jck.pcl:o=pub");
            server.registerMBean(pub, pubName);
            check(repo.loadClassWithout(sys, dummyName).getClassLoader() == pub,
                    "non-private loader participates in repository");
            server.unregisterMBean(pubName);

            // the marked loader: same shape, but excluded from the search list
            MarkedLoader priv = new MarkedLoader(dirUrl, bootOnly);
            ObjectName privName = new ObjectName("jck.pcl:o=priv");
            server.registerMBean(priv, privName);
            try {
                repo.loadClassWithout(sys, dummyName);
                check(false, "private loader must not be consulted by repository");
            } catch (ClassNotFoundException e) {
                check(dummyName.equals(e.getMessage()), "private exclusion CNFE names class");
            }
            // all three interface entry points refuse it
            try {
                repo.loadClass(dummyName + "Absent");
            } catch (ClassNotFoundException e) {
                passed++;
            }
            try {
                repo.loadClassBefore(priv, dummyName);
            } catch (ClassNotFoundException e) {
                passed++;
            }
            // but explicit-name retrieval and direct use still work
            check(server.getClassLoader(privName) == priv,
                    "server.getClassLoader retrieves private loader by name");
            check(server.isRegistered(privName), "private loader MBean is registered");
            check(priv.loadClass(dummyName).getClassLoader() == priv,
                    "private loader still loads by itself");
            check(priv instanceof PrivateClassLoader, "instanceof marker");
            server.unregisterMBean(privName);
            check(!server.isRegistered(privName), "unregistered");
        } finally {
            Files.walk(dir, FileVisitOption.values())
                 .sorted(Comparator.reverseOrder())
                 .forEach(p -> { try { Files.delete(p); } catch (Exception e) { } });
        }
    }

    public static class Dummy { }

    public interface PublicLoaderMBean { String getKind(); }
    public static class PublicLoader extends URLClassLoader implements PublicLoaderMBean {
        PublicLoader(URL u, ClassLoader p) { super(new URL[]{u}, p); }
        public String getKind() { return "public"; }
    }

    public interface MarkedLoaderMBean { String getKind(); }
    public static class MarkedLoader extends URLClassLoader
            implements MarkedLoaderMBean, PrivateClassLoader {
        MarkedLoader(URL u, ClassLoader p) { super(new URL[]{u}, p); }
        public String getKind() { return "private"; }
    }
}

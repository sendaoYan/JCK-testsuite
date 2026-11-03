/*
 * @test
 * @summary javax.management.remote.rmi.RMIServerImpl conformance: white-box structure + black-box behavior.
 * @run main RMIServerImplTest
 */

import java.io.Closeable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.management.ManagementFactory;
import java.rmi.Remote;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.security.auth.Subject;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl;

public class RMIServerImplTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(RMIServer.class.isAssignableFrom(c), "implements RMIServer");
        Method[] mm = c.getMethods();
        check(has(mm, "toStub"), "toStub");
        check(has(mm, "newClient"), "newClient");
        check(has(mm, "close"), "close");
        check(has(mm, "getVersion"), "getVersion");
        check(has(mm, "setMBeanServer"), "setMBeanServer");
        check(has(mm, "getMBeanServer"), "getMBeanServer");
        check(has(mm, "setDefaultClassLoader"), "setDefaultClassLoader");
        check(has(mm, "getDefaultClassLoader"), "getDefaultClassLoader");
        // Protected lifecycle hooks are declared on the abstract class.
        Method[] dm = c.getDeclaredMethods();
        check(has(dm, "export"), "export");
        check(has(dm, "getProtocol"), "getProtocol");
        check(has(dm, "closeServer"), "closeServer");
        check(has(dm, "makeClient"), "makeClient");
        check(has(dm, "closeClient"), "closeClient");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        RMIServerImpl impl = new RMIServerImpl(new HashMap<String, Object>()) {
            protected void export() { }
            public Remote toStub() { return null; }
            protected RMIConnection makeClient(String id, Subject subject) { return null; }
            protected void closeClient(RMIConnection client) { }
            protected String getProtocol() { return "rmi"; }
            protected void closeServer() { }
        };

        check(impl.getVersion().startsWith("1.0 "), "getVersion prefix: " + impl.getVersion());
        check(impl.getMBeanServer() == null, "mbean server initially null");
        check(impl.getDefaultClassLoader() == null, "class loader initially null");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        impl.setMBeanServer(mbs);
        check(impl.getMBeanServer() == mbs, "set/getMBeanServer round-trip");

        ClassLoader cl = RMIServerImplTest.class.getClassLoader();
        impl.setDefaultClassLoader(cl);
        check(impl.getDefaultClassLoader() == cl, "set/getDefaultClassLoader round-trip");

        impl.close(); // closeServer() is a no-op for this anonymous subclass.
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

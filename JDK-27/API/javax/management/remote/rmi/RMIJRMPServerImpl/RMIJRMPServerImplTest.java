/*
 * @test
 * @summary javax.management.remote.rmi.RMIJRMPServerImpl conformance: white-box structure + black-box behavior.
 * @run main RMIJRMPServerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.management.ManagementFactory;
import java.util.HashMap;

import javax.management.MBeanServer;

import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServerImpl;

public class RMIJRMPServerImplTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIJRMPServerImpl");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(RMIServerImpl.class.isAssignableFrom(c), "extends RMIServerImpl");
        Method[] dm = c.getDeclaredMethods();
        check(has(dm, "toStub"), "toStub");
        check(has(dm, "getProtocol"), "getProtocol");
        check(has(dm, "export"), "export");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // Port 0, no socket factories, and no export: constructing must not bind anything.
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        check(impl.getVersion().startsWith("1.0 "), "getVersion prefix");
        check(impl.getMBeanServer() == null, "mbean server initially null");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        impl.setMBeanServer(mbs);
        check(impl.getMBeanServer() == mbs, "set/getMBeanServer round-trip");

        ClassLoader cl = RMIJRMPServerImplTest.class.getClassLoader();
        impl.setDefaultClassLoader(cl);
        check(impl.getDefaultClassLoader() == cl, "set/getDefaultClassLoader round-trip");

        // Note: close() is intentionally not called here. For a JRMP server that
        // was never exported, closeServer() attempts to unexport the object and
        // throws NoSuchObjectException; no ports were bound and no threads were
        // started, so there is nothing to clean up.

        // Negative port is rejected with a stable IllegalArgumentException.
        try {
            new RMIJRMPServerImpl(-1, null, null, null);
            check(false, "expected IAE for negative port");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Negative port: -1"), "negative port message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

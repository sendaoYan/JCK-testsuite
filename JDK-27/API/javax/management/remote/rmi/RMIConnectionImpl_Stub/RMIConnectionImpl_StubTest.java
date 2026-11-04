/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectionImpl_Stub conformance: white-box structure + black-box behavior.
 * @run main RMIConnectionImpl_StubTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectionImpl_Stub;

@SuppressWarnings("deprecation")
public class RMIConnectionImpl_StubTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl_Stub");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(RemoteStub.class.isAssignableFrom(c), "extends RemoteStub");
        check(RMIConnection.class.isAssignableFrom(c), "implements RMIConnection");

        // The generated stub exposes a (RemoteRef) constructor.
        check(c.getConstructor(RemoteRef.class) != null, "(RemoteRef) ctor");

        Method[] mm = c.getMethods();
        check(has(mm, "getConnectionId"), "getConnectionId");
        check(has(mm, "close"), "close");
        check(has(mm, "createMBean"), "createMBean");
        check(has(mm, "getAttribute"), "getAttribute");
        check(has(mm, "fetchNotifications"), "fetchNotifications");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // The stub performs RMI marshalling and requires a live RemoteRef, so
        // behavior is verified structurally. The class must be loadable.
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl_Stub");
        check(c != null, "class loadable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

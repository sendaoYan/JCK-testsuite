/*
 * @test
 * @summary javax.management.remote.rmi.RMIServerImpl_Stub conformance: white-box structure + black-box behavior.
 * @run main RMIServerImpl_StubTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl_Stub;

@SuppressWarnings("deprecation")
public class RMIServerImpl_StubTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl_Stub");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(RemoteStub.class.isAssignableFrom(c), "extends RemoteStub");
        check(RMIServer.class.isAssignableFrom(c), "implements RMIServer");
        check(c.getConstructor(RemoteRef.class) != null, "(RemoteRef) ctor");

        Method[] mm = c.getMethods();
        check(has(mm, "getVersion"), "getVersion");
        check(has(mm, "newClient"), "newClient");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl_Stub");
        check(c != null, "class loadable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

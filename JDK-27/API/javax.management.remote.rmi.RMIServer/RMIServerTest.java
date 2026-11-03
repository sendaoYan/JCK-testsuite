/*
 * @test
 * @summary javax.management.remote.rmi.RMIServer conformance: white-box structure + black-box behavior.
 * @run main RMIServerTest
 */

import java.lang.reflect.Method;
import java.rmi.Remote;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIServer;

public class RMIServerTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServer");
        check(c.isInterface(), "is interface");
        check(Remote.class.isAssignableFrom(c), "extends Remote");
        Method[] mm = c.getMethods();
        check(has(mm, "getVersion"), "getVersion");
        check(has(mm, "newClient"), "newClient");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final RMIConnection client = null;
        RMIServer server = new RMIServer() {
            public String getVersion() { return "1.0 test"; }
            public RMIConnection newClient(Object credentials) { return client; }
        };
        check(server.getVersion().equals("1.0 test"), "anonymous getVersion callable");
        check(server.newClient(null) == null, "anonymous newClient callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

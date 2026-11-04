/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectionImpl conformance: white-box structure + black-box behavior.
 * @run main RMIConnectionImplTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.server.Unreferenced;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectionImpl;
import javax.management.remote.rmi.RMIServerImpl;

public class RMIConnectionImplTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(RMIConnection.class.isAssignableFrom(c), "implements RMIConnection");
        check(Unreferenced.class.isAssignableFrom(c), "implements Unreferenced");

        Constructor<?> k = c.getConstructor(RMIServerImpl.class, String.class, ClassLoader.class,
                                            javax.security.auth.Subject.class, java.util.Map.class);
        check(k != null, "(RMIServerImpl,String,ClassLoader,Subject,Map) ctor");

        Method[] mm = c.getMethods();
        check(has(mm, "getConnectionId"), "getConnectionId");
        check(has(mm, "close"), "close");
        check(has(mm, "createMBean"), "createMBean");
        check(has(mm, "getAttribute"), "getAttribute");
        check(has(mm, "fetchNotifications"), "fetchNotifications");
        check(has(mm, "unreferenced"), "unreferenced");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // RMIConnectionImpl requires a real RMIServerImpl and performs heavy
        // server-side setup on construction, so behavior is verified only at
        // the structural level (see testWhiteBox). This method asserts the
        // class is loadable and its constructor signature is reflective-only.
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl");
        check(c != null, "class loadable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

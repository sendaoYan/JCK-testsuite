/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnection conformance: white-box structure + black-box behavior.
 * @run main RMIConnectionTest
 */

import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.Remote;

import javax.management.remote.NotificationResult;
import javax.management.remote.rmi.RMIConnection;

public class RMIConnectionTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnection");
        check(c.isInterface(), "is interface");
        check(Closeable.class.isAssignableFrom(c), "extends Closeable");
        check(Remote.class.isAssignableFrom(c), "extends Remote");
        Method[] mm = c.getMethods();
        check(has(mm, "getConnectionId"), "getConnectionId");
        check(has(mm, "close"), "close");
        check(has(mm, "createMBean"), "createMBean");
        check(has(mm, "unregisterMBean"), "unregisterMBean");
        check(has(mm, "getAttribute"), "getAttribute");
        check(has(mm, "setAttribute"), "setAttribute");
        check(has(mm, "invoke"), "invoke");
        check(has(mm, "getMBeanInfo"), "getMBeanInfo");
        check(has(mm, "fetchNotifications"), "fetchNotifications");
        check(has(mm, "addNotificationListener"), "addNotificationListener");
        check(has(mm, "removeNotificationListener"), "removeNotificationListener");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final boolean[] closed = new boolean[1];
        final NotificationResult result = new NotificationResult(1L, 2L, new javax.management.remote.TargetedNotification[0]);
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if (name.equals("getConnectionId")) return "conn-42";
                if (name.equals("fetchNotifications")) return result;
                if (name.equals("close")) { closed[0] = true; return null; }
                Class<?> rt = method.getReturnType();
                if (!rt.isPrimitive()) return null;
                if (rt == boolean.class) return false;
                if (rt == char.class) return (char) 0;
                if (rt == byte.class) return (byte) 0;
                if (rt == short.class) return (short) 0;
                if (rt == int.class) return 0;
                if (rt == long.class) return 0L;
                if (rt == float.class) return 0f;
                if (rt == double.class) return 0d;
                return null;
            }
        };
        RMIConnection conn = (RMIConnection) Proxy.newProxyInstance(
            RMIConnection.class.getClassLoader(),
            new Class<?>[]{ RMIConnection.class },
            handler);

        check(conn.getConnectionId().equals("conn-42"), "getConnectionId callable");
        check(conn.fetchNotifications(0, 10, 0) == result, "fetchNotifications callable");
        conn.close();
        check(closed[0], "close callable");
        check(conn.getMBeanCount(null) == null, "other methods return default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.management.remote.MBeanServerForwarder conformance: white-box structure + black-box behavior.
 * @run main MBeanServerForwarderTest
 */

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.management.MBeanServer;
import javax.management.remote.MBeanServerForwarder;

public class MBeanServerForwarderTest {
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
        Class<?> c = Class.forName("javax.management.remote.MBeanServerForwarder");
        check(c.isInterface(), "is interface");
        check(MBeanServer.class.isAssignableFrom(c), "extends MBeanServer");
        Method[] mm = c.getMethods();
        check(has(mm, "getMBeanServer"), "getMBeanServer");
        check(has(mm, "setMBeanServer"), "setMBeanServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // A dynamic proxy proves the two forwarder methods are callable
        // without writing a 30-method MBeanServer implementation by hand.
        final MBeanServer[] set = new MBeanServer[1];
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if (name.equals("getMBeanServer")) return set[0];
                if (name.equals("setMBeanServer")) { set[0] = (MBeanServer) args[0]; return null; }
                // Default return for other MBeanServer methods: null/false/0.
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
        MBeanServerForwarder fwd = (MBeanServerForwarder) Proxy.newProxyInstance(
            MBeanServerForwarder.class.getClassLoader(),
            new Class<?>[]{ MBeanServerForwarder.class },
            handler);

        check(fwd.getMBeanServer() == null, "getMBeanServer initially null");
        MBeanServer delegate = java.lang.management.ManagementFactory.getPlatformMBeanServer();
        fwd.setMBeanServer(delegate);
        check(fwd.getMBeanServer() == delegate, "set/getMBeanServer round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

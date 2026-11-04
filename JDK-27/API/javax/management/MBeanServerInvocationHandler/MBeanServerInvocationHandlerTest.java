/*
 * @test
 * @summary javax.management.MBeanServerInvocationHandler conformance: white-box structure + black-box behavior.
 * @run main MBeanServerInvocationHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class MBeanServerInvocationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
        void setCount(int c);
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerInvocationHandler");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(InvocationHandler.class.isAssignableFrom(c), "implements InvocationHandler");
        check(hasMethod(c, "getMBeanServerConnection"), "has getMBeanServerConnection");
        check(hasMethod(c, "getObjectName"), "has getObjectName");
        check(hasMethod(c, "isMXBean"), "has isMXBean");
        check(hasMethod(c, "newProxyInstance"), "has static newProxyInstance");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=MBeanServerInvocationHandlerTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        try {
            // constructor + accessors
            MBeanServerInvocationHandler handler = new MBeanServerInvocationHandler(mbs, on);
            check(handler.getMBeanServerConnection() == mbs, "getMBeanServerConnection");
            check(handler.getObjectName().equals(on), "getObjectName");
            check(!handler.isMXBean(), "isMXBean false for standard MBean");

            MBeanServerInvocationHandler mxHandler = new MBeanServerInvocationHandler(mbs, on, true);
            check(mxHandler.isMXBean(), "isMXBean true when requested");

            // static newProxyInstance produces a working proxy
            ThingMBean proxy = MBeanServerInvocationHandler.newProxyInstance(mbs, on, ThingMBean.class, false);
            check(proxy.getCount() == 7, "proxy getCount");
            proxy.setCount(50);
            check(proxy.getCount() == 50, "proxy setCount round-trip");

            // direct invoke() through InvocationHandler contract
            Method getCount = ThingMBean.class.getMethod("getCount");
            Object result;
            try {
                result = handler.invoke(proxy, getCount, null);
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
            check(((Integer) result).intValue() == 50, "invoke via handler");
        } finally {
            mbs.unregisterMBean(on);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.management.JMX conformance: white-box structure + black-box behavior.
 * @run main JMXTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class JMXTest {
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
    public interface ThingMXBean {
        int getCount();
    }
    public static class ThingX implements ThingMXBean {
        public int getCount() { return 9; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.JMX");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(hasMethod(c, "newMBeanProxy"), "has newMBeanProxy");
        check(hasMethod(c, "newMXBeanProxy"), "has newMXBeanProxy");
        check(hasMethod(c, "isMXBeanInterface"), "has isMXBeanInterface");
        check("defaultValue".equals(JMX.DEFAULT_VALUE_FIELD), "DEFAULT_VALUE_FIELD");
        check("immutableInfo".equals(JMX.IMMUTABLE_INFO_FIELD), "IMMUTABLE_INFO_FIELD");
        check("interfaceClassName".equals(JMX.INTERFACE_CLASS_NAME_FIELD), "INTERFACE_CLASS_NAME_FIELD");
        check("mxbean".equals(JMX.MXBEAN_FIELD), "MXBEAN_FIELD");
        check("openType".equals(JMX.OPEN_TYPE_FIELD), "OPEN_TYPE_FIELD");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // isMXBeanInterface: interface named *MXBean is an MXBean; *MBean is not.
        check(JMX.isMXBeanInterface(ThingMXBean.class), "ThingMXBean is MXBean interface");
        check(!JMX.isMXBeanInterface(ThingMBean.class), "ThingMBean is not MXBean interface");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=JMXTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        try {
            // newMBeanProxy (notificationBroadcast=false)
            ThingMBean proxy = JMX.newMBeanProxy(mbs, on, ThingMBean.class);
            check(proxy.getCount() == 7, "proxy getCount");
            proxy.setCount(100);
            check(proxy.getCount() == 100, "proxy setCount round-trip");

            // newMBeanProxy with broadcast flag
            ThingMBean proxy2 = JMX.newMBeanProxy(mbs, on, ThingMBean.class, true);
            check(proxy2.getCount() == 100, "broadcast proxy getCount");

            // newMXBeanProxy on an MXBean
            ObjectName mxn = new ObjectName("jck.test:type=JMXTestMX");
            mbs.registerMBean(new StandardMBean(new ThingX(), ThingMXBean.class, true), mxn);
            try {
                ThingMXBean mxProxy = JMX.newMXBeanProxy(mbs, mxn, ThingMXBean.class);
                check(mxProxy.getCount() == 9, "mxbean proxy getCount");
            } finally {
                mbs.unregisterMBean(mxn);
            }
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

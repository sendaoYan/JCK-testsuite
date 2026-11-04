/*
 * @test
 * @summary javax.management.MBeanServer conformance: white-box structure + light behavior.
 * @run main MBeanServerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class MBeanServerTest {
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
        Class<?> c = load("javax.management.MBeanServer");
        check(c.isInterface(), "is interface");
        check(MBeanServerConnection.class.isAssignableFrom(c), "extends MBeanServerConnection");
        check(hasMethod(c, "registerMBean"), "has registerMBean");
        check(hasMethod(c, "unregisterMBean"), "has unregisterMBean");
        check(hasMethod(c, "createMBean"), "has createMBean");
        check(hasMethod(c, "queryNames"), "has queryNames");
        check(hasMethod(c, "getAttribute"), "has getAttribute");
        check(hasMethod(c, "invoke"), "has invoke");
        check(hasMethod(c, "getMBeanCount"), "has getMBeanCount");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=MBeanServerTest");
        mbs.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        try {
            check(mbs.isRegistered(on), "isRegistered");
            check(((Integer) mbs.getAttribute(on, "Count")).intValue() == 7, "getAttribute");
            check(mbs.getObjectInstance(on).getObjectName().equals(on), "getObjectInstance");
            Set<ObjectName> names = mbs.queryNames(on, null);
            check(names.size() == 1 && names.contains(on), "queryNames exact");
            check(mbs.isInstanceOf(on, Thing.class.getName()), "isInstanceOf");
            check(mbs.getMBeanCount() > 0, "getMBeanCount");
            check(mbs.getDomains().length > 0, "getDomains");
            check(mbs.getDefaultDomain() != null, "getDefaultDomain");
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

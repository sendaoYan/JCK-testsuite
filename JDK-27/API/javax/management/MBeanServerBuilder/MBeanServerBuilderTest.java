/*
 * @test
 * @summary javax.management.MBeanServerBuilder conformance: white-box structure + black-box behavior.
 * @run main MBeanServerBuilderTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanServerBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
    }
    public static class Thing implements ThingMBean {
        public int getCount() { return 7; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerBuilder");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(hasMethod(c, "newMBeanServerDelegate"), "has newMBeanServerDelegate");
        check(hasMethod(c, "newMBeanServer"), "has newMBeanServer");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanServerBuilder builder = new MBeanServerBuilder();

        MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
        check(delegate != null, "newMBeanServerDelegate non-null");
        check(delegate.getMBeanServerId() != null, "delegate id non-null");

        MBeanServer server = builder.newMBeanServer("built.domain", null, delegate);
        check(server != null, "newMBeanServer non-null");
        check("built.domain".equals(server.getDefaultDomain()), "default domain set");
        check(server.getMBeanCount() == 1, "fresh server has only the delegate MBean");

        // the constructed server is a working MBeanServer
        ObjectName on = new ObjectName("built.domain:type=Thing");
        server.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        check(server.isRegistered(on), "register works on built server");
        check(server.getAttribute(on, "Count").equals(7), "getAttribute works");
        server.unregisterMBean(on);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

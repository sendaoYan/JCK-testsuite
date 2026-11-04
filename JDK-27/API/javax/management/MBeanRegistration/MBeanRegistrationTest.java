/*
 * @test
 * @summary javax.management.MBeanRegistration conformance: white-box structure + light behavior.
 * @run main MBeanRegistrationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class MBeanRegistrationTest {
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
    public static class Thing implements ThingMBean, MBeanRegistration {
        private int count = 7;
        private final List<String> events;
        Thing(List<String> events) { this.events = events; }
        public int getCount() { return count; }
        public ObjectName preRegister(MBeanServer server, ObjectName name) { events.add("preRegister"); return name; }
        public void postRegister(Boolean registrationDone) { events.add("postRegister"); }
        public void preDeregister() { events.add("preDeregister"); }
        public void postDeregister() { events.add("postDeregister"); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanRegistration");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "preRegister"), "has preRegister");
        check(hasMethod(c, "postRegister"), "has postRegister");
        check(hasMethod(c, "preDeregister"), "has preDeregister");
        check(hasMethod(c, "postDeregister"), "has postDeregister");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        List<String> events = new ArrayList<String>();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=MBeanRegistrationTest");
        mbs.registerMBean(new Thing(events), on);
        check(events.contains("preRegister"), "preRegister invoked");
        check(events.contains("postRegister"), "postRegister invoked");

        mbs.unregisterMBean(on);
        check(events.contains("preDeregister"), "preDeregister invoked");
        check(events.contains("postDeregister"), "postDeregister invoked");

        // lifecycle order: preRegister before postRegister, preDeregister before postDeregister
        int preR = events.indexOf("preRegister"), postR = events.indexOf("postRegister");
        int preD = events.indexOf("preDeregister"), postD = events.indexOf("postDeregister");
        check(preR >= 0 && postR > preR, "register order");
        check(preD >= 0 && postD > preD, "deregister order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

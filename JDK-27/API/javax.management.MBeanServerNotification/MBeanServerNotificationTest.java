/*
 * @test
 * @summary javax.management.MBeanServerNotification conformance: white-box structure + black-box behavior.
 * @run main MBeanServerNotificationTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanServerNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerNotification");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == Notification.class, "extends Notification");
        check(hasMethod(c, "getMBeanName"), "has getMBeanName");
        check("JMX.mbean.registered".equals(MBeanServerNotification.REGISTRATION_NOTIFICATION), "REGISTRATION_NOTIFICATION constant");
        check("JMX.mbean.unregistered".equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION), "UNREGISTRATION_NOTIFICATION constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectName on = new ObjectName("domain:type=Thing");
        MBeanServerNotification n = new MBeanServerNotification(
                MBeanServerNotification.REGISTRATION_NOTIFICATION, "src", 5L, on);
        check(n.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION), "getType registration");
        check(n.getMBeanName().equals(on), "getMBeanName");
        check(n.getSequenceNumber() == 5L, "getSequenceNumber");
        check("src".equals(n.getSource()), "getSource");

        MBeanServerNotification u = new MBeanServerNotification(
                MBeanServerNotification.UNREGISTRATION_NOTIFICATION, "src", 6L, on);
        check(u.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION), "getType unregistration");
        check(u.getMBeanName().equals(on), "unregistration getMBeanName");

        check(n.toString() != null, "toString non-null");
        check(!n.getType().equals(u.getType()), "registration vs unregistration types differ");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

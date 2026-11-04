/*
 * @test
 * @summary javax.management.relation.MBeanServerNotificationFilter conformance: white-box structure + black-box behavior.
 * @run main MBeanServerNotificationFilterTest
 */
import java.util.Vector;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.ObjectName;
import javax.management.relation.MBeanServerNotificationFilter;

public class MBeanServerNotificationFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static ObjectName on(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static MBeanServerNotification reg(ObjectName o) {
        return new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, o, 1L, o);
    }
    static MBeanServerNotification unreg(ObjectName o) {
        return new MBeanServerNotification(MBeanServerNotification.UNREGISTRATION_NOTIFICATION, o, 2L, o);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.MBeanServerNotificationFilter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "filter must be public");
        check(!c.isInterface(), "filter must be a class");
        check(c.getSuperclass() == NotificationFilterSupport.class, "filter must extend NotificationFilterSupport");
        c.getMethod("enableObjectName", ObjectName.class);
        c.getMethod("disableObjectName", ObjectName.class);
        c.getMethod("enableAllObjectNames");
        c.getMethod("disableAllObjectNames");
        c.getMethod("getEnabledObjectNames");
        c.getMethod("getDisabledObjectNames");
        c.getMethod("isNotificationEnabled", Notification.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectName n1 = on("d:type=one");
        ObjectName n2 = on("d:type=two");

        // fresh filter: constructor enables both registration and unregistration types
        MBeanServerNotificationFilter fresh = new MBeanServerNotificationFilter();
        Vector<String> types = fresh.getEnabledTypes();
        check(types.contains(MBeanServerNotification.REGISTRATION_NOTIFICATION), "registration type enabled");
        check(types.contains(MBeanServerNotification.UNREGISTRATION_NOTIFICATION), "unregistration type enabled");
        check(fresh.getEnabledObjectNames() != null && fresh.getEnabledObjectNames().isEmpty(), "fresh enabled ObjectNames empty");
        check(fresh.getDisabledObjectNames() == null, "fresh disabled ObjectNames null");

        // enableAllObjectNames: accepts all names
        MBeanServerNotificationFilter f = new MBeanServerNotificationFilter();
        f.enableAllObjectNames();
        check(f.getEnabledObjectNames() == null, "enableAll -> enabled names null");
        check(f.getDisabledObjectNames() != null && f.getDisabledObjectNames().isEmpty(), "enableAll -> disabled names empty");
        check(f.isNotificationEnabled(reg(n1)), "enableAll accepts registration for n1");
        check(f.isNotificationEnabled(unreg(n2)), "enableAll accepts unregistration for n2");

        // disableAllObjectNames: rejects everything
        f.disableAllObjectNames();
        check(f.getEnabledObjectNames() != null && f.getEnabledObjectNames().isEmpty(), "disableAll -> enabled names empty");
        check(f.getDisabledObjectNames() == null, "disableAll -> disabled names null");
        check(!f.isNotificationEnabled(reg(n1)), "disableAll rejects registration");

        // enableAll then disableObjectName(n1): rejects n1, accepts n2
        f.enableAllObjectNames();
        f.disableObjectName(n1);
        check(!f.isNotificationEnabled(reg(n1)), "explicitly disabled name rejected");
        check(f.isNotificationEnabled(reg(n2)), "non-disabled name accepted");

        // disableAll then enableObjectName(n1): accepts n1, rejects n2
        f.disableAllObjectNames();
        f.enableObjectName(n1);
        check(f.isNotificationEnabled(reg(n1)), "explicitly enabled name accepted");
        check(!f.isNotificationEnabled(reg(n2)), "non-enabled name rejected");

        // a non-MBeanServerNotification type is always rejected
        f.enableAllObjectNames();
        check(!f.isNotificationEnabled(new Notification("other.type", n1, 3L)), "non MBeanServer type rejected");

        // null arguments
        try { f.enableObjectName(null); check(false, "enableObjectName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "enableObjectName null message"); }
        try { f.disableObjectName(null); check(false, "disableObjectName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "disableObjectName null message"); }
        try { f.isNotificationEnabled(null); check(false, "isNotificationEnabled(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "isNotificationEnabled null message"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

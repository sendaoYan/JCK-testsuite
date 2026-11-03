/*
 * @test
 * @summary javax.management.MBeanServerDelegate conformance: white-box structure + black-box behavior.
 * @run main MBeanServerDelegateTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanServerDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerDelegate");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(MBeanServerDelegateMBean.class.isAssignableFrom(c), "implements MBeanServerDelegateMBean");
        check(NotificationEmitter.class.isAssignableFrom(c), "implements NotificationEmitter");
        check(hasMethod(c, "getMBeanServerId"), "has getMBeanServerId");
        check(hasMethod(c, "getNotificationInfo"), "has getNotificationInfo");
        check(hasMethod(c, "sendNotification"), "has sendNotification");
        check(MBeanServerDelegate.DELEGATE_NAME.equals(new ObjectName("JMImplementation:type=MBeanServerDelegate")),
                "DELEGATE_NAME constant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanServerDelegate d = new MBeanServerDelegate();
        check(d.getMBeanServerId() != null && d.getMBeanServerId().length() > 0, "getMBeanServerId non-empty");

        check("Java Management Extensions".equals(d.getSpecificationName()), "getSpecificationName");
        check("1.4".equals(d.getSpecificationVersion()), "getSpecificationVersion");
        check("Oracle Corporation".equals(d.getSpecificationVendor()), "getSpecificationVendor");
        check("JMX".equals(d.getImplementationName()), "getImplementationName");
        check(d.getImplementationVersion() != null, "getImplementationVersion non-null");
        check("Oracle Corporation".equals(d.getImplementationVendor()), "getImplementationVendor");

        // notification info declares registered/unregistered types
        MBeanNotificationInfo[] infos = d.getNotificationInfo();
        check(infos.length == 1, "one notification info");
        check(infos[0].getNotifTypes().length == 2, "two notification types");
        List<String> types = Arrays.asList(infos[0].getNotifTypes());
        check(types.contains(MBeanServerNotification.REGISTRATION_NOTIFICATION), "registration type present");
        check(types.contains(MBeanServerNotification.UNREGISTRATION_NOTIFICATION), "unregistration type present");

        // send/receive notifications
        final List<Notification> received = new ArrayList<Notification>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object handback) { received.add(n); }
        };
        d.addNotificationListener(listener, null, null);
        d.sendNotification(new Notification("custom.type", d, 1L));
        check(received.size() == 1, "listener received sent notification");
        check("custom.type".equals(received.get(0).getType()), "received type");

        d.removeNotificationListener(listener);
        d.sendNotification(new Notification("custom.type", d, 2L));
        check(received.size() == 1, "removed listener no longer receives");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

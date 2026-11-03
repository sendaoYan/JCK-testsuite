/*
 * @test
 * @summary javax.management.NotificationBroadcasterSupport conformance: white-box structure + black-box behavior.
 * @run main NotificationBroadcasterSupportTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationBroadcasterSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationBroadcasterSupport");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(NotificationEmitter.class.isAssignableFrom(c), "implements NotificationEmitter");
        check(hasMethod(c, "addNotificationListener"), "has addNotificationListener");
        check(hasMethod(c, "removeNotificationListener"), "has removeNotificationListener");
        check(hasMethod(c, "getNotificationInfo"), "has getNotificationInfo");
        check(hasMethod(c, "sendNotification"), "has sendNotification");
        Method handle = c.getDeclaredMethod("handleNotification", NotificationListener.class, Notification.class, Object.class);
        check(Modifier.isProtected(handle.getModifiers()), "protected handleNotification");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanNotificationInfo info = new MBeanNotificationInfo(new String[]{"t1", "t2"}, "javax.management.Notification", "d");
        NotificationBroadcasterSupport b = new NotificationBroadcasterSupport(info);
        check(b.getNotificationInfo().length == 1, "getNotificationInfo length");
        check("t1".equals(b.getNotificationInfo()[0].getNotifTypes()[0]), "notification info type");

        final List<Notification> received = new ArrayList<Notification>();
        final List<Object> handbacks = new ArrayList<Object>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object handback) {
                received.add(n);
                handbacks.add(handback);
            }
        };
        Object handback = "handback";
        b.addNotificationListener(listener, null, handback);

        Notification n1 = new Notification("t1", "src", 1L);
        b.sendNotification(n1);
        check(received.size() == 1, "listener received notification");
        check(received.get(0) == n1, "received same notification");
        check("handback".equals(handbacks.get(0)), "handback passed through");

        // filter: only notifications matching the filter are delivered
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType("t2");
        b.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { received.add(n); }
        }, filter, null);

        b.sendNotification(new Notification("t1", "src", 2L)); // only unfiltered listener gets this
        check(received.size() == 2, "filtered listener skipped t1");

        b.sendNotification(new Notification("t2", "src", 3L)); // both listeners get this
        check(received.size() == 4, "both listeners get t2");

        // remove via the simple overload
        b.removeNotificationListener(listener);
        b.sendNotification(new Notification("t1", "src", 4L));
        check(received.size() == 4, "removed listener receives nothing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

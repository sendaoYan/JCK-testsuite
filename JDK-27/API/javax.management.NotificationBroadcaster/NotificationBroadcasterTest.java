/*
 * @test
 * @summary javax.management.NotificationBroadcaster conformance: white-box structure + light behavior.
 * @run main NotificationBroadcasterTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationBroadcasterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationBroadcaster");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "addNotificationListener"), "has addNotificationListener");
        check(hasMethod(c, "removeNotificationListener"), "has removeNotificationListener");
        check(hasMethod(c, "getNotificationInfo"), "has getNotificationInfo");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final NotificationBroadcaster b = new NotificationBroadcasterSupport();
        final List<Notification> received = new ArrayList<Notification>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { received.add(n); }
        };
        b.addNotificationListener(listener, null, null);
        check(b.getNotificationInfo() != null, "getNotificationInfo non-null");

        ((NotificationBroadcasterSupport) b).sendNotification(new Notification("t", "s", 1L));
        check(received.size() == 1, "listener receives through interface");

        b.removeNotificationListener(listener);
        ((NotificationBroadcasterSupport) b).sendNotification(new Notification("t", "s", 2L));
        check(received.size() == 1, "removed listener receives nothing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

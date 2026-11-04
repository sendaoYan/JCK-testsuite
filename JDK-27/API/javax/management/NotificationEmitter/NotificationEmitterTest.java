/*
 * @test
 * @summary javax.management.NotificationEmitter conformance: white-box structure + light behavior.
 * @run main NotificationEmitterTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationEmitterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationEmitter");
        check(c.isInterface(), "is interface");
        check(NotificationBroadcaster.class.isAssignableFrom(c), "extends NotificationBroadcaster");
        check(hasMethod(c, "removeNotificationListener"), "has removeNotificationListener");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        NotificationEmitter emitter = new NotificationBroadcasterSupport();
        final List<Notification> received = new ArrayList<Notification>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { received.add(n); }
        };
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType("wanted");
        emitter.addNotificationListener(listener, filter, null);

        ((NotificationBroadcasterSupport) emitter).sendNotification(new Notification("wanted", "s", 1L));
        ((NotificationBroadcasterSupport) emitter).sendNotification(new Notification("other", "s", 2L));
        check(received.size() == 1, "emitter filter applies");

        // emitter overload of removeNotificationListener (listener, filter, handback)
        emitter.removeNotificationListener(listener, filter, null);
        ((NotificationBroadcasterSupport) emitter).sendNotification(new Notification("wanted", "s", 3L));
        check(received.size() == 1, "removed via filter overload");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    // ---- deepened (batch d): exact signatures ----
    static void testSignaturesExact() throws Exception {
        Class<?> c = load("javax.management.NotificationBroadcaster");
        Class<?> listener = NotificationListener.class;
        Class<?> filter = NotificationFilter.class;
        check(c.getDeclaredMethods().length == 3, "exactly three methods declared");

        Method add = c.getMethod("addNotificationListener", listener, filter, Object.class);
        check(add.getExceptionTypes().length == 1
                && "IllegalArgumentException".equals(add.getExceptionTypes()[0].getSimpleName()),
                "addNotificationListener declares IllegalArgumentException");
        Method rem = c.getMethod("removeNotificationListener", listener);
        check(rem.getExceptionTypes().length == 1
                && "ListenerNotFoundException".equals(rem.getExceptionTypes()[0].getSimpleName()),
                "removeNotificationListener declares ListenerNotFoundException");
        Method info = c.getMethod("getNotificationInfo");
        check("[Ljavax.management.MBeanNotificationInfo;".equals(info.getReturnType().getName()),
                "getNotificationInfo returns MBeanNotificationInfo[]");
        check(void.class == add.getReturnType() && void.class == rem.getReturnType(),
                "both mutators return void");
    }

    // ---- deepened (batch d): listener lifecycle through the interface reference ----
    static void testInterfaceLoop() throws Exception {
        MBeanNotificationInfo meta = new MBeanNotificationInfo(
                new String[]{"jck.iface"}, Notification.class.getName(), "iface loop");
        NotificationBroadcaster b = new NotificationBroadcasterSupport(meta);

        check(b.getNotificationInfo().length == 1, "getNotificationInfo through interface");
        check("jck.iface".equals(b.getNotificationInfo()[0].getNotifTypes()[0]),
                "notification info content through interface");
        b.getNotificationInfo()[0] = null;
        check(b.getNotificationInfo()[0] != null,
                "getNotificationInfo defensive clone through interface");

        final List<Notification> seen = new ArrayList<Notification>();
        final List<Object> hbs = new ArrayList<Object>();
        NotificationListener l1 = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); hbs.add(h); }
        };
        NotificationListener l2 = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); hbs.add(h); }
        };
        Object hb1 = new Object(), hb2 = new Object();
        b.addNotificationListener(l1, null, hb1);
        b.addNotificationListener(l2, null, hb2);
        Notification sent = new Notification("jck.iface", b, 1L);
        ((NotificationBroadcasterSupport) b).sendNotification(sent);
        check(seen.size() == 2, "both listeners delivered through interface");
        check(seen.get(0) == sent && seen.get(1) == sent,
                "identical notification instance delivered to each listener");
        check(hbs.contains(hb1) && hbs.contains(hb2),
                "per-registration handback delivered through interface");

        b.removeNotificationListener(l1);
        ((NotificationBroadcasterSupport) b).sendNotification(new Notification("jck.iface", b, 2L));
        check(seen.size() == 3, "only the remaining listener after interface removal");
        try {
            b.removeNotificationListener(l1);
            check(false, "removing an unregistered listener through the interface must throw");
        } catch (ListenerNotFoundException ex) {
            check(true, "remove unregistered through interface -> ListenerNotFoundException");
        }
        try {
            b.addNotificationListener(null, null, null);
            check(false, "addNotificationListener(null,...) must throw");
        } catch (IllegalArgumentException ex) {
            check(true, "addNotificationListener(null,...) -> IllegalArgumentException");
        }
        // hierarchy: NotificationEmitter extends NotificationBroadcaster, so every
        // emitter is also a broadcaster; the reference implementation implements both
        check(NotificationBroadcaster.class.isAssignableFrom(NotificationEmitter.class),
                "NotificationEmitter is-a NotificationBroadcaster");
        check(NotificationBroadcaster.class.isAssignableFrom(NotificationBroadcasterSupport.class),
                "NotificationBroadcasterSupport usable as NotificationBroadcaster");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSignaturesExact();
        testInterfaceLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    // ---- deepened (batch d): declared-method contract ----
    static void testEmitterContract() throws Exception {
        Class<?> c = load("javax.management.NotificationEmitter");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "NotificationEmitter declares exactly one method");
        Method m = declared[0];
        check("removeNotificationListener".equals(m.getName()),
                "the declared method is removeNotificationListener");
        Class<?>[] p = m.getParameterTypes();
        check(p.length == 3
                && p[0] == NotificationListener.class
                && p[1] == NotificationFilter.class
                && p[2] == Object.class,
                "exact parameter types (listener, filter, handback)");
        check(m.getExceptionTypes().length == 1
                && "ListenerNotFoundException".equals(m.getExceptionTypes()[0].getSimpleName()),
                "declares ListenerNotFoundException");
        check(m.getReturnType() == void.class, "returns void");
    }

    // ---- deepened (batch d): multi-registration removal loop ----
    static void testMultiRegistrationRemoval() throws Exception {
        NotificationEmitter e = new NotificationBroadcasterSupport(
                new MBeanNotificationInfo(new String[]{"jck.emitter"},
                        Notification.class.getName(), "d"));
        final List<Object> handbacksSeen = new ArrayList<Object>();
        NotificationListener l = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { handbacksSeen.add(h); }
        };
        NotificationFilter f = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return true; }
        };
        Object h1 = new Object(), h2 = new Object();
        e.addNotificationListener(l, f, h1);
        e.addNotificationListener(l, f, h2);
        ((NotificationBroadcasterSupport) e).sendNotification(new Notification("jck.emitter", e, 1L));
        check(handbacksSeen.size() == 2 && handbacksSeen.contains(h1) && handbacksSeen.contains(h2),
                "same listener registered twice delivers once per registration");

        try {
            e.removeNotificationListener(l, f, new Object());
            check(false, "removal with a mismatched handback must throw");
        } catch (ListenerNotFoundException ex) {
            check(true, "mismatched handback -> ListenerNotFoundException");
        }
        e.removeNotificationListener(l, f, h1);   // removes exactly the h1 registration
        handbacksSeen.clear();
        ((NotificationBroadcasterSupport) e).sendNotification(new Notification("jck.emitter", e, 2L));
        check(handbacksSeen.size() == 1 && handbacksSeen.get(0) == h2,
                "exact-match removal leaves the other registration delivering");

        try {
            e.removeNotificationListener(l, f, h1);
            check(false, "removing an already-removed registration must throw");
        } catch (ListenerNotFoundException ex) {
            check(true, "double removal -> ListenerNotFoundException");
        }
        // the inherited single-arg removal clears the remaining registration
        e.removeNotificationListener(l);
        handbacksSeen.clear();
        ((NotificationBroadcasterSupport) e).sendNotification(new Notification("jck.emitter", e, 3L));
        check(handbacksSeen.isEmpty(), "single-arg removal clears the remaining registration");
        // reference implementation relationship
        check(NotificationEmitter.class.isAssignableFrom(NotificationBroadcasterSupport.class),
                "NotificationBroadcasterSupport is a NotificationEmitter");
        check(NotificationBroadcaster.class.isAssignableFrom(NotificationEmitter.class),
                "NotificationEmitter is-a NotificationBroadcaster");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmitterContract();
        testMultiRegistrationRemoval();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

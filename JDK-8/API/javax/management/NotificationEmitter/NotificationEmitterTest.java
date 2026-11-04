/*
 * @test
 * @summary javax.management.NotificationEmitter conformance: interface reflection.
 *          exact declared signature of the 3-arg removeNotificationListener,
 *          single-method surface, and the multi-registration add/remove loop
 *          driven through NotificationBroadcasterSupport.
 * @run main NotificationEmitterTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationEmitterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- deepened (batch d): declared-method contract ----
    static void testEmitterContract() throws Exception {
        Class<?> c = Class.forName("javax.management.NotificationEmitter");
        Method[] declared = c.getDeclaredMethods();
        check(declared.length == 1, "NotificationEmitter declares exactly one method");
        Method m = declared[0];
        check("removeNotificationListener".equals(m.getName()),
                "the declared method is removeNotificationListener");
        Class<?>[] p = m.getParameterTypes();
        check(p.length == 3
                && "javax.management.NotificationListener".equals(p[0].getName())
                && "javax.management.NotificationFilter".equals(p[1].getName())
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
        Class<?> c = Class.forName("javax.management.NotificationEmitter");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.NotificationBroadcaster").isAssignableFrom(c), "implements/extends javax.management.NotificationBroadcaster");
        check(hasMethod(c, "removeNotificationListener"), "declares removeNotificationListener");
        testEmitterContract();
        testMultiRegistrationRemoval();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

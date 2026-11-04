/*
 * @test
 * @summary javax.management.NotificationFilter conformance: interface reflection.
 *          functional-interface shape, built-in implementors, filter-driven
 *          delivery through NotificationBroadcasterSupport (consulted with the
 *          exact notification, false suppresses, thrower is skipped), and
 *          serialization of a custom filter keeping its behavior.
 * @run main NotificationFilterTest
 */

import java.io.*;
import java.util.*;
import javax.management.*;

public class NotificationFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    /** Custom serializable filter so serial-form behavior can be checked. */
    static class RecordingFilter implements NotificationFilter {
        private static final long serialVersionUID = 1L;
        int calls;
        Notification lastSeen;
        public boolean isNotificationEnabled(Notification n) {
            calls++;
            lastSeen = n;
            return "pass".equals(n.getType());
        }
    }

    // ---- deepened (batch d): interface shape ----
    static void testFilterShape() throws Exception {
        Class<?> c = Class.forName("javax.management.NotificationFilter");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method");
        java.lang.reflect.Method m = c.getDeclaredMethod("isNotificationEnabled",
                Class.forName("javax.management.Notification"));
        check(m.getReturnType() == boolean.class, "isNotificationEnabled returns boolean");
        check(m.getExceptionTypes().length == 0, "declares no checked exceptions");
        check(new NotificationFilterSupport() instanceof NotificationFilter,
                "NotificationFilterSupport implements NotificationFilter");
        check(new AttributeChangeNotificationFilter() instanceof NotificationFilter,
                "AttributeChangeNotificationFilter implements NotificationFilter");
        // functional-interface use via lambda
        NotificationFilter lambda = (Notification n) -> "lam".equals(n.getType());
        check(lambda.isNotificationEnabled(new Notification("lam", "s", 1L)),
                "lambda filter enables matching type");
        check(!lambda.isNotificationEnabled(new Notification("other", "s", 1L)),
                "lambda filter rejects non-matching type");
    }

    // ---- deepened (batch d): filter-driven delivery loop ----
    static void testFilterDeliveryLoop() throws Exception {
        RecordingFilter f = new RecordingFilter();
        final List<Notification> seen = new ArrayList<Notification>();
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        nbs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); }
        }, f, null);

        Notification blocked = new Notification("drop", "s", 1L);
        nbs.sendNotification(blocked);
        check(f.calls == 1 && f.lastSeen == blocked,
                "filter consulted with the exact notification instance");
        check(seen.isEmpty(), "filter returning false suppresses delivery");

        Notification pass = new Notification("pass", "s", 2L);
        nbs.sendNotification(pass);
        check(seen.size() == 1 && seen.get(0) == pass, "filter-approved notification delivered");

        // a filter that throws does not break sendNotification; its listener is skipped
        NotificationFilter thrower = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) {
                throw new IllegalStateException("boom");
            }
        };
        nbs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); }
        }, thrower, null);
        nbs.sendNotification(new Notification("pass", "s", 3L));   // must not propagate
        check(seen.size() == 2, "throwing filter skipped, other listener still delivered");

        // a custom filter survives serialization with its behavior intact
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(f);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        RecordingFilter back = (RecordingFilter) ois.readObject();
        ois.close();
        check(back.isNotificationEnabled(new Notification("pass", "s", 9L)),
                "filter behavior survives serialization round-trip");
        check(!back.isNotificationEnabled(new Notification("drop", "s", 9L)),
                "serialized filter still rejects non-matching type");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.NotificationFilter");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "isNotificationEnabled"), "declares isNotificationEnabled");
        testFilterShape();
        testFilterDeliveryLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

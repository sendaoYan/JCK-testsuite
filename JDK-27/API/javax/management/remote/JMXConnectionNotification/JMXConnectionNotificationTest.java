/*
 * @test
 * @summary javax.management.remote.JMXConnectionNotification conformance: white-box structure + black-box behavior.
 * @run main JMXConnectionNotificationTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import javax.management.Notification;
import javax.management.remote.JMXConnectionNotification;

public class JMXConnectionNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectionNotification");
        check(Notification.class.isAssignableFrom(c), "extends Notification");
        Constructor<?> k = c.getConstructor(String.class, Object.class, String.class, long.class, String.class, Object.class);
        check(k != null, "6-arg ctor");

        // Public constant fields are static final Strings.
        check(Modifier.isStatic(c.getField("OPENED").getModifiers()), "OPENED static");
        check(Modifier.isStatic(c.getField("CLOSED").getModifiers()), "CLOSED static");
        check(Modifier.isStatic(c.getField("FAILED").getModifiers()), "FAILED static");
        check(Modifier.isStatic(c.getField("NOTIFS_LOST").getModifiers()), "NOTIFS_LOST static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(JMXConnectionNotification.OPENED.equals("jmx.remote.connection.opened"), "OPENED value");
        check(JMXConnectionNotification.CLOSED.equals("jmx.remote.connection.closed"), "CLOSED value");
        check(JMXConnectionNotification.FAILED.equals("jmx.remote.connection.failed"), "FAILED value");
        check(JMXConnectionNotification.NOTIFS_LOST.equals("jmx.remote.connection.notifs.lost"), "NOTIFS_LOST value");

        Object source = new Object();
        JMXConnectionNotification n =
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, source, "conn-7", 99L, "opened", "extra");
        check(n.getConnectionId().equals("conn-7"), "connection id");
        check(n.getType().equals(JMXConnectionNotification.OPENED), "type propagated to Notification");
        check(n.getSource() == source, "source propagated");
        check(n.getSequenceNumber() == 99L, "sequence number");
        check(n.getMessage().equals("opened"), "message");
        check(n.getUserData().equals("extra"), "user data");

        // A notification for each of the other standard types.
        JMXConnectionNotification closed =
            new JMXConnectionNotification(JMXConnectionNotification.CLOSED, source, "conn-7", 100L, null, null);
        check(closed.getType().equals(JMXConnectionNotification.CLOSED), "closed type");
        check(closed.getMessage() == null, "null message allowed");
        check(closed.getUserData() == null, "null user data allowed");

        // Null type/source/connectionId each throw NullPointerException with stable message.
        try {
            new JMXConnectionNotification(null, source, "c", 1L, null, null);
            check(false, "expected NPE for null type");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("Illegal null argument"), "null type message: " + e.getMessage());
        }
        try {
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, null, "c", 1L, null, null);
            check(false, "expected NPE for null source");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("Illegal null argument"), "null source message: " + e.getMessage());
        }
        try {
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, source, null, 1L, null, null);
            check(false, "expected NPE for null connectionId");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("Illegal null argument"), "null connectionId message: " + e.getMessage());
        }
        try {
            new JMXConnectionNotification(JMXConnectionNotification.OPENED, source, "c", -1L, null, null);
            check(false, "expected IAE for negative sequence");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Negative sequence number"), "negative seq message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

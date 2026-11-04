/*
 * @test
 * @summary javax.management.remote.TargetedNotification conformance: white-box structure + black-box behavior.
 * @run main TargetedNotificationTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;

import javax.management.Notification;
import javax.management.remote.TargetedNotification;

public class TargetedNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.TargetedNotification");
        check(!c.isInterface(), "concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        Constructor<?> k = c.getConstructor(Notification.class, Integer.class);
        check(k != null, "(Notification,Integer) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Notification notif = new Notification("my.type", "src", 123L);
        TargetedNotification tn = new TargetedNotification(notif, 42);
        check(tn.getNotification() == notif, "same notification instance");
        check(tn.getNotification().getType().equals("my.type"), "notification type");
        check(tn.getNotification().getSequenceNumber() == 123L, "notification seq");
        check(tn.getListenerID().intValue() == 42, "listener id");

        // toString combines notification toString and the id.
        check(tn.toString().equals("{" + notif + ", 42}"), "toString: " + tn.toString());

        // Constructor validation: null arguments throw IllegalArgumentException with stable message.
        try {
            new TargetedNotification(null, 5);
            check(false, "expected IAE for null notification");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Invalid notification: null"), "null notification message: " + e.getMessage());
        }
        try {
            new TargetedNotification(new Notification("t", "s", 1L), null);
            check(false, "expected IAE for null listener id");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Invalid listener ID: null"), "null id message: " + e.getMessage());
        }

        // Two distinct instances with equal content are still independent objects.
        TargetedNotification tn2 = new TargetedNotification(new Notification("my.type", "src", 123L), 42);
        check(tn != tn2, "distinct instances");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

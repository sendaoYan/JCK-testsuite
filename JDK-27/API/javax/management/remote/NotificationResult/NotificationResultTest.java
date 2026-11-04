/*
 * @test
 * @summary javax.management.remote.NotificationResult conformance: white-box structure + black-box behavior.
 * @run main NotificationResultTest
 */

import java.io.Serializable;
import java.lang.reflect.Constructor;

import javax.management.Notification;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;

public class NotificationResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.NotificationResult");
        check(!c.isInterface(), "concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        Constructor<?> k = c.getConstructor(long.class, long.class, TargetedNotification[].class);
        check(k != null, "(long,long,TargetedNotification[]) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TargetedNotification tn = new TargetedNotification(new Notification("t", "s", 1L), 7);
        NotificationResult nr = new NotificationResult(10L, 25L, new TargetedNotification[]{ tn });
        check(nr.getEarliestSequenceNumber() == 10L, "earliest");
        check(nr.getNextSequenceNumber() == 25L, "next");
        check(nr.getTargetedNotifications().length == 1, "one targeted");
        check(nr.getTargetedNotifications()[0].getListenerID().intValue() == 7, "targeted content");

        // toString carries all three parts.
        check(nr.toString().equals("NotificationResult: earliest=10; next=25; nnotifs=1"), "toString: " + nr.toString());

        // getTargetedNotifications returns a clone: mutating the returned array does not
        // affect the internal state of the NotificationResult.
        TargetedNotification[] returned = nr.getTargetedNotifications();
        returned[0] = null;
        check(nr.getTargetedNotifications()[0] != null, "defensive copy (internal state preserved)");

        // Empty array round-trips.
        NotificationResult empty = new NotificationResult(0L, 0L, new TargetedNotification[0]);
        check(empty.getTargetedNotifications().length == 0, "empty array");
        check(empty.toString().equals("NotificationResult: earliest=0; next=0; nnotifs=0"), "empty toString");

        // Constructor validation.
        try {
            new NotificationResult(0L, 0L, null);
            check(false, "expected IAE for null array");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Notifications null"), "null array message: " + e.getMessage());
        }
        try {
            new NotificationResult(-1L, 0L, new TargetedNotification[0]);
            check(false, "expected IAE for negative earliest");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Bad sequence numbers"), "negative earliest message: " + e.getMessage());
        }
        try {
            new NotificationResult(0L, -1L, new TargetedNotification[0]);
            check(false, "expected IAE for negative next");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Bad sequence numbers"), "negative next message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

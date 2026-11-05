/*
 * @test
 * @summary javax.management.remote.NotificationResult conformance: white-box structure + black-box behavior.
 * @run main NotificationResultTest
 */
import javax.management.*;
import javax.management.remote.*;


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
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): validation matrix, defensive copy, identity equals, serialization ====
    static void testDeepened() throws Exception {
        Notification n1 = new Notification("jck.a", "src", 1L);
        Notification n2 = new Notification("jck.b", "src", 2L);
        TargetedNotification[] arr = { new TargetedNotification(n1, Integer.valueOf(1)),
                                       new TargetedNotification(n2, Integer.valueOf(2)) };
        NotificationResult nr = new NotificationResult(10L, 20L, arr);
        check(nr.getEarliestSequenceNumber() == 10L, "d-earliest");
        check(nr.getNextSequenceNumber() == 20L, "d-next");
        check(nr.getTargetedNotifications().length == 2, "d-length");
        check(nr.getTargetedNotifications()[0].getNotification() == n1, "d-element 0 identity");
        check(nr.getTargetedNotifications()[1].getNotification() == n2, "d-element 1 identity");
        check(nr.toString().contains("earliest=10") && nr.toString().contains("next=20"), "d-toString");
        // Defensive copy: mutating the returned array must not affect the result.
        TargetedNotification[] got = nr.getTargetedNotifications();
        got[0] = null;
        check(nr.getTargetedNotifications()[0] != null, "d-defensive copy of array");
        // Negative sequence numbers are rejected with one precise message.
        try {
            new NotificationResult(-1L, 5L, arr);
            check(false, "d-negative earliest should throw");
        } catch (IllegalArgumentException e) {
            check("Bad sequence numbers".equals(e.getMessage()), "d-negative earliest message: " + e.getMessage());
        }
        try {
            new NotificationResult(5L, -1L, arr);
            check(false, "d-negative next should throw");
        } catch (IllegalArgumentException e) {
            check("Bad sequence numbers".equals(e.getMessage()), "d-negative next message: " + e.getMessage());
        }
        // next < earliest is NOT validated (recorded quirk, identical on both JDKs).
        check(new NotificationResult(5L, 1L, arr).getNextSequenceNumber() == 1L, "d-ordering not validated quirk");
        // Null array rejected.
        try {
            new NotificationResult(0L, 0L, null);
            check(false, "d-null array should throw");
        } catch (IllegalArgumentException e) {
            check("Notifications null".equals(e.getMessage()), "d-null array message: " + e.getMessage());
        }
        // No equals override: identity semantics (contract quirk).
        check(!nr.equals(new NotificationResult(10L, 20L, arr)), "d-no value equals (identity semantics)");
        check(nr.equals(nr), "d-reflexive");
        check(!nr.equals(null), "d-not equals null");
        // Serialization preserves fields (value equals does not exist, so check field-by-field).
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(nr);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            NotificationResult back = (NotificationResult) ois.readObject();
            check(back.getEarliestSequenceNumber() == 10L, "d-ser earliest");
            check(back.getNextSequenceNumber() == 20L, "d-ser next");
            check(back.getTargetedNotifications().length == 2, "d-ser length");
            check(back.getTargetedNotifications()[0].getListenerID().intValue() == 1, "d-ser element id");
        } finally {
            if (ois != null) ois.close();
        }
    }

}

/*
 * @test
 * @summary javax.management.remote.NotificationResult conformance: sequence numbers and notifications.
 * @run main NotificationResultTest
 */

import javax.management.*;
import javax.management.remote.*;
public class NotificationResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        TargetedNotification[] tns = new TargetedNotification[]{
            new TargetedNotification(new Notification("t", "src", 1L), Integer.valueOf(1))
        };
        NotificationResult nr = new NotificationResult(1L, 2L, tns);
        check(nr.getEarliestSequenceNumber() == 1L, "earliest");
        check(nr.getNextSequenceNumber() == 2L, "next");
        check(nr.getTargetedNotifications().length == 1, "notifications");
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

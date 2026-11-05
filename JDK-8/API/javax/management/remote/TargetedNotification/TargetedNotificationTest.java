/*
 * @test
 * @summary javax.management.remote.TargetedNotification conformance: notification + listener id.
 * @run main TargetedNotificationTest
 */

import javax.management.*;
import javax.management.remote.*;
public class TargetedNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Notification n = new Notification("t", "src", 1L);
        TargetedNotification tn = new TargetedNotification(n, Integer.valueOf(5));
        check(tn.getNotification() == n, "notification");
        check(Integer.valueOf(5).equals(tn.getListenerID()), "listener id");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): identity getters, null matrix, identity equals, serialization ====
    static void testDeepened() throws Exception {
        Notification n = new Notification("jck.t", "src", 7L);
        TargetedNotification tn = new TargetedNotification(n, Integer.valueOf(5));
        check(tn.getNotification() == n, "d-getNotification identity");
        check(Integer.valueOf(5).equals(tn.getListenerID()), "d-getListenerID value");
        String s = tn.toString();
        check(s.contains("5") && s.contains("jck.t"), "d-toString carries id and type");
        try {
            new TargetedNotification(null, Integer.valueOf(1));
            check(false, "d-null notification should throw");
        } catch (IllegalArgumentException e) {
            check("Invalid notification: null".equals(e.getMessage()), "d-null notification message: " + e.getMessage());
        }
        try {
            new TargetedNotification(n, null);
            check(false, "d-null listener id should throw");
        } catch (IllegalArgumentException e) {
            check("Invalid listener ID: null".equals(e.getMessage()), "d-null listener id message: " + e.getMessage());
        }
        // No equals/hashCode override: identity semantics (contract quirk locked on both JDKs).
        check(!tn.equals(new TargetedNotification(n, Integer.valueOf(5))), "d-no value equals (identity semantics)");
        check(!tn.equals(null), "d-not equals null");
        check(tn.equals(tn), "d-reflexive");
        // Serialization round-trip preserves both fields.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(tn);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            TargetedNotification back = (TargetedNotification) ois.readObject();
            check("jck.t".equals(back.getNotification().getType()), "d-ser notification type");
            check(Integer.valueOf(5).equals(back.getListenerID()), "d-ser listener id");
            check("src".equals(back.getNotification().getSource()), "d-ser notification source");
        } finally {
            if (ois != null) ois.close();
        }
    }

}

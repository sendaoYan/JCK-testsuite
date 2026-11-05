/*
 * @test
 * @summary javax.management.remote.TargetedNotification conformance: white-box structure + black-box behavior.
 * @run main TargetedNotificationTest
 */
import javax.management.*;
import javax.management.remote.*;


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

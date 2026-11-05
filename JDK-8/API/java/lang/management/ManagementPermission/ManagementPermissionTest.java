/*
 * @test
 * @summary java.lang.management.ManagementPermission conformance: name/actions/equals.
 * @run main ManagementPermissionTest
 */

import java.lang.management.ManagementPermission;
import java.security.BasicPermission;

public class ManagementPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testManagementPermission() {
        ManagementPermission p = new ManagementPermission("monitor");
        check("monitor".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new ManagementPermission("monitor")), "equals monitor");
        check(!p.equals(new ManagementPermission("control")), "monitor != control");
    }


    static void expectIAE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, what + " throws IllegalArgumentException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectNPE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw NullPointerException");
        } catch (NullPointerException e) {
            check(true, what + " throws NullPointerException");
            for (String f : fragments) {
                check(e.getMessage() == null || e.getMessage().contains(f),
                      what + " message contains '" + f + "' or is null (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectUOE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(true, what + " throws UnsupportedOperationException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void testDeepImpliesMatrix() {
        ManagementPermission ctrl = new ManagementPermission("control");
        ManagementPermission ctrl2 = new ManagementPermission("control");
        ManagementPermission mon = new ManagementPermission("monitor");
        check(ctrl.implies(ctrl2) && ctrl2.implies(ctrl), "same-name implies is symmetric");
        check(mon.implies(new ManagementPermission("monitor")), "monitor implies monitor");
        // The pair is NOT a total order: neither target implies the other.
        check(!ctrl.implies(mon), "control does NOT imply monitor");
        check(!mon.implies(ctrl), "monitor does NOT imply control");
        check(!ctrl.implies(null), "implies(null) is false");
        check(!mon.implies(null), "implies(null) is false");
        check(!ctrl.implies(new java.lang.RuntimePermission("exitVm")),
              "implies is false across permission kinds");
    }

    static void testDeepEqualsHashCode() {
        ManagementPermission ctrl = new ManagementPermission("control");
        ManagementPermission mon = new ManagementPermission("monitor");
        check(ctrl.equals(new ManagementPermission("control")), "value equals for same name");
        check(!ctrl.equals(mon), "different names not equal");
        check(!ctrl.equals(null), "equals(null) false");
        check(ctrl.hashCode() == 951543133, "control hashCode == \"control\".hashCode()");
        check(mon.hashCode() == 1236319578, "monitor hashCode == \"monitor\".hashCode()");
        check(ctrl.hashCode() == "control".hashCode(), "hashCode delegates to the name");
        check("".equals(ctrl.getActions()), "no actions: empty string");
        check("".equals(mon.getActions()), "monitor actions empty");
    }

    static void testDeepSerialization() throws Exception {
        ManagementPermission original = new ManagementPermission("monitor");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        ManagementPermission back = (ManagementPermission) ois.readObject();
        ois.close();
        check(back.equals(original), "serialization round-trip preserves equals");
        check("monitor".equals(back.getName()), "serialization preserves the target name");
        check(java.io.ObjectStreamClass.lookup(ManagementPermission.class).getSerialVersionUID()
                == 1897496590799378737L, "serialVersionUID locked");
    }

    static void testDeepNegativeMatrix() {
        expectIAE(new Runnable() { public void run() { new ManagementPermission("bogus"); } },
                  "bogus target", "bogus");
        expectIAE(new Runnable() { public void run() { new ManagementPermission(""); } },
                  "empty target", "empty");
        expectIAE(new Runnable() { public void run() { new ManagementPermission("*"); } },
                  "wildcard target", "*");
        expectIAE(new Runnable() { public void run() { new ManagementPermission("monitor", "read"); } },
                  "non-empty actions", "read");
        expectIAE(new Runnable() { public void run() { new ManagementPermission("monitor control"); } },
                  "two-word target");
        expectNPE(new Runnable() { public void run() { new ManagementPermission(null); } },
                  "null target");
        // empty-string actions are accepted (BasicPermission treats "" as no actions)
        ManagementPermission empty = new ManagementPermission("monitor", "");
        check("".equals(empty.getActions()), "empty actions string accepted");
    }

    static void testDeepPermissionCollection() {
        ManagementPermission mon = new ManagementPermission("monitor");
        java.security.PermissionCollection coll = mon.newPermissionCollection();
        check(coll != null, "newPermissionCollection non-null");
        coll.add(new ManagementPermission("monitor"));
        check(coll.implies(new ManagementPermission("monitor")), "collection implies added target");
        check(!coll.implies(new ManagementPermission("control")), "collection does not imply absent target");
        check(coll.isReadOnly() == false, "collection writable before setReadOnly");
    }

    public static void main(String[] args) throws Exception {
        testManagementPermission();
        testDeepImpliesMatrix();
        testDeepEqualsHashCode();
        testDeepSerialization();
        testDeepNegativeMatrix();
        testDeepPermissionCollection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

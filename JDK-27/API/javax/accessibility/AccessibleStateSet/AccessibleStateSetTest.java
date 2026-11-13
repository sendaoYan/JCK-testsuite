/*
 * @test
 * @summary javax.accessibility.AccessibleStateSet conformance: white-box structure + black-box behavior.
 * @run main AccessibleStateSetTest
 */

import java.lang.reflect.*;
import java.util.Arrays;

public class AccessibleStateSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleStateSet");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "remove"), "declares remove");
        check(hasMethod(c, "contains"), "declares contains");
        check(hasMethod(c, "toArray"), "declares toArray");
        check(hasMethod(c, "clear"), "declares clear");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleStateSet ss = new javax.accessibility.AccessibleStateSet();
        check(ss.toString() == null, "empty toString null");
        check(ss.toArray().length == 0, "empty toArray length 0");
        check(!ss.contains(javax.accessibility.AccessibleState.ACTIVE), "empty contains false");
        check(!ss.remove(javax.accessibility.AccessibleState.ACTIVE), "remove absent returns false");

        check(ss.add(javax.accessibility.AccessibleState.ACTIVE), "add first returns true");
        check(!ss.add(javax.accessibility.AccessibleState.ACTIVE), "add duplicate returns false");
        check(ss.contains(javax.accessibility.AccessibleState.ACTIVE), "contains after add");
        check(ss.toArray().length == 1, "toArray length 1");

        ss.add(javax.accessibility.AccessibleState.ENABLED);
        javax.accessibility.AccessibleState[] arr = ss.toArray();
        check(arr.length == 2, "two states toArray length 2");
        check(ss.toString().indexOf(',') >= 0, "two states toString comma-separated");

        check(ss.remove(javax.accessibility.AccessibleState.ACTIVE), "remove present returns true");
        check(!ss.contains(javax.accessibility.AccessibleState.ACTIVE), "removed absent");
        check(ss.contains(javax.accessibility.AccessibleState.ENABLED), "other state still present");

        javax.accessibility.AccessibleStateSet ss2 = new javax.accessibility.AccessibleStateSet(
                new javax.accessibility.AccessibleState[]{
                    javax.accessibility.AccessibleState.ENABLED,
                    javax.accessibility.AccessibleState.ENABLED,
                    javax.accessibility.AccessibleState.VISIBLE});
        check(ss2.toArray().length == 2, "ctor dedupes duplicates");
        check(ss2.contains(javax.accessibility.AccessibleState.ENABLED), "ctor contains ENABLED");
        check(ss2.contains(javax.accessibility.AccessibleState.VISIBLE), "ctor contains VISIBLE");

        ss2.addAll(new javax.accessibility.AccessibleState[]{ javax.accessibility.AccessibleState.FOCUSED });
        check(ss2.contains(javax.accessibility.AccessibleState.FOCUSED), "addAll adds");
        ss2.clear();
        check(ss2.toArray().length == 0, "clear empties");
        check(ss2.toString() == null, "cleared toString null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: add/remove/contains/addAll/clear/toArray full matrix + null toString ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleStateSet ss = new javax.accessibility.AccessibleStateSet();
        check(ss.toArray().length == 0, "empty set 0 states");
        check(!ss.contains(javax.accessibility.AccessibleState.ENABLED), "empty contains false");
        check(ss.toString() == null, "empty set toString is null (locked quirk)");
        ss.add(javax.accessibility.AccessibleState.ENABLED);
        ss.add(javax.accessibility.AccessibleState.FOCUSABLE);
        check(ss.toArray().length == 2, "2 after adds");
        check(ss.contains(javax.accessibility.AccessibleState.ENABLED), "contains");
        // AccessibleStateSet.toString() joins each state's display string, which is
        // resolved against Locale.getDefault(); pin English for the literal.
        java.util.Locale savedDefaultLocale = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            check("enabled,focusable".equals(ss.toString()), "toString comma-joined display strings [" + ss.toString() + "]");
        } finally {
            java.util.Locale.setDefault(savedDefaultLocale);
        }
        check(!ss.add(javax.accessibility.AccessibleState.ENABLED), "duplicate add returns false");
        check(ss.toArray().length == 2, "size unchanged by dup");
        check(ss.remove(javax.accessibility.AccessibleState.ENABLED), "remove true");
        check(!ss.contains(javax.accessibility.AccessibleState.ENABLED), "gone");
        check(!ss.remove(javax.accessibility.AccessibleState.ENABLED), "double remove false");
        check(ss.toArray().length == 1, "1 remains");
        ss.addAll(new javax.accessibility.AccessibleState[] { javax.accessibility.AccessibleState.VISIBLE, javax.accessibility.AccessibleState.SHOWING });
        check(ss.toArray().length == 3, "addAll to 3");
        javax.accessibility.AccessibleState[] view = ss.toArray();
        view[0] = null;
        check(ss.toArray()[0] != null, "toArray defensively copied");
        ss.clear();
        check(ss.toArray().length == 0 && ss.toString() == null, "clear back to empty");
        // real Swing stateset
        javax.swing.JButton b = new javax.swing.JButton("x");
        javax.accessibility.AccessibleStateSet bs = b.getAccessibleContext().getAccessibleStateSet();
        check(bs.contains(javax.accessibility.AccessibleState.ENABLED), "JButton ENABLED");
        check(bs.contains(javax.accessibility.AccessibleState.FOCUSABLE), "JButton FOCUSABLE");
        check(!bs.contains(javax.accessibility.AccessibleState.SELECTED), "JButton not SELECTED");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // concrete AccessibleContext recording subclass
    static class RecCtx extends javax.accessibility.AccessibleContext {
        public String getAccessibleName() { return accessibleName; }
        public String getAccessibleDescription() { return accessibleDescription; }
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public java.util.Locale getLocale() throws java.awt.IllegalComponentStateException { return java.util.Locale.ENGLISH; }
    }

}

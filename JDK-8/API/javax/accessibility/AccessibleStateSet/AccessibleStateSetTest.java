/*
 * @test
 * @summary javax.accessibility.AccessibleStateSet conformance: class reflection.
 * @run main AccessibleStateSetTest
 */

import java.util.Locale;

public class AccessibleStateSetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        Class<?> c = Class.forName("javax.accessibility.AccessibleStateSet");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "addAll"), "method addAll");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "toArray"), "method toArray");
        check(hasMethod(c, "toString"), "method toString");
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

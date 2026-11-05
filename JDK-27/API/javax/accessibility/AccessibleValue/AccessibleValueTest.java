/*
 * @test
 * @summary javax.accessibility.AccessibleValue conformance: white-box structure + black-box behavior.
 * @run main AccessibleValueTest
 */

import java.lang.reflect.*;

public class AccessibleValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleValue");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getCurrentAccessibleValue"), "declares getCurrentAccessibleValue");
        check(hasMethod(c, "setCurrentAccessibleValue"), "declares setCurrentAccessibleValue");
        check(hasMethod(c, "getMinimumAccessibleValue"), "declares getMinimumAccessibleValue");
        check(hasMethod(c, "getMaximumAccessibleValue"), "declares getMaximumAccessibleValue");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleValue v = new javax.accessibility.AccessibleValue() {
            Number cur = Integer.valueOf(0);
            public Number getCurrentAccessibleValue() { return cur; }
            public boolean setCurrentAccessibleValue(Number n) { cur = n; return true; }
            public Number getMinimumAccessibleValue() { return Integer.valueOf(0); }
            public Number getMaximumAccessibleValue() { return Integer.valueOf(100); }
        };
        check(Integer.valueOf(0).equals(v.getCurrentAccessibleValue()), "initial current 0");
        check(Integer.valueOf(0).equals(v.getMinimumAccessibleValue()), "minimum 0");
        check(Integer.valueOf(100).equals(v.getMaximumAccessibleValue()), "maximum 100");
        check(v.setCurrentAccessibleValue(Integer.valueOf(42)), "setCurrent returns true");
        check(Integer.valueOf(42).equals(v.getCurrentAccessibleValue()), "current after set 42");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + real JButton value ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleValue v = new javax.accessibility.AccessibleValue() {
            public Number getCurrentAccessibleValue() { return java.math.BigInteger.valueOf(7); }
            public boolean setCurrentAccessibleValue(Number n) { return n != null; }
            public Number getMinimumAccessibleValue() { return Integer.valueOf(0); }
            public Number getMaximumAccessibleValue() { return Integer.valueOf(10); }
        };
        check(v.getCurrentAccessibleValue().intValue() == 7, "current");
        check(v.getMinimumAccessibleValue().intValue() == 0 && v.getMaximumAccessibleValue().intValue() == 10, "min/max");
        check(v.setCurrentAccessibleValue(Integer.valueOf(3)), "set true");
        check(v.setCurrentAccessibleValue(null) == false, "set null false");
        check(javax.accessibility.AccessibleValue.class.isInterface(), "interface");
        javax.swing.JButton b = new javax.swing.JButton();
        javax.accessibility.AccessibleValue bv = b.getAccessibleContext().getAccessibleValue();
        check(bv != null, "JButton exposes AccessibleValue");
        check(bv.getCurrentAccessibleValue().intValue() == 0 && bv.getMinimumAccessibleValue().intValue() == 0 && bv.getMaximumAccessibleValue().intValue() == 1, "button value triple 0/0/1");
        javax.swing.JLabel l = new javax.swing.JLabel();
        check(l.getAccessibleContext().getAccessibleValue() == null, "JLabel has no value");
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

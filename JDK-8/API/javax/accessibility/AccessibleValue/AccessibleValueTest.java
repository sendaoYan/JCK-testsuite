/*
 * @test
 * @summary javax.accessibility.AccessibleValue conformance: interface reflection.
 * @run main AccessibleValueTest
 */

public class AccessibleValueTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getCurrentAccessibleValue"), "method getCurrentAccessibleValue");
        check(hasMethod(c, "setCurrentAccessibleValue"), "method setCurrentAccessibleValue");
        check(hasMethod(c, "getMinimumAccessibleValue"), "method getMinimumAccessibleValue");
        check(hasMethod(c, "getMaximumAccessibleValue"), "method getMaximumAccessibleValue");
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

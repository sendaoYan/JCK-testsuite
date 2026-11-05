/*
 * @test
 * @summary javax.accessibility.Accessible conformance: interface reflection.
 * @run main AccessibleTest
 */

public class AccessibleTest {
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
        Class<?> c = Class.forName("javax.accessibility.Accessible");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: marker interface adoption table ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.Accessible.class.isInterface(), "interface");
        check(javax.accessibility.Accessible.class.getDeclaredMethods().length == 1, "single method getAccessibleContext");
        check(javax.accessibility.Accessible.class.getMethod("getAccessibleContext").getReturnType() == javax.accessibility.AccessibleContext.class, "return type");
        check(javax.accessibility.Accessible.class.isAssignableFrom(javax.swing.JLabel.class), "JLabel is Accessible");
        check(javax.accessibility.Accessible.class.isAssignableFrom(javax.swing.JButton.class), "JButton is Accessible");
        javax.accessibility.Accessible rec = new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
        };
        check(rec.getAccessibleContext() == null, "recording impl may return null");
        javax.swing.JButton b = new javax.swing.JButton("x");
        check(b.getAccessibleContext() != null, "real component context non-null");
        check(b.getAccessibleContext().getAccessibleParent() == null, "standalone button parent null until set");
        javax.accessibility.AccessibleContext bc = b.getAccessibleContext();
        bc.setAccessibleParent(null);
        check(bc.getAccessibleParent() == null, "setting null parent keeps null");
        check("x".equals(bc.getAccessibleName()), "button text feeds accessible name");
        check(bc.getAccessibleRole() == javax.accessibility.AccessibleRole.PUSH_BUTTON, "button role");
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

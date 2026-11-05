/*
 * @test
 * @summary javax.accessibility.Accessible conformance: white-box structure + black-box behavior.
 * @run main AccessibleTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // Minimal AccessibleContext for exercising the Accessible contract.
    static class MinContext extends javax.accessibility.AccessibleContext {
        public javax.accessibility.AccessibleRole getAccessibleRole() { return javax.accessibility.AccessibleRole.UNKNOWN; }
        public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return new javax.accessibility.AccessibleStateSet(); }
        public int getAccessibleIndexInParent() { return -1; }
        public int getAccessibleChildrenCount() { return 0; }
        public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
        public Locale getLocale() { return Locale.ROOT; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.Accessible");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleContext"), "declares getAccessibleContext");
    }

    static void testBlackBox() throws Exception {
        final javax.accessibility.AccessibleContext ctx = new MinContext();
        javax.accessibility.Accessible a = new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return ctx; }
        };
        check(a.getAccessibleContext() == ctx, "getAccessibleContext returns same instance");
        ctx.setAccessibleName("name");
        check("name".equals(a.getAccessibleContext().getAccessibleName()), "context state reachable through Accessible");

        // A subclass that is not accessible may return null.
        javax.accessibility.Accessible empty = new javax.accessibility.Accessible() {
            public javax.accessibility.AccessibleContext getAccessibleContext() { return null; }
        };
        check(empty.getAccessibleContext() == null, "null context allowed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

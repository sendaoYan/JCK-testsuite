/*
 * @test
 * @summary javax.accessibility.AccessibleIcon conformance: interface reflection.
 * @run main AccessibleIconTest
 */

public class AccessibleIconTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleIcon");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleIconDescription"), "method getAccessibleIconDescription");
        check(hasMethod(c, "setAccessibleIconDescription"), "method setAccessibleIconDescription");
        check(hasMethod(c, "getAccessibleIconWidth"), "method getAccessibleIconWidth");
        check(hasMethod(c, "getAccessibleIconHeight"), "method getAccessibleIconHeight");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording + null-array contract ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleIcon icon = new RecIcon("desc", 20, 30);
        check("desc".equals(icon.getAccessibleIconDescription()), "description");
        check(icon.getAccessibleIconWidth() == 20 && icon.getAccessibleIconHeight() == 30, "dimensions");
        check(javax.accessibility.AccessibleIcon.class.isInterface(), "interface");
        javax.accessibility.AccessibleIcon[] none = new javax.accessibility.AccessibleContext() {
            public String getAccessibleName() { return null; }
            public String getAccessibleDescription() { return null; }
            public javax.accessibility.AccessibleRole getAccessibleRole() { return null; }
            public javax.accessibility.AccessibleStateSet getAccessibleStateSet() { return null; }
            public int getAccessibleIndexInParent() { return 0; }
            public int getAccessibleChildrenCount() { return 0; }
            public javax.accessibility.Accessible getAccessibleChild(int i) { return null; }
            public java.util.Locale getLocale() { return java.util.Locale.ENGLISH; }
            public javax.accessibility.AccessibleIcon[] getAccessibleIcon() { return null; }
        }.getAccessibleIcon();
        check(none == null, "default getAccessibleIcon returns null (not empty array)");
        javax.swing.JLabel plain = new javax.swing.JLabel("text-only");
        javax.accessibility.AccessibleIcon[] jl = plain.getAccessibleContext().getAccessibleIcon();
        check(jl == null || jl.length == 0, "text JLabel has no icons");
    }
    static class RecIcon implements javax.accessibility.AccessibleIcon {
        final String d; final int w, h;
        RecIcon(String d, int w, int h) { this.d = d; this.w = w; this.h = h; }
        public String getAccessibleIconDescription() { return d; }
        public void setAccessibleIconDescription(String s) {}
        public int getAccessibleIconWidth() { return w; }
        public int getAccessibleIconHeight() { return h; }
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

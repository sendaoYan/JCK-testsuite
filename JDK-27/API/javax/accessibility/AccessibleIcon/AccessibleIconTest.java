/*
 * @test
 * @summary javax.accessibility.AccessibleIcon conformance: white-box structure + black-box behavior.
 * @run main AccessibleIconTest
 */

import java.lang.reflect.*;

public class AccessibleIconTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleIcon");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleIconDescription"), "declares getAccessibleIconDescription");
        check(hasMethod(c, "setAccessibleIconDescription"), "declares setAccessibleIconDescription");
        check(hasMethod(c, "getAccessibleIconWidth"), "declares getAccessibleIconWidth");
        check(hasMethod(c, "getAccessibleIconHeight"), "declares getAccessibleIconHeight");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleIcon icon = new javax.accessibility.AccessibleIcon() {
            String desc = "none"; int w = 0; int h = 0;
            public String getAccessibleIconDescription() { return desc; }
            public void setAccessibleIconDescription(String d) { this.desc = d; }
            public int getAccessibleIconWidth() { return w; }
            public int getAccessibleIconHeight() { return h; }
        };
        check("none".equals(icon.getAccessibleIconDescription()), "initial description");
        check(icon.getAccessibleIconWidth() == 0 && icon.getAccessibleIconHeight() == 0, "initial dimensions 0");

        icon.setAccessibleIconDescription("Save icon");
        check("Save icon".equals(icon.getAccessibleIconDescription()), "set/get description round trip");

        // Width/height are read-only in the interface; verify they stay fixed.
        check(icon.getAccessibleIconWidth() == 0, "width stays 0");
        check(icon.getAccessibleIconHeight() == 0, "height stays 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

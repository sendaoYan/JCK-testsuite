/*
 * @test
 * @summary javax.accessibility.AccessibleResourceBundle conformance: white-box structure + black-box behavior.
 * @run main AccessibleResourceBundleTest
 */

import java.lang.reflect.*;
import java.util.Enumeration;

public class AccessibleResourceBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleResourceBundle");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.util.ListResourceBundle.class, "extends ListResourceBundle");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
        check(hasMethod(c, "getContents"), "declares getContents");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleResourceBundle b = new javax.accessibility.AccessibleResourceBundle();
        Object[][] contents = b.getContents();
        check(contents != null, "getContents non-null");
        check(contents.length > 0, "getContents non-empty");

        // Every row is a two-element key/value pair.
        for (Object[] row : contents) {
            check(row != null && row.length == 2, "content row is [key,value]");
        }

        // getString (inherited from ResourceBundle) resolves known keys.
        check("active".equals(b.getString("active")), "getString active");
        check("push button".equals(b.getString("pushbutton")), "getString pushbutton");
        check("label for".equals(b.getString("labelFor")), "getString labelFor");

        // handleGetObject returns the localized value.
        check("active".equals(b.handleGetObject("active")), "handleGetObject active");

        // Unknown keys throw MissingResourceException (getString).
        boolean threw = false;
        try { b.getString("noSuchKey_zzz"); } catch (java.util.MissingResourceException e) { threw = true; }
        check(threw, "unknown key throws MissingResourceException");

        // getKeys yields all keys.
        Enumeration<String> keys = b.getKeys();
        int n = 0;
        while (keys.hasMoreElements()) { keys.nextElement(); n++; }
        check(n == contents.length, "getKeys count == contents count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: deprecated legacy class still functional ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleResourceBundle rb = new javax.accessibility.AccessibleResourceBundle();
        check(javax.accessibility.AccessibleResourceBundle.class.getSuperclass() == java.util.ListResourceBundle.class, "extends ListResourceBundle");
        java.lang.reflect.Method gm = javax.accessibility.AccessibleResourceBundle.class.getMethod("getContents");
        check(gm.getReturnType() == Object[][].class, "getContents signature");
        Object[][] contents = rb.getContents();
        check(contents.length == 80, "80 key/value pairs [" + contents.length + "]");
        boolean hasRole = false, hasState = false;
        for (Object[] pair : contents) {
            if ("pushbutton".equals(pair[0])) hasRole = true;
            if ("enabled".equals(pair[0])) hasState = true;
        }
        check(hasRole, "pushbutton key");
        check(hasState, "enabled key");
        boolean deprecated = false;
        for (java.lang.annotation.Annotation an : javax.accessibility.AccessibleResourceBundle.class.getAnnotations()) if (an.toString().contains("Deprecated")) deprecated = true;
        check(deprecated, "@Deprecated since 1.3 (kept for compatibility)");
        java.util.Enumeration<String> keys = rb.getKeys();
        check(keys.hasMoreElements(), "bundle usable");
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

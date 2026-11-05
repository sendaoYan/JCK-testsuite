/*
 * @test
 * @summary javax.accessibility.AccessibleResourceBundle conformance: class reflection.
 * @run main AccessibleResourceBundleTest
 */

public class AccessibleResourceBundleTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleResourceBundle");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass java.util.ListResourceBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getContents"), "method getContents");
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

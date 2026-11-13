/*
 * @test
 * @summary javax.accessibility.AccessibleBundle conformance: abstract class reflection.
 * @run main AccessibleBundleTest
 */

import java.util.Locale;

public class AccessibleBundleTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleBundle");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "toDisplayString"), "method toDisplayString");
        check(hasMethod(c, "toString"), "method toString");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: shared key/display machinery via Role+State probes ----
    static void testContractDeep() throws Exception {
        check(javax.accessibility.AccessibleBundle.class.getSuperclass() == java.lang.Object.class, "extends Object");
        java.lang.reflect.Field key = javax.accessibility.AccessibleBundle.class.getDeclaredField("key");
        check(java.lang.reflect.Modifier.isProtected(key.getModifiers()), "protected key field");
        // toString() == toDisplayString(Locale) only when Locale is the default;
        // pin English instead of assuming the host JVM already runs under it.
        java.util.Locale savedDefaultLocale = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            check(javax.accessibility.AccessibleRole.LABEL.toString().equals(javax.accessibility.AccessibleRole.LABEL.toDisplayString(java.util.Locale.ENGLISH)), "toString/display equivalence");
        } finally {
            java.util.Locale.setDefault(savedDefaultLocale);
        }
        check(javax.accessibility.AccessibleRole.LABEL.toString()
                        .equals(javax.accessibility.AccessibleRole.LABEL.toDisplayString(java.util.Locale.getDefault())),
                "toString/display equivalence for caller's locale");
        // unknown key falls back to the key itself (no bundle entry)
        javax.accessibility.AccessibleRole custom = new javax.accessibility.AccessibleRole("deep-x") {};
        check("deep-x".equals(custom.toDisplayString(java.util.Locale.ENGLISH)), "fallback = key");
        check("deep-x".equals(custom.toDisplayString(java.util.Locale.CHINESE)), "fallback any locale");
        check(javax.accessibility.AccessibleBundle.class.getDeclaredMethods().length >= 2, "declared methods (toDisplayString x2 + toString)");
        check(!java.io.Serializable.class.isAssignableFrom(javax.accessibility.AccessibleBundle.class), "not Serializable");
        check(!javax.accessibility.AccessibleRole.LABEL.equals(null), "equals(null) false");
        check(!javax.accessibility.AccessibleRole.LABEL.equals("label"), "equals(foreign) false");
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

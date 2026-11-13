/*
 * @test
 * @summary javax.accessibility.AccessibleBundle conformance: white-box structure + black-box behavior.
 * @run main AccessibleBundleTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    // Minimal concrete subclass exposing the protected key field and the
    // protected toDisplayString(String, Locale) helper.
    static class TestBundle extends javax.accessibility.AccessibleBundle {
        TestBundle(String k) { this.key = k; }
        String display(String bundle, Locale l) { return toDisplayString(bundle, l); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleBundle");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "toDisplayString"), "declares toDisplayString");
        check(hasMethod(c, "toString"), "declares toString");
    }

    static void testBlackBox() throws Exception {
        TestBundle active = new TestBundle("active");
        check("active".equals(active.toDisplayString(Locale.US)), "known key localized US");
        check("active".equals(active.toDisplayString(Locale.ROOT)), "known key localized ROOT");

        TestBundle unknown = new TestBundle("completelyUnknownKey_zzz_123");
        check("completelyUnknownKey_zzz_123".equals(unknown.toDisplayString(Locale.US)),
                "unknown key falls back to raw key (US)");
        check("completelyUnknownKey_zzz_123".equals(unknown.display("no.such.bundle.Name", Locale.US)),
                "missing bundle falls back to raw key");
        check("active".equals(active.display("com.sun.accessibility.internal.resources.accessibility", Locale.US)),
                "protected toDisplayString(name,locale) works");

        // toString() and toDisplayString() both use the default locale.
        check(active.toString() != null, "toString non-null");
        check(active.toString().equals(active.toDisplayString()), "toString == toDisplayString()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

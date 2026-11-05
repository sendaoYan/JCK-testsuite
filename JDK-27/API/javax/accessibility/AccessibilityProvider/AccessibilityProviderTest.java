/*
 * @test
 * @summary javax.accessibility.AccessibilityProvider conformance: white-box structure + black-box behavior.
 * @run main AccessibilityProviderTest
 */

import java.lang.reflect.*;

public class AccessibilityProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibilityProvider");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "activate"), "declares activate");
    }

    static void testBlackBox() throws Exception {
        final boolean[] activated = new boolean[1];
        javax.accessibility.AccessibilityProvider p = new javax.accessibility.AccessibilityProvider() {
            public String getName() { return "testProvider"; }
            public void activate() { activated[0] = true; }
        };
        check("testProvider".equals(p.getName()), "getName");
        check(!activated[0], "not activated initially");
        p.activate();
        check(activated[0], "activate sets flag");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract surface + activate-callback loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.accessibility.AccessibilityProvider.class.getModifiers()), "abstract");
        java.lang.reflect.Constructor<?>[] cts = javax.accessibility.AccessibilityProvider.class.getDeclaredConstructors();
        check(cts.length == 1 && java.lang.reflect.Modifier.isProtected(cts[0].getModifiers()), "single protected ctor");
        javax.accessibility.AccessibilityProvider.class.getMethod("getName");
        javax.accessibility.AccessibilityProvider.class.getMethod("activate");
        passed += 2;
        final boolean[] activated = new boolean[1];
        javax.accessibility.AccessibilityProvider p = new javax.accessibility.AccessibilityProvider() {
            public String getName() { return "deep-provider"; }
            public void activate() { activated[0] = true; }
        };
        check("deep-provider".equals(p.getName()), "name");
        check(!activated[0], "not yet activated");
        p.activate();
        check(activated[0], "activate callback fired");
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

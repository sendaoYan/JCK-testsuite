/*
 * @test
 * @summary javax.accessibility.AccessibleState conformance: class reflection.
 * @run main AccessibleStateTest
 */

import java.util.Locale;

public class AccessibleStateTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleState");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("javax.accessibility.AccessibleBundle"), "superclass javax.accessibility.AccessibleBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: full 29-constant table + display strings ----
    static void testContractDeep() throws Exception {
        javax.accessibility.AccessibleState[] states = {
            javax.accessibility.AccessibleState.ACTIVE, javax.accessibility.AccessibleState.PRESSED, javax.accessibility.AccessibleState.ARMED,
            javax.accessibility.AccessibleState.BUSY, javax.accessibility.AccessibleState.CHECKED, javax.accessibility.AccessibleState.EDITABLE,
            javax.accessibility.AccessibleState.EXPANDABLE, javax.accessibility.AccessibleState.COLLAPSED, javax.accessibility.AccessibleState.EXPANDED,
            javax.accessibility.AccessibleState.ENABLED, javax.accessibility.AccessibleState.FOCUSABLE, javax.accessibility.AccessibleState.FOCUSED,
            javax.accessibility.AccessibleState.ICONIFIED, javax.accessibility.AccessibleState.MODAL, javax.accessibility.AccessibleState.OPAQUE,
            javax.accessibility.AccessibleState.RESIZABLE, javax.accessibility.AccessibleState.MULTISELECTABLE, javax.accessibility.AccessibleState.SELECTABLE,
            javax.accessibility.AccessibleState.SELECTED, javax.accessibility.AccessibleState.SHOWING, javax.accessibility.AccessibleState.VISIBLE,
            javax.accessibility.AccessibleState.VERTICAL, javax.accessibility.AccessibleState.HORIZONTAL, javax.accessibility.AccessibleState.SINGLE_LINE,
            javax.accessibility.AccessibleState.MULTI_LINE, javax.accessibility.AccessibleState.TRANSIENT, javax.accessibility.AccessibleState.MANAGES_DESCENDANTS,
            javax.accessibility.AccessibleState.INDETERMINATE, javax.accessibility.AccessibleState.TRUNCATED };
        check(states.length == 29, "29 state constants");
        for (javax.accessibility.AccessibleState s : states) check(s != null && s.toDisplayString(java.util.Locale.ENGLISH) != null, "state displayable " + s);
        check("focused".equals(javax.accessibility.AccessibleState.FOCUSED.toDisplayString(java.util.Locale.ENGLISH)), "focused");
        check("enabled".equals(javax.accessibility.AccessibleState.ENABLED.toDisplayString(java.util.Locale.ENGLISH)), "enabled");
        // toString() follows Locale.getDefault(); pin English for the literal check.
        java.util.Locale savedDefaultLocale = java.util.Locale.getDefault();
        try {
            java.util.Locale.setDefault(java.util.Locale.ENGLISH);
            check("enabled".equals(javax.accessibility.AccessibleState.ENABLED.toString()), "toString = English display string");
        } finally {
            java.util.Locale.setDefault(savedDefaultLocale);
        }
        check(javax.accessibility.AccessibleState.ENABLED.toString()
                        .equals(javax.accessibility.AccessibleState.ENABLED.toDisplayString(java.util.Locale.getDefault())),
                "toString follows the caller's default locale");
        check("activ\u00E9".equals(javax.accessibility.AccessibleState.ENABLED.toDisplayString(java.util.Locale.FRENCH)), "French activ\u00E9");
        check(javax.accessibility.AccessibleState.ENABLED.equals(javax.accessibility.AccessibleState.ENABLED), "self equals");
        check(!javax.accessibility.AccessibleState.ENABLED.equals(javax.accessibility.AccessibleState.FOCUSED), "distinct unequal");
        check(javax.accessibility.AccessibleState.class.getSuperclass() == javax.accessibility.AccessibleBundle.class, "extends AccessibleBundle");
        check(!java.lang.reflect.Modifier.isPublic(javax.accessibility.AccessibleState.class.getDeclaredConstructors()[0].getModifiers()), "state ctor protected");
        java.lang.reflect.Field kf = javax.accessibility.AccessibleBundle.class.getDeclaredField("key");
        try {
            kf.setAccessible(true);
            check("enabled".equals(kf.get(javax.accessibility.AccessibleState.ENABLED)), "key field = enabled (JDK-8 open reflection)");
            check(java.lang.reflect.Modifier.isProtected(kf.getModifiers()), "key protected modifier");
        } catch (RuntimeException e) {
            check(e.getClass().getName().endsWith("InaccessibleObjectException"), "JDK-9+ module java.desktop does not open protected key");
        }
        javax.accessibility.AccessibleState custom = new javax.accessibility.AccessibleState("deep-state") {};
        check("deep-state".equals(custom.toDisplayString(java.util.Locale.ENGLISH)), "custom state key passthrough");
        check(!javax.accessibility.AccessibleState.ENABLED.equals(javax.accessibility.AccessibleRole.LABEL), "cross-bundle equals false");
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

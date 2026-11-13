/*
 * @test
 * @summary javax.accessibility.AccessibleState conformance: white-box structure + black-box behavior.
 * @run main AccessibleStateTest
 */

import java.lang.reflect.*;
import java.util.Locale;

public class AccessibleStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static class CustomState extends javax.accessibility.AccessibleState {
        CustomState(String k) { super(k); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleState");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == javax.accessibility.AccessibleBundle.class, "extends AccessibleBundle");
        boolean hasTD = false;
        for (Method m : c.getMethods()) if (m.getName().equals("toDisplayString")) hasTD = true;
        check(hasTD, "inherits toDisplayString");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleState active = javax.accessibility.AccessibleState.ACTIVE;
        check(active != null, "ACTIVE non-null");
        check(javax.accessibility.AccessibleState.ENABLED != null, "ENABLED non-null");
        check(javax.accessibility.AccessibleState.VISIBLE != null, "VISIBLE non-null");
        check(javax.accessibility.AccessibleState.SHOWING != null, "SHOWING non-null");
        check(active != javax.accessibility.AccessibleState.ENABLED, "distinct constants");

        check("active".equals(active.toDisplayString(Locale.US)), "ACTIVE localized");
        check("enabled".equals(javax.accessibility.AccessibleState.ENABLED.toDisplayString(Locale.US)),
                "ENABLED localized");
        check(active.toString() != null, "toString non-null");
        check(active.toString().equals(active.toDisplayString()), "toString == toDisplayString()");

        CustomState custom = new CustomState("customStateUnknownKey_xyz");
        check("customStateUnknownKey_xyz".equals(custom.toDisplayString(Locale.US)),
                "unknown state key falls back to raw key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        check("activé".equals(javax.accessibility.AccessibleState.ENABLED.toDisplayString(java.util.Locale.FRENCH)), "French activé");
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

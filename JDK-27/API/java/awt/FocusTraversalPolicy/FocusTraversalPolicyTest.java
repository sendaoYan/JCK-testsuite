/*
 * @test
 * @summary java.awt.FocusTraversalPolicy conformance: white-box structure + black-box behavior.
 * @run main FocusTraversalPolicyTest
 */

import java.lang.reflect.*;

public class FocusTraversalPolicyTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.FocusTraversalPolicy", false, FocusTraversalPolicyTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.FocusTraversalPolicy");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getInitialComponent"), "declares getInitialComponent");
    }

    static void testBlackBox() throws Exception {
        java.awt.FocusTraversalPolicy p = new java.awt.FocusTraversalPolicy() {
            public java.awt.Component getComponentAfter(java.awt.Container c, java.awt.Component a) { return null; }
            public java.awt.Component getComponentBefore(java.awt.Container c, java.awt.Component a) { return null; }
            public java.awt.Component getFirstComponent(java.awt.Container c) { return null; }
            public java.awt.Component getLastComponent(java.awt.Container c) { return null; }
            public java.awt.Component getDefaultComponent(java.awt.Container c) { return null; }
        };
        boolean iae = false;
        try { p.getInitialComponent(null); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "getInitialComponent(null) throws IAE");
        check(p.getFirstComponent(null) == null, "getFirstComponent null (anonymous override)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

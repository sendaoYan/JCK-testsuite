/*
 * @test
 * @summary java.awt.DefaultFocusTraversalPolicy conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main DefaultFocusTraversalPolicyTest
 */

import java.lang.reflect.*;

public class DefaultFocusTraversalPolicyTest {
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
        Class<?> c = Class.forName("java.awt.DefaultFocusTraversalPolicy", false, DefaultFocusTraversalPolicyTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.DefaultFocusTraversalPolicy");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.getSuperclass().getName().equals("java.awt.ContainerOrderFocusTraversalPolicy"), "extends ContainerOrderFocusTraversalPolicy");
        check(hasMethod(c, "accept"), "declares accept");
    }

    static void testBlackBox() throws Exception {
        java.awt.DefaultFocusTraversalPolicy p = new java.awt.DefaultFocusTraversalPolicy();
        java.awt.Frame f = new java.awt.Frame();
        java.awt.Button b1 = new java.awt.Button("a");
        java.awt.Button b2 = new java.awt.Button("b");
        f.setLayout(new java.awt.FlowLayout());
        f.add(b1);
        f.add(b2);
        f.setVisible(true);
        try {
            check(p.getFirstComponent(f) == b1, "getFirstComponent == b1");
            check(p.getComponentAfter(f, b1) == b2, "getComponentAfter b1 -> b2");
            check(p.getComponentBefore(f, b2) == b1, "getComponentBefore b2 -> b1");
            check(p.getLastComponent(f) == b2, "getLastComponent == b2");
            check(p.getDefaultComponent(f) == b1, "getDefaultComponent == b1");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

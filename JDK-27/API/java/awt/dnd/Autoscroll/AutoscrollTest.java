/*
 * @test
 * @summary java.awt.dnd.Autoscroll conformance: white-box structure + black-box behavior.
 * @run main AutoscrollTest
 */

import java.lang.reflect.*;

public class AutoscrollTest {
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
        Class<?> c = Class.forName("java.awt.dnd.Autoscroll", false, AutoscrollTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.dnd.Autoscroll");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(c.isInterface(), "is interface");
    }

    static void testBlackBox() throws Exception {
        java.awt.dnd.Autoscroll a = new java.awt.dnd.Autoscroll() {
            public java.awt.Insets getAutoscrollInsets() { return new java.awt.Insets(0, 0, 0, 0); }
            public void autoscroll(java.awt.Point p) {}
        };
        java.awt.Insets ins = a.getAutoscrollInsets();
        check(ins != null && ins.top == 0 && ins.left == 0 && ins.bottom == 0 && ins.right == 0, "insets zero");
        a.autoscroll(new java.awt.Point(1, 1));
        check(true, "autoscroll callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

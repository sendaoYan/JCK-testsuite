/*
 * @test
 * @summary javax.accessibility.AccessibleContext conformance: abstract class.
 * @run main AccessibleContextTest
 */

import java.lang.reflect.*;


public class AccessibleContextTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleContext");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "getAccessibleName"), "declares getAccessibleName");
        check(hasMethod(c, "setAccessibleName"), "declares setAccessibleName");
        check(hasMethod(c, "getAccessibleDescription"), "declares getAccessibleDescription");
        check(hasMethod(c, "setAccessibleDescription"), "declares setAccessibleDescription");
        check(hasMethod(c, "getAccessibleRole"), "declares getAccessibleRole");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

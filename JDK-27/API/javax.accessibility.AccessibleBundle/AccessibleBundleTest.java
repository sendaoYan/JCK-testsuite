/*
 * @test
 * @summary javax.accessibility.AccessibleBundle conformance: abstract class.
 * @run main AccessibleBundleTest
 */

import java.lang.reflect.*;


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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleBundle");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "toDisplayString"), "declares toDisplayString");
        check(hasMethod(c, "toString"), "declares toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

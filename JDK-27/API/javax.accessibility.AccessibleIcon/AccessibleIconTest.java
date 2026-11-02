/*
 * @test
 * @summary javax.accessibility.AccessibleIcon conformance: interface.
 * @run main AccessibleIconTest
 */

import java.lang.reflect.*;


public class AccessibleIconTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleIcon");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleIconDescription"), "declares getAccessibleIconDescription");
        check(hasMethod(c, "setAccessibleIconDescription"), "declares setAccessibleIconDescription");
        check(hasMethod(c, "getAccessibleIconWidth"), "declares getAccessibleIconWidth");
        check(hasMethod(c, "getAccessibleIconHeight"), "declares getAccessibleIconHeight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

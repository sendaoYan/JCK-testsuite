/*
 * @test
 * @summary javax.accessibility.AccessibleValue conformance: interface.
 * @run main AccessibleValueTest
 */

import java.lang.reflect.*;


public class AccessibleValueTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleValue");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getCurrentAccessibleValue"), "declares getCurrentAccessibleValue");
        check(hasMethod(c, "setCurrentAccessibleValue"), "declares setCurrentAccessibleValue");
        check(hasMethod(c, "getMinimumAccessibleValue"), "declares getMinimumAccessibleValue");
        check(hasMethod(c, "getMaximumAccessibleValue"), "declares getMaximumAccessibleValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

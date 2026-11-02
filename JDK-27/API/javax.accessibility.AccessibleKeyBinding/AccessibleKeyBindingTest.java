/*
 * @test
 * @summary javax.accessibility.AccessibleKeyBinding conformance: interface.
 * @run main AccessibleKeyBindingTest
 */

import java.lang.reflect.*;


public class AccessibleKeyBindingTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleKeyBinding");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleKeyBindingCount"), "declares getAccessibleKeyBindingCount");
        check(hasMethod(c, "getAccessibleKeyBinding"), "declares getAccessibleKeyBinding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

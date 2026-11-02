/*
 * @test
 * @summary javax.accessibility.AccessibleComponent conformance: interface.
 * @run main AccessibleComponentTest
 */

import java.lang.reflect.*;


public class AccessibleComponentTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleComponent");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getBackground"), "declares getBackground");
        check(hasMethod(c, "setBackground"), "declares setBackground");
        check(hasMethod(c, "getForeground"), "declares getForeground");
        check(hasMethod(c, "setForeground"), "declares setForeground");
        check(hasMethod(c, "getCursor"), "declares getCursor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

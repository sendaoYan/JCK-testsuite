/*
 * @test
 * @summary javax.accessibility.AccessibleTableModelChange conformance: interface.
 * @run main AccessibleTableModelChangeTest
 */

import java.lang.reflect.*;


public class AccessibleTableModelChangeTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleTableModelChange");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getType"), "declares getType");
        check(hasMethod(c, "getFirstRow"), "declares getFirstRow");
        check(hasMethod(c, "getLastRow"), "declares getLastRow");
        check(hasMethod(c, "getFirstColumn"), "declares getFirstColumn");
        check(hasMethod(c, "getLastColumn"), "declares getLastColumn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

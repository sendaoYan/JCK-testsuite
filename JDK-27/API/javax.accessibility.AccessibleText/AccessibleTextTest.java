/*
 * @test
 * @summary javax.accessibility.AccessibleText conformance: interface.
 * @run main AccessibleTextTest
 */

import java.lang.reflect.*;


public class AccessibleTextTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleText");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getIndexAtPoint"), "declares getIndexAtPoint");
        check(hasMethod(c, "getCharacterBounds"), "declares getCharacterBounds");
        check(hasMethod(c, "getCharCount"), "declares getCharCount");
        check(hasMethod(c, "getCaretPosition"), "declares getCaretPosition");
        check(hasMethod(c, "getAtIndex"), "declares getAtIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

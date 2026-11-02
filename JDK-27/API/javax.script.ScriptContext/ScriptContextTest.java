/*
 * @test
 * @summary javax.script.ScriptContext conformance: interface.
 * @run main ScriptContextTest
 */

import java.lang.reflect.*;


public class ScriptContextTest {
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
        Class<?> c = Class.forName("javax.script.ScriptContext");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "setBindings"), "declares setBindings");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setAttribute"), "declares setAttribute");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "removeAttribute"), "declares removeAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

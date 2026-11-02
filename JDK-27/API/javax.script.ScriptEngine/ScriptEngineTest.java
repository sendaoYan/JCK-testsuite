/*
 * @test
 * @summary javax.script.ScriptEngine conformance: interface.
 * @run main ScriptEngineTest
 */

import java.lang.reflect.*;


public class ScriptEngineTest {
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
        Class<?> c = Class.forName("javax.script.ScriptEngine");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "eval"), "declares eval");
        check(hasMethod(c, "put"), "declares put");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setBindings"), "declares setBindings");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

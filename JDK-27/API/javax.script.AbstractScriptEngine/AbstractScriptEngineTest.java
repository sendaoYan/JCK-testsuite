/*
 * @test
 * @summary javax.script.AbstractScriptEngine conformance: abstract class.
 * @run main AbstractScriptEngineTest
 */

import java.lang.reflect.*;


public class AbstractScriptEngineTest {
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
        Class<?> c = Class.forName("javax.script.AbstractScriptEngine");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasMethod(c, "setContext"), "declares setContext");
        check(hasMethod(c, "getContext"), "declares getContext");
        check(hasMethod(c, "getBindings"), "declares getBindings");
        check(hasMethod(c, "setBindings"), "declares setBindings");
        check(hasMethod(c, "put"), "declares put");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.script.ScriptEngineFactory conformance: interface.
 * @run main ScriptEngineFactoryTest
 */

import java.lang.reflect.*;


public class ScriptEngineFactoryTest {
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
        Class<?> c = Class.forName("javax.script.ScriptEngineFactory");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getEngineName"), "declares getEngineName");
        check(hasMethod(c, "getEngineVersion"), "declares getEngineVersion");
        check(hasMethod(c, "getExtensions"), "declares getExtensions");
        check(hasMethod(c, "getMimeTypes"), "declares getMimeTypes");
        check(hasMethod(c, "getNames"), "declares getNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

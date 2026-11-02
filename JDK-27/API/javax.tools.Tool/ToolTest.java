/*
 * @test
 * @summary javax.tools.Tool conformance: interface.
 * @run main ToolTest
 */

import java.lang.reflect.*;


public class ToolTest {
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
        Class<?> c = Class.forName("javax.tools.Tool");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "name"), "declares name");
        check(hasMethod(c, "run"), "declares run");
        check(hasMethod(c, "getSourceVersions"), "declares getSourceVersions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

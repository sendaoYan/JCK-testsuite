/*
 * @test
 * @summary javax.tools.Diagnostic conformance: interface.
 * @run main DiagnosticTest
 */

import java.lang.reflect.*;


public class DiagnosticTest {
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
        Class<?> c = Class.forName("javax.tools.Diagnostic");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getKind"), "declares getKind");
        check(hasMethod(c, "getSource"), "declares getSource");
        check(hasMethod(c, "getPosition"), "declares getPosition");
        check(hasMethod(c, "getStartPosition"), "declares getStartPosition");
        check(hasMethod(c, "getEndPosition"), "declares getEndPosition");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

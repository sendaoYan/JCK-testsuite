/*
 * @test
 * @summary javax.tools.OptionChecker conformance: interface.
 * @run main OptionCheckerTest
 */

import java.lang.reflect.*;


public class OptionCheckerTest {
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
        Class<?> c = Class.forName("javax.tools.OptionChecker");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "isSupportedOption"), "declares isSupportedOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

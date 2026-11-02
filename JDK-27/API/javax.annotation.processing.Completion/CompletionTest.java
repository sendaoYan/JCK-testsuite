/*
 * @test
 * @summary javax.annotation.processing.Completion conformance: interface.
 * @run main CompletionTest
 */

import java.lang.reflect.*;


public class CompletionTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.Completion");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "getMessage"), "declares getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

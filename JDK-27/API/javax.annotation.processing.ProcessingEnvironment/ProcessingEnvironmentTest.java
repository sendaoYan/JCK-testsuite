/*
 * @test
 * @summary javax.annotation.processing.ProcessingEnvironment conformance: interface.
 * @run main ProcessingEnvironmentTest
 */

import java.lang.reflect.*;


public class ProcessingEnvironmentTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.ProcessingEnvironment");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getOptions"), "declares getOptions");
        check(hasMethod(c, "getMessager"), "declares getMessager");
        check(hasMethod(c, "getFiler"), "declares getFiler");
        check(hasMethod(c, "getElementUtils"), "declares getElementUtils");
        check(hasMethod(c, "getTypeUtils"), "declares getTypeUtils");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

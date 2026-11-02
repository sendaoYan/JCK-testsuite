/*
 * @test
 * @summary javax.annotation.processing.RoundEnvironment conformance: interface.
 * @run main RoundEnvironmentTest
 */

import java.lang.reflect.*;


public class RoundEnvironmentTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.RoundEnvironment");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "processingOver"), "declares processingOver");
        check(hasMethod(c, "errorRaised"), "declares errorRaised");
        check(hasMethod(c, "getRootElements"), "declares getRootElements");
        check(hasMethod(c, "getElementsAnnotatedWith"), "declares getElementsAnnotatedWith");
        check(hasMethod(c, "getElementsAnnotatedWithAny"), "declares getElementsAnnotatedWithAny");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.annotation.processing.SupportedSourceVersion conformance: annotation interface.
 * @run main SupportedSourceVersionTest
 */

import java.lang.reflect.*;


public class SupportedSourceVersionTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.SupportedSourceVersion");
        check(c.isInterface(), "isInterface");
        check(c.isAnnotation(), "isAnnotation");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.lang.annotation.Annotation"), "extends java.lang.annotation.Annotation");
        check(hasMethod(c, "value"), "declares value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.annotation.processing.AbstractProcessor conformance: abstract class.
 * @run main AbstractProcessorTest
 */

import java.lang.reflect.*;


public class AbstractProcessorTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.AbstractProcessor");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasMethod(c, "getSupportedOptions"), "declares getSupportedOptions");
        check(hasMethod(c, "getSupportedAnnotationTypes"), "declares getSupportedAnnotationTypes");
        check(hasMethod(c, "getSupportedSourceVersion"), "declares getSupportedSourceVersion");
        check(hasMethod(c, "init"), "declares init");
        check(hasMethod(c, "process"), "declares process");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

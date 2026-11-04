/*
 * @test
 * @summary javax.annotation.processing.Processor conformance: interface hierarchy and methods.
 * @run main ProcessorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ProcessorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.annotation.processing.Processor");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSupportedOptions"), "has method getSupportedOptions");
        check(names.contains("getSupportedAnnotationTypes"), "has method getSupportedAnnotationTypes");
        check(names.contains("getSupportedSourceVersion"), "has method getSupportedSourceVersion");
        check(names.contains("init"), "has method init");
        check(names.contains("process"), "has method process");
        check(names.contains("getCompletions"), "has method getCompletions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

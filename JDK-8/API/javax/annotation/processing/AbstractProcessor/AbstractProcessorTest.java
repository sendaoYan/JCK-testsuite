/*
 * @test
 * @summary javax.annotation.processing.AbstractProcessor conformance: abstract class hierarchy and methods.
 * @run main AbstractProcessorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AbstractProcessorTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.AbstractProcessor");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.annotation.processing.Processor"), "implements javax.annotation.processing.Processor");
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

/*
 * @test
 * @summary javax.xml.bind.Validator conformance: interface hierarchy and methods.
 * @run main ValidatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ValidatorTest {
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
        Class<?> c = Class.forName("javax.xml.bind.Validator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("validate"), "has method validate");
        check(names.contains("validateRoot"), "has method validateRoot");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

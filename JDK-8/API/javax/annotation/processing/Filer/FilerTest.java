/*
 * @test
 * @summary javax.annotation.processing.Filer conformance: interface hierarchy and methods.
 * @run main FilerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class FilerTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.Filer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createSourceFile"), "has method createSourceFile");
        check(names.contains("createClassFile"), "has method createClassFile");
        check(names.contains("createResource"), "has method createResource");
        check(names.contains("getResource"), "has method getResource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

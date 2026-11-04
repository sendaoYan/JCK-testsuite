/*
 * @test
 * @summary javax.activation.FileTypeMap conformance: abstract class hierarchy and methods.
 * @run main FileTypeMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class FileTypeMapTest {
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
        Class<?> c = Class.forName("javax.activation.FileTypeMap");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContentType"), "has method getContentType");
        check(names.contains("setDefaultFileTypeMap"), "has method setDefaultFileTypeMap");
        check(names.contains("getDefaultFileTypeMap"), "has method getDefaultFileTypeMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

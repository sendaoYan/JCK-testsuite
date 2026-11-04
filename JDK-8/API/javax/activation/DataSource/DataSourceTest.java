/*
 * @test
 * @summary javax.activation.DataSource conformance: interface hierarchy and methods.
 * @run main DataSourceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DataSourceTest {
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
        Class<?> c = Class.forName("javax.activation.DataSource");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getInputStream"), "has method getInputStream");
        check(names.contains("getOutputStream"), "has method getOutputStream");
        check(names.contains("getContentType"), "has method getContentType");
        check(names.contains("getName"), "has method getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

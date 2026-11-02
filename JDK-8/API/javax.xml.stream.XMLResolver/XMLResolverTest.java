/*
 * @test
 * @summary javax.xml.stream.XMLResolver conformance: interface hierarchy and methods.
 * @run main XMLResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLResolverTest {
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
        Class<?> c = Class.forName("javax.xml.stream.XMLResolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("resolveEntity"), "has method resolveEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

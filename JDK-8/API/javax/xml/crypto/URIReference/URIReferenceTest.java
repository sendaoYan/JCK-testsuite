/*
 * @test
 * @summary javax.xml.crypto.URIReference conformance: interface hierarchy and methods.
 * @run main URIReferenceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class URIReferenceTest {
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
        Class<?> c = Class.forName("javax.xml.crypto.URIReference");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getURI"), "has method getURI");
        check(names.contains("getType"), "has method getType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

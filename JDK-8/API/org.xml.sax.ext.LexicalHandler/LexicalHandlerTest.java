/*
 * @test
 * @summary org.xml.sax.ext.LexicalHandler conformance: interface hierarchy and methods.
 * @run main LexicalHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class LexicalHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.ext.LexicalHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("startDTD"), "has method startDTD");
        check(names.contains("endDTD"), "has method endDTD");
        check(names.contains("startEntity"), "has method startEntity");
        check(names.contains("endEntity"), "has method endEntity");
        check(names.contains("startCDATA"), "has method startCDATA");
        check(names.contains("endCDATA"), "has method endCDATA");
        check(names.contains("comment"), "has method comment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

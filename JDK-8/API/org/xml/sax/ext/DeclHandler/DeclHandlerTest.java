/*
 * @test
 * @summary org.xml.sax.ext.DeclHandler conformance: interface hierarchy and methods.
 * @run main DeclHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DeclHandlerTest {
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
        Class<?> c = Class.forName("org.xml.sax.ext.DeclHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("elementDecl"), "has method elementDecl");
        check(names.contains("attributeDecl"), "has method attributeDecl");
        check(names.contains("internalEntityDecl"), "has method internalEntityDecl");
        check(names.contains("externalEntityDecl"), "has method externalEntityDecl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

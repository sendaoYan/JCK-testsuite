/*
 * @test
 * @summary javax.xml.xpath.XPath conformance: interface hierarchy and methods.
 * @run main XPathTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XPathTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPath");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("reset"), "has method reset");
        check(names.contains("setXPathVariableResolver"), "has method setXPathVariableResolver");
        check(names.contains("getXPathVariableResolver"), "has method getXPathVariableResolver");
        check(names.contains("setXPathFunctionResolver"), "has method setXPathFunctionResolver");
        check(names.contains("getXPathFunctionResolver"), "has method getXPathFunctionResolver");
        check(names.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(names.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(names.contains("compile"), "has method compile");
        check(names.contains("evaluate"), "has method evaluate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

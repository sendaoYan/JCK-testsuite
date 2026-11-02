/*
 * @test
 * @summary javax.xml.transform.Transformer conformance: abstract class hierarchy and methods.
 * @run main TransformerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class TransformerTest {
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
        Class<?> c = Class.forName("javax.xml.transform.Transformer");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("reset"), "has method reset");
        check(names.contains("transform"), "has method transform");
        check(names.contains("setParameter"), "has method setParameter");
        check(names.contains("getParameter"), "has method getParameter");
        check(names.contains("clearParameters"), "has method clearParameters");
        check(names.contains("setURIResolver"), "has method setURIResolver");
        check(names.contains("getURIResolver"), "has method getURIResolver");
        check(names.contains("setOutputProperties"), "has method setOutputProperties");
        check(names.contains("getOutputProperties"), "has method getOutputProperties");
        check(names.contains("setOutputProperty"), "has method setOutputProperty");
        check(names.contains("getOutputProperty"), "has method getOutputProperty");
        check(names.contains("setErrorListener"), "has method setErrorListener");
        check(names.contains("getErrorListener"), "has method getErrorListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

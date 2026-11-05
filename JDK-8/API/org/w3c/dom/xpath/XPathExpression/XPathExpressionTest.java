/*
 * @test
 * @summary org.w3c.dom.xpath.XPathExpression conformance: reflection shape checks.
 * @run main XPathExpressionTest
 */

import java.lang.reflect.*;

public class XPathExpressionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathExpression");
        check(c != null, "org.w3c.dom.xpath.XPathExpression loads");
        check(c.getName().equals("org.w3c.dom.xpath.XPathExpression"), "binary name is org.w3c.dom.xpath.XPathExpression");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.xpath.XPathExpression is public");
        check(c.isInterface(), "org.w3c.dom.xpath.XPathExpression is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "evaluate"), "declares evaluate");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathExpression shape-lock (JDK 8 reflection) ----
    static void deepContract() throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathExpression");
        check(c.isInterface(), "XPathExpression is an interface");
        java.lang.reflect.Method m = c.getMethod("evaluate", org.w3c.dom.Node.class, short.class, Object.class);
        check(m != null, "evaluate(Node,short,Object) declared");
        java.util.Set<String> exn = new java.util.HashSet<String>();
        for (Class<?> x : m.getExceptionTypes()) exn.add(x.getName());
        check(exn.contains("org.w3c.dom.xpath.XPathException") && exn.contains("org.w3c.dom.DOMException"), "evaluate declares XPathException + DOMException");
    }
}

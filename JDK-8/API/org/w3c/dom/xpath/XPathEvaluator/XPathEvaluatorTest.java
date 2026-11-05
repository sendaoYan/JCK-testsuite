/*
 * @test
 * @summary org.w3c.dom.xpath.XPathEvaluator conformance: reflection shape checks.
 * @run main XPathEvaluatorTest
 */

import java.lang.reflect.*;

public class XPathEvaluatorTest {
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
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathEvaluator");
        check(c != null, "org.w3c.dom.xpath.XPathEvaluator loads");
        check(c.getName().equals("org.w3c.dom.xpath.XPathEvaluator"), "binary name is org.w3c.dom.xpath.XPathEvaluator");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.xpath.XPathEvaluator is public");
        check(c.isInterface(), "org.w3c.dom.xpath.XPathEvaluator is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "createExpression"), "declares createExpression");
        check(declares(c, "createNSResolver"), "declares createNSResolver");
        check(declares(c, "evaluate"), "declares evaluate");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathEvaluator shape-lock (JDK 8: org.w3c.dom.xpath absent from ct.sym -> reflection) ----
    static void deepContract() throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathEvaluator");
        check(c.isInterface(), "XPathEvaluator is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("createExpression") && names.contains("createNSResolver") && names.contains("evaluate"), "three evaluator methods declared");
        // the module is never wired into the DOM
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        check(d.getImplementation().getFeature("XPath", "3.0") == null, "getFeature(XPath,3.0) -> null (module absent)");
        check(!c.isInstance(d), "Document is not an XPathEvaluator");
        // createExpression/evaluate parameter shapes
        java.lang.reflect.Method ce = c.getMethod("createExpression", String.class, Class.forName("org.w3c.dom.xpath.XPathNSResolver"));
        check(ce.getReturnType().getName().equals("org.w3c.dom.xpath.XPathExpression"), "createExpression returns XPathExpression");
        java.lang.reflect.Method ev = c.getMethod("evaluate", String.class, org.w3c.dom.Node.class,
            Class.forName("org.w3c.dom.xpath.XPathNSResolver"), short.class, Object.class);
        check(ev != null, "evaluate(String,Node,XPathNSResolver,short,Object) declared");
    }
}

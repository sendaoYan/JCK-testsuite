/*
 * @test
 * @summary javax.xml.xpath.XPathFunction conformance: interface hierarchy and methods.
 * @run main XPathFunctionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XPathFunctionTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathFunction");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("evaluate"), "has method evaluate");
        check(c.getConstructors().length == 0, "no public constructors (interface)");
        testInvocation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    // XPathFunction.evaluate(List) receives XPath 1.0-typed arguments and its
    // return value flows back into the expression (closed loop through the runtime).
    static void testInvocation() throws Exception {
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
            public java.util.Iterator<String> getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
        });
        final java.util.List<Object> seen = new java.util.ArrayList<Object>();
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) {
                seen.addAll(args);
                return Double.valueOf(9);
            }
        });
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
        Double r = (Double) xp.evaluate("f:g(1, 's', true())", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r != null && r.doubleValue() == 9.0, "returned Double flows back as NUMBER result");
        check(seen.size() == 3, "evaluate receives all 3 arguments in order");
        check(seen.get(0) instanceof Double && ((Double) seen.get(0)).doubleValue() == 1.0,
              "numeric argument mapped to Double");
        check("s".equals(seen.get(1)), "string literal passed as String");
        check(Boolean.TRUE.equals(seen.get(2)), "boolean literal passed as Boolean");

        // Boolean and String returns map to BOOLEAN/STRING results
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) { return Boolean.TRUE; }
        });
        check(Boolean.TRUE.equals(xp.evaluate("f:g()", d, javax.xml.xpath.XPathConstants.BOOLEAN)),
              "Boolean return drives BOOLEAN result");
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) { return "strresult"; }
        });
        check("strresult".equals(xp.evaluate("f:g()", d, javax.xml.xpath.XPathConstants.STRING)),
              "String return drives STRING result");
    }
}

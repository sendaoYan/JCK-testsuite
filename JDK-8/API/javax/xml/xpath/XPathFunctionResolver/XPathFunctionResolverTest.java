/*
 * @test
 * @summary javax.xml.xpath.XPathFunctionResolver conformance: interface hierarchy and methods.
 * @run main XPathFunctionResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XPathFunctionResolverTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathFunctionResolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("resolveFunction"), "has method resolveFunction");
        check(c.getConstructors().length == 0, "no public constructors (interface)");
        testResolution();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static javax.xml.namespace.NamespaceContext NS_F = new javax.xml.namespace.NamespaceContext() {
        public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
        public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
        public java.util.Iterator<String> getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
    };

    static org.w3c.dom.Document emptyDoc() throws Exception {
        return javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
    }

    static void testResolution() throws Exception {
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(NS_F);
        final javax.xml.namespace.QName[] asked = new javax.xml.namespace.QName[1];
        final int[] arity = new int[1];
        xp.setXPathFunctionResolver(new javax.xml.xpath.XPathFunctionResolver() {
            public javax.xml.xpath.XPathFunction resolveFunction(javax.xml.namespace.QName fn, int a) {
                asked[0] = fn;
                arity[0] = a;
                return new javax.xml.xpath.XPathFunction() {
                    public Object evaluate(java.util.List args) { return Double.valueOf(5); }
                };
            }
        });
        org.w3c.dom.Document d = emptyDoc();
        Double r = (Double) xp.evaluate("f:myfn(1)", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r != null && r.doubleValue() == 5.0, "resolved function executes (closed loop)");
        check(asked[0] != null && "urn:f".equals(asked[0].getNamespaceURI())
              && "myfn".equals(asked[0].getLocalPart()),
              "resolver receives prefixed QName {urn:f}myfn");
        check(arity[0] == 1, "resolver receives arity");

        // arity is part of the resolution: distinct arities seen
        xp.evaluate("f:myfn(1,2,3)", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(arity[0] == 3, "resolver receives arity 3 for 3-arg call");

        // resolver returning null -> evaluation fails
        xp.setXPathFunctionResolver((qn, a) -> null);
        boolean saw = false;
        try { xp.evaluate("f:any(1)", d, javax.xml.xpath.XPathConstants.NUMBER); }
        catch (javax.xml.xpath.XPathExpressionException e) { saw = true; }
        check(saw, "resolver returning null -> XPathExpressionException");

        // resolver not consulted for built-in functions
        final boolean[] consulted = {false};
        xp.setXPathFunctionResolver((qn, a) -> { consulted[0] = true; return null; });
        Double builtIn = (Double) xp.evaluate("count(//*)", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(builtIn != null && builtIn.doubleValue() == 1.0 && !consulted[0],
              "built-in count() never consults the resolver");
    }
}

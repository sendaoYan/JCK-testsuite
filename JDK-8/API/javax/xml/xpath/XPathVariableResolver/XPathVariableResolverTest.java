/*
 * @test
 * @summary javax.xml.xpath.XPathVariableResolver conformance: interface hierarchy and methods.
 * @run main XPathVariableResolverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XPathVariableResolverTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathVariableResolver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("resolveVariable"), "has method resolveVariable");
        check(c.getConstructors().length == 0, "no public constructors (interface)");
        testResolution();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static org.w3c.dom.Document emptyDoc() throws Exception {
        return javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
    }

    static void testResolution() throws Exception {
        org.w3c.dom.Document d = emptyDoc();
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();

        // resolver receives the variable QName and its value participates in the expression
        final javax.xml.namespace.QName[] asked = new javax.xml.namespace.QName[1];
        final int[] calls = {0};
        xp.setXPathVariableResolver(new javax.xml.xpath.XPathVariableResolver() {
            public Object resolveVariable(javax.xml.namespace.QName v) {
                calls[0]++;
                asked[0] = v;
                return Integer.valueOf(42);
            }
        });
        Double r = (Double) xp.evaluate("$x+1", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r != null && r.doubleValue() == 43.0, "$x=42 -> $x+1 == 43.0 (closed loop)");
        check(calls[0] == 1, "resolveVariable called exactly once");
        check(asked[0] != null && "x".equals(asked[0].getLocalPart())
              && asked[0].getNamespaceURI().length() == 0,
              "unprefixed variable QName has empty namespace");

        // different value types flow through: Double and String
        xp.setXPathVariableResolver(v -> Double.valueOf(2.5));
        Double r2 = (Double) xp.evaluate("$x*2", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(r2 != null && r2.doubleValue() == 5.0, "Double variable value: $x*2 == 5.0");
        xp.setXPathVariableResolver(v -> "hello");
        String r3 = (String) xp.evaluate("$x", d, javax.xml.xpath.XPathConstants.STRING);
        check("hello".equals(r3), "String variable value readable as STRING");

        // resolver returning null -> XPathExpressionException
        xp.setXPathVariableResolver(v -> null);
        boolean sawNull = false;
        try { xp.evaluate("$x", d, javax.xml.xpath.XPathConstants.NUMBER); }
        catch (javax.xml.xpath.XPathExpressionException e) { sawNull = true; }
        check(sawNull, "resolver returning null -> XPathExpressionException");

        // resolver consulted only for referenced variables
        final int[] calls2 = {0};
        xp.setXPathVariableResolver(v -> { calls2[0]++; return Integer.valueOf(1); });
        xp.evaluate("1+1", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(calls2[0] == 0, "resolver not consulted when expression has no variables");
        xp.evaluate("$a + $b", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(calls2[0] == 2, "resolver consulted once per referenced variable ($a + $b)");
    }
}

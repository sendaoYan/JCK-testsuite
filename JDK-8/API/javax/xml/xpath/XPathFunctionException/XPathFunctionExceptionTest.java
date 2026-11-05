/*
 * @test
 * @summary javax.xml.xpath.XPathFunctionException conformance: exception hierarchy and constructors.
 * @run main XPathFunctionExceptionTest
 */


public class XPathFunctionExceptionTest {
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
        check(javax.xml.xpath.XPathFunctionException.class.getSuperclass().getName().equals("javax.xml.xpath.XPathExpressionException"), "extends javax.xml.xpath.XPathExpressionException");
        testConstructors();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static void testConstructors() {
        // (String)
        javax.xml.xpath.XPathFunctionException e1 = new javax.xml.xpath.XPathFunctionException("fn failed");
        check("fn failed".equals(e1.getMessage()), "(String) message round-trip");
        check(e1.getCause() == null, "(String) cause null");
        // (Throwable)
        RuntimeException cause = new RuntimeException("fn root");
        javax.xml.xpath.XPathFunctionException e2 = new javax.xml.xpath.XPathFunctionException(cause);
        check(e2.getCause() == cause, "(Throwable) cause round-trip");
        check(e2.getMessage() != null && e2.getMessage().contains("fn root"), "(Throwable) message derived");
        // hierarchy: nested under expression failure
        check(e2 instanceof javax.xml.xpath.XPathExpressionException, "is-a XPathExpressionException");
        check(javax.xml.xpath.XPathFunctionException.class.getConstructors().length == 2,
              "exactly two public constructors");
    }

    // real trigger: exception thrown inside an XPathFunction propagates unchanged
    static void testRealTrigger() throws Exception {
        javax.xml.xpath.XPath xp = javax.xml.xpath.XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new javax.xml.namespace.NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
            public java.util.Iterator<String> getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
        });
        xp.setXPathFunctionResolver((qn, arity) -> new javax.xml.xpath.XPathFunction() {
            public Object evaluate(java.util.List args) throws javax.xml.xpath.XPathFunctionException {
                throw new javax.xml.xpath.XPathFunctionException("boom");
            }
        });
        org.w3c.dom.Document d = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root/>".getBytes("UTF-8")));
        boolean saw = false;
        try { xp.evaluate("f:g(1)", d, javax.xml.xpath.XPathConstants.NUMBER); }
        catch (javax.xml.xpath.XPathFunctionException e) { saw = "boom".equals(e.getMessage()); }
        check(saw, "XPathFunctionException thrown inside function propagates unchanged");
    }
}

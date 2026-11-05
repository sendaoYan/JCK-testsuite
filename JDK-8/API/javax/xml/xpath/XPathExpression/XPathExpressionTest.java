/*
 * @test
 * @summary javax.xml.xpath.XPathExpression conformance: interface hierarchy and methods.
 * @run main XPathExpressionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XPathExpressionTest {
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
        Class<?> c = Class.forName("javax.xml.xpath.XPathExpression");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("evaluate"), "has method evaluate");
        testCompiledEvaluation();
        testResolverCapture();
        testErrorContracts();
        testVersionSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static final String XML = "<root><a>1</a><a>2</a><a>3</a><b attr='v'>bt</b></root>";

    interface Thrower { void run() throws Exception; }
    static Throwable ex(Thrower t) {
        try { t.run(); return null; }
        catch (Throwable e) { return e; }
    }

    static org.w3c.dom.Document doc() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream(XML.getBytes("UTF-8")));
    }

    static javax.xml.xpath.XPathFactory factory() {
        return javax.xml.xpath.XPathFactory.newInstance();
    }

    // ---- compiled expression: reuse, item forms, both source forms ----
    static void testCompiledEvaluation() throws Exception {
        org.w3c.dom.Document d = doc();
        javax.xml.xpath.XPathExpression e = factory().newXPath().compile("/root/a[2]");

        // same compiled object evaluated repeatedly gives identical results
        javax.xml.xpath.XPathExpression c = factory().newXPath().compile("/root/a[2]");
        org.w3c.dom.Node n1 = (org.w3c.dom.Node) c.evaluate(d, javax.xml.xpath.XPathConstants.NODE);
        org.w3c.dom.Node n2 = (org.w3c.dom.Node) c.evaluate(d, javax.xml.xpath.XPathConstants.NODE);
        check(n1 != null && n2 != null && "2".equals(n1.getTextContent()) && n1.isSameNode(n2),
              "compiled expression reusable with identical result");

        // all four evaluate shapes
        check("2".equals(c.evaluate((Object) d)), "evaluate(Object) convenience String");
        check("2".equals(c.evaluate(d, javax.xml.xpath.XPathConstants.STRING)), "evaluate(Object, STRING)");
        check("2".equals(c.evaluate(new org.xml.sax.InputSource(new java.io.StringReader(XML)))),
              "evaluate(InputSource) convenience String");
        check("2".equals(c.evaluate(new org.xml.sax.InputSource(new java.io.StringReader(XML)),
                                    javax.xml.xpath.XPathConstants.STRING)),
              "evaluate(InputSource, STRING)");

        // number and nodeset through the compiled expression
        javax.xml.xpath.XPathExpression cnt = factory().newXPath().compile("count(/root/a)");
        check("3".equals(cnt.evaluate((Object) d)), "count evaluates to 3 as String form");
        javax.xml.xpath.XPathExpression all = factory().newXPath().compile("/root/*");
        org.w3c.dom.NodeList nl = (org.w3c.dom.NodeList) all.evaluate(d, javax.xml.xpath.XPathConstants.NODESET);
        check(nl.getLength() == 4, "compiled NODESET /root/* length 4");

        // item = element (context node): relative expression
        javax.xml.xpath.XPathExpression rel = factory().newXPath().compile("count(a)");
        Double relN = (Double) rel.evaluate(d.getDocumentElement(), javax.xml.xpath.XPathConstants.NUMBER);
        check(relN != null && relN.doubleValue() == 3.0, "relative expression with Element item: count(a) == 3.0");

        // expression without document context: constant expression, null item
        javax.xml.xpath.XPathExpression konst = factory().newXPath().compile("1+1");
        check("2".equals(konst.evaluate((Object) null)), "constant expression evaluates with null item");
        check(((Double) konst.evaluate((Object) null, javax.xml.xpath.XPathConstants.NUMBER)).doubleValue() == 2.0,
              "constant expression NUMBER with null item");

        // non-node item: locked actual -> runtime ClassCastException (internal cast to Node)
        check(ex(() -> c.evaluate((Object) new java.lang.Object(), javax.xml.xpath.XPathConstants.STRING))
              instanceof ClassCastException,
              "non-node item -> ClassCastException (locked actual)");
    }

    // ---- resolver state is captured at compile time (locked actual) ----
    static void testResolverCapture() throws Exception {
        org.w3c.dom.Document d = doc();
        javax.xml.xpath.XPath xp = factory().newXPath();
        javax.xml.xpath.XPathExpression before = xp.compile("$x+1");
        // resolver set AFTER compile is used by evaluate() on the XPath itself
        xp.setXPathVariableResolver(v -> Integer.valueOf(41));
        Double viaXpath = (Double) xp.evaluate("$x+1", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(viaXpath != null && viaXpath.doubleValue() == 42.0,
              "evaluate() sees resolver installed after compile");

        // ...but the already-compiled expression does NOT see it (captured at compile)
        Throwable t = ex(() -> before.evaluate(d, javax.xml.xpath.XPathConstants.NUMBER));
        check(t instanceof javax.xml.xpath.XPathExpressionException,
              "compiled expression keeps compile-time resolver state (fails: none at compile)");

        // expression compiled after the resolver is set works
        javax.xml.xpath.XPathExpression after = xp.compile("$x+1");
        Double viaExpr = (Double) after.evaluate(d, javax.xml.xpath.XPathConstants.NUMBER);
        check(viaExpr != null && viaExpr.doubleValue() == 42.0,
              "expression compiled with resolver in place resolves $x");
    }

    // ---- error contracts ----
    static void testErrorContracts() throws Exception {
        javax.xml.xpath.XPath xp = factory().newXPath();
        Throwable t1 = ex(() -> xp.compile("//a["));
        check(t1 instanceof javax.xml.xpath.XPathExpressionException
              && t1.getMessage() != null && t1.getMessage().contains("A location path was expected"),
              "compile('//a[') message explains syntax error");
        Throwable t2 = ex(() -> xp.compile(""));
        check(t2 instanceof javax.xml.xpath.XPathExpressionException
              && t2.getMessage() != null && t2.getMessage().contains("Empty expression"),
              "compile('') -> XPathExpressionException (Empty expression)");
        check(ex(() -> xp.compile(null)) instanceof NullPointerException, "compile(null) -> NPE");

        javax.xml.xpath.XPathExpression e = xp.compile("/root/a");
        check(ex(() -> e.evaluate((Object) null, javax.xml.xpath.XPathConstants.STRING))
              instanceof javax.xml.xpath.XPathExpressionException,
              "path expression with null item -> XPathExpressionException");
        check(ex(() -> e.evaluate((Object) doc(), null)) instanceof NullPointerException,
              "evaluate(item, null QName) -> NPE");
        check(ex(() -> e.evaluate((Object) doc(), new javax.xml.namespace.QName("urn:x", "bogus")))
              instanceof IllegalArgumentException,
              "evaluate with unsupported QName -> IllegalArgumentException");
        check(ex(() -> e.evaluate((org.xml.sax.InputSource) null, javax.xml.xpath.XPathConstants.STRING))
              instanceof NullPointerException,
              "evaluate(null InputSource) -> NPE (locked actual)");
    }

    // ---- JDK-8 must NOT expose the JDK-9+ evaluateExpression family ----
    static void testVersionSurface() throws Exception {
        check(!hasMethod("evaluateExpression"), "JDK-8: XPathExpression has no evaluateExpression");
    }

    static boolean hasMethod(String n) {
        for (java.lang.reflect.Method m : javax.xml.xpath.XPathExpression.class.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
}

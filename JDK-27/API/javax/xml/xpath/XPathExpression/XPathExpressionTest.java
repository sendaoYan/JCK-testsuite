/*
 * @test
 * @summary javax.xml.xpath.XPathExpression conformance: white-box structure + black-box behavior.
 * @run main XPathExpressionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XPathExpressionTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("javax.xml.xpath.XPathExpression");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "evaluate"), "method evaluate");
        check(hasMethod(c, "evaluateExpression"), "method evaluateExpression");

        javax.xml.xpath.XPathExpression e = javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("1+1");
        check("2".equals(e.evaluate((Object) null)), "evaluate");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document d = db.parse(new java.io.ByteArrayInputStream("<root><item id='a'>one</item><item id='b'>two</item></root>".getBytes("UTF-8")));
        javax.xml.xpath.XPathExpression e = javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("/root/item[2]");
        check("two".equals(e.evaluate(d)), "evaluate(document) string");
        check("two".equals(e.evaluate(new org.xml.sax.InputSource(new java.io.StringReader("<root><item id='a'>one</item><item id='b'>two</item></root>")))), "evaluate(InputSource) string");
        javax.xml.xpath.XPathExpression cnt = javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("count(/root/item)");
        check("2".equals(cnt.evaluate(d)), "count evaluate");
        org.w3c.dom.NodeList nl = (org.w3c.dom.NodeList) javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate("/root/item", d, javax.xml.xpath.XPathConstants.NODESET);
        check(nl.getLength() == 2, "nodeset length");
        check("item".equals(nl.item(0).getNodeName()), "nodeset node name");
        boolean ok = false;
        try { javax.xml.xpath.XPathFactory.newInstance().newXPath().compile("//[bad"); }
        catch (javax.xml.xpath.XPathExpressionException ex) { ok = true; }
        check(ok, "invalid expression -> XPathExpressionException");
        javax.xml.xpath.XPathEvaluationResult<?> res = javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluateExpression("/root/item[1]/text()", d);
        check(res.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODESET, "evaluateExpression result type");
        javax.xml.xpath.XPathNodes nodes = (javax.xml.xpath.XPathNodes) res.value();
        check(nodes.size() == 1 && "one".equals(nodes.get(0).getNodeValue()), "evaluateExpression value");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCompiledEvaluation();
        testResolverCapture();
        testErrorContracts();
        testEvaluateExpression();
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
        return dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(XML.getBytes("UTF-8")));
    }

    static javax.xml.xpath.XPathFactory factory() {
        return javax.xml.xpath.XPathFactory.newInstance();
    }

    static void testCompiledEvaluation() throws Exception {
        org.w3c.dom.Document d = doc();
        javax.xml.xpath.XPathExpression c = factory().newXPath().compile("/root/a[2]");
        org.w3c.dom.Node n1 = (org.w3c.dom.Node) c.evaluate(d, javax.xml.xpath.XPathConstants.NODE);
        org.w3c.dom.Node n2 = (org.w3c.dom.Node) c.evaluate(d, javax.xml.xpath.XPathConstants.NODE);
        check(n1 != null && n2 != null && "2".equals(n1.getTextContent()) && n1.isSameNode(n2),
              "compiled expression reusable with identical result");
        check("2".equals(c.evaluate((Object) d)), "evaluate(Object) convenience String");
        check("2".equals(c.evaluate(d, javax.xml.xpath.XPathConstants.STRING)), "evaluate(Object, STRING)");
        check("2".equals(c.evaluate(new org.xml.sax.InputSource(new java.io.StringReader(XML)))),
              "evaluate(InputSource) convenience String");
        check("2".equals(c.evaluate(new org.xml.sax.InputSource(new java.io.StringReader(XML)),
                                    javax.xml.xpath.XPathConstants.STRING)),
              "evaluate(InputSource, STRING)");
        javax.xml.xpath.XPathExpression all = factory().newXPath().compile("/root/*");
        check(((org.w3c.dom.NodeList) all.evaluate(d, javax.xml.xpath.XPathConstants.NODESET)).getLength() == 4,
              "compiled NODESET /root/* length 4");
        javax.xml.xpath.XPathExpression rel = factory().newXPath().compile("count(a)");
        Double relN = (Double) rel.evaluate(d.getDocumentElement(), javax.xml.xpath.XPathConstants.NUMBER);
        check(relN != null && relN.doubleValue() == 3.0, "relative expression with Element item");
        javax.xml.xpath.XPathExpression konst = factory().newXPath().compile("1+1");
        check("2".equals(konst.evaluate((Object) null)), "constant expression evaluates with null item");
        // JDK version difference: JDK-8 lets the raw ClassCastException escape;
        // JDK-27 wraps it into XPathExpressionException (both name the cast failure)
        Throwable nonNode = ex(() -> c.evaluate((Object) new java.lang.Object(), javax.xml.xpath.XPathConstants.STRING));
        check(nonNode instanceof ClassCastException
              || (nonNode instanceof javax.xml.xpath.XPathExpressionException
                  && nonNode.getMessage() != null
                  && nonNode.getMessage().contains("ClassCastException")),
              "non-node item rejected naming cast failure (locked per-version actual)");
    }

    static void testResolverCapture() throws Exception {
        org.w3c.dom.Document d = doc();
        javax.xml.xpath.XPath xp = factory().newXPath();
        javax.xml.xpath.XPathExpression before = xp.compile("$x+1");
        xp.setXPathVariableResolver(v -> Integer.valueOf(41));
        Double viaXpath = (Double) xp.evaluate("$x+1", d, javax.xml.xpath.XPathConstants.NUMBER);
        check(viaXpath != null && viaXpath.doubleValue() == 42.0,
              "evaluate() sees resolver installed after compile");
        check(ex(() -> before.evaluate(d, javax.xml.xpath.XPathConstants.NUMBER))
              instanceof javax.xml.xpath.XPathExpressionException,
              "compiled expression keeps compile-time resolver state");
        javax.xml.xpath.XPathExpression after = xp.compile("$x+1");
        Double viaExpr = (Double) after.evaluate(d, javax.xml.xpath.XPathConstants.NUMBER);
        check(viaExpr != null && viaExpr.doubleValue() == 42.0,
              "expression compiled with resolver in place resolves $x");
    }

    static void testErrorContracts() throws Exception {
        javax.xml.xpath.XPath xp = factory().newXPath();
        Throwable t1 = ex(() -> xp.compile("//a["));
        check(t1 instanceof javax.xml.xpath.XPathExpressionException
              && t1.getMessage() != null && t1.getMessage().contains("A location path was expected"),
              "compile('//a[') message explains syntax error");
        Throwable t2 = ex(() -> xp.compile(""));
        check(t2 instanceof javax.xml.xpath.XPathExpressionException
              && t2.getMessage() != null && t2.getMessage().contains("Empty expression"),
              "compile('') -> XPathExpressionException");
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

    // ---- JDK-9+ evaluateExpression family on a compiled expression ----
    static void testEvaluateExpression() throws Exception {
        org.w3c.dom.Document d = doc();
        javax.xml.xpath.XPathExpression e = factory().newXPath().compile("//a[2]");
        check("2".equals(e.evaluateExpression((Object) d, String.class)),
              "evaluateExpression(Object, String.class)");
        org.w3c.dom.Node n = e.evaluateExpression((Object) d, org.w3c.dom.Node.class);
        check(n != null && "2".equals(n.getTextContent()), "evaluateExpression(Object, Node.class)");
        javax.xml.xpath.XPathEvaluationResult<?> gen = e.evaluateExpression((Object) d);
        check(gen.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODESET,
              "generic evaluateExpression(Object) NODESET classification");
        check(gen.value() instanceof javax.xml.xpath.XPathNodes
              && ((javax.xml.xpath.XPathNodes) gen.value()).size() == 1,
              "generic value is XPathNodes of size 1");
        check("2".equals(e.evaluateExpression(
                  new org.xml.sax.InputSource(new java.io.StringReader(XML)), String.class)),
              "evaluateExpression(InputSource, String.class)");
        javax.xml.xpath.XPathEvaluationResult<?> genIs = e.evaluateExpression(
            new org.xml.sax.InputSource(new java.io.StringReader(XML)));
        check(genIs.type() == javax.xml.xpath.XPathEvaluationResult.XPathResultType.NODESET,
              "generic evaluateExpression(InputSource)");
        // unsupported class
        check(ex(() -> e.evaluateExpression((Object) d, java.util.List.class)) instanceof IllegalArgumentException,
              "evaluateExpression(List.class) -> IllegalArgumentException");
    }
}

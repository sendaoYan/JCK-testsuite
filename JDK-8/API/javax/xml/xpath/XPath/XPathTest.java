/*
 * @test
 * @summary javax.xml.xpath.XPath conformance: compile, evaluate return types (nodeset/string/number/boolean/node), namespace/variable/function resolvers, reset.
 * @run main XPathTest
 */

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Locale;

public class XPathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isXpe(Thrower t) {
        try { t.run(); return false; }
        catch (XPathExpressionException e) { return true; }
        catch (Exception e) { return false; }
    }

    static final String XML = "<root><item id=\"1\">one</item><item id=\"2\">two</item></root>";

    static Document doc() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(XML.getBytes("UTF-8")));
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        XPath xp = XPathFactory.newInstance().newXPath();
        Document d = doc();

        // ---- compile ----
        XPathExpression ok = xp.compile("/root/item");
        check(ok != null, "compile valid expression");
        check(isXpe(() -> xp.compile("///[[")), "compile invalid -> XPathExpressionException");
        check(isNpe(() -> xp.compile(null)), "compile(null) -> NPE");

        // ---- evaluate return types on Document ----
        String s = (String) xp.evaluate("/root/item[1]/text()", d, XPathConstants.STRING);
        check("one".equals(s), "evaluate STRING");
        Double n = (Double) xp.evaluate("count(/root/item)", d, XPathConstants.NUMBER);
        check(n != null && n.doubleValue() == 2.0, "evaluate NUMBER");
        Boolean b = (Boolean) xp.evaluate("count(/root/item) = 2", d, XPathConstants.BOOLEAN);
        check(Boolean.TRUE.equals(b), "evaluate BOOLEAN");
        NodeList nl = (NodeList) xp.evaluate("/root/item", d, XPathConstants.NODESET);
        check(nl.getLength() == 2, "evaluate NODESET");
        check(nl.item(0).getNodeName().equals("item"), "NODESET item type");
        org.w3c.dom.Node node = (org.w3c.dom.Node) xp.evaluate("/root/item[2]", d, XPathConstants.NODE);
        check("item".equals(node.getNodeName()), "evaluate NODE");
        check("two".equals(node.getTextContent()), "NODE textContent");

        // ---- evaluate(String, Object) default -> String ----
        String def = xp.evaluate("/root/item[2]/text()", d);
        check("two".equals(def), "evaluate(String, Object) default String");

        // ---- evaluate(InputSource, ...) ----
        String viaIs = xp.evaluate("/root/item[1]/text()", new InputSource(new StringReader(XML)));
        check("one".equals(viaIs), "evaluate(InputSource, String)");

        // ---- invalid expression over Document ----
        check(isXpe(() -> xp.evaluate("///[[", d, XPathConstants.STRING)), "evaluate invalid -> XPathExpressionException");
        check(isNpe(() -> xp.evaluate(null, (Object) null)), "evaluate(null, null) -> NPE");

        // ---- namespace context ----
        NamespaceContext nc = new NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "p".equals(prefix) ? "urn:p" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:p".equals(uri) ? "p" : null; }
            public Iterator getPrefixes(String uri) { return java.util.Collections.singletonList("p").iterator(); }
        };
        xp.setNamespaceContext(nc);
        check(xp.getNamespaceContext() == nc, "setNamespaceContext/getNamespaceContext round-trip");
        String nsXml = "<root xmlns:p=\"urn:p\"><p:item>val</p:item></root>";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document nsd = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(nsXml.getBytes("UTF-8")));
        check("val".equals(xp.evaluate("/root/p:item/text()", nsd, XPathConstants.STRING)), "evaluate with prefix resolution");

        // ---- variable resolver ----
        XPathVariableResolver vr = new XPathVariableResolver() {
            public Object resolveVariable(QName name) { return "VV"; }
        };
        xp.setXPathVariableResolver(vr);
        check(xp.getXPathVariableResolver() == vr, "setXPathVariableResolver/getXPathVariableResolver round-trip");
        check("VV".equals(xp.evaluate("$x", d, XPathConstants.STRING)), "variable resolver invoked");

        // ---- function resolver (round-trip only) ----
        XPathFunctionResolver fr = new XPathFunctionResolver() {
            public XPathFunction resolveFunction(QName name, int arity) { return null; }
        };
        xp.setXPathFunctionResolver(fr);
        check(xp.getXPathFunctionResolver() == fr, "setXPathFunctionResolver/getXPathFunctionResolver round-trip");

        // ---- reset ----
        xp.reset();
        check("one".equals(xp.evaluate("/root/item[1]/text()", d)), "usable after reset");

        testFiveTypeMatrix();
        testVariableClosedLoop();
        testNamespaceClosedLoop();
        testFunctionClosedLoop();
        testErrorContracts();
        testVersionSurface();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended) =====================

    static final String XML2 = "<root><a>1</a><a>2</a><a>3</a><b attr='v'>bt</b><c><d>deep</d></c></root>";

    static boolean isXpee(Thrower t) {
        try { t.run(); return false; }
        catch (XPathExpressionException e) { return true; }
        catch (Exception e) { return false; }
    }
    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }

    static Document doc2() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(XML2.getBytes("UTF-8")));
    }

    // ---- five return types full matrix, both entry points ----
    static void testFiveTypeMatrix() throws Exception {
        XPath xp = XPathFactory.newInstance().newXPath();
        Document d = doc2();

        // NODE: selected element, text content readable
        org.w3c.dom.Node n = (org.w3c.dom.Node) xp.evaluate("/root/a[2]", d, XPathConstants.NODE);
        check(n != null && "a".equals(n.getNodeName()) && "2".equals(n.getTextContent()),
              "NODE /root/a[2] element with textContent 2");

        // NUMBER: count
        Double cnt = (Double) xp.evaluate("count(/root/a)", d, XPathConstants.NUMBER);
        check(cnt != null && cnt.doubleValue() == 3.0, "NUMBER count(/root/a) == 3.0");

        // NUMBER: sum over numeric element texts
        Double sum = (Double) xp.evaluate("sum(/root/a)", d, XPathConstants.NUMBER);
        check(sum != null && sum.doubleValue() == 6.0, "NUMBER sum(/root/a) == 6.0");

        // STRING: attribute string value
        check("v".equals(xp.evaluate("string(/root/b/@attr)", d, XPathConstants.STRING)),
              "STRING string(/root/b/@attr) == v");

        // BOOLEAN: true and false forms
        check(Boolean.TRUE.equals(xp.evaluate("count(//a) > 2", d, XPathConstants.BOOLEAN)),
              "BOOLEAN count(//a)>2 true");
        check(Boolean.FALSE.equals(xp.evaluate("count(//zz) > 2", d, XPathConstants.BOOLEAN)),
              "BOOLEAN count(//zz)>2 false");

        // NODESET: wildcard + per-item inspection
        NodeList nl = (NodeList) xp.evaluate("/root/*", d, XPathConstants.NODESET);
        check(nl.getLength() == 5, "NODESET /root/* length 5");
        String[] expect = {"a", "a", "a", "b", "c"};
        boolean orderOk = true;
        for (int i = 0; i < 5; i++) orderOk &= expect[i].equals(nl.item(i).getNodeName());
        check(orderOk, "NODESET /root/* document-order names a,a,a,b,c");

        // predicates: text and attribute
        check("2".equals(((org.w3c.dom.Node) xp.evaluate("//a[text()='2']", d, XPathConstants.NODE)).getTextContent()),
              "predicate //a[text()='2']");
        check("v".equals(xp.evaluate("/root/b[@attr='v']/@attr", d, XPathConstants.STRING)),
              "predicate /root/b[@attr='v']/@attr");

        // axes: parent and ancestor
        org.w3c.dom.Node par = (org.w3c.dom.Node) xp.evaluate("/root/c/d/..", d, XPathConstants.NODE);
        check(par != null && "c".equals(par.getNodeName()), "parent axis /root/c/d/..");
        NodeList anc = (NodeList) xp.evaluate("/root/c/d/ancestor::*", d, XPathConstants.NODESET);
        check(anc.getLength() == 2 && "root".equals(anc.item(0).getNodeName()) && "c".equals(anc.item(1).getNodeName()),
              "ancestor axis /root/c/d/ancestor::* == [root, c]");

        // empty-selection forms per type
        check(((NodeList) xp.evaluate("/root/zzz", d, XPathConstants.NODESET)).getLength() == 0,
              "empty NODESET length 0");
        check(xp.evaluate("/root/zzz", d, XPathConstants.NODE) == null, "empty NODE null");
        check("".equals(xp.evaluate("/root/zzz", d, XPathConstants.STRING)), "empty STRING is empty string");
        check(((Double) xp.evaluate("count(/root/zzz)", d, XPathConstants.NUMBER)).doubleValue() == 0.0,
              "empty count is 0.0");

        // two entry points equivalent: (String, Object, QName) vs (String, InputSource, QName)
        Object viaDoc = xp.evaluate("count(/root/a)", d, XPathConstants.NUMBER);
        Object viaIs = xp.evaluate("count(/root/a)",
                new InputSource(new StringReader(XML2)), XPathConstants.NUMBER);
        check(viaIs instanceof Double && viaIs.equals(viaDoc),
              "InputSource entry equals Document entry for NUMBER");

        // item = document element (context node)
        Double elemCtx = (Double) xp.evaluate("count(a)", d.getDocumentElement(), XPathConstants.NUMBER);
        check(elemCtx != null && elemCtx.doubleValue() == 3.0, "item=Document element: count(a) == 3.0");

        // String convenience overload
        check("2".equals(xp.evaluate("/root/a[2]", d)), "evaluate(String, Object) convenience returns String");
    }

    // ---- $x variable resolver closed loop ----
    static void testVariableClosedLoop() throws Exception {
        Document d = doc2();
        XPath xp = XPathFactory.newInstance().newXPath();
        // no resolver: $x fails
        check(isXpee(() -> xp.evaluate("$x+1", d, XPathConstants.NUMBER)),
              "no variable resolver: $x+1 -> XPathExpressionException");

        // resolver returning Integer 42 participates in arithmetic
        final int[] calls = {0};
        xp.setXPathVariableResolver(new XPathVariableResolver() {
            public Object resolveVariable(QName v) {
                calls[0]++;
                return new QName(null, "x").equals(v) ? Integer.valueOf(42) : null;
            }
        });
        Double res = (Double) xp.evaluate("$x+1", d, XPathConstants.NUMBER);
        check(res != null && res.doubleValue() == 43.0, "$x=42 -> $x+1 == 43.0 closed loop");
        check(calls[0] == 1, "resolveVariable invoked exactly once");

        // QName visible to the resolver
        final QName[] seen = new QName[1];
        xp.setXPathVariableResolver(new XPathVariableResolver() {
            public Object resolveVariable(QName v) { seen[0] = v; return Double.valueOf(1); }
        });
        xp.evaluate("$myvar", d, XPathConstants.NUMBER);
        check(seen[0] != null && "myvar".equals(seen[0].getLocalPart()) && seen[0].getNamespaceURI().length() == 0,
              "resolver receives unprefixed variable QName with empty ns");

        // resolver returning null -> XPathExpressionException
        xp.setXPathVariableResolver(new XPathVariableResolver() {
            public Object resolveVariable(QName v) { return null; }
        });
        check(isXpee(() -> xp.evaluate("$x", d, XPathConstants.NUMBER)),
              "resolver returning null -> XPathExpressionException");

        // reset clears the resolver
        xp.reset();
        check(xp.getXPathVariableResolver() == null, "reset clears XPathVariableResolver");
        check(isXpee(() -> xp.evaluate("$x+1", d, XPathConstants.NUMBER)),
              "after reset: $x fails again");

        // factory-level resolver inherited by newXPath()
        XPathFactory xf = XPathFactory.newInstance();
        XPathVariableResolver fvr = new XPathVariableResolver() {
            public Object resolveVariable(QName v) { return Integer.valueOf(7); }
        };
        xf.setXPathVariableResolver(fvr);
        XPath fromFactory = xf.newXPath();
        check(fromFactory.getXPathVariableResolver() == fvr,
              "factory-level resolver inherited by newXPath()");
        Double fin = (Double) fromFactory.evaluate("$x+1", d, XPathConstants.NUMBER);
        check(fin != null && fin.doubleValue() == 8.0, "factory resolver active in evaluation ($x+1 == 8.0)");
    }

    // ---- NamespaceContext closed loop ----
    static void testNamespaceClosedLoop() throws Exception {
        String nsXml = "<r:root xmlns:r='urn:p'><r:a>pv</r:a></r:root>";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document nsd = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(nsXml.getBytes("UTF-8")));

        XPath xp = XPathFactory.newInstance().newXPath();
        // without context the prefix cannot be resolved: no match (empty), not an exception (JDK-8 actual)
        check("".equals(xp.evaluate("/r:root/r:a", nsd, XPathConstants.STRING)),
              "no NamespaceContext: prefixed expression yields empty string (locked actual)");
        check(xp.evaluate("/r:root/r:a", nsd, XPathConstants.NODE) == null,
              "no NamespaceContext: NODE form yields null");
        check(((NodeList) xp.evaluate("/r:root/r:a", nsd, XPathConstants.NODESET)).getLength() == 0,
              "no NamespaceContext: NODESET form empty");

        // with context the prefixed expression resolves
        NamespaceContext nc = new NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "p".equals(prefix) ? "urn:p" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:p".equals(uri) ? "p" : null; }
            public Iterator getPrefixes(String uri) { return java.util.Collections.singletonList("p").iterator(); }
        };
        xp.setNamespaceContext(nc);
        check(xp.getNamespaceContext() == nc, "setNamespaceContext/getNamespaceContext round-trip");
        check("pv".equals(xp.evaluate("/p:root/p:a", nsd, XPathConstants.STRING)),
              "NamespaceContext p->urn:p closed loop");
        // with a context installed, a prefix the context cannot resolve fails fast
        // (locked actual: internal impl lets a raw TransformerException subclass escape
        //  with the message "Prefix must resolve to a namespace")
        try {
            String rr = (String) xp.evaluate("/r:root/r:a", nsd, XPathConstants.STRING);
            check("".equals(rr), "context maps p only: r-prefixed expression yields empty string");
        } catch (Exception e) {
            check(e.getMessage() != null && e.getMessage().contains("Prefix must resolve to a namespace"),
                  "context maps p only: r-prefixed expression fails fast (locked actual)");
        }
    }

    // ---- XPathFunctionResolver closed loop ----
    static void testFunctionClosedLoop() throws Exception {
        Document d = doc2();
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
            public Iterator getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
        });
        final QName[] asked = new QName[1];
        final int[] arity = new int[1];
        xp.setXPathFunctionResolver(new XPathFunctionResolver() {
            public XPathFunction resolveFunction(QName f, int a) {
                asked[0] = f; arity[0] = a;
                return new XPathFunction() {
                    public Object evaluate(java.util.List args) {
                        double s = 0;
                        for (Object o : args) s += ((Number) o).doubleValue();
                        return Double.valueOf(s);
                    }
                };
            }
        });
        Double r2 = (Double) xp.evaluate("f:sum2(1,2)", d, XPathConstants.NUMBER);
        check(r2 != null && r2.doubleValue() == 3.0, "f:sum2(1,2) == 3.0 closed loop");
        check(asked[0] != null && "urn:f".equals(asked[0].getNamespaceURI()) && "sum2".equals(asked[0].getLocalPart()),
              "resolver receives QName {urn:f}sum2");
        check(arity[0] == 2, "resolver receives arity 2");
        Double r3 = (Double) xp.evaluate("f:sum2(1,2,3)", d, XPathConstants.NUMBER);
        check(r3 != null && r3.doubleValue() == 6.0, "f:sum2(1,2,3) == 6.0 arity-sensitive dispatch");

        // function throwing XPathFunctionException propagates as-is
        xp.setXPathFunctionResolver(new XPathFunctionResolver() {
            public XPathFunction resolveFunction(QName f, int a) {
                return new XPathFunction() {
                    public Object evaluate(java.util.List args) throws XPathFunctionException {
                        throw new XPathFunctionException("boom");
                    }
                };
            }
        });
        boolean saw = false;
        try { xp.evaluate("f:sum2(1)", d, XPathConstants.NUMBER); }
        catch (XPathFunctionException e) { saw = "boom".equals(e.getMessage()); }
        check(saw, "XPathFunctionException thrown inside function propagates unchanged");

        // resolver returning null -> XPathExpressionException
        xp.setXPathFunctionResolver(new XPathFunctionResolver() {
            public XPathFunction resolveFunction(QName f, int a) { return null; }
        });
        check(isXpee(() -> xp.evaluate("f:sum2(1)", d, XPathConstants.NUMBER)),
              "function resolver returning null -> XPathExpressionException");

        // resolver unset -> XPathExpressionException
        XPath bare = XPathFactory.newInstance().newXPath();
        bare.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) { return "f".equals(prefix) ? "urn:f" : javax.xml.XMLConstants.NULL_NS_URI; }
            public String getPrefix(String uri) { return "urn:f".equals(uri) ? "f" : null; }
            public Iterator getPrefixes(String uri) { return java.util.Collections.singletonList("f").iterator(); }
        });
        check(isXpee(() -> bare.evaluate("f:g(1)", d, XPathConstants.NUMBER)),
              "no function resolver: extension call -> XPathExpressionException");

        // factory-level function resolver inherited by newXPath()
        XPathFactory xf = XPathFactory.newInstance();
        XPathFunctionResolver inherited = new XPathFunctionResolver() {
            public XPathFunction resolveFunction(QName f, int a) { return null; }
        };
        xf.setXPathFunctionResolver(inherited);
        check(xf.newXPath().getXPathFunctionResolver() == inherited,
              "factory-level function resolver inherited by newXPath()");
    }

    // ---- expression error contracts ----
    static void testErrorContracts() throws Exception {
        Document d = doc2();
        XPath xp = XPathFactory.newInstance().newXPath();

        // invalid expression: message explains the syntax problem
        boolean bad = false;
        try { xp.compile("//a["); }
        catch (XPathExpressionException e) {
            bad = e.getMessage() != null && e.getMessage().contains("A location path was expected");
        }
        check(bad, "compile('//a[') XPathExpressionException message explains syntax error");

        // empty expression
        boolean empty = false;
        try { xp.evaluate("", d, XPathConstants.STRING); }
        catch (XPathExpressionException e) { empty = e.getMessage() != null && e.getMessage().contains("Empty expression"); }
        check(empty, "empty expression -> XPathExpressionException (Empty expression)");

        boolean compEmpty = false;
        try { xp.compile(""); }
        catch (XPathExpressionException e) { compEmpty = e.getMessage() != null && e.getMessage().contains("Empty expression"); }
        check(compEmpty, "compile('') -> XPathExpressionException");

        // null expression -> NPE
        check(isNpe(() -> xp.compile(null)), "compile(null) -> NPE");
        check(isNpe(() -> xp.evaluate(null, (Object) null)), "evaluate(null, item) -> NPE");

        // null returnType -> NPE; unsupported QName -> IAE
        check(isNpe(() -> xp.evaluate("/root/a", d, null)), "evaluate(expr, item, null QName) -> NPE");
        check(isIae(() -> xp.evaluate("/root/a", d, new QName("urn:x", "bogus"))),
              "evaluate with unsupported QName -> IllegalArgumentException");

        // null item -> XPathExpressionException
        check(isXpee(() -> xp.evaluate("/root/a", (Object) null, XPathConstants.STRING)),
              "evaluate(expr, null item, QName) -> XPathExpressionException");
    }

    // ---- version surface: JDK-8 must NOT expose the JDK-9+ evaluateExpression family ----
    static void testVersionSurface() throws Exception {
        check(!hasMethod(javax.xml.xpath.XPath.class, "evaluateExpression"),
              "JDK-8: XPath has no evaluateExpression");
        check(!hasMethod(javax.xml.xpath.XPathExpression.class, "evaluateExpression"),
              "JDK-8: XPathExpression has no evaluateExpression");
        try {
            Class.forName("javax.xml.xpath.XPathEvaluationResult");
            check(false, "JDK-8: XPathEvaluationResult should not exist");
        } catch (ClassNotFoundException e) {
            check(true, "JDK-8: XPathEvaluationResult absent");
        }
        try {
            Class.forName("javax.xml.xpath.XPathNodes");
            check(false, "JDK-8: XPathNodes should not exist");
        } catch (ClassNotFoundException e) {
            check(true, "JDK-8: XPathNodes absent");
        }
    }

    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
}

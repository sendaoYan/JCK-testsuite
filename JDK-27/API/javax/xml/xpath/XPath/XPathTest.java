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

        System.out.println("PASS (" + passed + " assertions)");
    }
}

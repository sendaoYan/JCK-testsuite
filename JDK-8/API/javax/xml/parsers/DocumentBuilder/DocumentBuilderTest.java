/*
 * @test
 * @summary javax.xml.parsers.DocumentBuilder conformance: parse overloads, null semantics, malformed XML exception, reset, DOM tree production.
 * @run main DocumentBuilderTest
 */

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;

public class DocumentBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean isIae(Thrower t) {
        try { t.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }

    static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<catalog xmlns:p=\"urn:parts\"><book id=\"b1\" lang=\"en\">" +
        "<title>T1</title><p:price currency=\"USD\">9.99</p:price>" +
        "</book><book id=\"b2\"><title>T2</title></book></catalog>";

    static InputStream xmlStream() throws Exception {
        return new ByteArrayInputStream(XML.getBytes("UTF-8"));
    }

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder db = f.newDocumentBuilder();

        // ---- documented defaults ----
        check(!db.isValidating(), "isValidating default false");
        check(db.isNamespaceAware(), "namespace-aware builder from namespace-aware factory");
        check(!db.isXIncludeAware(), "isXIncludeAware default false");
        check(db.getSchema() == null, "schema default null");

        // ---- parse(InputStream) ----
        Document d = db.parse(xmlStream());
        check("catalog".equals(d.getDocumentElement().getNodeName()), "root element name");
        NodeList books = d.getElementsByTagName("book");
        check(books.getLength() == 2, "two books");
        Element b1 = (Element) books.item(0);
        check("b1".equals(b1.getAttribute("id")), "attribute id");
        check("en".equals(b1.getAttribute("lang")), "attribute lang");
        check("T1".equals(b1.getElementsByTagName("title").item(0).getTextContent()), "nested title");
        // namespace element reached via getElementsByTagNameNS
        NodeList prices = d.getElementsByTagNameNS("urn:parts", "price");
        check(prices.getLength() == 1, "namespace-aware price element");
        check("9.99".equals(prices.item(0).getTextContent()), "price text");
        check("USD".equals(((Element) prices.item(0)).getAttribute("currency")), "price currency");

        // ---- parse(InputStream, systemId) ----
        Document d2 = db.parse(xmlStream(), "http://example.invalid/doc.xml");
        check("catalog".equals(d2.getDocumentElement().getNodeName()), "parse(InputStream, systemId)");

        // ---- parse(InputSource) ----
        Document d3 = db.parse(new InputSource(xmlStream()));
        check("catalog".equals(d3.getDocumentElement().getNodeName()), "parse(InputSource)");

        // ---- parse(String uri) with file: URI works against a temp file (deterministic, self-cleaned) ----
        java.io.File tmp = java.io.File.createTempFile("dbtest", ".xml");
        tmp.deleteOnExit();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
        fos.write(XML.getBytes("UTF-8"));
        fos.close();
        Document d4 = db.parse(tmp.toURI().toString());
        check("catalog".equals(d4.getDocumentElement().getNodeName()), "parse(String uri) file: URI");
        Document d5 = db.parse(tmp);
        check("catalog".equals(d5.getDocumentElement().getNodeName()), "parse(File)");
        tmp.delete();

        // ---- null semantics ----
        check(isIae(() -> db.parse((InputStream) null)), "parse((InputStream)null) -> IAE");
        check(isIae(() -> db.parse((java.io.File) null)), "parse((File)null) -> IAE");
        check(isIae(() -> db.parse((String) null)), "parse((String)null) -> IAE");
        check(isIae(() -> db.parse((InputSource) null)), "parse((InputSource)null) -> IAE");

        // ---- malformed XML -> SAXParseException with position ----
        boolean ok = false;
        try {
            db.parse(new ByteArrayInputStream("<a><b></a>".getBytes("UTF-8")));
        } catch (SAXParseException e) {
            ok = e.getLineNumber() > 0 && e.getColumnNumber() > 0 && e.getSystemId() == null;
        }
        check(ok, "malformed XML -> SAXParseException with line/column");

        // ---- empty document, usable ----
        Document empty = db.newDocument();
        check(empty != null, "newDocument non-null");
        check(empty.getDocumentElement() == null, "newDocument has no document element");
        check(empty.getChildNodes().getLength() == 0, "newDocument has no children");
        check(empty.createElement("e") != null, "newDocument createElement usable");
        check(db.getDOMImplementation() != null, "getDOMImplementation non-null");
        check(db.getDOMImplementation().hasFeature("XML", "3.0"), "DOMImplementation hasFeature XML 3.0");

        // ---- reset keeps parser reusable ----
        db.setEntityResolver(null);
        db.setErrorHandler(null);
        db.reset();
        Document d6 = db.parse(xmlStream());
        check("catalog".equals(d6.getDocumentElement().getNodeName()), "parser reusable after reset");

        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== contract deepening (appended, do not remove above) =====================

    // Rich fixed document: comments + PI + CDATA + entities + ns + nesting.
    static final String DEEP_XML =
        "<?xml version=\"1.0\"?>"
        + "<!--top comment-->"
        + "<?top-pi topdata?>"
        + "<root xmlns:p=\"urn:p\" id=\"r1\">"
        + "text&amp;more&#65;"
        + "<!--inner comment-->"
        + "<?inner-pi pidata?>"
        + "<![CDATA[cdata<section>]]>"
        + "<child a=\"1\" p:b=\"2\"/>"
        + "<p:nschild>x</p:nschild>"
        + "<deep><child>nested-child</child></deep>"
        + "</root>";

    static InputSource deepSrc() {
        return new InputSource(new StringReader(DEEP_XML));
    }

    static void testDeep() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        DocumentBuilder db = f.newDocumentBuilder();

        // ---- node type constants: precise values ----
        check(Node.ELEMENT_NODE == 1, "ELEMENT_NODE=1");
        check(Node.ATTRIBUTE_NODE == 2, "ATTRIBUTE_NODE=2");
        check(Node.TEXT_NODE == 3, "TEXT_NODE=3");
        check(Node.CDATA_SECTION_NODE == 4, "CDATA_SECTION_NODE=4");
        check(Node.ENTITY_REFERENCE_NODE == 5, "ENTITY_REFERENCE_NODE=5");
        check(Node.PROCESSING_INSTRUCTION_NODE == 7, "PROCESSING_INSTRUCTION_NODE=7");
        check(Node.COMMENT_NODE == 8, "COMMENT_NODE=8");
        check(Node.DOCUMENT_NODE == 9, "DOCUMENT_NODE=9");
        check(Node.DOCUMENT_TYPE_NODE == 10, "DOCUMENT_TYPE_NODE=10");
        check(Node.DOCUMENT_FRAGMENT_NODE == 11, "DOCUMENT_FRAGMENT_NODE=11");
        check(DOMException.WRONG_DOCUMENT_ERR == 4, "DOMException.WRONG_DOCUMENT_ERR=4");
        check(DOMException.HIERARCHY_REQUEST_ERR == 3, "HIERARCHY_REQUEST_ERR=3");
        check(DOMException.NOT_FOUND_ERR == 8, "NOT_FOUND_ERR=8");

        // ---- parse tree: document level children (comment, PI, root) ----
        Document d = db.parse(deepSrc());
        check(d.getNodeType() == Node.DOCUMENT_NODE, "document nodeType=9");
        check(d.getNodeName().equals("#document"), "document nodeName=#document");
        check(d.getChildNodes().getLength() == 3, "document has 3 children (comment, pi, root)");
        Node c0 = d.getChildNodes().item(0);
        check(c0.getNodeType() == Node.COMMENT_NODE, "first doc child is comment");
        check(c0.getNodeValue().equals("top comment"), "comment value");
        Node c1 = d.getChildNodes().item(1);
        check(c1.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE, "second doc child is PI");
        check(c1.getNodeName().equals("top-pi"), "PI target");
        check(c1.getNodeValue().equals("topdata"), "PI data");
        check(d.getFirstChild() == c0 && d.getLastChild() == d.getDocumentElement(), "first/last child chain");

        // ---- root element: name / namespace / attributes ----
        Element root = d.getDocumentElement();
        check(root.getNodeName().equals("root"), "root nodeName");
        check(root.getLocalName().equals("root"), "root localName (ns-aware)");
        check(root.getNamespaceURI() == null, "root has no namespace");
        check(root.getPrefix() == null, "root prefix null");
        check(root.getNodeValue() == null, "element nodeValue null");
        check(root.getParentNode() == d, "root parent is document");

        // ---- root children sequence: text(comment..PI..CDATA) then elements ----
        NodeList rc = root.getChildNodes();
        check(rc.getLength() == 7, "root has 7 children");
        int[] expect = {Node.TEXT_NODE, Node.COMMENT_NODE, Node.PROCESSING_INSTRUCTION_NODE,
                        Node.CDATA_SECTION_NODE, Node.ELEMENT_NODE, Node.ELEMENT_NODE, Node.ELEMENT_NODE};
        for (int i = 0; i < expect.length; i++)
            check(rc.item(i).getNodeType() == expect[i], "root child " + i + " type " + expect[i]);
        check(rc.item(0).getNodeValue().equals("text&moreA"), "entity-decoded text (&amp; &#65;)");
        check(rc.item(1).getNodeValue().equals("inner comment"), "inner comment value");
        ProcessingInstruction innerPi = (ProcessingInstruction) rc.item(2);
        check(innerPi.getTarget().equals("inner-pi"), "inner PI target");
        check(innerPi.getData().equals("pidata"), "inner PI data");
        CDATASection cd = (CDATASection) rc.item(3);
        check(cd.getData().equals("cdata<section>"), "CDATA raw content not parsed as markup");

        // ---- getElementsByTagName recursive vs getElementsByTagNameNS matrix ----
        check(d.getElementsByTagName("child").getLength() == 2, "getElementsByTagName recursive");
        check(root.getElementsByTagName("child").getLength() == 2, "search from root is also recursive");
        check(root.getElementsByTagName("deep").item(0).getChildNodes().getLength() == 1, "deep has one child");
        NodeList all = d.getElementsByTagNameNS("*", "child");
        check(all.getLength() == 2, "getElementsByTagNameNS('*') matches all namespaces");
        check(d.getElementsByTagNameNS(null, "child").getLength() == 2, "null nsuri matches no-namespace elements");
        check(d.getElementsByTagNameNS("", "child").getLength() == 2, "empty nsuri behaves like null");
        check(d.getElementsByTagNameNS("urn:p", "nschild").getLength() == 1, "exact ns match");
        check(d.getElementsByTagNameNS("urn:p", "child").getLength() == 0, "ns mismatch excludes");
        check(d.getElementsByTagNameNS("*", "*").getLength() >= 4, "'*','*' wildcard matches every element");
        Element nschild = (Element) d.getElementsByTagNameNS("urn:p", "nschild").item(0);
        check(nschild.getLocalName().equals("nschild"), "nschild localName");
        check(nschild.getPrefix().equals("p"), "nschild prefix");
        check(nschild.getNamespaceURI().equals("urn:p"), "nschild namespaceURI");
        check(nschild.getNodeName().equals("p:nschild"), "nschild nodeName keeps prefix form");

        // ---- attributes: getAttribute vs getAttributeNode ----
        Element childEl = (Element) d.getElementsByTagName("child").item(0);
        check(childEl.getAttribute("a").equals("1"), "getAttribute(a)");
        check(childEl.getAttribute("p:b").equals("2"), "getAttribute(qname)");
        check(childEl.getAttribute("nope").equals(""), "missing attribute returns empty string");
        Attr a1 = childEl.getAttributeNode("a");
        check(a1 != null && a1.getValue().equals("1"), "getAttributeNode value");
        check(a1.getNodeType() == Node.ATTRIBUTE_NODE, "attr node type 2");
        check(a1.getName().equals("a"), "attr getName");
        check(a1.getSpecified(), "attr specified");
        check(a1.getOwnerElement() == childEl, "attr ownerElement");
        Attr b1 = childEl.getAttributeNodeNS("urn:p", "b");
        check(b1 != null && b1.getValue().equals("2"), "getAttributeNodeNS by ns+local");
        check(b1.getLocalName().equals("b"), "attr localName (ns-aware)");
        check(root.getAttribute("id").equals("r1"), "root attribute");

        // ---- text content ----
        check(childEl.getTextContent().equals(""), "empty element textContent");
        check(nschild.getTextContent().equals("x"), "nschild text");
        check(root.getTextContent().equals("text&moreAcdata<section>xnested-child"), "root getTextContent full concatenation");
        check(d.getElementsByTagName("deep").item(0).getTextContent().equals("nested-child"), "nested textContent");

        // ---- createXxx family + appendChild same-node identity ----
        Document fresh = db.newDocument();
        Element fe = fresh.createElement("e");
        check(fe.getOwnerDocument() == fresh, "createElement ownerDocument");
        check(fresh.appendChild(fe) == fe, "appendChild returns the same node");
        check(fresh.getDocumentElement() == fe, "appended element becomes document element");
        Text ft = fresh.createTextNode("t");
        check(ft.getData().equals("t") && ft.getNodeValue().equals("t"), "createTextNode value");
        check(fe.appendChild(ft) == ft, "append text node");
        Comment fcm = fresh.createComment("c");
        check(fcm.getData().equals("c"), "createComment");
        ProcessingInstruction fpi = fresh.createProcessingInstruction("t", "d");
        check(fpi.getTarget().equals("t") && fpi.getData().equals("d"), "createProcessingInstruction");
        CDATASection fc = fresh.createCDATASection("cd");
        check(fc.getNodeType() == Node.CDATA_SECTION_NODE, "createCDATASection type");
        Attr fa = fresh.createAttribute("k");
        check(fa.getName().equals("k"), "createAttribute");
        check(fa.getValue().equals(""), "createAttribute default value empty");
        check(fe.setAttributeNode(fa) == null, "setAttributeNode returns old attr (none)");
        check(fe.getAttributeNode("k") == fa, "setAttributeNode installs the node");
        fa.setValue("v");
        check(fe.getAttribute("k").equals("v"), "attr node value change reflected in element");
        Element fns = fresh.createElementNS("urn:x", "q:e");
        check(fns.getNamespaceURI().equals("urn:x") && fns.getLocalName().equals("e") && fns.getPrefix().equals("q"), "createElementNS triple");

        // ---- appendChild move semantics ----
        Element m1 = fresh.createElement("m1");
        Element m2 = fresh.createElement("m2");
        Element holder = fresh.createElement("holder");
        fresh.getDocumentElement().appendChild(holder);
        holder.appendChild(m1);
        holder.appendChild(m2);
        check(holder.getChildNodes().getLength() == 2, "holder has 2");
        Element root2 = fresh.getDocumentElement();
        int rootBefore = root2.getChildNodes().getLength();
        root2.appendChild(m1);            // move m1 out of holder
        check(holder.getChildNodes().getLength() == 1, "appendChild moved node: old parent loses it");
        check(root2.getChildNodes().getLength() == rootBefore + 1, "new parent gains node");
        check(m1.getParentNode() == root2, "parentNode switched");
        int rootNow = root2.getChildNodes().getLength();
        root2.appendChild(root2.getLastChild()); // re-append own last child: no duplication
        check(root2.getChildNodes().getLength() == rootNow, "re-append same parent's own child keeps count");
        Node removed = holder.removeChild(holder.getFirstChild());
        check(removed == m2, "removeChild returns removed node");
        check(m2.getParentNode() == null, "removed node detached");
        check(root2.insertBefore(m2, holder).equals(m2), "insertBefore returns node");
        check(m2.getNextSibling() == holder && holder.getPreviousSibling() == m2, "sibling links after insertBefore");

        // ---- cross-document appendChild -> DOMException WRONG_DOCUMENT_ERR ----
        Document other = db.newDocument();
        Element foreign = other.createElement("foreign");
        boolean wrongDoc = false;
        try { root2.appendChild(foreign); }
        catch (DOMException e) {
            wrongDoc = e.code == DOMException.WRONG_DOCUMENT_ERR
                    && e.getMessage() != null && e.getMessage().contains("WRONG_DOCUMENT_ERR");
        }
        check(wrongDoc, "cross-doc appendChild -> DOMException WRONG_DOCUMENT_ERR (code+message)");

        // ---- importNode matrix ----
        Node impDeep = other.importNode(d.getElementsByTagName("deep").item(0), true);
        check(impDeep.getOwnerDocument() == other, "importNode re-owns to target doc");
        check(impDeep.getChildNodes().getLength() == 1, "importNode deep copies subtree");
        check(impDeep.getChildNodes().item(0).getTextContent().equals("nested-child"), "importNode deep text preserved");
        Node impShallow = other.importNode(d.getElementsByTagName("deep").item(0), false);
        check(impShallow.getChildNodes().getLength() == 0, "importNode shallow drops children");
        Element otherRoot = other.createElement("wrap");
        other.appendChild(otherRoot);
        check(other.getDocumentElement() == otherRoot, "imported-into document gains root");
        otherRoot.appendChild(impDeep);
        check(other.getElementsByTagName("child").getLength() == 1, "imported node appendable cross-doc");

        // ---- cloneNode matrix ----
        Element orig = fresh.createElement("o");
        orig.appendChild(fresh.createElement("k"));
        Element deepClone = (Element) orig.cloneNode(true);
        check(deepClone.getChildNodes().getLength() == 1, "cloneNode(true) copies subtree");
        check(deepClone != orig && deepClone.getNodeName().equals("o"), "clone is distinct same-name node");
        deepClone.removeChild(deepClone.getFirstChild());
        check(orig.getChildNodes().getLength() == 1, "mutating clone leaves original intact");
        Element shallowClone = (Element) orig.cloneNode(false);
        check(shallowClone.getChildNodes().getLength() == 0, "cloneNode(false) has no children");

        // ---- normalize / splitText round-trip ----
        Element nEl = fresh.createElement("n");
        Text t1 = fresh.createTextNode("ab");
        nEl.appendChild(t1);
        Text t2 = t1.splitText(1);
        check(t1.getData().equals("a") && t2.getData().equals("b"), "splitText splits at offset");
        check(nEl.getChildNodes().getLength() == 2, "splitText yields two adjacent text nodes");
        check(t2.getPreviousSibling() == t1, "split parts are siblings");
        nEl.normalize();
        check(nEl.getChildNodes().getLength() == 1, "normalize merges adjacent text nodes");
        check(nEl.getFirstChild().getNodeValue().equals("ab"), "normalized text equals original");

        // ---- setTextContent replaces whole content ----
        Element sEl = fresh.createElement("s");
        sEl.appendChild(fresh.createElement("a"));
        sEl.appendChild(fresh.createTextNode("z"));
        sEl.setTextContent("replaced");
        check(sEl.getChildNodes().getLength() == 1, "setTextContent leaves single text node");
        check(sEl.getFirstChild().getNodeType() == Node.TEXT_NODE, "setTextContent child is TEXT");
        check(sEl.getTextContent().equals("replaced"), "setTextContent value");

        // ---- ErrorHandler matrix: rethrowing fatalError propagates identical object ----
        final SAXParseException[] seen = new SAXParseException[1];
        DocumentBuilder dbE = f.newDocumentBuilder();
        dbE.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) { }
            public void error(SAXParseException e) { }
            public void fatalError(SAXParseException e) throws SAXException {
                seen[0] = e;
                throw e;
            }
        });
        SAXParseException caught = null;
        try { dbE.parse(new InputSource(new StringReader("<a><b></a>"))); }
        catch (SAXParseException e) { caught = e; }
        check(caught != null && caught == seen[0], "fatalError rethrow propagates identical SAXParseException");
        check(caught.getLineNumber() == 1 && caught.getColumnNumber() == 9, "fatalError position 1:9");

        // ---- error() recovery: validating + DTD violation is non-fatal, parse completes ----
        final StringBuilder recover = new StringBuilder();
        DocumentBuilderFactory vf = DocumentBuilderFactory.newInstance();
        vf.setValidating(true);
        DocumentBuilder dbV = vf.newDocumentBuilder();
        dbV.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) { recover.append("w;"); }
            public void error(SAXParseException e) {
                recover.append("e:").append(e.getLineNumber()).append(',').append(e.getColumnNumber())
                       .append(':').append(e.getMessage()).append(';');
            }
            public void fatalError(SAXParseException e) throws SAXException { recover.append("f;"); throw e; }
        });
        Document rec = dbV.parse(new InputSource(new StringReader(
            "<!DOCTYPE r [<!ELEMENT r (a,b)><!ELEMENT a (#PCDATA)><!ELEMENT b (#PCDATA)>]><r><a>1</a></r>")));
        check(rec != null && rec.getDocumentElement().getNodeName().equals("r"), "recovered document still returned");
        check(recover.toString().startsWith("e:1,93:"), "error() callback at 1:93");
        check(recover.toString().contains("must match \"(a,b)\""), "error() message reports content model");

        // ---- EntityResolver adoption: replacement InputSource is used ----
        DocumentBuilder dbR = f.newDocumentBuilder();
        dbR.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) {
                return new InputSource(new StringReader("<!ENTITY e 'RESOLVED-TEXT'>"));
            }
        });
        Document res = dbR.parse(new InputSource(new StringReader(
            "<!DOCTYPE r SYSTEM \"http://example.invalid/ext.dtd\"><r>&e;</r>")));
        check(res.getDocumentElement().getTextContent().equals("RESOLVED-TEXT"), "entity resolver replacement adopted");
        check(dbR.getDOMImplementation() != null, "getDOMImplementation after resolver use");

        // ---- parse("") -> SAXParseException (not IOException) ----
        boolean emptyFail = false;
        try { db.parse(""); }
        catch (SAXParseException e) { emptyFail = e.getMessage() != null; }
        check(emptyFail, "parse(\"\") -> SAXParseException");

        // ---- reset twice idempotent ----
        db.reset(); db.reset();
        check(db.parse(deepSrc()).getDocumentElement().getNodeName().equals("root"), "reusable after double reset");
    }
}

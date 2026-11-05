/*
 * @test
 * @summary org.w3c.dom.Document conformance: create* factory null/char semantics, tree construction, namespace, import/adopt, document-level queries.
 * @run main DocumentTest
 */

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DocumentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Thrower { void run() throws Exception; }

    static boolean domCode(Thrower t, short code) {
        try { t.run(); return false; }
        catch (DOMException e) { return e.code == code; }
        catch (Exception e) { return false; }
    }
    static boolean isNpe(Thrower t) {
        try { t.run(); return false; }
        catch (NullPointerException e) { return true; }
        catch (Exception e) { return false; }
    }

    static Document newDoc() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().newDocument();
    }

    static Document parsedDoc() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>";
        return f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    public static void main(String[] args) throws Exception {
        Document d = newDoc();

        // ---- createElement precise exceptions ----
        check(domCode(() -> d.createElement(null), DOMException.INVALID_CHARACTER_ERR), "createElement(null) -> INVALID_CHARACTER_ERR");
        check(domCode(() -> d.createElement(""), DOMException.INVALID_CHARACTER_ERR), "createElement(\"\") -> INVALID_CHARACTER_ERR");
        check(domCode(() -> d.createElement("1abc"), DOMException.INVALID_CHARACTER_ERR), "createElement(name starting digit) -> INVALID_CHARACTER_ERR");
        check(domCode(() -> d.createElement("a b"), DOMException.INVALID_CHARACTER_ERR), "createElement(space) -> INVALID_CHARACTER_ERR");
        Element root = d.createElement("root");
        check(root != null && "root".equals(root.getTagName()), "createElement returns named element");
        check(root.getOwnerDocument() == d, "created element owned by its creating document");

        // ---- namespace factory ----
        check(domCode(() -> d.createElementNS(null, "p:x"), DOMException.NAMESPACE_ERR), "createElementNS(null uri, prefixed name) -> NAMESPACE_ERR");
        Element ns = d.createElementNS("urn:test", "t:item");
        check("urn:test".equals(ns.getNamespaceURI()), "createElementNS namespace uri");
        check("item".equals(ns.getLocalName()), "createElementNS local name");
        check("t:item".equals(ns.getNodeName()), "createElementNS qualified node name");
        Element noNs = d.createElementNS("", "plain");
        check(noNs.getNamespaceURI() == null, "createElementNS empty uri -> no namespace");
        check(domCode(() -> d.createAttribute(null), DOMException.INVALID_CHARACTER_ERR), "createAttribute(null) -> INVALID_CHARACTER_ERR");
        check(d.createAttributeNS("urn:test", "t:a") != null, "createAttributeNS");

        // ---- other node factories ----
        check("text".equals(d.createTextNode("text").getData()), "createTextNode");
        check("c".equals(d.createComment("c").getData()), "createComment");
        check(d.createCDATASection("cdata") != null, "createCDATASection");
        check(d.createDocumentFragment() != null, "createDocumentFragment");
        check("target".equals(d.createProcessingInstruction("target", "data").getTarget()), "createProcessingInstruction target");
        check(d.createEntityReference("ref") != null, "createEntityReference");
        check(domCode(() -> d.createProcessingInstruction(null, "d"), DOMException.INVALID_CHARACTER_ERR), "createProcessingInstruction(null target) -> INVALID_CHARACTER_ERR");

        // ---- append to build tree ----
        d.appendChild(root);
        check(d.getDocumentElement() == root, "getDocumentElement returns appended root");
        check(root.getOwnerDocument() == d, "appendChild sets ownerDocument");
        Element child = d.createElement("child");
        root.appendChild(child);
        check(root.getFirstChild() == child && root.getLastChild() == child, "first/last child");
        check(root.getChildNodes().getLength() == 1, "childNodes count");
        check(root.hasChildNodes(), "hasChildNodes true");
        check(!d.hasChildNodes() || d.getChildNodes().getLength() == 1, "document has root as single child");

        // ---- appendChild(null) -> NPE on the JDK impl ----
        check(isNpe(() -> d.appendChild(null)), "appendChild(null) -> NPE");

        // ---- getElementsByTagName / NS ----
        Element b1 = d.createElement("book"); root.appendChild(b1);
        Element b2 = d.createElement("book"); root.appendChild(b2);
        NodeList books = d.getElementsByTagName("book");
        check(books.getLength() == 2, "getElementsByTagName count");
        check(books.item(0) == b1 && books.item(1) == b2, "getElementsByTagName document order");
        check(d.getElementsByTagName("*").getLength() >= 3, "getElementsByTagName(*) wildcard");
        root.appendChild(ns);
        Element nsb = d.createElementNS("urn:test", "t:book"); root.appendChild(nsb);
        check(d.getElementsByTagNameNS("urn:test", "book").getLength() == 1, "getElementsByTagNameNS");
        check(d.getElementsByTagNameNS("urn:test", "*").getLength() == 2, "getElementsByTagNameNS wildcard (item+book)");

        // ---- importNode / adoptNode ----
        Document d2 = newDoc();
        Element imported = (Element) d2.importNode(root, true);
        check(imported.getOwnerDocument() == d2, "importNode sets ownerDocument");
        check("root".equals(imported.getNodeName()), "importNode deep preserves name");
        check(imported.getChildNodes().getLength() == root.getChildNodes().getLength(), "importNode deep copies children");
        Element moved = d.createElement("moved");
        Node adopted = d2.adoptNode(moved);
        check(adopted.getOwnerDocument() == d2, "adoptNode changes ownerDocument");
        check(moved.getOwnerDocument() == d2, "adoptNode mutates original ownerDocument");

        // ---- document-level queries on parsed document ----
        Document pd = parsedDoc();
        check("1.0".equals(pd.getXmlVersion()), "parsed getXmlVersion 1.0");
        check(!pd.getXmlStandalone(), "parsed getXmlStandalone default false");
        check(pd.getDoctype() == null, "getDoctype null for doc without DTD");
        check(pd.getImplementation() != null, "getImplementation non-null");
        check(pd.getImplementation().hasFeature("Core", "3.0"), "getImplementation hasFeature Core 3.0");

        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Document contract (renameNode/adoptNode/xml declarations) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        // XML declaration state setters
        d.setXmlVersion("1.1");
        check("1.1".equals(d.getXmlVersion()), "setXmlVersion/getXmlVersion round-trip");
        try { d.setXmlVersion("9.9"); check(false, "setXmlVersion(9.9) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "setXmlVersion(9.9) -> NOT_SUPPORTED_ERR"); }
        d.setXmlStandalone(true);
        check(d.getXmlStandalone(), "setXmlStandalone round-trip");
        d.setStrictErrorChecking(false);
        check(!d.getStrictErrorChecking(), "setStrictErrorChecking round-trip");
        d.setStrictErrorChecking(true);
        d.setDocumentURI("urn:doc");
        check("urn:doc".equals(d.getDocumentURI()), "setDocumentURI round-trip");
        check(d.getDomConfig() != null, "getDomConfig non-null on new document");
        // parsed document: declaration-derived getters
        String xmlDecl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><r/>";
        org.w3c.dom.Document pdd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xmlDecl.getBytes("UTF-8")));
        check("UTF-8".equals(pdd.getXmlEncoding()), "declared encoding visible via getXmlEncoding");
        check("UTF-8".equals(pdd.getInputEncoding()), "parse input encoding recorded");
        check("1.0".equals(pdd.getXmlVersion()) && !pdd.getXmlStandalone(), "declaration version/standalone defaults");
        check(d.createElement("x") != null, "getDomConfig does not break the factory");
        // cloneNode(true) of a document copies the root but not the doctype (Xerces)
        org.w3c.dom.Document cdoc = (org.w3c.dom.Document) pdd.cloneNode(true);
        check(cdoc.getDocumentElement() != null, "document clone keeps the root element");
        check(cdoc.getDoctype() == null, "document clone drops the doctype (no DTD here)");
        // renameNode matrix
        org.w3c.dom.Document rd = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element rroot = rd.createElement("r");
        rd.appendChild(rroot);
        org.w3c.dom.Element old = rd.createElementNS("urn:x", "p:old");
        rroot.appendChild(old);
        org.w3c.dom.Node renamed = rd.renameNode(old, "urn:y", "q:new");
        check(renamed == old, "renameNode returns the same node");
        check("q:new".equals(old.getNodeName()) && "urn:y".equals(old.getNamespaceURI()) && "new".equals(old.getLocalName()), "renameNode updates ns name triplet");
        rd.renameNode(old, null, "plain");
        check(old.getNamespaceURI() == null && "plain".equals(old.getNodeName()), "renameNode to null ns clears namespace");
        org.w3c.dom.Attr ra = rd.createAttributeNS("urn:a", "p:at");
        rroot.setAttributeNodeNS(ra);
        org.w3c.dom.Node renamedAttr = rd.renameNode(ra, "urn:b", "q:bt");
        check(renamedAttr == ra && "urn:b".equals(ra.getNamespaceURI()) && rroot.getAttributeNodeNS("urn:b", "bt") == ra, "renameNode on attr keeps it mounted under the new name");
        try { rd.renameNode(rd.createTextNode("x"), null, "t"); check(false, "renameNode(text) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "renameNode on non-Element/Attr -> NOT_SUPPORTED_ERR"); }
        org.w3c.dom.Document foreign = f.newDocumentBuilder().newDocument();
        try { rd.renameNode(foreign.createElement("f"), null, "g"); check(false, "renameNode(foreign) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR, "renameNode(foreign node) -> WRONG_DOCUMENT_ERR"); }
        try { rd.renameNode(rd.createElement("z"), null, "bad name"); check(false, "renameNode(illegal) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "renameNode(illegal name) -> INVALID_CHARACTER_ERR"); }
        // adoptNode vs importNode contrast
        org.w3c.dom.Document src = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element se = src.createElement("src");
        se.setAttribute("a", "1");
        se.appendChild(src.createTextNode("c"));
        src.appendChild(se);
        org.w3c.dom.Node adopted = foreign.adoptNode(se);
        check(adopted == se && se.getOwnerDocument() == foreign, "adoptNode moves ownership in place (same instance)");
        check(src.getDocumentElement() == null, "adoptNode detaches from the original document");
        check(foreign.getDocumentElement() == null, "adoptNode does NOT auto-install the node as target root");
        check(foreign.adoptNode(se) == se, "adoptNode is idempotent");
        org.w3c.dom.Document deep = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element sd = src.createElement("sd");
        sd.setAttribute("k", "v");
        sd.appendChild(src.createElement("kid"));
        src.appendChild(sd);
        org.w3c.dom.Node shallow = deep.importNode(sd, false);
        check(shallow.getOwnerDocument() == deep && shallow.getChildNodes().getLength() == 0, "importNode(false) copies node without children");
        check(((org.w3c.dom.Element) shallow).getAttribute("k").equals("v"), "importNode(false) keeps attributes");
        org.w3c.dom.Node full = deep.importNode(sd, true);
        check(full.getChildNodes().getLength() == 1 && full != sd, "importNode(true) deep-copies as a new instance");
        check(sd.getOwnerDocument() == src, "importNode never disturbs the source");
        // adoptNode restrictions on readonly/doctype nodes
        org.w3c.dom.Document cdoc2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DocumentType pdt = f.newDocumentBuilder().newDocument().getImplementation().createDocumentType("q", null, null);
        try { cdoc2.adoptNode(pdt); check(false, "adoptNode(doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "adoptNode(doctype) -> NOT_SUPPORTED_ERR"); }
        try { cdoc2.importNode(pdt, true); check(false, "importNode(doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "importNode(doctype) -> NOT_SUPPORTED_ERR"); }
        // doctype placement before root
        org.w3c.dom.Document wdt = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DocumentType head = wdt.getImplementation().createDocumentType("h", null, null);
        wdt.appendChild(head);
        wdt.appendChild(wdt.createElement("hr"));
        check(wdt.getChildNodes().getLength() == 2 && org.w3c.dom.Node.DOCUMENT_TYPE_NODE == wdt.getFirstChild().getNodeType(), "doctype may precede the root element");
        check(wdt.getDoctype() == head, "getDoctype returns the prepended doctype");
        // getElementById without DTD
        check(pdd.getElementById("x") == null, "getElementById null without DTD/schema");
    }
}

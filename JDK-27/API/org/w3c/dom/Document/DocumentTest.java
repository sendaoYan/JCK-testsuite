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

        System.out.println("PASS (" + passed + " assertions)");
    }
}

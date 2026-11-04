/*
 * @test
 * @summary org.w3c.dom.Node conformance: node-type constants, tree mutation, clone/normalize/textContent, compareDocumentPosition, equality, namespace lookup.
 * @run main NodeTest
 */

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class NodeTest {
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

    public static void main(String[] args) throws Exception {
        // ---- node-type constants (DOM Level 3 values) ----
        check(Node.ELEMENT_NODE == 1, "ELEMENT_NODE == 1");
        check(Node.ATTRIBUTE_NODE == 2, "ATTRIBUTE_NODE == 2");
        check(Node.TEXT_NODE == 3, "TEXT_NODE == 3");
        check(Node.CDATA_SECTION_NODE == 4, "CDATA_SECTION_NODE == 4");
        check(Node.ENTITY_REFERENCE_NODE == 5, "ENTITY_REFERENCE_NODE == 5");
        check(Node.ENTITY_NODE == 6, "ENTITY_NODE == 6");
        check(Node.PROCESSING_INSTRUCTION_NODE == 7, "PROCESSING_INSTRUCTION_NODE == 7");
        check(Node.COMMENT_NODE == 8, "COMMENT_NODE == 8");
        check(Node.DOCUMENT_NODE == 9, "DOCUMENT_NODE == 9");
        check(Node.DOCUMENT_TYPE_NODE == 10, "DOCUMENT_TYPE_NODE == 10");
        check(Node.DOCUMENT_FRAGMENT_NODE == 11, "DOCUMENT_FRAGMENT_NODE == 11");
        check(Node.NOTATION_NODE == 12, "NOTATION_NODE == 12");

        Document d = newDoc();
        Element root = d.createElement("root");
        d.appendChild(root);

        // ---- node name / type / value ----
        check(d.getNodeType() == Node.DOCUMENT_NODE, "document nodeType");
        check("#document".equals(d.getNodeName()), "document nodeName");
        check(d.getNodeValue() == null, "document nodeValue null");
        check(root.getNodeType() == Node.ELEMENT_NODE, "element nodeType");
        check("root".equals(root.getNodeName()), "element nodeName");
        check(root.getNodeValue() == null, "element nodeValue null");
        check(root.getNodeName().equals(root.getTagName()), "element nodeName == tagName");
        // setNodeValue on element is a no-op, remains null
        root.setNodeValue("ignored");
        check(root.getNodeValue() == null, "element setNodeValue is no-op");

        // text node name/value
        Text txt = d.createTextNode("hello");
        check(txt.getNodeType() == Node.TEXT_NODE, "text nodeType");
        check("#text".equals(txt.getNodeName()), "text nodeName");
        check("hello".equals(txt.getNodeValue()), "text nodeValue");
        txt.setNodeValue("world");
        check("world".equals(txt.getData()), "text setNodeValue updates data");

        // ---- tree mutation: append/insert/replace/remove ----
        Element a = d.createElement("a");
        Element b = d.createElement("b");
        Element c = d.createElement("c");
        Node ra = root.appendChild(a);
        check(ra == a, "appendChild returns appended node");
        root.appendChild(b);
        root.appendChild(c);
        check(root.getFirstChild() == a && root.getLastChild() == c, "first/last child");
        check(a.getNextSibling() == b && b.getPreviousSibling() == a, "next/previous sibling");
        check(a.getParentNode() == root, "parentNode");
        // insertBefore at front
        Element z = d.createElement("z");
        root.insertBefore(z, a);
        check(root.getFirstChild() == z, "insertBefore front");
        check(z.getNextSibling() == a, "insertBefore ordering");
        // insertBefore with null ref == append
        Element tail = d.createElement("tail");
        root.insertBefore(tail, null);
        check(root.getLastChild() == tail, "insertBefore(null ref) appends");
        // replaceChild returns old
        Element nw = d.createElement("nw");
        Node old = root.replaceChild(nw, z);
        check(old == z && root.getFirstChild() == nw, "replaceChild returns old and replaces");
        // removeChild returns removed, removes from tree
        Node removed = root.removeChild(nw);
        check(removed == nw && nw.getParentNode() == null, "removeChild detaches");
        check(domCode(() -> root.removeChild(d.createElement("stranger")), DOMException.NOT_FOUND_ERR), "removeChild(non-child) -> NOT_FOUND_ERR");
        check(isNpe(() -> root.appendChild(null)), "appendChild(null) -> NPE");

        // ---- childNodes is a live NodeList ----
        NodeList live = root.getChildNodes();
        int before = live.getLength();
        root.appendChild(d.createElement("extra"));
        check(live.getLength() == before + 1, "childNodes is live");
        check(live.item(before) == root.getLastChild(), "live childNodes item");

        // ---- cloneNode shallow vs deep ----
        Element parent = d.createElement("parent");
        parent.appendChild(d.createTextNode("T"));
        Element kid = d.createElement("kid");
        parent.appendChild(kid);
        Node shallow = parent.cloneNode(false);
        check(shallow.getChildNodes().getLength() == 0, "cloneNode(false) shallow");
        check(shallow.getNodeType() == Node.ELEMENT_NODE && "parent".equals(shallow.getNodeName()), "cloneNode preserves type/name");
        Node deep = parent.cloneNode(true);
        check(deep.getChildNodes().getLength() == 2, "cloneNode(true) deep");
        check("kid".equals(deep.getLastChild().getNodeName()), "cloneNode deep child");

        // ---- normalize merges adjacent text ----
        Element e = d.createElement("e");
        e.appendChild(d.createTextNode("ab"));
        e.appendChild(d.createTextNode("cd"));
        check(e.getChildNodes().getLength() == 2, "two text children before normalize");
        e.normalize();
        check(e.getChildNodes().getLength() == 1, "normalize merges text nodes");
        check("abcd".equals(e.getFirstChild().getNodeValue()), "normalize concatenates text");

        // ---- text content ----
        Element e2 = d.createElement("e2");
        e2.appendChild(d.createTextNode("hello"));
        e2.appendChild(d.createElement("sub"));
        check("hello".equals(e2.getTextContent()), "getTextContent concatenates text descendants");
        e2.setTextContent("replaced");
        check(e2.getChildNodes().getLength() == 1, "setTextContent replaces children with one text node");
        check("replaced".equals(e2.getTextContent()), "setTextContent value");
        check(e2.getFirstChild().getNodeType() == Node.TEXT_NODE, "setTextContent creates text node");

        // ---- compareDocumentPosition ----
        Element parent2 = d.createElement("p2");
        Element child2 = d.createElement("c2");
        parent2.appendChild(child2);
        check((parent2.compareDocumentPosition(child2) & Node.DOCUMENT_POSITION_CONTAINED_BY) != 0, "parent contains child (CONTAINED_BY)");
        check((child2.compareDocumentPosition(parent2) & Node.DOCUMENT_POSITION_CONTAINS) != 0, "child contained by parent (CONTAINS)");
        Element sibling1 = d.createElement("s1");
        Element sibling2 = d.createElement("s2");
        Element holder = d.createElement("h");
        holder.appendChild(sibling1);
        holder.appendChild(sibling2);
        check((sibling1.compareDocumentPosition(sibling2) & Node.DOCUMENT_POSITION_FOLLOWING) != 0, "later sibling FOLLOWING");

        // ---- equality ----
        check(parent2.isSameNode(parent2), "isSameNode self");
        check(!parent2.isSameNode(child2), "isSameNode distinct false");
        Element twin = (Element) parent2.cloneNode(true);
        check(parent2.isEqualNode(twin), "isEqualNode deep-equal clone");
        check(!parent2.isEqualNode(child2), "isEqualNode false for distinct");

        // ---- namespace direct properties ----
        Element ne = d.createElementNS("urn:ns", "p:item");
        check("urn:ns".equals(ne.getNamespaceURI()), "namespace URI");
        check("item".equals(ne.getLocalName()), "local name");
        check("p".equals(ne.getPrefix()), "prefix");
        check("p:item".equals(ne.getNodeName()), "qualified node name");
        ne.setPrefix("q");
        check("q".equals(ne.getPrefix()), "setPrefix");
        check("q:item".equals(ne.getNodeName()), "setPrefix updates nodeName");

        // ---- namespace lookup on parsed document ----
        DocumentBuilderFactory pf = DocumentBuilderFactory.newInstance();
        pf.setNamespaceAware(true);
        Document pd = pf.newDocumentBuilder().parse(
            new java.io.ByteArrayInputStream("<root xmlns:p=\"urn:ns\"><p:item/></root>".getBytes("UTF-8")));
        Element item = (Element) pd.getElementsByTagNameNS("urn:ns", "item").item(0);
        check("urn:ns".equals(item.lookupNamespaceURI("p")), "lookupNamespaceURI resolves declared prefix");
        check("p".equals(item.lookupPrefix("urn:ns")), "lookupPrefix resolves declared namespace");
        check(!item.isDefaultNamespace("urn:ns"), "isDefaultNamespace false for prefixed element");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

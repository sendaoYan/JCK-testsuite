/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataNode conformance: behavioral.
 * @run main IIOMetadataNodeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class IIOMetadataNodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static Throwable expectThrows(String expectedClassName, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            check(t.getClass().getName().equals(expectedClassName),
                  m + " expected " + expectedClassName + " but got " + t.getClass().getName());
            return t;
        }
        check(false, m + " should throw " + expectedClassName);
        return null;
    }

    // ---- Part 2: attribute contract (probed on jdk8u372-b07) ----
    static void testAttributes() throws Exception {
        IIOMetadataNode e = new IIOMetadataNode("e");
        check("".equals(e.getAttribute("missing")), "absent attribute empty string");
        e.setAttribute("k", "v1");
        Attr a1 = e.getAttributeNode("k");
        check(a1 != null, "getAttributeNode non-null");
        check("k".equals(a1.getName()), "attr getName");
        check("v1".equals(a1.getValue()), "attr getValue");
        check("k".equals(a1.getNodeName()), "attr getNodeName == name");
        check("v1".equals(a1.getNodeValue()), "attr getNodeValue == value");
        check(a1.getNodeType() == Node.ATTRIBUTE_NODE, "attr nodeType ATTRIBUTE_NODE");
        check(a1.getSpecified() == true, "attr getSpecified true");
        check(a1.getOwnerElement() == e, "attr getOwnerElement identity");
        a1.setValue("v2");
        check("v2".equals(e.getAttribute("k")), "attr setValue visible via getAttribute");
        check("v2".equals(a1.getValue()), "attr getValue after setValue");
        a1.setNodeValue("v3");
        check("v3".equals(e.getAttribute("k")), "attr setNodeValue visible via getAttribute");
        // null attribute value is stored as null (not "")
        e.setAttribute("n", null);
        check(e.getAttribute("n") == null, "setAttribute(null) value stored as null");
        check(e.getAttributeNode("n").getValue() == null, "attr getValue null");
        e.removeAttribute("n");
        check(!e.hasAttribute("n"), "null-valued attribute removed");
        // removeAttributeNode returns the same attr and clears ownership
        e.setAttribute("k2", "x");
        Attr ak2 = e.getAttributeNode("k2");
        Attr removed = e.removeAttributeNode(ak2);
        check(removed == ak2, "removeAttributeNode returns same attr");
        check(ak2.getOwnerElement() == null, "removed attr owner cleared");
        check(e.getAttributes().getLength() == 1, "attribute count after removeAttributeNode");
        // setAttributeNode: same-owner no-op returning null
        check(e.setAttributeNode(e.getAttributeNode("k")) == null, "setAttributeNode same owner returns null");
        check("v3".equals(e.getAttribute("k")), "setAttributeNode same owner keeps value");
        // setAttributeNode: attr owned by another element -> INUSE_ATTRIBUTE_ERR (10)
        IIOMetadataNode e2 = new IIOMetadataNode("e2");
        e2.setAttribute("k", "other");
        DOMException de = (DOMException) expectThrows("org.w3c.dom.DOMException",
            () -> e.setAttributeNode(e2.getAttributeNode("k")), "setAttributeNode other-owner");
        check(de.code == DOMException.INUSE_ATTRIBUTE_ERR, "INUSE_ATTRIBUTE_ERR code 10");
        // setAttributeNode re-add of a detached attr replaces and returns the old attr
        Attr detached = e.getAttributeNode("k");
        e.removeAttributeNode(detached);
        e.setAttribute("k", "v4");
        Attr current = e.getAttributeNode("k");
        Attr oldRet = e.setAttributeNode(detached);
        check(oldRet == current, "setAttributeNode re-add returns previous attr");
        check("v3".equals(e.getAttribute("k")), "re-added attr value restored");
        check(current.getOwnerElement() == null, "displaced attr owner cleared");
        // setAttributeNode wrapping a foreign Attr implementation
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Attr foreign = doc.createAttribute("q");
        foreign.setValue("qv");
        check(e.setAttributeNode(foreign) == null, "setAttributeNode foreign attr returns null (no previous)");
        check("qv".equals(e.getAttribute("q")), "foreign attr value wrapped");
        check(e.getAttributeNode("q") != foreign, "foreign attr wrapped into new node");
        check(e.getAttributeNode("q").getOwnerElement() == e, "wrapped attr owned by element");
        // insertion order preserved
        IIOMetadataNode ord = new IIOMetadataNode("ord");
        ord.setAttribute("o1", "1"); ord.setAttribute("o2", "2"); ord.setAttribute("o3", "3");
        NamedNodeMap nnm = ord.getAttributes();
        check(nnm.getLength() == 3, "attribute map length");
        check("o1".equals(nnm.item(0).getNodeName()) && "o2".equals(nnm.item(1).getNodeName())
              && "o3".equals(nnm.item(2).getNodeName()), "attribute insertion order");
        check(nnm.getNamedItem("o2") == ord.getAttributeNode("o2"), "getNamedItem identity");
        check(nnm.getNamedItemNS("urn:x", "o2") == ord.getAttributeNode("o2"), "getNamedItemNS synonym");
        check(nnm.getNamedItem("zz") == null, "getNamedItem missing null");
        check(nnm != ord.getAttributes(), "getAttributes returns fresh wrapper each call");
        ord.setAttribute("o4", "4");
        check(nnm.getLength() == 4, "attribute map is a live view of the list");
    }

    // ---- Part 3: NamedNodeMap write channels are read-only ----
    static void testNamedNodeMapReadOnly() throws Exception {
        IIOMetadataNode e = new IIOMetadataNode("e");
        e.setAttribute("k", "v");
        NamedNodeMap m = e.getAttributes();
        final Attr a = e.getAttributeNode("k");
        DOMException d1 = (DOMException) expectThrows("org.w3c.dom.DOMException",
            () -> m.setNamedItem(a), "setNamedItem read-only");
        check(d1.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "NO_MODIFICATION_ALLOWED_ERR code 7");
        DOMException d2 = (DOMException) expectThrows("org.w3c.dom.DOMException",
            () -> m.setNamedItemNS(a), "setNamedItemNS read-only");
        check(d2.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "setNamedItemNS code 7");
        DOMException d3 = (DOMException) expectThrows("org.w3c.dom.DOMException",
            () -> m.removeNamedItem("k"), "removeNamedItem read-only (existing key)");
        check(d3.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "removeNamedItem code 7");
        DOMException d4 = (DOMException) expectThrows("org.w3c.dom.DOMException",
            () -> m.removeNamedItemNS(null, "k"), "removeNamedItemNS read-only");
        check(d4.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "removeNamedItemNS code 7");
        check(m.getLength() == 1 && "v".equals(e.getAttribute("k")), "state intact after rejected map writes");
        // item() out of bounds: raw List.get -> IndexOutOfBoundsException (JDK-8: negative index is ArrayIndexOutOfBoundsException)
        expectThrows("java.lang.IndexOutOfBoundsException", () -> m.item(1), "map item(1) out of bounds");
        expectThrows("java.lang.ArrayIndexOutOfBoundsException", () -> m.item(-1), "map item(-1) negative index");
        check(m.item(0).getNodeName().equals("k"), "map item(0) valid");
    }

    // ---- Part 4: child list maintenance ----
    static void testChildMaintenance() throws Exception {
        IIOMetadataNode p = new IIOMetadataNode("p");
        IIOMetadataNode c1 = new IIOMetadataNode("c1");
        IIOMetadataNode c2 = new IIOMetadataNode("c2");
        check(p.appendChild(c1) == c1, "appendChild returns the appended node");
        p.appendChild(c2);
        IIOMetadataNode c0 = new IIOMetadataNode("c0");
        check(p.insertBefore(c0, null) == c0, "insertBefore(child, null) returns child");
        check(p.getLastChild() == c0, "insertBefore(child, null) appends at end");
        check(p.getLength() == 3, "length after insertBefore null ref");
        check(c0.getPreviousSibling() == c2, "appended node previous sibling");
        // null arguments -> IllegalArgumentException
        expectThrows("java.lang.IllegalArgumentException", () -> p.appendChild(null), "appendChild null");
        expectThrows("java.lang.IllegalArgumentException", () -> p.removeChild(null), "removeChild null");
        expectThrows("java.lang.IllegalArgumentException", () -> p.insertBefore(null, c1), "insertBefore null newChild");
        expectThrows("java.lang.IllegalArgumentException", () -> p.replaceChild(null, c1), "replaceChild null newChild");
        // foreign (non-IIOMetadataNode) nodes -> DOMException WRONG_DOCUMENT_ERR (4) via package-private IIODOMException
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final Element foreign = doc.createElement("foreign");
        DOMException f1 = (DOMException) expectThrows("javax.imageio.metadata.IIODOMException",
            () -> p.appendChild(foreign), "appendChild foreign node");
        check(f1.code == DOMException.WRONG_DOCUMENT_ERR, "foreign appendChild code 4");
        DOMException f2 = (DOMException) expectThrows("javax.imageio.metadata.IIODOMException",
            () -> p.insertBefore(foreign, c1), "insertBefore foreign node");
        check(f2.code == DOMException.WRONG_DOCUMENT_ERR, "foreign insertBefore code 4");
        DOMException f3 = (DOMException) expectThrows("javax.imageio.metadata.IIODOMException",
            () -> p.removeChild(foreign), "removeChild foreign node");
        check(f3.code == DOMException.WRONG_DOCUMENT_ERR, "foreign removeChild code 4");
        DOMException f4 = (DOMException) expectThrows("javax.imageio.metadata.IIODOMException",
            () -> p.replaceChild(foreign, foreign), "replaceChild foreign node");
        check(f4.code == DOMException.WRONG_DOCUMENT_ERR, "foreign replaceChild code 4");
        check(p.getLength() == 3, "child count intact after rejected mutations");
        // removeChild on a node that is not a child does not throw (implementation quirk; not asserted further)
        IIOMetadataNode orphan = new IIOMetadataNode("orphan");
        p.removeChild(orphan);
        check(orphan.getParentNode() == null, "removed non-child has null parent");
        // NodeList accessors on the element (live list)
        check(p.item(-1) == null, "item(-1) null");
        check(p.item(99) == null, "item(99) null");
        check(p.item(2) == c0, "item(2) live");
    }

    // ---- Part 5: clone semantics ----
    static void testCloneSemantics() throws Exception {
        IIOMetadataNode src = new IIOMetadataNode("src");
        src.setAttribute("a", "1");
        src.setUserObject("uo");
        IIOMetadataNode kid = new IIOMetadataNode("kid");
        kid.setAttribute("ka", "kv");
        src.appendChild(kid);
        IIOMetadataNode deep = (IIOMetadataNode) src.cloneNode(true);
        check(deep != src, "deep clone distinct");
        check("src".equals(deep.getNodeName()), "deep clone name");
        check(deep.getLength() == 1, "deep clone one child");
        check(deep.getFirstChild() != kid, "deep clone child is a copy");
        check("kid".equals(deep.getFirstChild().getNodeName()), "deep clone child name");
        check(!((Element) deep.getFirstChild()).hasAttribute("ka"), "deep clone grandchild attributes not copied");
        check("".equals(((Element) deep.getFirstChild()).getAttribute("ka")), "grandchild absent attribute reads empty");
        check("uo".equals(deep.getUserObject()), "deep clone copies user object");
        // deep-clone copies do not carry element attributes (probed actual behavior)
        check(!deep.hasAttribute("a"), "deep clone does not copy attributes");
        check(deep.getAttribute("a").equals(""), "cloned absent attribute reads empty");
        // independence of deep clone
        src.appendChild(new IIOMetadataNode("extra"));
        check(deep.getLength() == 1, "mutating original does not affect deep clone");
        ((IIOMetadataNode) deep.getFirstChild()).setAttribute("new", "x");
        check(!kid.hasAttribute("new"), "mutating deep clone does not affect original");
        IIOMetadataNode shallow = (IIOMetadataNode) src.cloneNode(false);
        check(shallow.getLength() == 0, "shallow clone has no children");
        check("src".equals(shallow.getNodeName()), "shallow clone name");
        check("uo".equals(shallow.getUserObject()), "shallow clone copies user object");
        check(!shallow.hasAttribute("a"), "shallow clone does not copy attributes");
    }

    // ---- Part 6: DOM Level 3 methods are unsupported (NOT_SUPPORTED_ERR = 9) ----
    static void testDomLevel3Unsupported() throws Exception {
        IIOMetadataNode e = new IIOMetadataNode("e");
        e.setAttribute("k", "v");
        final Attr a = e.getAttributeNode("k");
        DOMException d;
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.getUserData("k"), "getUserData");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getUserData code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.setUserData("k", "v", null), "setUserData");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setUserData code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.getTextContent(), "getTextContent");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getTextContent code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.setTextContent("t"), "setTextContent");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setTextContent code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.isSameNode(e), "isSameNode");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "isSameNode code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.isEqualNode(e), "isEqualNode");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "isEqualNode code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.getFeature("f", "1.0"), "getFeature");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getFeature code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.compareDocumentPosition(e), "compareDocumentPosition");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "compareDocumentPosition code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.getBaseURI(), "getBaseURI");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getBaseURI code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.lookupPrefix("ns"), "lookupPrefix");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "lookupPrefix code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.lookupNamespaceURI("p"), "lookupNamespaceURI");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "lookupNamespaceURI code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.isDefaultNamespace("ns"), "isDefaultNamespace");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "isDefaultNamespace code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.getSchemaTypeInfo(), "getSchemaTypeInfo");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getSchemaTypeInfo code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.setIdAttribute("k", true), "setIdAttribute");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setIdAttribute code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.setIdAttributeNS(null, "k", true), "setIdAttributeNS");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setIdAttributeNS code 9");
        d = (DOMException) expectThrows("org.w3c.dom.DOMException", () -> e.setIdAttributeNode(a, true), "setIdAttributeNode");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setIdAttributeNode code 9");
        check(e.hasAttribute("k"), "state intact after L3 rejections");
    }

    // ---- Part 7: misc DOM contract ----
    static void testMiscDom() throws Exception {
        IIOMetadataNode e = new IIOMetadataNode("e");
        DOMException d = (DOMException) expectThrows("javax.imageio.metadata.IIODOMException",
            () -> e.removeAttribute("missing"), "removeAttribute missing");
        check(d.code == DOMException.NOT_FOUND_ERR, "removeAttribute missing code 8");
        d = (DOMException) expectThrows("javax.imageio.metadata.IIODOMException",
            () -> e.setAttribute("bad\ufffe", "x"), "setAttribute illegal char 0xFFFE");
        check(d.code == DOMException.INVALID_CHARACTER_ERR, "setAttribute illegal char code 5");
        check(!e.hasAttributes(), "no attributes after rejected setAttribute");
        // no-arg constructor leaves the node name null
        IIOMetadataNode blank = new IIOMetadataNode();
        check(blank.getNodeName() == null, "no-arg ctor node name null");
        check(blank.getTagName() == null, "no-arg ctor tag name null");
        check(blank.getNodeType() == Node.ELEMENT_NODE, "no-arg ctor node type");
        // toString
        IIOMetadataNode ts = new IIOMetadataNode("ts");
        check(ts.toString().startsWith("javax.imageio.metadata.IIOMetadataNode@"), "toString format");
        // getElementsByTagName returns a snapshot list
        IIOMetadataNode root = new IIOMetadataNode("root");
        IIOMetadataNode t1 = new IIOMetadataNode("t");
        root.appendChild(t1);
        NodeList snap = root.getElementsByTagName("t");
        check(snap.getLength() == 1, "snapshot length");
        root.appendChild(new IIOMetadataNode("t"));
        check(snap.getLength() == 1, "getElementsByTagName snapshot not live");
        check(root.getLength() == 2, "live list grew");
        check(root.getElementsByTagNameNS("urn:x", "t").getLength() == 2, "getElementsByTagNameNS synonym");
        NodeList star = root.getElementsByTagName("*");
        check(star.getLength() == 3, "getElementsByTagName * includes self plus children");
        // nested recursion finds grandchildren
        IIOMetadataNode g = new IIOMetadataNode("t");
        t1.appendChild(g);
        check(root.getElementsByTagName("t").getLength() == 3, "getElementsByTagName recurses");
        // normalize / isSupported / namespaces
        e.normalize();
        check(!e.isSupported("XML", "1.0"), "isSupported false");
        check(e.isSupported(null, null) == false, "isSupported(null,null) false");
        check(e.getNamespaceURI() == null, "namespace URI null");
        check(e.getPrefix() == null, "prefix null");
        e.setPrefix("p");
        check(e.getPrefix() == null, "setPrefix no-op");
        check(e.getOwnerDocument() == null, "owner document null");
        check(e.getAttributes() != null && e.getAttributes().getLength() == 0, "fresh node empty attribute map");
        check(e.getChildNodes() == e, "getChildNodes returns this");
    }

    public static void main(String[] a) throws Exception {
        javax.imageio.metadata.IIOMetadataNode n = new javax.imageio.metadata.IIOMetadataNode("root");
        check("root".equals(n.getNodeName()), "nodeName");
        check(n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE, "nodeType element");
        n.setAttribute("k", "v");
        check("v".equals(n.getAttribute("k")), "attribute");
        javax.imageio.metadata.IIOMetadataNode child = new javax.imageio.metadata.IIOMetadataNode("child");
        n.appendChild(child);
        check(n.getChildNodes().getLength() == 1, "child length");
        check(n.getFirstChild() == child, "firstChild");
        check(n.getLength() == 1, "nodelist length");
        check(n.item(0) == child, "item");
        check(n.getUserObject() == null, "userObject null");
        n.setUserObject("x");
        check("x".equals(n.getUserObject()), "setUserObject");
        testAttributes();
        testNamedNodeMapReadOnly();
        testChildMaintenance();
        testCloneSemantics();
        testDomLevel3Unsupported();
        testMiscDom();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

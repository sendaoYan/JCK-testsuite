/*
 * @test
 * @summary javax.imageio.metadata.IIOMetadataNode conformance: white-box structure + black-box behavior.
 * @run main IIOMetadataNodeTest
 */

import java.lang.reflect.Modifier;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IIOMetadataNodeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }
    static Throwable expectThrowsNamed(String expectedClassName, ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            check(t.getClass().getName().equals(expectedClassName),
                  msg + " expected " + expectedClassName + " but got " + t.getClass().getName());
            return t;
        }
        check(false, msg + " should throw " + expectedClassName);
        return null;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOMetadataNode");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is not interface");
        check(!c.isAnnotation(), "is not annotation");
        check(!c.isEnum(), "is not enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 2, "implements 2 interfaces");
        boolean hasElement = false, hasNodeList = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals("org.w3c.dom.Element")) hasElement = true;
            if (i.getName().equals("org.w3c.dom.NodeList")) hasNodeList = true;
        }
        check(hasElement, "implements org.w3c.dom.Element");
        check(hasNodeList, "implements org.w3c.dom.NodeList");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // construction + node identity
        IIOMetadataNode root = new IIOMetadataNode("root");
        check("root".equals(root.getNodeName()), "getNodeName");
        check("root".equals(root.getTagName()), "getTagName");
        check("root".equals(root.getLocalName()), "getLocalName");
        check(root.getNodeValue() == null, "node value null initially");
        root.setNodeValue("v");
        check("v".equals(root.getNodeValue()), "set/get node value");
        check(root.getNodeType() == Node.ELEMENT_NODE, "node type ELEMENT_NODE");

        IIOMetadataNode empty = new IIOMetadataNode();
        check(empty.getNodeName() == null, "default ctor node name null");
        check(empty.getNodeType() == Node.ELEMENT_NODE, "default ctor node type");

        // root topology
        check(root.getParentNode() == null, "root parent null");
        check(root.getFirstChild() == null, "first child null");
        check(root.getLastChild() == null, "last child null");
        check(root.getPreviousSibling() == null, "previous sibling null");
        check(root.getNextSibling() == null, "next sibling null");
        check(root.getOwnerDocument() == null, "owner document null");
        check(root.getLength() == 0, "child length 0");
        check(!root.hasChildNodes(), "no child nodes");

        // build a small tree and walk it
        IIOMetadataNode a = new IIOMetadataNode("a");
        IIOMetadataNode b = new IIOMetadataNode("b");
        IIOMetadataNode c = new IIOMetadataNode("c");
        root.appendChild(a);
        root.appendChild(b);
        root.appendChild(c);
        check(root.getLength() == 3, "three children");
        check(root.hasChildNodes(), "has child nodes");
        check(root.getFirstChild() == a, "first child a");
        check(root.getLastChild() == c, "last child c");
        check(a.getParentNode() == root, "a parent root");
        check(a.getNextSibling() == b, "a next b");
        check(b.getPreviousSibling() == a, "b previous a");
        check(b.getNextSibling() == c, "b next c");
        check(c.getPreviousSibling() == b, "c previous b");
        check(c.getNextSibling() == null, "c next null");

        // NodeList view
        NodeList children = root.getChildNodes();
        check(children == root, "getChildNodes returns this");
        check(children.getLength() == 3, "node list length 3");
        check(children.item(0) == a && children.item(1) == b && children.item(2) == c, "node list items");
        check(children.item(-1) == null, "item(-1) null");
        check(children.item(3) == null, "item(3) null");

        // insertBefore at front and middle
        IIOMetadataNode first = new IIOMetadataNode("first");
        root.insertBefore(first, a);
        check(root.getFirstChild() == first, "insertBefore at front");
        check(first.getNextSibling() == a && a.getPreviousSibling() == first, "insertBefore links");
        check(root.getLength() == 4, "length after insertBefore");
        IIOMetadataNode mid = new IIOMetadataNode("mid");
        root.insertBefore(mid, b);
        check(a.getNextSibling() == mid && mid.getNextSibling() == b, "insertBefore middle links");
        check(root.getLength() == 5, "length after middle insert");

        // removeChild restores sibling links
        Node removed = root.removeChild(mid);
        check(removed == mid, "removeChild returns removed");
        check(mid.getParentNode() == null, "removed parent null");
        check(a.getNextSibling() == b && b.getPreviousSibling() == a, "sibling links after remove");
        check(root.getLength() == 4, "length after remove");

        // replaceChild
        IIOMetadataNode repl = new IIOMetadataNode("repl");
        Node old = root.replaceChild(repl, b);
        check(old == b, "replaceChild returns old");
        check(a.getNextSibling() == repl && repl.getPreviousSibling() == a
              && repl.getNextSibling() == c && c.getPreviousSibling() == repl, "replace links");
        check(b.getParentNode() == null, "replaced node parent null");
        check(root.getLength() == 4, "length unchanged after replace");

        // attributes
        check(!root.hasAttributes(), "no attributes initially");
        root.setAttribute("k", "v");
        check("v".equals(root.getAttribute("k")), "getAttribute");
        check(root.hasAttribute("k"), "hasAttribute");
        check(root.hasAttributes(), "hasAttributes");
        check("".equals(root.getAttribute("absent")), "absent attribute empty string");
        check(!root.hasAttribute("absent"), "absent hasAttribute false");
        Attr attr = root.getAttributeNode("k");
        check(attr != null && "k".equals(attr.getName()) && "v".equals(attr.getValue()), "getAttributeNode");
        NamedNodeMap attrs = root.getAttributes();
        check(attrs.getLength() == 1, "attributes length 1");
        check(attrs.getNamedItem("k") == attr, "named item identity");
        root.setAttribute("k", "v2");
        check("v2".equals(root.getAttribute("k")), "setAttribute replaces value");
        root.removeAttribute("k");
        check(!root.hasAttribute("k") && !root.hasAttributes(), "removeAttribute");
        // namespace variants are synonyms
        root.setAttributeNS("urn:x", "ns", "nv");
        check("nv".equals(root.getAttributeNS("urn:y", "ns")), "getAttributeNS synonym");
        check("nv".equals(root.getAttribute("ns")), "setAttributeNS writes local name");
        check(root.hasAttributeNS("urn:z", "ns"), "hasAttributeNS synonym");
        root.removeAttributeNS("urn:x", "ns");
        check(!root.hasAttribute("ns"), "removeAttributeNS synonym");

        // user object
        check(root.getUserObject() == null, "user object null initially");
        Object marker = new Object();
        root.setUserObject(marker);
        check(root.getUserObject() == marker, "set/get user object");

        // clone: shallow vs deep
        a.setUserObject("aobj");
        IIOMetadataNode shallow = (IIOMetadataNode) a.cloneNode(false);
        check(shallow != a && "a".equals(shallow.getNodeName()), "shallow clone distinct same name");
        check(shallow.getLength() == 0, "shallow clone has no children");
        check("aobj".equals(shallow.getUserObject()), "shallow clone copies user object");

        IIOMetadataNode tree = new IIOMetadataNode("tree");
        IIOMetadataNode leaf = new IIOMetadataNode("leaf");
        tree.appendChild(leaf);
        IIOMetadataNode deep = (IIOMetadataNode) tree.cloneNode(true);
        check(deep != tree && "tree".equals(deep.getNodeName()), "deep clone distinct");
        check(deep.getLength() == 1, "deep clone has one child");
        check(deep.getFirstChild() != leaf, "deep clone child distinct");
        check("leaf".equals(deep.getFirstChild().getNodeName()), "deep clone child name");

        // getElementsByTagName
        IIOMetadataNode root2 = new IIOMetadataNode("root");
        IIOMetadataNode k1 = new IIOMetadataNode("kid");
        IIOMetadataNode k2 = new IIOMetadataNode("kid");
        IIOMetadataNode other = new IIOMetadataNode("other");
        root2.appendChild(k1); root2.appendChild(k2); root2.appendChild(other);
        NodeList kids = root2.getElementsByTagName("kid");
        check(kids.getLength() == 2 && kids.item(0) == k1 && kids.item(1) == k2, "getElementsByTagName kid");
        NodeList all = root2.getElementsByTagName("*");
        check(all.getLength() == 4, "getElementsByTagName * includes self");

        // misc DOM contract
        check(root2.isSupported("XML", "1.0") == false, "isSupported false");
        check(root2.getNamespaceURI() == null, "namespace URI null");
        check(root2.getPrefix() == null, "prefix null");
        root2.setPrefix("p"); // no-op, namespaces ignored
        check(root2.getPrefix() == null, "setPrefix no-op");
        root2.normalize(); // no-op

        // exception contracts
        expectThrows(IllegalArgumentException.class, () -> root.appendChild(null), "appendChild null");
        expectThrows(IllegalArgumentException.class, () -> root.insertBefore(null, a), "insertBefore null");
        expectThrows(IllegalArgumentException.class, () -> root.replaceChild(null, a), "replaceChild null");
        expectThrows(IllegalArgumentException.class, () -> root.removeChild(null), "removeChild null");
        expectThrows(DOMException.class, () -> root.removeAttribute("missing"), "removeAttribute missing");
        expectThrows(DOMException.class, () -> root2.isEqualNode(k1), "isEqualNode unsupported");
        expectThrows(DOMException.class, () -> root2.getTextContent(), "getTextContent unsupported");
        expectThrows(DOMException.class, () -> root2.getSchemaTypeInfo(), "getSchemaTypeInfo unsupported");
        expectThrows(DOMException.class, () -> root2.setIdAttribute("k", true), "setIdAttribute unsupported");
        // illegal attribute name (char >= 0xfffe)
        expectThrows(DOMException.class, () -> root2.setAttribute("bad\ufffe", "x"), "setAttribute illegal char");
    }

    // ---- Part 3: attribute node contract (probed on jdk-27.0.1) ----
    static void testAttributesDeep() throws Exception {
        IIOMetadataNode e = new IIOMetadataNode("e");
        e.setAttribute("k", "v1");
        Attr a1 = e.getAttributeNode("k");
        check("k".equals(a1.getNodeName()), "attr getNodeName == name");
        check("v1".equals(a1.getNodeValue()), "attr getNodeValue == value");
        check(a1.getNodeType() == Node.ATTRIBUTE_NODE, "attr nodeType ATTRIBUTE_NODE");
        check(a1.getSpecified(), "attr getSpecified true");
        check(a1.getOwnerElement() == e, "attr getOwnerElement identity");
        a1.setValue("v2");
        check("v2".equals(e.getAttribute("k")), "attr setValue visible via getAttribute");
        a1.setNodeValue("v3");
        check("v3".equals(e.getAttribute("k")), "attr setNodeValue visible via getAttribute");
        // null attribute value is stored as null (not empty string)
        e.setAttribute("n", null);
        check(e.getAttribute("n") == null, "setAttribute(null) value stored as null");
        e.removeAttribute("n");
        // removeAttributeNode returns the same attr and clears ownership
        e.setAttribute("k2", "x");
        Attr ak2 = e.getAttributeNode("k2");
        Attr removed = e.removeAttributeNode(ak2);
        check(removed == ak2, "removeAttributeNode returns same attr");
        check(ak2.getOwnerElement() == null, "removed attr owner cleared");
        // setAttributeNode: same-owner no-op returning null
        check(e.setAttributeNode(e.getAttributeNode("k")) == null, "setAttributeNode same owner returns null");
        // setAttributeNode re-add of a detached attr replaces and returns the old attr
        Attr detached = e.getAttributeNode("k");
        e.removeAttributeNode(detached);
        e.setAttribute("k", "v4");
        Attr current = e.getAttributeNode("k");
        check(e.setAttributeNode(detached) == current, "setAttributeNode re-add returns previous attr");
        check("v3".equals(e.getAttribute("k")), "re-added attr value restored");
        check(current.getOwnerElement() == null, "displaced attr owner cleared");
        // setAttributeNode wrapping a foreign Attr implementation
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        Attr foreign = doc.createAttribute("q");
        foreign.setValue("qv");
        check(e.setAttributeNode(foreign) == null, "setAttributeNode foreign attr returns null");
        check("qv".equals(e.getAttribute("q")), "foreign attr value wrapped");
        check(e.getAttributeNode("q") != foreign, "foreign attr wrapped into new node");
        // insertion order + live map view
        IIOMetadataNode ord = new IIOMetadataNode("ord");
        ord.setAttribute("o1", "1"); ord.setAttribute("o2", "2"); ord.setAttribute("o3", "3");
        NamedNodeMap nnm = ord.getAttributes();
        check(nnm.getLength() == 3, "attribute map length");
        check("o1".equals(nnm.item(0).getNodeName()) && "o2".equals(nnm.item(1).getNodeName())
              && "o3".equals(nnm.item(2).getNodeName()), "attribute insertion order");
        check(nnm.getNamedItemNS("urn:x", "o2") == ord.getAttributeNode("o2"), "getNamedItemNS synonym");
        check(nnm.getNamedItem("zz") == null, "getNamedItem missing null");
        check(nnm != ord.getAttributes(), "getAttributes returns fresh wrapper each call");
        ord.setAttribute("o4", "4");
        check(nnm.getLength() == 4, "attribute map is a live view of the list");
        // write channels of the map are read-only (code 7)
        final Attr a0 = ord.getAttributeNode("o1");
        DOMException d1 = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException",
            () -> nnm.setNamedItem(a0), "setNamedItem read-only");
        check(d1.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "NO_MODIFICATION_ALLOWED_ERR");
        DOMException d2 = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException",
            () -> nnm.setNamedItemNS(a0), "setNamedItemNS read-only");
        check(d2.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "setNamedItemNS code 7");
        DOMException d3 = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException",
            () -> nnm.removeNamedItem("o1"), "removeNamedItem read-only");
        check(d3.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "removeNamedItem code 7");
        DOMException d4 = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException",
            () -> nnm.removeNamedItemNS(null, "o1"), "removeNamedItemNS read-only");
        check(d4.code == DOMException.NO_MODIFICATION_ALLOWED_ERR, "removeNamedItemNS code 7");
        check(nnm.getLength() == 4, "map intact after rejected writes");
        // setAttributeNode attr owned by another element -> INUSE_ATTRIBUTE_ERR (10)
        IIOMetadataNode e2 = new IIOMetadataNode("e2");
        e2.setAttribute("shared", "s");
        DOMException inuse = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException",
            () -> ord.setAttributeNode(e2.getAttributeNode("shared")), "setAttributeNode other-owner");
        check(inuse.code == DOMException.INUSE_ATTRIBUTE_ERR, "INUSE_ATTRIBUTE_ERR code 10");
        // item() out of bounds: JDK-27 raises plain IndexOutOfBoundsException for both directions
        expectThrowsNamed("java.lang.IndexOutOfBoundsException", () -> nnm.item(99), "map item(99) out of bounds");
        expectThrowsNamed("java.lang.IndexOutOfBoundsException", () -> nnm.item(-1), "map item(-1) negative");
        check(nnm.item(3).getNodeName().equals("o4"), "map item(3) valid");
    }

    // ---- Part 4: child maintenance extra ----
    static void testChildMaintenanceDeep() throws Exception {
        IIOMetadataNode p = new IIOMetadataNode("p");
        IIOMetadataNode c1 = new IIOMetadataNode("c1");
        IIOMetadataNode c2 = new IIOMetadataNode("c2");
        check(p.appendChild(c1) == c1, "appendChild returns the appended node");
        p.appendChild(c2);
        IIOMetadataNode c0 = new IIOMetadataNode("c0");
        check(p.insertBefore(c0, null) == c0, "insertBefore(child, null) returns child");
        check(p.getLastChild() == c0, "insertBefore(child, null) appends at end");
        check(c0.getPreviousSibling() == c2, "appended node previous sibling");
        check(p.getLength() == 3, "length after insertBefore null ref");
        // foreign (non-IIOMetadataNode) nodes -> WRONG_DOCUMENT_ERR (4) from package-private IIODOMException
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final org.w3c.dom.Element foreign = doc.createElement("foreign");
        DOMException f1 = (DOMException) expectThrowsNamed("javax.imageio.metadata.IIODOMException",
            () -> p.appendChild(foreign), "appendChild foreign node");
        check(f1.code == DOMException.WRONG_DOCUMENT_ERR, "foreign appendChild code 4");
        DOMException f2 = (DOMException) expectThrowsNamed("javax.imageio.metadata.IIODOMException",
            () -> p.insertBefore(foreign, c1), "insertBefore foreign node");
        check(f2.code == DOMException.WRONG_DOCUMENT_ERR, "foreign insertBefore code 4");
        DOMException f3 = (DOMException) expectThrowsNamed("javax.imageio.metadata.IIODOMException",
            () -> p.removeChild(foreign), "removeChild foreign node");
        check(f3.code == DOMException.WRONG_DOCUMENT_ERR, "foreign removeChild code 4");
        DOMException f4 = (DOMException) expectThrowsNamed("javax.imageio.metadata.IIODOMException",
            () -> p.replaceChild(foreign, foreign), "replaceChild foreign node");
        check(f4.code == DOMException.WRONG_DOCUMENT_ERR, "foreign replaceChild code 4");
        check(p.getLength() == 3, "child count intact after rejected mutations");
        check(p.getChildNodes() == p, "getChildNodes returns this");
        check(p.item(-1) == null && p.item(99) == null, "element NodeList item OOB null");
    }

    // ---- Part 5: clone attributes quirk + snapshot lists ----
    static void testCloneAndLists() throws Exception {
        IIOMetadataNode src = new IIOMetadataNode("src");
        src.setAttribute("a", "1");
        src.setUserObject("uo");
        IIOMetadataNode kid = new IIOMetadataNode("kid");
        kid.setAttribute("ka", "kv");
        src.appendChild(kid);
        IIOMetadataNode deep = (IIOMetadataNode) src.cloneNode(true);
        check(!((org.w3c.dom.Element) deep.getFirstChild()).hasAttribute("ka"), "deep clone grandchild attributes not copied");
        check(((org.w3c.dom.Element) deep.getFirstChild()).getAttribute("ka").equals(""), "grandchild absent attribute reads empty");
        check(!deep.hasAttribute("a"), "deep clone does not copy attributes");
        check(deep.getAttribute("a").equals(""), "cloned absent attribute reads empty");
        check("uo".equals(deep.getUserObject()), "deep clone copies user object");
        src.appendChild(new IIOMetadataNode("extra"));
        check(deep.getLength() == 1, "mutating original does not affect deep clone");
        ((IIOMetadataNode) deep.getFirstChild()).setAttribute("new", "x");
        check(!kid.hasAttribute("new"), "mutating deep clone does not affect original");
        // getElementsByTagName returns a snapshot (IIONodeList), unlike getChildNodes
        NodeList snap = src.getElementsByTagName("kid");
        check(snap.getLength() == 1, "snapshot length");
        src.appendChild(new IIOMetadataNode("kid"));
        check(snap.getLength() == 1, "getElementsByTagName snapshot not live");
        check(src.getLength() == 3, "live list grew");
        check(src.getElementsByTagNameNS("urn:x", "kid").getLength() == 2, "getElementsByTagNameNS synonym");
        NodeList star = src.getElementsByTagName("*");
        check(star.getLength() == 4, "getElementsByTagName * includes self");
    }

    // ---- Part 6: DOM Level 3 codes + misc ----
    static void testDomLevel3AndMisc() throws Exception {
        IIOMetadataNode e = new IIOMetadataNode("e");
        e.setAttribute("k", "v");
        final Attr a = e.getAttributeNode("k");
        DOMException d;
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.getUserData("k"), "getUserData");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getUserData code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.setUserData("k", "v", null), "setUserData");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setUserData code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.setTextContent("t"), "setTextContent");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setTextContent code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.isSameNode(e), "isSameNode");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "isSameNode code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.getFeature("f", "1.0"), "getFeature");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getFeature code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.compareDocumentPosition(e), "compareDocumentPosition");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "compareDocumentPosition code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.getBaseURI(), "getBaseURI");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "getBaseURI code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.lookupPrefix("ns"), "lookupPrefix");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "lookupPrefix code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.lookupNamespaceURI("p"), "lookupNamespaceURI");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "lookupNamespaceURI code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.isDefaultNamespace("ns"), "isDefaultNamespace");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "isDefaultNamespace code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.setIdAttributeNS(null, "k", true), "setIdAttributeNS");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setIdAttributeNS code 9");
        d = (DOMException) expectThrowsNamed("org.w3c.dom.DOMException", () -> e.setIdAttributeNode(a, true), "setIdAttributeNode");
        check(d.code == DOMException.NOT_SUPPORTED_ERR, "setIdAttributeNode code 9");
        check(e.hasAttribute("k"), "state intact after L3 rejections");
        // exception codes for Element channel
        d = (DOMException) expectThrowsNamed("javax.imageio.metadata.IIODOMException",
            () -> e.removeAttribute("missing"), "removeAttribute missing code");
        check(d.code == DOMException.NOT_FOUND_ERR, "removeAttribute missing code 8");
        d = (DOMException) expectThrowsNamed("javax.imageio.metadata.IIODOMException",
            () -> e.setAttribute("bad\ufffe", "x"), "setAttribute illegal char");
        check(d.code == DOMException.INVALID_CHARACTER_ERR, "setAttribute illegal char code 5");
        // misc
        IIOMetadataNode blank = new IIOMetadataNode();
        check(blank.getTagName() == null, "no-arg ctor tag name null");
        IIOMetadataNode ts = new IIOMetadataNode("ts");
        check(ts.toString().startsWith("javax.imageio.metadata.IIOMetadataNode@"), "toString format");
        check(!e.isSupported(null, null), "isSupported(null,null) false");
        check(e.getChildNodes() == e, "getChildNodes returns this");
        e.setNodeValue(null);
        check(e.getNodeValue() == null, "setNodeValue(null) round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAttributesDeep();
        testChildMaintenanceDeep();
        testCloneAndLists();
        testDomLevel3AndMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

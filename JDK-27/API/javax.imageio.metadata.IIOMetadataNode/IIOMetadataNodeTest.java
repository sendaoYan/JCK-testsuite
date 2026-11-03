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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

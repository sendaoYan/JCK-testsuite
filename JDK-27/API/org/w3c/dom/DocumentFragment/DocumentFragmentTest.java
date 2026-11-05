/*
 * @test
 * @summary org.w3c.dom.DocumentFragment conformance: interface extends Node.
 * @run main DocumentFragmentTest
 */

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

public class DocumentFragmentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentFragment.class.isInterface(), "DocumentFragment is an interface");
        check(Node.class.isAssignableFrom(DocumentFragment.class), "extends Node");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DocumentFragment a = new org.w3c.dom.DocumentFragment(){
            public java.lang.String getNodeName() { return "s"; }
            public java.lang.String getNodeValue() { return "s"; }
            public void setNodeValue(java.lang.String p0) { }
            public short getNodeType() { return (short)2; }
            public org.w3c.dom.Node getParentNode() { return null; }
            public org.w3c.dom.NodeList getChildNodes() { return null; }
            public org.w3c.dom.Node getFirstChild() { return null; }
            public org.w3c.dom.Node getLastChild() { return null; }
            public org.w3c.dom.Node getPreviousSibling() { return null; }
            public org.w3c.dom.Node getNextSibling() { return null; }
            public org.w3c.dom.NamedNodeMap getAttributes() { return null; }
            public org.w3c.dom.Document getOwnerDocument() { return null; }
            public org.w3c.dom.Node insertBefore(org.w3c.dom.Node p0, org.w3c.dom.Node p1) { return null; }
            public org.w3c.dom.Node replaceChild(org.w3c.dom.Node p0, org.w3c.dom.Node p1) { return null; }
            public org.w3c.dom.Node removeChild(org.w3c.dom.Node p0) { return null; }
            public org.w3c.dom.Node appendChild(org.w3c.dom.Node p0) { return null; }
            public boolean hasChildNodes() { return true; }
            public org.w3c.dom.Node cloneNode(boolean p0) { return null; }
            public void normalize() { }
            public boolean isSupported(java.lang.String p0, java.lang.String p1) { return true; }
            public java.lang.String getNamespaceURI() { return "s"; }
            public java.lang.String getPrefix() { return "s"; }
            public void setPrefix(java.lang.String p0) { }
            public java.lang.String getLocalName() { return "s"; }
            public boolean hasAttributes() { return true; }
            public java.lang.String getBaseURI() { return "s"; }
            public short compareDocumentPosition(org.w3c.dom.Node p0) { return (short)2; }
            public java.lang.String getTextContent() { return "s"; }
            public void setTextContent(java.lang.String p0) { }
            public boolean isSameNode(org.w3c.dom.Node p0) { return true; }
            public java.lang.String lookupPrefix(java.lang.String p0) { return "s"; }
            public boolean isDefaultNamespace(java.lang.String p0) { return true; }
            public java.lang.String lookupNamespaceURI(java.lang.String p0) { return "s"; }
            public boolean isEqualNode(org.w3c.dom.Node p0) { return true; }
            public java.lang.Object getFeature(java.lang.String p0, java.lang.String p1) { return null; }
            public java.lang.Object setUserData(java.lang.String p0, java.lang.Object p1, org.w3c.dom.UserDataHandler p2) { return null; }
            public java.lang.Object getUserData(java.lang.String p0) { return null; }
        };
        check(a != null, "anon impl of DocumentFragment");
        check("s".equals(a.getNodeName()), "getNodeName() returns fixed default");
        check("s".equals(a.getNodeValue()), "getNodeValue() returns fixed default");
        a.setNodeValue("s");
        check(a.getNodeType() == (short)2, "getNodeType() returns fixed default");
        check(a.getParentNode() == null, "getParentNode() returns fixed default");
        check(a.getChildNodes() == null, "getChildNodes() returns fixed default");
        check(a.getFirstChild() == null, "getFirstChild() returns fixed default");
        check(a.getLastChild() == null, "getLastChild() returns fixed default");
        check(a.getPreviousSibling() == null, "getPreviousSibling() returns fixed default");
        check(a.getNextSibling() == null, "getNextSibling() returns fixed default");
        check(a.getAttributes() == null, "getAttributes() returns fixed default");
        check(a.getOwnerDocument() == null, "getOwnerDocument() returns fixed default");
        check(a.insertBefore((org.w3c.dom.Node) null, (org.w3c.dom.Node) null) == null, "insertBefore() returns fixed default");
        check(a.replaceChild((org.w3c.dom.Node) null, (org.w3c.dom.Node) null) == null, "replaceChild() returns fixed default");
        check(a.removeChild((org.w3c.dom.Node) null) == null, "removeChild() returns fixed default");
        check(a.appendChild((org.w3c.dom.Node) null) == null, "appendChild() returns fixed default");
        check(a.hasChildNodes() == true, "hasChildNodes() returns fixed default");
        check(a.cloneNode(true) == null, "cloneNode() returns fixed default");
        a.normalize();
        check(a.isSupported("s", "s") == true, "isSupported() returns fixed default");
        check("s".equals(a.getNamespaceURI()), "getNamespaceURI() returns fixed default");
        check("s".equals(a.getPrefix()), "getPrefix() returns fixed default");
        a.setPrefix("s");
        check("s".equals(a.getLocalName()), "getLocalName() returns fixed default");
        check(a.hasAttributes() == true, "hasAttributes() returns fixed default");
        check("s".equals(a.getBaseURI()), "getBaseURI() returns fixed default");
        check(a.compareDocumentPosition((org.w3c.dom.Node) null) == (short)2, "compareDocumentPosition() returns fixed default");
        check("s".equals(a.getTextContent()), "getTextContent() returns fixed default");
        a.setTextContent("s");
        check(a.isSameNode((org.w3c.dom.Node) null) == true, "isSameNode() returns fixed default");
        check("s".equals(a.lookupPrefix("s")), "lookupPrefix() returns fixed default");
        check(a.isDefaultNamespace("s") == true, "isDefaultNamespace() returns fixed default");
        check("s".equals(a.lookupNamespaceURI("s")), "lookupNamespaceURI() returns fixed default");
        check(a.isEqualNode((org.w3c.dom.Node) null) == true, "isEqualNode() returns fixed default");
        check(a.getFeature("s", "s") == null, "getFeature() returns fixed default");
        check(a.setUserData("s", (java.lang.Object) null, (org.w3c.dom.UserDataHandler) null) == null, "setUserData() returns fixed default");
        check(a.getUserData("s") == null, "getUserData() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DocumentFragment contract (porter semantics) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DocumentFragment df = d.createDocumentFragment();
        check(org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE == df.getNodeType(), "fragment nodeType 11");
        check("#document-fragment".equals(df.getNodeName()), "fragment nodeName #document-fragment");
        check(df.getParentNode() == null, "fragment has no parent");
        check(df.getOwnerDocument() == d, "fragment owner document");
        check(df.getAttributes() == null, "fragment getAttributes null");
        check(df.getChildNodes().getLength() == 0 && df.getFirstChild() == null, "fragment starts empty");
        check(df.isEqualNode(d.createDocumentFragment()), "two empty fragments are equal");
        // lightweight container: children of any type
        df.appendChild(d.createElement("x"));
        df.appendChild(d.createTextNode("t"));
        df.appendChild(d.createComment("c"));
        check(df.getChildNodes().getLength() == 3, "fragment accepts mixed children");
        check("t".equals(df.getTextContent()), "fragment textContent aggregates text-only children");
        // porter semantics: appendChild(fragment) moves the children, not the fragment
        org.w3c.dom.Element root = d.createElement("root");
        d.appendChild(root);
        org.w3c.dom.Element host = d.createElement("host");
        root.appendChild(host);
        host.appendChild(df);
        check(df.getChildNodes().getLength() == 0, "fragment emptied after being inserted");
        check(host.getChildNodes().getLength() == 3, "children transferred in order to host");
        check("x".equals(host.getChildNodes().item(0).getNodeName()) && "t".equals(host.getChildNodes().item(1).getNodeValue()), "transfer preserves order");
        // insertBefore(fragment, null) behaves the same way
        org.w3c.dom.DocumentFragment df2 = d.createDocumentFragment();
        df2.appendChild(d.createElement("m1"));
        df2.appendChild(d.createElement("m2"));
        org.w3c.dom.Element slot = d.createElement("slot");
        root.appendChild(slot);
        slot.insertBefore(df2, null);
        check(slot.getChildNodes().getLength() == 2 && df2.getChildNodes().getLength() == 0, "insertBefore(fragment) also ports children");
        // replaceChild with a fragment ports its children too
        org.w3c.dom.DocumentFragment df3 = d.createDocumentFragment();
        df3.appendChild(d.createElement("rep1"));
        df3.appendChild(d.createElement("rep2"));
        org.w3c.dom.Node old = slot.replaceChild(df3, slot.getFirstChild());
        check("m1".equals(old.getNodeName()) && slot.getChildNodes().getLength() == 3
            && "rep1".equals(slot.getChildNodes().item(0).getNodeName()), "replaceChild(fragment) splices children in place");
        // normalize on a fragment merges adjacent text
        org.w3c.dom.DocumentFragment df4 = d.createDocumentFragment();
        df4.appendChild(d.createTextNode("a"));
        df4.appendChild(d.createTextNode("b"));
        df4.normalize();
        check(df4.getChildNodes().getLength() == 1 && "ab".equals(df4.getFirstChild().getNodeValue()), "fragment normalize merges adjacent text");
        // setTextContent replaces the child list
        org.w3c.dom.DocumentFragment df5 = d.createDocumentFragment();
        df5.appendChild(d.createElement("z"));
        df5.setTextContent("zz");
        check(df5.getChildNodes().getLength() == 1 && "zz".equals(df5.getFirstChild().getNodeValue()), "fragment setTextContent swaps children for one text node");
        // clone and import
        org.w3c.dom.DocumentFragment df6 = d.createDocumentFragment();
        df6.appendChild(d.createElement("y"));
        org.w3c.dom.DocumentFragment cl = (org.w3c.dom.DocumentFragment) df6.cloneNode(true);
        check(cl.getChildNodes().getLength() == 1 && cl.isEqualNode(df6), "fragment cloneNode(true) deep-copies");
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DocumentFragment imp = (org.w3c.dom.DocumentFragment) other.importNode(df6, true);
        check(imp.getOwnerDocument() == other && imp.getChildNodes().getLength() == 1, "fragment importNode rebinds owner");
        org.w3c.dom.DocumentFragment impShallow = (org.w3c.dom.DocumentFragment) other.importNode(df6, false);
        check(impShallow.getChildNodes().getLength() == 0, "fragment importNode(false) yields empty fragment");
        // subtree scanning goes through the children's element API
        org.w3c.dom.DocumentFragment df7 = d.createDocumentFragment();
        org.w3c.dom.Element deep = d.createElement("deep");
        deep.appendChild(d.createElement("deep"));
        df7.appendChild(deep);
        check(deep.getElementsByTagName("deep").getLength() == 1, "fragment subtree reachable via element scan");
        // document-level port rules: element/text children are rejected at document level
        boolean docReject = false;
        try { d.appendChild(df7); } catch (org.w3c.dom.DOMException ex) { docReject = ex.code == org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR; }
        check(docReject, "fragment with element child cannot append to document (HIERARCHY_REQUEST_ERR)");
        check(df7.getChildNodes().getLength() == 1, "rejected port leaves fragment children intact");
        org.w3c.dom.DocumentFragment textOnly = d.createDocumentFragment();
        textOnly.appendChild(d.createTextNode("t"));
        boolean textReject = false;
        try { d.appendChild(textOnly); } catch (org.w3c.dom.DOMException ex) { textReject = ex.code == org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR; }
        check(textReject, "text-only fragment also rejected at document level");
        check(d.appendChild(d.createDocumentFragment()) != null, "empty fragment append is a legal no-op");
        // into an EMPTY document the ported element becomes the document element
        org.w3c.dom.Document fresh = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DocumentFragment rootFrag = fresh.createDocumentFragment();
        rootFrag.appendChild(fresh.createElement("r9"));
        fresh.appendChild(rootFrag);
        check("r9".equals(fresh.getDocumentElement().getNodeName()) && rootFrag.getChildNodes().getLength() == 0, "fragment port into empty document installs its element as documentElement");
    }
}

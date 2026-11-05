/*
 * @test
 * @summary org.w3c.dom.Attr conformance: interface extends Node.
 * @run main AttrTest
 */

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

public class AttrTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Attr.class.isInterface(), "Attr is an interface");
        check(Node.class.isAssignableFrom(Attr.class), "extends Node");
        check(Attr.class.getMethod("getName") != null, "getName()");
        check(Attr.class.getMethod("getValue") != null, "getValue()");
        check(Attr.class.getMethod("getOwnerElement") != null, "getOwnerElement()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.Attr a = new org.w3c.dom.Attr(){
            public java.lang.String getName() { return "s"; }
            public boolean getSpecified() { return true; }
            public java.lang.String getValue() { return "s"; }
            public void setValue(java.lang.String p0) { }
            public org.w3c.dom.Element getOwnerElement() { return null; }
            public org.w3c.dom.TypeInfo getSchemaTypeInfo() { return null; }
            public boolean isId() { return true; }
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
        check(a != null, "anon impl of Attr");
        check("s".equals(a.getName()), "getName() returns fixed default");
        check(a.getSpecified() == true, "getSpecified() returns fixed default");
        check("s".equals(a.getValue()), "getValue() returns fixed default");
        a.setValue("s");
        check(a.getOwnerElement() == null, "getOwnerElement() returns fixed default");
        check(a.getSchemaTypeInfo() == null, "getSchemaTypeInfo() returns fixed default");
        check(a.isId() == true, "isId() returns fixed default");
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




    // ---- deepening round 2: attr value model / owner lifecycle / DTD isId / children model ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();

        // factory-created attr state before mounting
        org.w3c.dom.Attr a = d.createAttribute("free");
        check("free".equals(a.getName()) && "free".equals(a.getNodeName()), "attr name via getName/getNodeName");
        check(a.getOwnerElement() == null, "unmounted attr ownerElement null");
        check(a.getOwnerDocument() == d, "attr ownerDocument set");
        check(a.getSpecified(), "factory attr specified true");
        check(a.getOwnerElement() == null && a.getParentNode() == null, "attr has no parent node");
        // value model: value and child text stay in sync
        a.setValue("v1");
        check("v1".equals(a.getValue()) && "v1".equals(a.getNodeValue()), "setValue visible via getValue/getNodeValue");
        check(a.getChildNodes().getLength() == 1 && a.getFirstChild().getNodeType() == org.w3c.dom.Node.TEXT_NODE, "attr value exposed as text child");
        a.setNodeValue("v2");
        check("v2".equals(a.getValue()), "setNodeValue syncs getValue");
        a.setTextContent("v3");
        check("v3".equals(a.getValue()), "setTextContent syncs getValue");
        a.setValue("v4");
        check("v4".equals(a.getTextContent()), "getValue syncs getTextContent");
        // appending extra text extends the value
        a.appendChild(d.createTextNode("+"));
        check("v4+".equals(a.getValue()), "attr value concatenates text children");
        check(a.getChildNodes().getLength() == 2, "two text children after append");
        // character-data writes on the attr's text child are allowed (not read-only)
        org.w3c.dom.Attr wa = d.createAttribute("wa");
        wa.setValue("base");
        ((org.w3c.dom.Text) wa.getFirstChild()).setData("mod");
        check("mod".equals(wa.getValue()), "attr text child is writable");
        wa.getFirstChild().setNodeValue("nn");
        check("nn".equals(wa.getValue()), "attr text setNodeValue writable");
        // Xerces lazily exposes the text child of a valueless attr
        org.w3c.dom.Attr lazy = d.createAttribute("lazy");
        check(lazy.getFirstChild() == null, "fresh attr getFirstChild null (lazy)");
        lazy.setValue("");
        check(lazy.getFirstChild() != null && lazy.getFirstChild().getNodeValue().isEmpty(), "setValue('') materializes empty text child");
        // mounting lifecycle
        org.w3c.dom.Element e = d.createElement("e");
        e.setAttributeNode(a);
        check(a.getOwnerElement() == e, "mounted attr ownerElement points to element");
        check(e.getAttributeNode("free") == a, "element sees mounted attr");
        org.w3c.dom.Attr taken = e.removeAttributeNode(a);
        check(taken == a && a.getOwnerElement() == null, "unmounted attr ownerElement cleared");
        // clone independence
        org.w3c.dom.Attr ca = (org.w3c.dom.Attr) a.cloneNode(true);
        check(ca.getValue().equals(a.getValue()), "clone keeps value");
        ca.setValue("other");
        check(!a.getValue().equals("other"), "clone value independent");
        // importNode rebinding
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Attr imported = (org.w3c.dom.Attr) other.importNode(a, true);
        check(imported.getOwnerDocument() == other && imported.getValue().equals(a.getValue()), "imported attr rebinding");
        // spec: schema type info reachable, isId false without DTD
        check(!a.isId(), "manual attr isId false without DTD");
        check(a.getSchemaTypeInfo() != null, "attr getSchemaTypeInfo non-null");

        // DTD-backed semantics: declared ID attribute and default attribute
        String xml = "<!DOCTYPE r [<!ELEMENT r ANY><!ATTLIST item id ID #IMPLIED>"
            + "<!ATTLIST item def CDATA 'dflt'>]><r><item id='x1' def='user'/><item/></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Element item1 = (org.w3c.dom.Element) pd.getElementsByTagName("item").item(0);
        org.w3c.dom.Attr idAttr = item1.getAttributeNode("id");
        check(idAttr.isId(), "DTD-declared ID attr isId true");
        check(pd.getElementById("x1") == item1, "getElementById resolves DTD ID");
        check(pd.getElementById("zzz") == null, "getElementById(unknown) null");
        check("ID".equals(idAttr.getSchemaTypeInfo().getTypeName()), "ID attr typeName 'ID'");
        check(idAttr.getSpecified(), "explicitly given attr specified true");
        check("user".equals(idAttr.getOwnerElement().getAttribute("def")), "explicit default-attr value wins");
        org.w3c.dom.Element item2 = (org.w3c.dom.Element) pd.getElementsByTagName("item").item(1);
        check(!item2.hasAttribute("id"), "absent ID attr not fabricated");
        org.w3c.dom.Attr defAttr = item2.getAttributeNode("def");
        check(defAttr != null && "dflt".equals(defAttr.getValue()), "default attr injected from DTD");
        check(!defAttr.getSpecified(), "DTD default attr specified false");
        // attr on ns element
        org.w3c.dom.Element nsEl = pd.createElementNS("urn:x", "p:n");
        org.w3c.dom.Attr nsA = pd.createAttributeNS("urn:y", "q:k");
        nsA.setValue("1");
        nsEl.setAttributeNodeNS(nsA);
        check("urn:y".equals(nsA.getNamespaceURI()) && "k".equals(nsA.getLocalName()) && "q".equals(nsA.getPrefix()), "ns attr name decomposition");
        check(nsEl.getAttributeNodeNS("urn:y", "k") == nsA, "ns attr retrieval by uri+local");
        // attr of an attr-less document fragment state: attributes map is null on Attr itself
        check(a.getAttributes() == null, "Attr.getAttributes null");
        // getNodeType/getNodeValue defaults
        check(a.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE, "attr nodeType 2");
        a.setValue(null);
        check(a.getValue().isEmpty() && a.getFirstChild() == null, "setValue(null) empties value (empty string, child cleared)");
    }

}

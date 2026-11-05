/*
 * @test
 * @summary org.w3c.dom.EntityReference conformance: interface extends Node.
 * @run main EntityReferenceTest
 */

import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

public class EntityReferenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(EntityReference.class.isInterface(), "EntityReference is an interface");
        check(Node.class.isAssignableFrom(EntityReference.class), "extends Node");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.EntityReference a = new org.w3c.dom.EntityReference(){
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
        check(a != null, "anon impl of EntityReference");
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

    // ---- deepening round 2: behavioral EntityReference contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        String xml = "<!DOCTYPE r [<!ENTITY e1 'v1'>]><r>&e1;</r>";
        // expansion ON (default): reference replaced by its replacement text
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        check(pd.getDocumentElement().getChildNodes().getLength() == 1
            && org.w3c.dom.Node.TEXT_NODE == pd.getDocumentElement().getFirstChild().getNodeType(), "default parsing expands entity references to text");
        check("v1".equals(pd.getDocumentElement().getTextContent()), "expanded entity contributes replacement text");
        // expansion OFF: EntityReference node appears
        javax.xml.parsers.DocumentBuilderFactory fx = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        fx.setExpandEntityReferences(false);
        fx.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        org.w3c.dom.Document pdx = fx.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Node erNode = pdx.getDocumentElement().getFirstChild();
        check(org.w3c.dom.Node.ENTITY_REFERENCE_NODE == erNode.getNodeType(), "expandEntityReferences(false) keeps EntityReference nodes");
        check("e1".equals(erNode.getNodeName()), "parsed EntityReference nodeName is the entity name");
        check(erNode.getChildNodes().getLength() == 0, "Xerces leaves parsed EntityReference children unexpanded");
        // manual factory
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.EntityReference er = d.createEntityReference("e9");
        check(org.w3c.dom.Node.ENTITY_REFERENCE_NODE == er.getNodeType(), "manual entityReference nodeType 5");
        check("e9".equals(er.getNodeName()) && er.getOwnerDocument() == d, "manual entityReference name/owner");
        check(er.getChildNodes().getLength() == 0, "manual entityReference has no children");
        check(er.getParentNode() == null, "factory entityReference unparented");
        check(er.getNodeValue() == null, "entityReference nodeValue null");
        // name legality
        try { d.createEntityReference("a b"); check(false, "createEntityReference(illegal) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createEntityReference('a b') -> INVALID_CHARACTER_ERR"); }
        try { d.createEntityReference(null); check(false, "createEntityReference(null) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createEntityReference(null) -> INVALID_CHARACTER_ERR"); }
        check(d.createEntityReference("undeclared") != null, "Xerces does not validate the name against declared entities");
        // readonly: children cannot be attached
        try { er.appendChild(d.createElement("x")); check(false, "entityReference appendChild must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entityReference appendChild -> NO_MODIFICATION_ALLOWED_ERR"); }
        // usable as a normal tree member
        org.w3c.dom.Element host = d.createElement("host");
        d.appendChild(host);
        host.appendChild(er);
        check(er.getParentNode() == host && host.getFirstChild() == er, "entityReference can be placed in content");
        // clone/import keep the reference
        check("e9".equals(((org.w3c.dom.EntityReference) er.cloneNode(true)).getNodeName()), "entityReference clone keeps name");
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        check("e9".equals(((org.w3c.dom.EntityReference) other.importNode(er, true)).getNodeName()), "entityReference import keeps name");
        // textContent of unexpanded reference is empty
        check("".equals(host.getTextContent()) || host.getTextContent() == null, "unexpanded entityReference contributes no text");
    }
}

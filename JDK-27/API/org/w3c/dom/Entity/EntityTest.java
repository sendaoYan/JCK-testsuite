/*
 * @test
 * @summary org.w3c.dom.Entity conformance: interface extends Node.
 * @run main EntityTest
 */

import org.w3c.dom.Entity;
import org.w3c.dom.Node;

public class EntityTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Entity.class.isInterface(), "Entity is an interface");
        check(Node.class.isAssignableFrom(Entity.class), "extends Node");
        check(Entity.class.getMethod("getPublicId") != null, "getPublicId()");
        check(Entity.class.getMethod("getSystemId") != null, "getSystemId()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.Entity a = new org.w3c.dom.Entity(){
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
            public java.lang.String getNotationName() { return "s"; }
            public java.lang.String getInputEncoding() { return "s"; }
            public java.lang.String getXmlEncoding() { return "s"; }
            public java.lang.String getXmlVersion() { return "s"; }
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
        check(a != null, "anon impl of Entity");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check("s".equals(a.getNotationName()), "getNotationName() returns fixed default");
        check("s".equals(a.getInputEncoding()), "getInputEncoding() returns fixed default");
        check("s".equals(a.getXmlEncoding()), "getXmlEncoding() returns fixed default");
        check("s".equals(a.getXmlVersion()), "getXmlVersion() returns fixed default");
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

    // ---- deepening round 2: behavioral Entity contract (parsed DTD declarations) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        String xml = "<!DOCTYPE r [<!ELEMENT r ANY><!ENTITY e1 'v1'>"
            + "<!ENTITY e2 SYSTEM 'e2.xml'>"
            + "<!ENTITY e3 SYSTEM 'e3.bin' NDATA gif>"
            + "<!NOTATION gif PUBLIC 'gif pub' 'gif sys'>]><r>&e1;</r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.DocumentType dt = pd.getDoctype();
        org.w3c.dom.Entity e1 = (org.w3c.dom.Entity) dt.getEntities().getNamedItem("e1");
        org.w3c.dom.Entity e2 = (org.w3c.dom.Entity) dt.getEntities().getNamedItem("e2");
        org.w3c.dom.Entity e3 = (org.w3c.dom.Entity) dt.getEntities().getNamedItem("e3");
        check(org.w3c.dom.Node.ENTITY_NODE == e1.getNodeType(), "entity nodeType 6");
        check("e1".equals(e1.getNodeName()), "entity nodeName is the declaration name");
        check(e1.getParentNode() == null && e1.getOwnerDocument() == pd, "entity parent null, owned by the document");
        check(e1.getNotationName() == null && e1.getPublicId() == null && e1.getSystemId() == null, "internal entity has null external triple");
        check(e1.getChildNodes().getLength() == 0, "Xerces does not expand entity children in the DTD view");
        check("".equals(e1.getTextContent()) || e1.getTextContent() == null, "entity textContent empty/null (not expanded)");
        check(e1.getNodeValue() == null, "entity nodeValue null");
        check(e1.getAttributes() == null && e1.getPrefix() == null, "entity attributes/prefix null");
        check(e2.getSystemId() != null && e2.getSystemId().contains("e2.xml"), "external parsed entity keeps systemId");
        check(e2.getNotationName() == null, "parsed external entity has null notationName");
        check("gif".equals(e3.getNotationName()), "unparsed (NDATA) entity notationName set");
        check(e3.getSystemId() != null, "unparsed entity systemId set");
        // entities are readonly
        try { e1.setNodeValue("x"); check(false, "entity setNodeValue must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entity setNodeValue -> NO_MODIFICATION_ALLOWED_ERR"); }
        try { e1.appendChild(pd.createTextNode("x")); check(false, "entity appendChild must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entity appendChild -> NO_MODIFICATION_ALLOWED_ERR"); }
        // adoption is refused
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        try { other.adoptNode(e1); check(false, "adoptNode(entity) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "adoptNode(entity) -> NO_MODIFICATION_ALLOWED_ERR"); }
        // entity map lookup misses
        check(dt.getEntities().getNamedItem("nope") == null, "entities getNamedItem(unknown) null");
        // factory-created doctype carries no entities
        org.w3c.dom.Document blank = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DocumentType cdt = blank.getImplementation().createDocumentType("q", null, null);
        check(cdt.getEntities().getLength() == 0, "factory doctype entities empty");
    }
}

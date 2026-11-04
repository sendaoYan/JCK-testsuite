/*
 * @test
 * @summary org.w3c.dom.Document conformance: interface extends Node.
 * @run main DocumentTest
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DocumentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Document.class.isInterface(), "Document is an interface");
        check(Node.class.isAssignableFrom(Document.class), "extends Node");
        check(Document.class.getMethod("getDocumentElement") != null,
              "getDocumentElement()");
        check(Document.class.getMethod("createElement", String.class)
                .getReturnType() == Element.class, "createElement(String) returns Element");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.Document a = new org.w3c.dom.Document(){
            public org.w3c.dom.DocumentType getDoctype() { return null; }
            public org.w3c.dom.DOMImplementation getImplementation() { return null; }
            public org.w3c.dom.Element getDocumentElement() { return null; }
            public org.w3c.dom.Element createElement(java.lang.String p0) { return null; }
            public org.w3c.dom.DocumentFragment createDocumentFragment() { return null; }
            public org.w3c.dom.Text createTextNode(java.lang.String p0) { return null; }
            public org.w3c.dom.Comment createComment(java.lang.String p0) { return null; }
            public org.w3c.dom.CDATASection createCDATASection(java.lang.String p0) { return null; }
            public org.w3c.dom.ProcessingInstruction createProcessingInstruction(java.lang.String p0, java.lang.String p1) { return null; }
            public org.w3c.dom.Attr createAttribute(java.lang.String p0) { return null; }
            public org.w3c.dom.EntityReference createEntityReference(java.lang.String p0) { return null; }
            public org.w3c.dom.NodeList getElementsByTagName(java.lang.String p0) { return null; }
            public org.w3c.dom.Node importNode(org.w3c.dom.Node p0, boolean p1) { return null; }
            public org.w3c.dom.Element createElementNS(java.lang.String p0, java.lang.String p1) { return null; }
            public org.w3c.dom.Attr createAttributeNS(java.lang.String p0, java.lang.String p1) { return null; }
            public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String p0, java.lang.String p1) { return null; }
            public org.w3c.dom.Element getElementById(java.lang.String p0) { return null; }
            public java.lang.String getInputEncoding() { return "s"; }
            public java.lang.String getXmlEncoding() { return "s"; }
            public boolean getXmlStandalone() { return true; }
            public void setXmlStandalone(boolean p0) { }
            public java.lang.String getXmlVersion() { return "s"; }
            public void setXmlVersion(java.lang.String p0) { }
            public boolean getStrictErrorChecking() { return true; }
            public void setStrictErrorChecking(boolean p0) { }
            public java.lang.String getDocumentURI() { return "s"; }
            public void setDocumentURI(java.lang.String p0) { }
            public org.w3c.dom.Node adoptNode(org.w3c.dom.Node p0) { return null; }
            public org.w3c.dom.DOMConfiguration getDomConfig() { return null; }
            public void normalizeDocument() { }
            public org.w3c.dom.Node renameNode(org.w3c.dom.Node p0, java.lang.String p1, java.lang.String p2) { return null; }
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
        check(a != null, "anon impl of Document");
        check(a.getDoctype() == null, "getDoctype() returns fixed default");
        check(a.getImplementation() == null, "getImplementation() returns fixed default");
        check(a.getDocumentElement() == null, "getDocumentElement() returns fixed default");
        check(a.createElement("s") == null, "createElement() returns fixed default");
        check(a.createDocumentFragment() == null, "createDocumentFragment() returns fixed default");
        check(a.createTextNode("s") == null, "createTextNode() returns fixed default");
        check(a.createComment("s") == null, "createComment() returns fixed default");
        check(a.createCDATASection("s") == null, "createCDATASection() returns fixed default");
        check(a.createProcessingInstruction("s", "s") == null, "createProcessingInstruction() returns fixed default");
        check(a.createAttribute("s") == null, "createAttribute() returns fixed default");
        check(a.createEntityReference("s") == null, "createEntityReference() returns fixed default");
        check(a.getElementsByTagName("s") == null, "getElementsByTagName() returns fixed default");
        check(a.importNode((org.w3c.dom.Node) null, true) == null, "importNode() returns fixed default");
        check(a.createElementNS("s", "s") == null, "createElementNS() returns fixed default");
        check(a.createAttributeNS("s", "s") == null, "createAttributeNS() returns fixed default");
        check(a.getElementsByTagNameNS("s", "s") == null, "getElementsByTagNameNS() returns fixed default");
        check(a.getElementById("s") == null, "getElementById() returns fixed default");
        check("s".equals(a.getInputEncoding()), "getInputEncoding() returns fixed default");
        check("s".equals(a.getXmlEncoding()), "getXmlEncoding() returns fixed default");
        check(a.getXmlStandalone() == true, "getXmlStandalone() returns fixed default");
        a.setXmlStandalone(true);
        check("s".equals(a.getXmlVersion()), "getXmlVersion() returns fixed default");
        a.setXmlVersion("s");
        check(a.getStrictErrorChecking() == true, "getStrictErrorChecking() returns fixed default");
        a.setStrictErrorChecking(true);
        check("s".equals(a.getDocumentURI()), "getDocumentURI() returns fixed default");
        a.setDocumentURI("s");
        check(a.adoptNode((org.w3c.dom.Node) null) == null, "adoptNode() returns fixed default");
        check(a.getDomConfig() == null, "getDomConfig() returns fixed default");
        a.normalizeDocument();
        check(a.renameNode((org.w3c.dom.Node) null, "s", "s") == null, "renameNode() returns fixed default");
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

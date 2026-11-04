/*
 * @test
 * @summary org.w3c.dom.Node conformance: interface, node-type constants, methods.
 * @run main NodeTest
 */

import org.w3c.dom.Node;

public class NodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Node.class.isInterface(), "Node is an interface");

        check(Node.ELEMENT_NODE == 1, "ELEMENT_NODE == 1");
        check(Node.ATTRIBUTE_NODE == 2, "ATTRIBUTE_NODE == 2");
        check(Node.TEXT_NODE == 3, "TEXT_NODE == 3");
        check(Node.CDATA_SECTION_NODE == 4, "CDATA_SECTION_NODE == 4");
        check(Node.DOCUMENT_NODE == 9, "DOCUMENT_NODE == 9");
        check(Node.NOTATION_NODE == 12, "NOTATION_NODE == 12");

        check(Node.class.getMethod("getNodeName") != null, "getNodeName()");
        check(Node.class.getMethod("getNodeType") != null, "getNodeType()");
        check(Node.class.getMethod("getParentNode") != null, "getParentNode()");
        check(Node.class.getMethod("appendChild", Node.class) != null,
              "appendChild(Node)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.Node.ELEMENT_NODE == 1, "ELEMENT_NODE == 1");
        check(org.w3c.dom.Node.ATTRIBUTE_NODE == 2, "ATTRIBUTE_NODE == 2");
        check(org.w3c.dom.Node.TEXT_NODE == 3, "TEXT_NODE == 3");
        check(org.w3c.dom.Node.CDATA_SECTION_NODE == 4, "CDATA_SECTION_NODE == 4");
        check(org.w3c.dom.Node.ENTITY_REFERENCE_NODE == 5, "ENTITY_REFERENCE_NODE == 5");
        check(org.w3c.dom.Node.ENTITY_NODE == 6, "ENTITY_NODE == 6");
        check(org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE == 7, "PROCESSING_INSTRUCTION_NODE == 7");
        check(org.w3c.dom.Node.COMMENT_NODE == 8, "COMMENT_NODE == 8");
        check(org.w3c.dom.Node.DOCUMENT_NODE == 9, "DOCUMENT_NODE == 9");
        check(org.w3c.dom.Node.DOCUMENT_TYPE_NODE == 10, "DOCUMENT_TYPE_NODE == 10");
        check(org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE == 11, "DOCUMENT_FRAGMENT_NODE == 11");
        check(org.w3c.dom.Node.NOTATION_NODE == 12, "NOTATION_NODE == 12");
        check(org.w3c.dom.Node.DOCUMENT_POSITION_DISCONNECTED == 1, "DOCUMENT_POSITION_DISCONNECTED == 1");
        check(org.w3c.dom.Node.DOCUMENT_POSITION_PRECEDING == 2, "DOCUMENT_POSITION_PRECEDING == 2");
        check(org.w3c.dom.Node.DOCUMENT_POSITION_FOLLOWING == 4, "DOCUMENT_POSITION_FOLLOWING == 4");
        check(org.w3c.dom.Node.DOCUMENT_POSITION_CONTAINS == 8, "DOCUMENT_POSITION_CONTAINS == 8");
        check(org.w3c.dom.Node.DOCUMENT_POSITION_CONTAINED_BY == 16, "DOCUMENT_POSITION_CONTAINED_BY == 16");
        check(org.w3c.dom.Node.DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC == 32, "DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC == 32");
        org.w3c.dom.Node a = new org.w3c.dom.Node(){
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
        check(a != null, "anon impl of Node");
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

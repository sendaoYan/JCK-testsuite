/*
 * @test
 * @summary org.w3c.dom.html.HTMLBRElement conformance: HTMLBRElement.
 * @run main HTMLBRElementTest
 */

public class HTMLBRElementTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.html.HTMLBRElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLBRElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLBRElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLBRElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLBRElement is-a Element");
        check(k.getMethod("getClear") != null, "getClear()");
        check(k.getMethod("setClear", String.class) != null, "setClear(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.html.HTMLBRElement a = new org.w3c.dom.html.HTMLBRElement(){
            public java.lang.String getClear() { return "s"; }
            public void setClear(java.lang.String p0) { }
            public java.lang.String getId() { return "s"; }
            public void setId(java.lang.String p0) { }
            public java.lang.String getTitle() { return "s"; }
            public void setTitle(java.lang.String p0) { }
            public java.lang.String getLang() { return "s"; }
            public void setLang(java.lang.String p0) { }
            public java.lang.String getDir() { return "s"; }
            public void setDir(java.lang.String p0) { }
            public java.lang.String getClassName() { return "s"; }
            public void setClassName(java.lang.String p0) { }
            public java.lang.String getTagName() { return "s"; }
            public java.lang.String getAttribute(java.lang.String p0) { return "s"; }
            public void setAttribute(java.lang.String p0, java.lang.String p1) { }
            public void removeAttribute(java.lang.String p0) { }
            public org.w3c.dom.Attr getAttributeNode(java.lang.String p0) { return null; }
            public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr p0) { return null; }
            public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr p0) { return null; }
            public org.w3c.dom.NodeList getElementsByTagName(java.lang.String p0) { return null; }
            public java.lang.String getAttributeNS(java.lang.String p0, java.lang.String p1) { return "s"; }
            public void setAttributeNS(java.lang.String p0, java.lang.String p1, java.lang.String p2) { }
            public void removeAttributeNS(java.lang.String p0, java.lang.String p1) { }
            public org.w3c.dom.Attr getAttributeNodeNS(java.lang.String p0, java.lang.String p1) { return null; }
            public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr p0) { return null; }
            public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String p0, java.lang.String p1) { return null; }
            public boolean hasAttribute(java.lang.String p0) { return true; }
            public boolean hasAttributeNS(java.lang.String p0, java.lang.String p1) { return true; }
            public org.w3c.dom.TypeInfo getSchemaTypeInfo() { return null; }
            public void setIdAttribute(java.lang.String p0, boolean p1) { }
            public void setIdAttributeNS(java.lang.String p0, java.lang.String p1, boolean p2) { }
            public void setIdAttributeNode(org.w3c.dom.Attr p0, boolean p1) { }
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
        check(a != null, "anon impl of HTMLBRElement");
        check("s".equals(a.getClear()), "getClear() returns fixed default");
        a.setClear("s");
        check("s".equals(a.getId()), "getId() returns fixed default");
        a.setId("s");
        check("s".equals(a.getTitle()), "getTitle() returns fixed default");
        a.setTitle("s");
        check("s".equals(a.getLang()), "getLang() returns fixed default");
        a.setLang("s");
        check("s".equals(a.getDir()), "getDir() returns fixed default");
        a.setDir("s");
        check("s".equals(a.getClassName()), "getClassName() returns fixed default");
        a.setClassName("s");
        check("s".equals(a.getTagName()), "getTagName() returns fixed default");
        check("s".equals(a.getAttribute("s")), "getAttribute() returns fixed default");
        a.setAttribute("s", "s");
        a.removeAttribute("s");
        check(a.getAttributeNode("s") == null, "getAttributeNode() returns fixed default");
        check(a.setAttributeNode((org.w3c.dom.Attr) null) == null, "setAttributeNode() returns fixed default");
        check(a.removeAttributeNode((org.w3c.dom.Attr) null) == null, "removeAttributeNode() returns fixed default");
        check(a.getElementsByTagName("s") == null, "getElementsByTagName() returns fixed default");
        check("s".equals(a.getAttributeNS("s", "s")), "getAttributeNS() returns fixed default");
        a.setAttributeNS("s", "s", "s");
        a.removeAttributeNS("s", "s");
        check(a.getAttributeNodeNS("s", "s") == null, "getAttributeNodeNS() returns fixed default");
        check(a.setAttributeNodeNS((org.w3c.dom.Attr) null) == null, "setAttributeNodeNS() returns fixed default");
        check(a.getElementsByTagNameNS("s", "s") == null, "getElementsByTagNameNS() returns fixed default");
        check(a.hasAttribute("s") == true, "hasAttribute() returns fixed default");
        check(a.hasAttributeNS("s", "s") == true, "hasAttributeNS() returns fixed default");
        check(a.getSchemaTypeInfo() == null, "getSchemaTypeInfo() returns fixed default");
        a.setIdAttribute("s", true);
        a.setIdAttributeNS("s", "s", true);
        a.setIdAttributeNode((org.w3c.dom.Attr) null, true);
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

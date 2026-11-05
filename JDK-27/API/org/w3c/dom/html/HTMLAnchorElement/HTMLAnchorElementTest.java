/*
 * @test
 * @summary org.w3c.dom.html.HTMLAnchorElement conformance: HTMLAnchorElement.
 * @run main HTMLAnchorElementTest
 */

public class HTMLAnchorElementTest {
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

        Class<?> k = L("org.w3c.dom.html.HTMLAnchorElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLAnchorElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLAnchorElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLAnchorElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLAnchorElement is-a Element");
        check(k.getMethod("getHref") != null, "getHref()");
        check(k.getMethod("setHref", String.class) != null, "setHref(String)");
        check(k.getMethod("getTabIndex") != null, "getTabIndex()");
        check(k.getMethod("blur") != null, "blur()");
        check(k.getMethod("focus") != null, "focus()");
        check(k.getMethod("getAccessKey") != null, "getAccessKey()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.html.HTMLAnchorElement a = new org.w3c.dom.html.HTMLAnchorElement(){
            public java.lang.String getAccessKey() { return "s"; }
            public void setAccessKey(java.lang.String p0) { }
            public java.lang.String getCharset() { return "s"; }
            public void setCharset(java.lang.String p0) { }
            public java.lang.String getCoords() { return "s"; }
            public void setCoords(java.lang.String p0) { }
            public java.lang.String getHref() { return "s"; }
            public void setHref(java.lang.String p0) { }
            public java.lang.String getHreflang() { return "s"; }
            public void setHreflang(java.lang.String p0) { }
            public java.lang.String getName() { return "s"; }
            public void setName(java.lang.String p0) { }
            public java.lang.String getRel() { return "s"; }
            public void setRel(java.lang.String p0) { }
            public java.lang.String getRev() { return "s"; }
            public void setRev(java.lang.String p0) { }
            public java.lang.String getShape() { return "s"; }
            public void setShape(java.lang.String p0) { }
            public int getTabIndex() { return 3; }
            public void setTabIndex(int p0) { }
            public java.lang.String getTarget() { return "s"; }
            public void setTarget(java.lang.String p0) { }
            public java.lang.String getType() { return "s"; }
            public void setType(java.lang.String p0) { }
            public void blur() { }
            public void focus() { }
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
        check(a != null, "anon impl of HTMLAnchorElement");
        check("s".equals(a.getAccessKey()), "getAccessKey() returns fixed default");
        a.setAccessKey("s");
        check("s".equals(a.getCharset()), "getCharset() returns fixed default");
        a.setCharset("s");
        check("s".equals(a.getCoords()), "getCoords() returns fixed default");
        a.setCoords("s");
        check("s".equals(a.getHref()), "getHref() returns fixed default");
        a.setHref("s");
        check("s".equals(a.getHreflang()), "getHreflang() returns fixed default");
        a.setHreflang("s");
        check("s".equals(a.getName()), "getName() returns fixed default");
        a.setName("s");
        check("s".equals(a.getRel()), "getRel() returns fixed default");
        a.setRel("s");
        check("s".equals(a.getRev()), "getRev() returns fixed default");
        a.setRev("s");
        check("s".equals(a.getShape()), "getShape() returns fixed default");
        a.setShape("s");
        check(a.getTabIndex() == 3, "getTabIndex() returns fixed default");
        a.setTabIndex(3);
        check("s".equals(a.getTarget()), "getTarget() returns fixed default");
        a.setTarget("s");
        check("s".equals(a.getType()), "getType() returns fixed default");
        a.setType("s");
        a.blur();
        a.focus();
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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: HTML module shape-lock + producer absence ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("HTML", "2.0") && impl.getFeature("HTML", "2.0") == null, "HTML module unimplemented: hasFeature false, getFeature null");
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLAnchorElement.class), "HTMLAnchorElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLAnchorElement.class.isInterface(), "HTMLAnchorElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLAnchorElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 26, "HTMLAnchorElement declares exactly 26 methods");
        check(names.contains("blur"), "blur declared");
        check(names.contains("focus"), "focus declared");
        check(names.contains("getAccessKey"), "getAccessKey declared");
        check(names.contains("getCharset"), "getCharset declared");
        check(names.contains("getCoords"), "getCoords declared");
        check(names.contains("getHref"), "getHref declared");
        check(names.contains("getHreflang"), "getHreflang declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getRel"), "getRel declared");
        check(names.contains("getRev"), "getRev declared");
        check(names.contains("getShape"), "getShape declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getTarget"), "getTarget declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("setAccessKey"), "setAccessKey declared");
        check(names.contains("setCharset"), "setCharset declared");
        check(names.contains("setCoords"), "setCoords declared");
        check(names.contains("setHref"), "setHref declared");
        check(names.contains("setHreflang"), "setHreflang declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setRel"), "setRel declared");
        check(names.contains("setRev"), "setRev declared");
        check(names.contains("setShape"), "setShape declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setTarget"), "setTarget declared");
        check(names.contains("setType"), "setType declared");
    }
}

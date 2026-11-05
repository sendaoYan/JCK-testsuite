/*
 * @test
 * @summary org.w3c.dom.html.HTMLObjectElement conformance: HTMLObjectElement.
 * @run main HTMLObjectElementTest
 */

public class HTMLObjectElementTest {
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

        Class<?> k = L("org.w3c.dom.html.HTMLObjectElement");
        check(k.isInterface(), "org.w3c.dom.html.HTMLObjectElement is an interface");
        check(L("org.w3c.dom.html.HTMLElement").isAssignableFrom(k), "HTMLObjectElement extends HTMLElement");
        check(k.getInterfaces().length >= 1, "HTMLObjectElement has superinterface");
        check(L("org.w3c.dom.Element").isAssignableFrom(k), "HTMLObjectElement is-a Element");
        check(k.getMethod("getForm") != null, "getForm()");
        check(k.getMethod("getCode") != null, "getCode()");
        check(k.getMethod("setData", String.class) != null, "setData(String)");
        check(k.getMethod("getContentDocument") != null, "getContentDocument()");
        check(k.getMethod("getType") != null, "getType()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.html.HTMLObjectElement a = new org.w3c.dom.html.HTMLObjectElement(){
            public org.w3c.dom.html.HTMLFormElement getForm() { return null; }
            public java.lang.String getCode() { return "s"; }
            public void setCode(java.lang.String p0) { }
            public java.lang.String getAlign() { return "s"; }
            public void setAlign(java.lang.String p0) { }
            public java.lang.String getArchive() { return "s"; }
            public void setArchive(java.lang.String p0) { }
            public java.lang.String getBorder() { return "s"; }
            public void setBorder(java.lang.String p0) { }
            public java.lang.String getCodeBase() { return "s"; }
            public void setCodeBase(java.lang.String p0) { }
            public java.lang.String getCodeType() { return "s"; }
            public void setCodeType(java.lang.String p0) { }
            public java.lang.String getData() { return "s"; }
            public void setData(java.lang.String p0) { }
            public boolean getDeclare() { return true; }
            public void setDeclare(boolean p0) { }
            public java.lang.String getHeight() { return "s"; }
            public void setHeight(java.lang.String p0) { }
            public java.lang.String getHspace() { return "s"; }
            public void setHspace(java.lang.String p0) { }
            public java.lang.String getName() { return "s"; }
            public void setName(java.lang.String p0) { }
            public java.lang.String getStandby() { return "s"; }
            public void setStandby(java.lang.String p0) { }
            public int getTabIndex() { return 3; }
            public void setTabIndex(int p0) { }
            public java.lang.String getType() { return "s"; }
            public void setType(java.lang.String p0) { }
            public java.lang.String getUseMap() { return "s"; }
            public void setUseMap(java.lang.String p0) { }
            public java.lang.String getVspace() { return "s"; }
            public void setVspace(java.lang.String p0) { }
            public java.lang.String getWidth() { return "s"; }
            public void setWidth(java.lang.String p0) { }
            public org.w3c.dom.Document getContentDocument() { return null; }
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
        check(a != null, "anon impl of HTMLObjectElement");
        check(a.getForm() == null, "getForm() returns fixed default");
        check("s".equals(a.getCode()), "getCode() returns fixed default");
        a.setCode("s");
        check("s".equals(a.getAlign()), "getAlign() returns fixed default");
        a.setAlign("s");
        check("s".equals(a.getArchive()), "getArchive() returns fixed default");
        a.setArchive("s");
        check("s".equals(a.getBorder()), "getBorder() returns fixed default");
        a.setBorder("s");
        check("s".equals(a.getCodeBase()), "getCodeBase() returns fixed default");
        a.setCodeBase("s");
        check("s".equals(a.getCodeType()), "getCodeType() returns fixed default");
        a.setCodeType("s");
        check("s".equals(a.getData()), "getData() returns fixed default");
        a.setData("s");
        check(a.getDeclare() == true, "getDeclare() returns fixed default");
        a.setDeclare(true);
        check("s".equals(a.getHeight()), "getHeight() returns fixed default");
        a.setHeight("s");
        check("s".equals(a.getHspace()), "getHspace() returns fixed default");
        a.setHspace("s");
        check("s".equals(a.getName()), "getName() returns fixed default");
        a.setName("s");
        check("s".equals(a.getStandby()), "getStandby() returns fixed default");
        a.setStandby("s");
        check(a.getTabIndex() == 3, "getTabIndex() returns fixed default");
        a.setTabIndex(3);
        check("s".equals(a.getType()), "getType() returns fixed default");
        a.setType("s");
        check("s".equals(a.getUseMap()), "getUseMap() returns fixed default");
        a.setUseMap("s");
        check("s".equals(a.getVspace()), "getVspace() returns fixed default");
        a.setVspace("s");
        check("s".equals(a.getWidth()), "getWidth() returns fixed default");
        a.setWidth("s");
        check(a.getContentDocument() == null, "getContentDocument() returns fixed default");
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
        check(org.w3c.dom.html.HTMLElement.class.isAssignableFrom(org.w3c.dom.html.HTMLObjectElement.class), "HTMLObjectElement extends HTMLElement");
        check(org.w3c.dom.html.HTMLObjectElement.class.isInterface(), "HTMLObjectElement is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.w3c.dom.html.HTMLObjectElement.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 36, "HTMLObjectElement declares exactly 36 methods");
        check(names.contains("getAlign"), "getAlign declared");
        check(names.contains("getArchive"), "getArchive declared");
        check(names.contains("getBorder"), "getBorder declared");
        check(names.contains("getCode"), "getCode declared");
        check(names.contains("getCodeBase"), "getCodeBase declared");
        check(names.contains("getCodeType"), "getCodeType declared");
        check(names.contains("getContentDocument"), "getContentDocument declared");
        check(names.contains("getData"), "getData declared");
        check(names.contains("getDeclare"), "getDeclare declared");
        check(names.contains("getForm"), "getForm declared");
        check(names.contains("getHeight"), "getHeight declared");
        check(names.contains("getHspace"), "getHspace declared");
        check(names.contains("getName"), "getName declared");
        check(names.contains("getStandby"), "getStandby declared");
        check(names.contains("getTabIndex"), "getTabIndex declared");
        check(names.contains("getType"), "getType declared");
        check(names.contains("getUseMap"), "getUseMap declared");
        check(names.contains("getVspace"), "getVspace declared");
        check(names.contains("getWidth"), "getWidth declared");
        check(names.contains("setAlign"), "setAlign declared");
        check(names.contains("setArchive"), "setArchive declared");
        check(names.contains("setBorder"), "setBorder declared");
        check(names.contains("setCode"), "setCode declared");
        check(names.contains("setCodeBase"), "setCodeBase declared");
        check(names.contains("setCodeType"), "setCodeType declared");
        check(names.contains("setData"), "setData declared");
        check(names.contains("setDeclare"), "setDeclare declared");
        check(names.contains("setHeight"), "setHeight declared");
        check(names.contains("setHspace"), "setHspace declared");
        check(names.contains("setName"), "setName declared");
        check(names.contains("setStandby"), "setStandby declared");
        check(names.contains("setTabIndex"), "setTabIndex declared");
        check(names.contains("setType"), "setType declared");
        check(names.contains("setUseMap"), "setUseMap declared");
        check(names.contains("setVspace"), "setVspace declared");
        check(names.contains("setWidth"), "setWidth declared");
    }
}

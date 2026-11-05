/*
 * @test
 * @summary org.w3c.dom.DocumentType conformance: interface extends Node.
 * @run main DocumentTypeTest
 */

import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

public class DocumentTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentType.class.isInterface(), "DocumentType is an interface");
        check(Node.class.isAssignableFrom(DocumentType.class), "extends Node");
        check(DocumentType.class.getMethod("getName") != null, "getName()");
        check(DocumentType.class.getMethod("getEntities") != null, "getEntities()");
        check(DocumentType.class.getMethod("getNotations") != null, "getNotations()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DocumentType a = new org.w3c.dom.DocumentType(){
            public java.lang.String getName() { return "s"; }
            public org.w3c.dom.NamedNodeMap getEntities() { return null; }
            public org.w3c.dom.NamedNodeMap getNotations() { return null; }
            public java.lang.String getPublicId() { return "s"; }
            public java.lang.String getSystemId() { return "s"; }
            public java.lang.String getInternalSubset() { return "s"; }
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
        check(a != null, "anon impl of DocumentType");
        check("s".equals(a.getName()), "getName() returns fixed default");
        check(a.getEntities() == null, "getEntities() returns fixed default");
        check(a.getNotations() == null, "getNotations() returns fixed default");
        check("s".equals(a.getPublicId()), "getPublicId() returns fixed default");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check("s".equals(a.getInternalSubset()), "getInternalSubset() returns fixed default");
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

    // ---- deepening round 2: behavioral DocumentType contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DOMImplementation impl = d.getImplementation();
        // factory side
        org.w3c.dom.DocumentType cdt = impl.createDocumentType("q", "pub", "sys");
        check(org.w3c.dom.Node.DOCUMENT_TYPE_NODE == cdt.getNodeType(), "doctype nodeType 10");
        check("q".equals(cdt.getNodeName()) && "q".equals(cdt.getName()), "doctype nodeName == name");
        check("pub".equals(cdt.getPublicId()) && "sys".equals(cdt.getSystemId()), "createDocumentType publicId/systemId round-trip");
        check(cdt.getInternalSubset() == null, "factory doctype has no internal subset");
        check(cdt.getEntities().getLength() == 0 && cdt.getNotations().getLength() == 0, "factory doctype has empty entities/notations");
        check(cdt.getOwnerDocument() == null || cdt.getOwnerDocument() == d, "factory doctype ownerless until adopted");
        try { impl.createDocumentType("a b", null, null); check(false, "createDocumentType(illegal) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INVALID_CHARACTER_ERR, "createDocumentType('a b') -> INVALID_CHARACTER_ERR"); }
        try { impl.createDocumentType("", null, null); check(false, "createDocumentType('') must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NAMESPACE_ERR, "createDocumentType('') -> NAMESPACE_ERR (qualified-name rules)"); }
        boolean npe = false;
        try { impl.createDocumentType(null, null, null); } catch (NullPointerException expected) { npe = true; }
        check(npe, "createDocumentType(null) -> NullPointerException");
        // parsed side with public/system ids and internal subset
        String xml = "<!DOCTYPE r PUBLIC 'pub' 'sys' [<!ENTITY e1 'v1'>"
            + "<!ENTITY e2 SYSTEM 'e2.xml'>"
            + "<!ENTITY e3 SYSTEM 'e3.bin' NDATA gif>"
            + "<!NOTATION gif PUBLIC 'gp' 'gs'>]><r/>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.DocumentType dt = pd.getDoctype();
        check(dt != null && dt.getOwnerDocument() == pd, "parsed doctype owned by the document");
        check("r".equals(dt.getName()), "parsed doctype name is the document element name");
        check("pub".equals(dt.getPublicId()) && "sys".equals(dt.getSystemId()), "parsed doctype records external ids");
        check(dt.getInternalSubset() != null && dt.getInternalSubset().contains("<!ENTITY e1 'v1'>"), "internal subset preserves declarations verbatim");
        check(dt.getNodeValue() == null && dt.getTextContent() == null, "doctype nodeValue/textContent null");
        // readonly structure
        dt.setNodeValue("x");
        check(dt.getNodeValue() == null, "doctype setNodeValue is a silent no-op");
        try { dt.appendChild(pd.createElement("z")); check(false, "doctype appendChild must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "doctype appendChild -> NO_MODIFICATION_ALLOWED_ERR"); }
        // entities/notations maps are readonly views
        org.w3c.dom.NamedNodeMap ents = dt.getEntities();
        check(ents.getLength() == 3, "three declared entities");
        try { ents.removeNamedItem("e1"); check(false, "entities removeNamedItem must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entities map remove -> NO_MODIFICATION_ALLOWED_ERR"); }
        try { ents.setNamedItem(pd.createAttribute("x")); check(false, "entities setNamedItem must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "entities map set -> NO_MODIFICATION_ALLOWED_ERR"); }
        check(ents.getNamedItem("zz") == null, "entities getNamedItem(missing) null");
        org.w3c.dom.NamedNodeMap nots = dt.getNotations();
        check(nots.getLength() == 1 && nots.item(99) == null, "notations map length and out-of-bounds null");
        try { nots.removeNamedItem("gif"); check(false, "notations removeNamedItem must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR, "notations map remove -> NO_MODIFICATION_ALLOWED_ERR"); }
        // cross-document operations are unsupported
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        try { other.adoptNode(dt); check(false, "adoptNode(doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "adoptNode(doctype) -> NOT_SUPPORTED_ERR"); }
        try { other.importNode(dt, true); check(false, "importNode(doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "importNode(doctype) -> NOT_SUPPORTED_ERR"); }
        // createDocument cross-document doctype rule
        try { impl.createDocument(null, "z", dt); check(false, "createDocument(owned doctype) must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR, "createDocument(doctype owned elsewhere) -> WRONG_DOCUMENT_ERR"); }
        // createDocument happy path installs both doctype and root
        org.w3c.dom.DocumentType fresh = impl.createDocumentType("q", null, null);
        org.w3c.dom.Document made = impl.createDocument("urn:n", "n:doc", fresh);
        check(made.getChildNodes().getLength() == 2 && made.getDoctype() == fresh, "createDocument installs doctype as first child");
        check("urn:n".equals(made.getDocumentElement().getNamespaceURI()), "createDocument root namespace");
    }
}

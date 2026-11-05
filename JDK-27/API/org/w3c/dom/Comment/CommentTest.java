/*
 * @test
 * @summary org.w3c.dom.Comment conformance: interface extends CharacterData.
 * @run main CommentTest
 */

import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;

public class CommentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Comment.class.isInterface(), "Comment is an interface");
        check(CharacterData.class.isAssignableFrom(Comment.class),
              "extends CharacterData");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.Comment a = new org.w3c.dom.Comment(){
            public java.lang.String getData() { return "s"; }
            public void setData(java.lang.String p0) { }
            public int getLength() { return 3; }
            public java.lang.String substringData(int p0, int p1) { return "s"; }
            public void appendData(java.lang.String p0) { }
            public void insertData(int p0, java.lang.String p1) { }
            public void deleteData(int p0, int p1) { }
            public void replaceData(int p0, int p1, java.lang.String p2) { }
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
        check(a != null, "anon impl of Comment");
        check("s".equals(a.getData()), "getData() returns fixed default");
        a.setData("s");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check("s".equals(a.substringData(3, 3)), "substringData() returns fixed default");
        a.appendData("s");
        a.insertData(3, "s");
        a.deleteData(3, 3);
        a.replaceData(3, 3, "s");
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

    // ---- deepening round 2: behavioral Comment contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Comment c = d.createComment(" hi ");
        check("#comment".equals(c.getNodeName()), "comment nodeName #comment");
        check(org.w3c.dom.Node.COMMENT_NODE == c.getNodeType(), "comment nodeType 8");
        check(" hi ".equals(c.getData()), "comment preserves content verbatim incl. surrounding spaces");
        check(c.getLength() == 4, "comment getLength");
        check(c instanceof org.w3c.dom.CharacterData, "Comment is CharacterData");
        // Comment has no splitText (Text-only API)
        boolean absent = false;
        try { org.w3c.dom.Comment.class.getMethod("splitText", int.class); }
        catch (NoSuchMethodException expected) { absent = true; }
        check(absent, "Comment interface has no splitText");
        // CharacterData channels
        c.setData("n1");
        check("n1".equals(c.getNodeValue()), "comment setData syncs getNodeValue");
        c.setNodeValue("n2");
        check("n2".equals(c.getData()), "comment setNodeValue syncs getData");
        c.setTextContent("n3");
        check("n3".equals(c.getData()) && "n3".equals(c.getTextContent()), "comment setTextContent channel");
        c.appendData("!");
        check("n3!".equals(c.getData()), "comment appendData");
        c.insertData(0, ">");
        check(">n3!".equals(c.getData()), "comment insertData");
        c.deleteData(0, 1);
        check("n3!".equals(c.getData()), "comment deleteData");
        c.replaceData(0, 3, "z");
        check("z".equals(c.getData()), "comment replaceData");
        try { c.substringData(99, 1); check(false, "comment substringData OOB must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "comment substringData(99,1) -> INDEX_SIZE_ERR"); }
        // null handling
        org.w3c.dom.Comment n = d.createComment(null);
        check(n.getData() == null, "createComment(null) -> null data");
        boolean npe = false;
        try { n.getLength(); } catch (NullPointerException expected) { npe = true; }
        check(npe, "comment getLength after null -> NullPointerException");
        n.setData("z");
        check(n.getLength() == 1, "comment setData recovers");
        // structure: comment is a leaf
        check(c.getChildNodes().getLength() == 0 && c.getFirstChild() == null, "comment has no children");
        check(c.getAttributes() == null, "comment getAttributes null");
        check(c.getParentNode() == null, "factory comment has no parent");
        // placement: comments allowed anywhere, incl. document level before root
        org.w3c.dom.Element root = d.createElement("r");
        d.appendChild(root);
        root.appendChild(c);
        check(c.getParentNode() == root, "comment attaches inside element");
        org.w3c.dom.Comment docLevel = d.createComment("doc");
        d.insertBefore(docLevel, d.getDocumentElement());
        check(docLevel.getParentNode() == d && d.getChildNodes().getLength() == 2, "comment allowed at document level before root");
        d.removeChild(docLevel);
        check(d.getChildNodes().getLength() == 1, "comment removable from document");
        // equality and clone
        check(d.createComment("same").isEqualNode(d.createComment("same")), "comment isEqualNode same data");
        check(!d.createComment("a").isEqualNode(d.createComment("b")), "comment differs on data");
        check("same".equals(((org.w3c.dom.Comment) d.createComment("same").cloneNode(true)).getData()), "comment clone keeps data");
        // parse: comments survive parsing
        String xml = "<r><a/><!--cmt--></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Comment pc = (org.w3c.dom.Comment) pd.getDocumentElement().getChildNodes().item(1);
        check("cmt".equals(pc.getData()) && org.w3c.dom.Node.COMMENT_NODE == pc.getNodeType(), "parsed comment preserved");
        // textContent aggregation skips comments
        check("".equals(pd.getDocumentElement().getTextContent()), "comment invisible to textContent aggregation");
    }
}

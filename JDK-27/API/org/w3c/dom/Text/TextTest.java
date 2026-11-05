/*
 * @test
 * @summary org.w3c.dom.Text conformance: interface extends CharacterData.
 * @run main TextTest
 */

import org.w3c.dom.CharacterData;
import org.w3c.dom.Text;

public class TextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(Text.class.isInterface(), "Text is an interface");
        check(CharacterData.class.isAssignableFrom(Text.class), "extends CharacterData");
        check(Text.class.getMethod("splitText", int.class) != null, "splitText(int)");
        check(Text.class.getMethod("isElementContentWhitespace") != null,
              "isElementContentWhitespace()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.Text a = new org.w3c.dom.Text(){
            public org.w3c.dom.Text splitText(int p0) { return null; }
            public boolean isElementContentWhitespace() { return true; }
            public java.lang.String getWholeText() { return "s"; }
            public org.w3c.dom.Text replaceWholeText(java.lang.String p0) { return null; }
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
        check(a != null, "anon impl of Text");
        check(a.splitText(3) == null, "splitText() returns fixed default");
        check(a.isElementContentWhitespace() == true, "isElementContentWhitespace() returns fixed default");
        check("s".equals(a.getWholeText()), "getWholeText() returns fixed default");
        check(a.replaceWholeText("s") == null, "replaceWholeText() returns fixed default");
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

    // ---- deepening round 2: behavioral Text contract (splitText/wholeText/normalize) ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e = d.createElement("e");
        d.appendChild(e);
        // splitText mid-node
        org.w3c.dom.Text t = d.createTextNode("hello");
        e.appendChild(t);
        org.w3c.dom.Text tail = t.splitText(2);
        check("he".equals(t.getData()), "splitText keeps prefix in original");
        check("llo".equals(tail.getData()), "splitText moves suffix to new node");
        check(e.getChildNodes().getLength() == 2, "splitText grows parent child count");
        check(tail.getParentNode() == e, "new half attaches to same parent");
        check(tail.getPreviousSibling() == t && t.getNextSibling() == tail, "new half inserted directly after original");
        check(tail.getOwnerDocument() == d, "new half keeps owner document");
        // splitText at both extremes
        org.w3c.dom.Text t2 = d.createTextNode("abc");
        e.appendChild(t2);
        org.w3c.dom.Text emptyTail = t2.splitText(3);
        check("".equals(emptyTail.getData()) && "abc".equals(t2.getData()), "splitText(len) -> empty sibling");
        org.w3c.dom.Text t3 = d.createTextNode("xyz");
        e.appendChild(t3);
        org.w3c.dom.Text fullTail = t3.splitText(0);
        check("".equals(t3.getData()) && "xyz".equals(fullTail.getData()), "splitText(0) empties original, new holds all");
        // splitText bounds
        try { d.createTextNode("ab").splitText(99); check(false, "splitText offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "splitText(99) -> INDEX_SIZE_ERR"); }
        try { d.createTextNode("ab").splitText(-1); check(false, "splitText negative offset must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "splitText(-1) -> INDEX_SIZE_ERR"); }
        // parentless splitText still works
        org.w3c.dom.Text free = d.createTextNode("abcd");
        org.w3c.dom.Text freeTail = free.splitText(1);
        check("a".equals(free.getData()) && "bcd".equals(freeTail.getData()), "parentless splitText succeeds");
        check(freeTail.getParentNode() == null, "parentless split half has no parent");
        // getWholeText spans adjacent Text and CDATA, comments break adjacency
        org.w3c.dom.Document d2 = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e2 = d2.createElement("e2");
        d2.appendChild(e2);
        org.w3c.dom.Text a = d2.createTextNode("a");
        e2.appendChild(a);
        e2.appendChild(d2.createCDATASection("b"));
        e2.appendChild(d2.createTextNode("c"));
        check("abc".equals(a.getWholeText()), "getWholeText spans adjacent Text+CDATA");
        org.w3c.dom.Element e3 = d2.createElement("e3");
        e2.appendChild(e3);
        org.w3c.dom.Text a3 = d2.createTextNode("a");
        e3.appendChild(a3);
        e3.appendChild(d2.createComment("cmt"));
        e3.appendChild(d2.createTextNode("b"));
        check("a".equals(a3.getWholeText()), "comment breaks wholeText adjacency");
        // replaceWholeText merges siblings into one Text
        org.w3c.dom.Element e4 = d2.createElement("e4");
        e2.appendChild(e4);
        org.w3c.dom.Text m1 = d2.createTextNode("1");
        e4.appendChild(m1);
        e4.appendChild(d2.createCDATASection("2"));
        e4.appendChild(d2.createTextNode("3"));
        org.w3c.dom.Text merged = m1.replaceWholeText("N");
        check(merged == m1 && "N".equals(m1.getData()), "replaceWholeText returns this node with new data");
        check(e4.getChildNodes().getLength() == 1 && org.w3c.dom.Node.TEXT_NODE == e4.getFirstChild().getNodeType(), "replaceWholeText collapses to single Text");
        check("N".equals(e4.getTextContent()), "replaceWholeText visible via parent textContent");
        org.w3c.dom.Text gone = m1.replaceWholeText("");
        check(gone == null, "replaceWholeText('') returns null (Xerces)");
        check(e4.getChildNodes().getLength() == 0, "replaceWholeText('') detaches the node");
        check(m1.getParentNode() == null, "replaceWholeText('') clears parent link");
        org.w3c.dom.Element e5 = d2.createElement("e5");
        e2.appendChild(e5);
        org.w3c.dom.Text n5 = d2.createTextNode("s");
        e5.appendChild(n5);
        org.w3c.dom.Text goneNull = n5.replaceWholeText(null);
        check(goneNull == null && e5.getChildNodes().getLength() == 0, "replaceWholeText(null) removes node (Xerces)");
        // normalize merges adjacent Text only, CDATA survives
        org.w3c.dom.Element e6 = d2.createElement("e6");
        e2.appendChild(e6);
        e6.appendChild(d2.createTextNode("a"));
        e6.appendChild(d2.createCDATASection("m"));
        e6.appendChild(d2.createTextNode("b"));
        e6.appendChild(d2.createTextNode("c"));
        e6.normalize();
        check(e6.getChildNodes().getLength() == 3, "normalize keeps CDATA as separator");
        check("a".equals(e6.getChildNodes().item(0).getNodeValue())
            && org.w3c.dom.Node.CDATA_SECTION_NODE == e6.getChildNodes().item(1).getNodeType()
            && "bc".equals(e6.getChildNodes().item(2).getNodeValue()), "normalize merges Text runs around CDATA");
        // isElementContentWhitespace default false without validation
        check(!d2.createTextNode(" ").isElementContentWhitespace(), "unvalidated text isElementContentWhitespace false");
        // document.normalize cascades
        d2.normalize();
        check("bc".equals(e6.getChildNodes().item(2).getNodeValue()), "document.normalize is recursive");
        // Text basics
        org.w3c.dom.Text plain = d.createTextNode("t");
        check("#text".equals(plain.getNodeName()), "text nodeName #text");
        check(plain.getAttributes() == null, "Text.getAttributes null");
        check(plain.isEqualNode(d2.createTextNode("t")) && !plain.isEqualNode(d2.createTextNode("u")), "text isEqualNode compares data");
        plain.setNodeValue(null);
        check(plain.getData() == null, "setNodeValue(null) -> null data");
    }
}

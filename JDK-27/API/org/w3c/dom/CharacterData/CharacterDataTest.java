/*
 * @test
 * @summary org.w3c.dom.CharacterData conformance: interface extends Node.
 * @run main CharacterDataTest
 */

import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

public class CharacterDataTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(CharacterData.class.isInterface(), "CharacterData is an interface");
        check(Node.class.isAssignableFrom(CharacterData.class), "extends Node");
        check(CharacterData.class.getMethod("getData") != null, "getData()");
        check(CharacterData.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.CharacterData a = new org.w3c.dom.CharacterData(){
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
        check(a != null, "anon impl of CharacterData");
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

    // ---- deepening round 2: behavioral CharacterData contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Text t = d.createTextNode("hello");
        check(org.w3c.dom.Node.TEXT_NODE == t.getNodeType(), "text nodeType 3");
        check("hello".equals(t.getData()) && "hello".equals(t.getNodeValue()) && "hello".equals(t.getTextContent()), "initial data via getData/getNodeValue/getTextContent");
        check(t.getLength() == 5, "getLength counts UTF-16 units");
        // three write channels stay in sync
        t.setData("a1");
        check("a1".equals(t.getNodeValue()), "setData syncs getNodeValue");
        t.setNodeValue("a2");
        check("a2".equals(t.getData()), "setNodeValue syncs getData");
        t.setTextContent("a3");
        check("a3".equals(t.getData()), "setTextContent syncs getData");
        t.setData("a4");
        check("a4".equals(t.getTextContent()), "getData syncs getTextContent");
        // null handling: setData(null) stores null, later getLength NPEs (Xerces), setData recovers
        t.setData(null);
        check(t.getData() == null, "setData(null) leaves getData null");
        boolean npe = false;
        try { t.getLength(); } catch (NullPointerException expected) { npe = true; }
        check(npe, "getLength after setData(null) -> NullPointerException (both sides, message differs)");
        t.setData("ok");
        check(t.getLength() == 2, "setData recovers after null state");
        check(d.createTextNode(null).getData() == null, "createTextNode(null) -> null data");
        // appendData
        t.setData("a");
        t.appendData(null);
        check("a".equals(t.getData()), "appendData(null) is a no-op (Xerces)");
        t.appendData("XY");
        check("aXY".equals(t.getData()) && t.getLength() == 3, "appendData grows data and length");
        // substringData matrix
        t.setData("hello");
        check("hello".equals(t.substringData(0, 5)), "substringData(0,len) whole string");
        check("ell".equals(t.substringData(1, 3)), "substringData middle");
        check("o".equals(t.substringData(4, 1)), "substringData last char");
        check("o".equals(t.substringData(4, 2)), "substringData clamps count past end (offset+count>len allowed)");
        check("hello".equals(t.substringData(0, 6)), "substringData(0,len+1) clamps");
        check("llo".equals(t.substringData(2, 3)), "substringData tail");
        try { t.substringData(6, 1); check(false, "substringData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(6,1) -> INDEX_SIZE_ERR"); }
        try { t.substringData(5, 0); check(false, "substringData offset==len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(len,0) -> INDEX_SIZE_ERR (offset==len rejected)"); }
        try { t.substringData(0, -1); check(false, "substringData negative count must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(0,-1) -> INDEX_SIZE_ERR"); }
        try { t.substringData(-1, 2); check(false, "substringData negative offset must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "substringData(-1,2) -> INDEX_SIZE_ERR"); }
        // insertData: offset==len appends, offset>len throws
        t.insertData(5, "!");
        check("hello!".equals(t.getData()), "insertData(len,..) appends");
        t.setData("hello");
        try { t.insertData(6, "x"); check(false, "insertData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "insertData(6,..) -> INDEX_SIZE_ERR"); }
        t.insertData(2, "XY");
        check("heXYllo".equals(t.getData()), "insertData middle");
        // deleteData: clamps count, offset==len no-op, offset>len throws
        t.deleteData(3, 100);
        check("heX".equals(t.getData()), "deleteData clamps count past end");
        t.setData("hello");
        t.deleteData(1, 2);
        check("hlo".equals(t.getData()), "deleteData middle");
        t.setData("hello");
        t.deleteData(5, 0);
        check("hello".equals(t.getData()), "deleteData(len,0) no-op allowed");
        try { t.deleteData(6, 0); check(false, "deleteData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "deleteData(6,0) -> INDEX_SIZE_ERR"); }
        // replaceData: grows/shrinks, offset==len appends
        t.setData("hello");
        t.replaceData(0, 2, "J");
        check("Jllo".equals(t.getData()), "replaceData shrink");
        t.setData("hello");
        t.replaceData(5, 0, "X");
        check("helloX".equals(t.getData()), "replaceData(len,0,..) appends");
        try { t.replaceData(7, 1, "x"); check(false, "replaceData offset>len must throw"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.INDEX_SIZE_ERR, "replaceData(7,1,..) -> INDEX_SIZE_ERR"); }
        // mutation stays visible through the parent's textContent
        org.w3c.dom.Element host = d.createElement("h");
        d.appendChild(host);
        org.w3c.dom.Text ht = d.createTextNode("ab");
        host.appendChild(ht);
        ht.appendData("c");
        check("abc".equals(host.getTextContent()), "character-data edits visible via parent textContent");
        check(host.getChildNodes().getLength() == 1, "data edits never change child count");
        // Comment also exposes CharacterData channels
        org.w3c.dom.Comment c = d.createComment("c");
        check("#comment".equals(c.getNodeName()) && org.w3c.dom.Node.COMMENT_NODE == c.getNodeType(), "comment node identity");
        c.setTextContent("d");
        check("d".equals(c.getData()), "comment setTextContent syncs getData");
        c.setNodeValue("n");
        check("n".equals(c.getData()), "comment setNodeValue syncs getData");
        org.w3c.dom.Comment cn = d.createComment(null);
        check(cn.getData() == null, "createComment(null) -> null data");
        boolean cnpe = false;
        try { cn.getLength(); } catch (NullPointerException expected) { cnpe = true; }
        check(cnpe, "comment getLength after null data -> NullPointerException");
        cn.setData("z");
        check(cn.getLength() == 1, "comment setData recovers length");
    }
}

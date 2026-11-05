/*
 * @test
 * @summary org.w3c.dom.CDATASection conformance: interface extends Text.
 * @run main CDATASectionTest
 */

import org.w3c.dom.CDATASection;
import org.w3c.dom.Text;

public class CDATASectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(CDATASection.class.isInterface(), "CDATASection is an interface");
        check(Text.class.isAssignableFrom(CDATASection.class), "extends Text");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.CDATASection a = new org.w3c.dom.CDATASection(){
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
        check(a != null, "anon impl of CDATASection");
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

    // ---- deepening round 2: behavioral CDATASection contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setCoalescing(false);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.CDATASection c = d.createCDATASection("xy");
        check("#cdata-section".equals(c.getNodeName()), "cdata nodeName #cdata-section");
        check(org.w3c.dom.Node.CDATA_SECTION_NODE == c.getNodeType(), "cdata nodeType 4");
        check(c instanceof org.w3c.dom.Text, "CDATASection is a Text");
        check("xy".equals(c.getData()) && "xy".equals(c.getNodeValue()) && "xy".equals(c.getTextContent()), "cdata value channels agree");
        check(c.getLength() == 2, "cdata getLength");
        // CharacterData interface fully inherited
        c.setData("abc");
        c.appendData("d");
        check("abcd".equals(c.getData()), "cdata appendData inherited");
        check("bc".equals(c.substringData(1, 2)), "cdata substringData inherited");
        c.replaceData(0, 4, "z");
        check("z".equals(c.getData()), "cdata replaceData inherited");
        // splitText on CDATA: the new half is a plain Text node
        org.w3c.dom.Element e = d.createElement("e");
        d.appendChild(e);
        org.w3c.dom.CDATASection sc = d.createCDATASection("xy");
        e.appendChild(sc);
        org.w3c.dom.Node newHalf = sc.splitText(1);
        check("x".equals(sc.getData()) && "y".equals(newHalf.getNodeValue()), "cdata splitText halves data");
        check(org.w3c.dom.Node.TEXT_NODE == newHalf.getNodeType() && "#text".equals(newHalf.getNodeName()), "cdata splitText produces a plain Text sibling (not CDATA)");
        org.w3c.dom.CDATASection sc2 = d.createCDATASection("m");
        e.appendChild(sc2);
        org.w3c.dom.Node all = sc2.splitText(0);
        check("".equals(sc2.getData()) && "m".equals(all.getNodeValue()) && org.w3c.dom.Node.TEXT_NODE == all.getNodeType(), "cdata splitText(0) moves all into new Text");
        // wholeText family works through the Text view
        org.w3c.dom.Element e2 = d.createElement("e2");
        e.appendChild(e2);
        org.w3c.dom.CDATASection w = d.createCDATASection("m");
        e2.appendChild(w);
        check("m".equals(w.getWholeText()), "cdata getWholeText returns its data");
        org.w3c.dom.Text merged = w.replaceWholeText("N");
        check(e2.getChildNodes().getLength() == 1 && merged == w, "cdata replaceWholeText collapses to the same single node");
        check(org.w3c.dom.Node.CDATA_SECTION_NODE == merged.getNodeType() && "N".equals(merged.getData()), "cdata replaceWholeText keeps CDATA type and sets data");
        // adjacency in wholeText scanning
        org.w3c.dom.Element e3 = d.createElement("e3");
        e.appendChild(e3);
        org.w3c.dom.Text lead = d.createTextNode("a");
        e3.appendChild(lead);
        e3.appendChild(d.createCDATASection("b"));
        e3.appendChild(d.createTextNode("c"));
        check("abc".equals(lead.getWholeText()), "cdata participates in wholeText span");
        // ']]>' is storable in the DOM model (serialization handled elsewhere)
        org.w3c.dom.CDATASection term = d.createCDATASection("a]]>b");
        check("a]]>b".equals(term.getData()), "createCDATASection accepts ']]>' content");
        // parse: CDATA content is not entity- or markup-escaped
        String xml = "<!DOCTYPE r [<!ENTITY ent \"E\">]><r><t>A<![CDATA[x < y & z]]>B&amp;C&ent;</t><e><![CDATA[]]></e></r>";
        org.w3c.dom.Document pd = f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        org.w3c.dom.Element t2 = (org.w3c.dom.Element) pd.getElementsByTagName("t").item(0);
        check("Ax < y & zB&CE".equals(t2.getTextContent()), "cdata content stays raw while surroundings expand");
        org.w3c.dom.CDATASection pcd = (org.w3c.dom.CDATASection) t2.getChildNodes().item(1);
        check("x < y & z".equals(pcd.getData()), "parsed cdata data unescaped");
        org.w3c.dom.Element e4 = (org.w3c.dom.Element) pd.getElementsByTagName("e").item(0);
        org.w3c.dom.CDATASection empty = (org.w3c.dom.CDATASection) e4.getFirstChild();
        check(empty.getLength() == 0 && "".equals(empty.getData()), "empty cdata section parses to zero length");
        // equality and clone
        org.w3c.dom.CDATASection c1 = d.createCDATASection("x");
        org.w3c.dom.CDATASection c2 = d.createCDATASection("x");
        check(c1.isEqualNode(c2), "cdata isEqualNode same data");
        check(!c1.isEqualNode(d.createTextNode("x")), "cdata never equals a Text node");
        check("x".equals(((org.w3c.dom.CDATASection) c1.cloneNode(true)).getData()), "cdata clone keeps data");
        check(c.getAttributes() == null, "cdata getAttributes null");
    }
}

/*
 * @test
 * @summary org.w3c.dom.NamedNodeMap conformance: interface and methods.
 * @run main NamedNodeMapTest
 */

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NamedNodeMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NamedNodeMap.class.isInterface(), "NamedNodeMap is an interface");
        check(NamedNodeMap.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(NamedNodeMap.class.getMethod("item", int.class).getReturnType() == Node.class,
              "item(int) returns Node");
        check(NamedNodeMap.class.getMethod("getNamedItem", String.class) != null,
              "getNamedItem(String)");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.NamedNodeMap a = new org.w3c.dom.NamedNodeMap(){
            public org.w3c.dom.Node getNamedItem(java.lang.String p0) { return null; }
            public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node p0) { return null; }
            public org.w3c.dom.Node removeNamedItem(java.lang.String p0) { return null; }
            public org.w3c.dom.Node item(int p0) { return null; }
            public int getLength() { return 3; }
            public org.w3c.dom.Node getNamedItemNS(java.lang.String p0, java.lang.String p1) { return null; }
            public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node p0) { return null; }
            public org.w3c.dom.Node removeNamedItemNS(java.lang.String p0, java.lang.String p1) { return null; }
        };
        check(a != null, "anon impl of NamedNodeMap");
        check(a.getNamedItem("s") == null, "getNamedItem() returns fixed default");
        check(a.setNamedItem((org.w3c.dom.Node) null) == null, "setNamedItem() returns fixed default");
        check(a.removeNamedItem("s") == null, "removeNamedItem() returns fixed default");
        check(a.item(3) == null, "item() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.getNamedItemNS("s", "s") == null, "getNamedItemNS() returns fixed default");
        check(a.setNamedItemNS((org.w3c.dom.Node) null) == null, "setNamedItemNS() returns fixed default");
        check(a.removeNamedItemNS("s", "s") == null, "removeNamedItemNS() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }



    // ---- deepening round 2: behavioral NamedNodeMap contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.Element e = d.createElement("e");
        e.setAttribute("a", "1");
        e.setAttribute("b", "2");
        org.w3c.dom.NamedNodeMap m = e.getAttributes();
        org.w3c.dom.NamedNodeMap mAgain = e.getAttributes();
        check(m == mAgain, "getAttributes returns the same instance (cached)");
        check(m.getLength() == 2, "initial attribute count");
        // live view
        e.setAttribute("c", "3");
        check(m.getLength() == 3, "map is live: setAttribute grows it");
        e.removeAttribute("c");
        check(m.getLength() == 2, "map is live: removeAttribute shrinks it");
        // lookup contract
        check(m.getNamedItem("a") != null && "1".equals(m.getNamedItem("a").getNodeValue()), "getNamedItem by name");
        check(m.getNamedItem("zz") == null, "getNamedItem(missing) null");
        check(m.getNamedItemNS("urn:none", "q") == null, "getNamedItemNS(missing) null");
        check(m.item(0).getNodeName().equals("a") && m.item(1).getNodeName().equals("b"), "iteration order is insertion order");
        check(m.item(2) == null, "item(past end) null");
        try {
            m.item(-1);
            check(false, "item(negative) must throw on Xerces NamedNodeMap");
        } catch (IndexOutOfBoundsException expected) {
            check(true, "item(-1) -> IndexOutOfBoundsException (AIOOBE on JDK 8, IOOBE on JDK 27)");
        }
        // setNamedItem: returns replaced node, or null
        org.w3c.dom.Attr repl = d.createAttribute("a");
        repl.setValue("one");
        org.w3c.dom.Node prev = m.setNamedItem(repl);
        check(prev != null && "1".equals(prev.getNodeValue()), "setNamedItem returns previous node");
        check("one".equals(e.getAttribute("a")), "setNamedItem replacement visible via element");
        check(m.getLength() == 2, "setNamedItem same-name does not grow map");
        check(((org.w3c.dom.Attr) prev).getOwnerElement() == null, "replaced attr detached from element");
        org.w3c.dom.Attr fresh = d.createAttribute("newk");
        fresh.setValue("nv");
        check(m.setNamedItem(fresh) == null, "setNamedItem(new name) returns null");
        check(m.getLength() == 3, "setNamedItem grows map");
        check(m.item(2).getNodeName().equals("newk"), "appended attr ordered last");
        // ns variants
        org.w3c.dom.Attr nsAttr = d.createAttributeNS("urn:p", "q:nsa");
        nsAttr.setValue("v");
        m.setNamedItemNS(nsAttr);
        check(m.getNamedItemNS("urn:p", "nsa") == nsAttr, "setNamedItemNS/getNamedItemNS round-trip");
        check(m.getLength() == 4, "ns attr counted");
        // removals
        org.w3c.dom.Node rm = m.removeNamedItem("b");
        check(rm.getNodeName().equals("b"), "removeNamedItem returns removed");
        check(m.getLength() == 3, "removeNamedItem shrinks");
        org.w3c.dom.Document other = f.newDocumentBuilder().newDocument();
        try {
            m.removeNamedItem("b");
            check(false, "removeNamedItem(missing) must throw");
        } catch (org.w3c.dom.DOMException ex) {
            check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "removeNamedItem(missing) -> NOT_FOUND_ERR");
        }
        try {
            m.removeNamedItemNS("urn:none", "zz");
            check(false, "removeNamedItemNS(missing) must throw");
        } catch (org.w3c.dom.DOMException ex) {
            check(ex.code == org.w3c.dom.DOMException.NOT_FOUND_ERR, "removeNamedItemNS(missing) -> NOT_FOUND_ERR");
        }
        try {
            m.setNamedItem(other.createAttribute("foreign"));
            check(false, "setNamedItem(foreign) must throw");
        } catch (org.w3c.dom.DOMException ex) {
            check(ex.code == org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR, "setNamedItem(foreign attr) -> WRONG_DOCUMENT_ERR");
        }
        // attributes of a text node are null (not a map)
        check(d.createTextNode("t").getAttributes() == null, "Text.getAttributes null");
        // removal keeps element semantics in sync
        check(e.getAttribute("b").isEmpty(), "removed attr invisible via getAttribute");
        check(e.hasAttribute("a") && !e.hasAttribute("b"), "hasAttribute reflects map removal");
        // item() over a fresh clone is independent
        org.w3c.dom.NamedNodeMap cm = e.cloneNode(true).getAttributes();
        check(cm != m, "clone gets its own map");
        check(cm.getLength() == m.getLength(), "clone map mirrors attribute count");
    }

}

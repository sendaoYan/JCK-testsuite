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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

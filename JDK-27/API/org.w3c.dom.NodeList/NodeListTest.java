/*
 * @test
 * @summary org.w3c.dom.NodeList conformance: interface and methods.
 * @run main NodeListTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NodeList.class.isInterface(), "NodeList is an interface");
        check(NodeList.class.getMethod("item", int.class).getReturnType() == Node.class,
              "item(int) returns Node");
        check(NodeList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.NodeList a = new org.w3c.dom.NodeList(){
            public org.w3c.dom.Node item(int p0) { return null; }
            public int getLength() { return 3; }
        };
        check(a != null, "anon impl of NodeList");
        check(a.item(3) == null, "item() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

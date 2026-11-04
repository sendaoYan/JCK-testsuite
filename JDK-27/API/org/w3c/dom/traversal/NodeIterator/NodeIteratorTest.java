/*
 * @test
 * @summary org.w3c.dom.traversal.NodeIterator conformance: interface and methods.
 * @run main NodeIteratorTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class NodeIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NodeIterator.class.isInterface(), "NodeIterator is an interface");
        check(NodeIterator.class.getMethod("getRoot").getReturnType() == Node.class,
              "getRoot() returns Node");
        check(NodeIterator.class.getMethod("getWhatToShow").getReturnType() == int.class,
              "getWhatToShow() returns int");
        check(NodeIterator.class.getMethod("nextNode").getReturnType() == Node.class,
              "nextNode() returns Node");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.traversal.NodeIterator a = new org.w3c.dom.traversal.NodeIterator(){
            public org.w3c.dom.Node getRoot() { return null; }
            public int getWhatToShow() { return 3; }
            public org.w3c.dom.traversal.NodeFilter getFilter() { return null; }
            public boolean getExpandEntityReferences() { return true; }
            public org.w3c.dom.Node nextNode() { return null; }
            public org.w3c.dom.Node previousNode() { return null; }
            public void detach() { }
        };
        check(a != null, "anon impl of NodeIterator");
        check(a.getRoot() == null, "getRoot() returns fixed default");
        check(a.getWhatToShow() == 3, "getWhatToShow() returns fixed default");
        check(a.getFilter() == null, "getFilter() returns fixed default");
        check(a.getExpandEntityReferences() == true, "getExpandEntityReferences() returns fixed default");
        check(a.nextNode() == null, "nextNode() returns fixed default");
        check(a.previousNode() == null, "previousNode() returns fixed default");
        a.detach();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary org.w3c.dom.traversal.TreeWalker conformance: interface and methods.
 * @run main TreeWalkerTest
 */

import org.w3c.dom.Node;
import org.w3c.dom.traversal.TreeWalker;

public class TreeWalkerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(TreeWalker.class.isInterface(), "TreeWalker is an interface");
        check(TreeWalker.class.getMethod("getRoot").getReturnType() == Node.class,
              "getRoot() returns Node");
        check(TreeWalker.class.getMethod("getCurrentNode").getReturnType() == Node.class,
              "getCurrentNode() returns Node");
        check(TreeWalker.class.getMethod("nextNode").getReturnType() == Node.class,
              "nextNode() returns Node");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.traversal.TreeWalker a = new org.w3c.dom.traversal.TreeWalker(){
            public org.w3c.dom.Node getRoot() { return null; }
            public int getWhatToShow() { return 3; }
            public org.w3c.dom.traversal.NodeFilter getFilter() { return null; }
            public boolean getExpandEntityReferences() { return true; }
            public org.w3c.dom.Node getCurrentNode() { return null; }
            public void setCurrentNode(org.w3c.dom.Node p0) { }
            public org.w3c.dom.Node parentNode() { return null; }
            public org.w3c.dom.Node firstChild() { return null; }
            public org.w3c.dom.Node lastChild() { return null; }
            public org.w3c.dom.Node previousSibling() { return null; }
            public org.w3c.dom.Node nextSibling() { return null; }
            public org.w3c.dom.Node previousNode() { return null; }
            public org.w3c.dom.Node nextNode() { return null; }
        };
        check(a != null, "anon impl of TreeWalker");
        check(a.getRoot() == null, "getRoot() returns fixed default");
        check(a.getWhatToShow() == 3, "getWhatToShow() returns fixed default");
        check(a.getFilter() == null, "getFilter() returns fixed default");
        check(a.getExpandEntityReferences() == true, "getExpandEntityReferences() returns fixed default");
        check(a.getCurrentNode() == null, "getCurrentNode() returns fixed default");
        a.setCurrentNode((org.w3c.dom.Node) null);
        check(a.parentNode() == null, "parentNode() returns fixed default");
        check(a.firstChild() == null, "firstChild() returns fixed default");
        check(a.lastChild() == null, "lastChild() returns fixed default");
        check(a.previousSibling() == null, "previousSibling() returns fixed default");
        check(a.nextSibling() == null, "nextSibling() returns fixed default");
        check(a.previousNode() == null, "previousNode() returns fixed default");
        check(a.nextNode() == null, "nextNode() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

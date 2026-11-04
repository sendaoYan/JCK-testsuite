/*
 * @test
 * @summary org.w3c.dom.traversal.DocumentTraversal conformance: interface and methods.
 * @run main DocumentTraversalTest
 */

import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

public class DocumentTraversalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentTraversal.class.isInterface(), "DocumentTraversal is an interface");
        check(DocumentTraversal.class.getMethod("createNodeIterator",
                org.w3c.dom.Node.class, int.class, org.w3c.dom.traversal.NodeFilter.class,
                boolean.class).getReturnType() == NodeIterator.class,
              "createNodeIterator(...) returns NodeIterator");
        check(DocumentTraversal.class.getMethod("createTreeWalker",
                org.w3c.dom.Node.class, int.class, org.w3c.dom.traversal.NodeFilter.class,
                boolean.class).getReturnType() == TreeWalker.class,
              "createTreeWalker(...) returns TreeWalker");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.traversal.DocumentTraversal a = new org.w3c.dom.traversal.DocumentTraversal(){
            public org.w3c.dom.traversal.NodeIterator createNodeIterator(org.w3c.dom.Node p0, int p1, org.w3c.dom.traversal.NodeFilter p2, boolean p3) { return null; }
            public org.w3c.dom.traversal.TreeWalker createTreeWalker(org.w3c.dom.Node p0, int p1, org.w3c.dom.traversal.NodeFilter p2, boolean p3) { return null; }
        };
        check(a != null, "anon impl of DocumentTraversal");
        check(a.createNodeIterator((org.w3c.dom.Node) null, 3, (org.w3c.dom.traversal.NodeFilter) null, true) == null, "createNodeIterator() returns fixed default");
        check(a.createTreeWalker((org.w3c.dom.Node) null, 3, (org.w3c.dom.traversal.NodeFilter) null, true) == null, "createTreeWalker() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

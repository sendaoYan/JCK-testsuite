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

    public static void main(String[] args) throws Exception {
        check(DocumentTraversal.class.isInterface(), "DocumentTraversal is an interface");
        check(DocumentTraversal.class.getMethod("createNodeIterator",
                org.w3c.dom.Node.class, int.class, org.w3c.dom.traversal.NodeFilter.class,
                boolean.class).getReturnType() == NodeIterator.class,
              "createNodeIterator(...) returns NodeIterator");
        check(DocumentTraversal.class.getMethod("createTreeWalker",
                org.w3c.dom.Node.class, int.class, org.w3c.dom.traversal.NodeFilter.class,
                boolean.class).getReturnType() == TreeWalker.class,
              "createTreeWalker(...) returns TreeWalker");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

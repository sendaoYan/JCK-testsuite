/*
 * @test
 * @summary org.w3c.dom.traversal.NodeFilter conformance: interface, constants, acceptNode.
 * @run main NodeFilterTest
 */

import org.w3c.dom.traversal.NodeFilter;

public class NodeFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(NodeFilter.class.isInterface(), "NodeFilter is an interface");
        check(NodeFilter.FILTER_ACCEPT == 1, "FILTER_ACCEPT == 1");
        check(NodeFilter.FILTER_REJECT == 2, "FILTER_REJECT == 2");
        check(NodeFilter.FILTER_SKIP == 3, "FILTER_SKIP == 3");
        check(NodeFilter.class.getMethod("acceptNode", org.w3c.dom.Node.class)
                .getReturnType() == short.class, "acceptNode(Node) returns short");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

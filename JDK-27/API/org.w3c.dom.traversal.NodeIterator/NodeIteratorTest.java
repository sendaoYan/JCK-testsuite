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

    public static void main(String[] args) throws Exception {
        check(NodeIterator.class.isInterface(), "NodeIterator is an interface");
        check(NodeIterator.class.getMethod("getRoot").getReturnType() == Node.class,
              "getRoot() returns Node");
        check(NodeIterator.class.getMethod("getWhatToShow").getReturnType() == int.class,
              "getWhatToShow() returns int");
        check(NodeIterator.class.getMethod("nextNode").getReturnType() == Node.class,
              "nextNode() returns Node");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

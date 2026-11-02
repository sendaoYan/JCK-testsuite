/*
 * @test
 * @summary org.w3c.dom.Node conformance: interface, node-type constants, methods.
 * @run main NodeTest
 */

import org.w3c.dom.Node;

public class NodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(Node.class.isInterface(), "Node is an interface");

        check(Node.ELEMENT_NODE == 1, "ELEMENT_NODE == 1");
        check(Node.ATTRIBUTE_NODE == 2, "ATTRIBUTE_NODE == 2");
        check(Node.TEXT_NODE == 3, "TEXT_NODE == 3");
        check(Node.CDATA_SECTION_NODE == 4, "CDATA_SECTION_NODE == 4");
        check(Node.DOCUMENT_NODE == 9, "DOCUMENT_NODE == 9");
        check(Node.NOTATION_NODE == 12, "NOTATION_NODE == 12");

        check(Node.class.getMethod("getNodeName") != null, "getNodeName()");
        check(Node.class.getMethod("getNodeType") != null, "getNodeType()");
        check(Node.class.getMethod("getParentNode") != null, "getParentNode()");
        check(Node.class.getMethod("appendChild", Node.class) != null,
              "appendChild(Node)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    public static void main(String[] args) throws Exception {
        check(NodeList.class.isInterface(), "NodeList is an interface");
        check(NodeList.class.getMethod("item", int.class).getReturnType() == Node.class,
              "item(int) returns Node");
        check(NodeList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

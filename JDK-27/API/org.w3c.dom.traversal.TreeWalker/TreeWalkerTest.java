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

    public static void main(String[] args) throws Exception {
        check(TreeWalker.class.isInterface(), "TreeWalker is an interface");
        check(TreeWalker.class.getMethod("getRoot").getReturnType() == Node.class,
              "getRoot() returns Node");
        check(TreeWalker.class.getMethod("getCurrentNode").getReturnType() == Node.class,
              "getCurrentNode() returns Node");
        check(TreeWalker.class.getMethod("nextNode").getReturnType() == Node.class,
              "nextNode() returns Node");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.tree.DefaultMutableTreeNode conformance: tree nodes (headless-safe).
 * @run main DefaultMutableTreeNodeTest
 */

import javax.swing.tree.DefaultMutableTreeNode;

public class DefaultMutableTreeNodeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode("root");
        check(n.isRoot() && n.getChildCount() == 0, "root");
        check("root".equals(n.getUserObject()), "user object");
        n.add(new DefaultMutableTreeNode("child"));
        check(n.getChildCount() == 1, "child");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

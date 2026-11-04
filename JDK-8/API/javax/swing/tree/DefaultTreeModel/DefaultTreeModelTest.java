/*
 * @test
 * @summary javax.swing.tree.DefaultTreeModel conformance: tree model (headless-safe).
 * @run main DefaultTreeModelTest
 */

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

public class DefaultTreeModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("r");
        DefaultTreeModel m = new DefaultTreeModel(root);
        check(m.getRoot() == root, "root");
        check(m.getChildCount(root) == 0, "child count");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

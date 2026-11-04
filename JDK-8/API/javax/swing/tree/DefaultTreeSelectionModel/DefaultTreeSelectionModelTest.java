/*
 * @test
 * @summary javax.swing.tree.DefaultTreeSelectionModel conformance: selection (headless-safe).
 * @run main DefaultTreeSelectionModelTest
 */

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;

public class DefaultTreeSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        check(m.isSelectionEmpty(), "empty");
        m.setSelectionPath(new TreePath(new DefaultMutableTreeNode("r")));
        check(!m.isSelectionEmpty(), "selected");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

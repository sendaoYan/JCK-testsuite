/*
 * @test
 * @summary javax.swing.tree.TreeNode conformance: white-box structure + black-box behavior.
 * @run main TreeNodeTest
 */

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class TreeNodeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeNode");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final TreeNode child = new TreeNode() {
            public TreeNode getChildAt(int i) { throw new ArrayIndexOutOfBoundsException(); }
            public int getChildCount() { return 0; }
            public TreeNode getParent() { return null; }
            public int getIndex(TreeNode n) { return -1; }
            public boolean getAllowsChildren() { return false; }
            public boolean isLeaf() { return true; }
            public Enumeration<TreeNode> children() { return Collections.emptyEnumeration(); }
        };

        final TreeNode parent = new TreeNode() {
            public TreeNode getChildAt(int i) { return i == 0 ? child : null; }
            public int getChildCount() { return 1; }
            public TreeNode getParent() { return null; }
            public int getIndex(TreeNode n) { return (n == child) ? 0 : -1; }
            public boolean getAllowsChildren() { return true; }
            public boolean isLeaf() { return false; }
            public Enumeration<TreeNode> children() {
                return Collections.enumeration(Collections.singletonList(child));
            }
        };

        // Exercise every TreeNode method on the parent.
        check(parent.getChildCount() == 1, "parent getChildCount");
        check(parent.getChildAt(0) == child, "parent getChildAt");
        check(parent.getIndex(child) == 0, "parent getIndex child");
        check(parent.getIndex(parent) == -1, "parent getIndex non-child");
        check(parent.getParent() == null, "parent getParent");
        check(parent.getAllowsChildren() == true, "parent getAllowsChildren");
        check(parent.isLeaf() == false, "parent isLeaf");
        Enumeration<? extends TreeNode> kids = parent.children();
        check(kids.hasMoreElements() && kids.nextElement() == child, "parent children()");

        // Exercise every TreeNode method on the child (leaf).
        check(child.getChildCount() == 0, "child getChildCount");
        check(child.getParent() == null, "child getParent");
        check(child.getIndex(child) == -1, "child getIndex");
        check(child.getAllowsChildren() == false, "child getAllowsChildren");
        check(child.isLeaf() == true, "child isLeaf");
        check(!child.children().hasMoreElements(), "child children() empty");

        // DefaultMutableTreeNode satisfies the interface contract.
        check(TreeNode.class.isAssignableFrom(DefaultMutableTreeNode.class), "DefaultMutableTreeNode implements TreeNode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

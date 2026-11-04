/*
 * @test
 * @summary javax.swing.tree.TreeModel conformance: white-box structure + black-box behavior.
 * @run main TreeModelTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;

public class TreeModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeModel");
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
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        final DefaultMutableTreeNode child0 = new DefaultMutableTreeNode("child0", false);
        final DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("child1", false);
        root.add(child0);
        root.add(child1);

        final List<TreeModelListener> listeners = new ArrayList<>();
        final boolean[] valueChanged = new boolean[1];

        TreeModel model = new TreeModel() {
            public Object getRoot() { return root; }
            public Object getChild(Object parent, int index) { return ((TreeNode) parent).getChildAt(index); }
            public int getChildCount(Object parent) { return ((TreeNode) parent).getChildCount(); }
            public boolean isLeaf(Object node) { return ((TreeNode) node).isLeaf(); }
            public void valueForPathChanged(TreePath path, Object newValue) {
                valueChanged[0] = true;
                ((DefaultMutableTreeNode) path.getLastPathComponent()).setUserObject(newValue);
            }
            public int getIndexOfChild(Object parent, Object child) { return ((TreeNode) parent).getIndex((TreeNode) child); }
            public void addTreeModelListener(TreeModelListener l) { listeners.add(l); }
            public void removeTreeModelListener(TreeModelListener l) { listeners.remove(l); }
        };

        // Exercise every TreeModel method.
        check(model.getRoot() == root, "getRoot");
        check(model.getChildCount(root) == 2, "getChildCount root");
        check(model.getChildCount(child0) == 0, "getChildCount leaf");
        check(model.getChild(root, 0) == child0 && model.getChild(root, 1) == child1, "getChild");
        check(model.isLeaf(child0) == true, "isLeaf child0");
        check(model.isLeaf(root) == false, "isLeaf root");
        check(model.getIndexOfChild(root, child1) == 1, "getIndexOfChild child1");
        check(model.getIndexOfChild(root, root) == -1, "getIndexOfChild non-child");

        TreeModelListener listener = new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) { }
            public void treeNodesInserted(TreeModelEvent e) { }
            public void treeNodesRemoved(TreeModelEvent e) { }
            public void treeStructureChanged(TreeModelEvent e) { }
        };
        model.addTreeModelListener(listener);
        check(listeners.size() == 1 && listeners.get(0) == listener, "addTreeModelListener");
        model.removeTreeModelListener(listener);
        check(listeners.size() == 0, "removeTreeModelListener");

        model.valueForPathChanged(new TreePath(new Object[] { root, child0 }), "renamed");
        check(valueChanged[0] == true, "valueForPathChanged called");
        check("renamed".equals(child0.getUserObject()), "valueForPathChanged updates node");

        check(TreeModel.class.isAssignableFrom(DefaultTreeModel.class), "DefaultTreeModel implements TreeModel");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

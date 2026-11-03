/*
 * @test
 * @summary javax.swing.tree.DefaultTreeModel conformance: white-box structure + black-box behavior.
 * @run main DefaultTreeModelTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class DefaultTreeModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultTreeModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.tree.TreeModel"), "interface 1");
    }

    static final class Recorder implements TreeModelListener {
        final List<TreeModelEvent> inserted = new ArrayList<>();
        final List<TreeModelEvent> removed = new ArrayList<>();
        final List<TreeModelEvent> changed = new ArrayList<>();
        final List<TreeModelEvent> structure = new ArrayList<>();
        public void treeNodesInserted(TreeModelEvent e) { inserted.add(e); }
        public void treeNodesRemoved(TreeModelEvent e) { removed.add(e); }
        public void treeNodesChanged(TreeModelEvent e) { changed.add(e); }
        public void treeStructureChanged(TreeModelEvent e) { structure.add(e); }
    }

    static boolean sameInts(int[] a, int... b) {
        if (a == null || a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode child0 = new DefaultMutableTreeNode("child0", false);
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("child1", false);
        root.add(child0);
        root.add(child1);

        DefaultTreeModel model = new DefaultTreeModel(root);
        check(model.asksAllowsChildren() == false, "asksAllowsChildren default false");
        model.setAsksAllowsChildren(true);
        check(model.asksAllowsChildren() == true, "setAsksAllowsChildren true");
        model.setAsksAllowsChildren(false);

        // Root + basic queries.
        check(model.getRoot() == root, "getRoot");
        check(model.getChildCount(root) == 2, "getChildCount root 2");
        check(model.getChildCount(child0) == 0, "getChildCount leaf 0");
        check(model.getChild(root, 0) == child0, "getChild 0");
        check(model.getChild(root, 1) == child1, "getChild 1");
        check(model.getIndexOfChild(root, child1) == 1, "getIndexOfChild child1 == 1");
        check(model.getIndexOfChild(root, root) == -1, "getIndexOfChild non-child -1");
        check(model.getIndexOfChild(null, child1) == -1, "getIndexOfChild null parent -1");
        check(model.isLeaf(child0) == true, "isLeaf child0 true");
        check(model.isLeaf(root) == false, "isLeaf root false");

        // getPathToRoot.
        TreeNode[] path = model.getPathToRoot(child0);
        check(path.length == 2 && path[0] == root && path[1] == child0, "getPathToRoot child0");
        TreeNode[] rootPath = model.getPathToRoot(root);
        check(rootPath.length == 1 && rootPath[0] == root, "getPathToRoot root");

        // Listener registration + events.
        Recorder rec = new Recorder();
        model.addTreeModelListener(rec);
        check(model.getTreeModelListeners().length == 1 && model.getTreeModelListeners()[0] == rec, "getTreeModelListeners");

        // insertNodeInto at index 1 -> treeNodesInserted with child index 1.
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("new", false);
        model.insertNodeInto(newNode, root, 1);
        check(rec.inserted.size() == 1, "treeNodesInserted fired");
        check(rec.inserted.get(0).getChildIndices() != null && sameInts(rec.inserted.get(0).getChildIndices(), 1), "inserted child index 1");
        check(rec.inserted.get(0).getChildren().length == 1 && rec.inserted.get(0).getChildren()[0] == newNode, "inserted child object");
        check(model.getChildCount(root) == 3 && model.getChild(root, 1) == newNode, "inserted into model");
        check(root.getIndex(newNode) == 1, "inserted node index");

        // nodeChanged(child0) -> treeNodesChanged with child index 0.
        model.nodeChanged(child0);
        check(rec.changed.size() == 1, "treeNodesChanged fired");
        check(sameInts(rec.changed.get(0).getChildIndices(), 0), "changed child index 0");

        // valueForPathChanged -> sets user object + fires treeNodesChanged.
        TreePath child0Path = new TreePath(new Object[] { root, child0 });
        model.valueForPathChanged(child0Path, "renamed");
        check("renamed".equals(child0.getUserObject()), "valueForPathChanged sets user object");
        check(rec.changed.size() == 2, "valueForPathChanged fires treeNodesChanged");

        // removeNodeFromParent(newNode) -> treeNodesRemoved with index 1.
        model.removeNodeFromParent(newNode);
        check(rec.removed.size() == 1, "treeNodesRemoved fired");
        check(sameInts(rec.removed.get(0).getChildIndices(), 1), "removed child index 1");
        check(rec.removed.get(0).getChildren().length == 1 && rec.removed.get(0).getChildren()[0] == newNode, "removed child object");
        check(model.getChildCount(root) == 2, "removed from model");
        check(newNode.getParent() == null, "removed node parent null");

        // nodeStructureChanged(root) -> treeStructureChanged.
        model.nodeStructureChanged(root);
        check(rec.structure.size() == 1, "treeStructureChanged fired");
        check(rec.structure.get(0).getChildIndices() == null, "structure childIndices null");

        // reload() -> treeStructureChanged on root.
        model.reload();
        check(rec.structure.size() == 2, "reload fires treeStructureChanged");

        // removeTreeModelListener.
        model.removeTreeModelListener(rec);
        check(model.getTreeModelListeners().length == 0, "removeTreeModelListener");

        // setRoot -> new root, getRoot reflects it.
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("newRoot");
        model.setRoot(newRoot);
        check(model.getRoot() == newRoot, "setRoot");

        // null root tolerated.
        DefaultTreeModel emptyModel = new DefaultTreeModel(null);
        check(emptyModel.getRoot() == null, "null root getRoot");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

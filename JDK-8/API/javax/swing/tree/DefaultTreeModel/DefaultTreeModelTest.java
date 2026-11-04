/*
 * @test
 * @summary javax.swing.tree.DefaultTreeModel conformance: tree model (headless-safe):
 *          constructors and asksAllowsChildren/isLeaf interplay, child/index queries
 *          with null semantics, mutation methods with exact TreeModelEvent payloads
 *          (path/children/childIndices for inserted/removed/changed/structure events),
 *          reload/valueForPathChanged, listener registry, root replacement.
 * @run main DefaultTreeModelTest
 */

import java.util.Arrays;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class DefaultTreeModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, String m, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception");
    }

    /** Records the payload of every event, most recent last. */
    static class Rec implements TreeModelListener {
        String last = "none";
        int inserted, removed, changed, structure;
        public void treeNodesChanged(TreeModelEvent e) { changed++; last = dump("CHANGED", e); }
        public void treeNodesInserted(TreeModelEvent e) { inserted++; last = dump("INSERTED", e); }
        public void treeNodesRemoved(TreeModelEvent e) { removed++; last = dump("REMOVED", e); }
        public void treeStructureChanged(TreeModelEvent e) { structure++; last = dump("STRUCT", e); }
        static String dump(String k, TreeModelEvent e) {
            return k + " path=" + Arrays.toString(e.getPath())
                    + " children=" + Arrays.toString(e.getChildren())
                    + " idx=" + Arrays.toString(e.getChildIndices());
        }
    }

    public static void main(String[] args) {
        testModelBasics();
        testEvents();
        testExceptionsAndNulls();
        testInterfacePosition();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testModelBasics() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("R");
        DefaultMutableTreeNode a = new DefaultMutableTreeNode("A");
        DefaultMutableTreeNode b = new DefaultMutableTreeNode("B");
        root.add(a);
        root.add(b);
        DefaultTreeModel m = new DefaultTreeModel(root);
        check(m.getRoot() == root, "root wiring");
        check(!m.asksAllowsChildren(), "asksAllowsChildren defaults false");
        check(m.getChildCount(root) == 2, "child count");
        check(m.getChild(root, 0) == a && m.getChild(root, 1) == b, "child order");
        check(m.getIndexOfChild(root, a) == 0 && m.getIndexOfChild(root, b) == 1, "index of child");
        check(m.isLeaf(a), "childless node is leaf when asksAllowsChildren=false");
        check(!m.isLeaf(root), "node with children is not leaf");

        // asksAllowsChildren ctor flips isLeaf to !getAllowsChildren().
        DefaultTreeModel m2 = new DefaultTreeModel(root, true);
        check(m2.asksAllowsChildren(), "ctor(root, true)");
        check(!m2.isLeaf(new DefaultMutableTreeNode("x")), "asks: childless-but-allows is NOT a leaf");
        check(m2.isLeaf(new DefaultMutableTreeNode("y", false)), "asks: disallows-children is a leaf");
        m.setAsksAllowsChildren(true);
        check(m.asksAllowsChildren(), "setAsksAllowsChildren round-trip");
        check(!m.isLeaf(a), "isLeaf follows asksAllowsChildren dynamically");
        m.setAsksAllowsChildren(false);
        check(m.isLeaf(a), "isLeaf restored");

        // Model exposes itself through the TreeModel interface.
        check(m instanceof TreeModel, "implements TreeModel");
        check(m.getTreeModelListeners().length == 0, "no listeners initially");
        Rec rec = new Rec();
        m.addTreeModelListener(rec);
        check(m.getTreeModelListeners().length == 1, "listener added");
        check(m.getListeners(TreeModelListener.class).length == 1, "getListeners(Class)");
        m.removeTreeModelListener(rec);
        check(m.getTreeModelListeners().length == 0, "listener removed");
    }

    static void testEvents() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("R");
        DefaultMutableTreeNode a = new DefaultMutableTreeNode("A");
        DefaultMutableTreeNode b = new DefaultMutableTreeNode("B");
        root.add(a);
        root.add(b);
        DefaultTreeModel m = new DefaultTreeModel(root);
        Rec rec = new Rec();
        m.addTreeModelListener(rec);

        // insertNodeInto: INSERT with parent path, the child, and the exact index.
        DefaultMutableTreeNode nb = new DefaultMutableTreeNode("NB");
        m.insertNodeInto(nb, root, 1);
        check(rec.inserted == 1, "insert fired once");
        check(rec.last.equals("INSERTED path=[R] children=[NB] idx=[1]"), "insert payload: " + rec.last);
        check(root.getChildAt(1) == nb, "insert applied to the underlying node");
        check(rec.last.startsWith("INSERTED"), "event kind");

        // removeNodeFromParent: REMOVE with the pre-removal index.
        m.removeNodeFromParent(nb);
        check(rec.removed == 1, "remove fired once");
        check(rec.last.equals("REMOVED path=[R] children=[NB] idx=[1]"), "remove payload: " + rec.last);

        // nodeChanged(child): CHANGE carrying the node and its index under the parent.
        m.nodeChanged(a);
        check(rec.changed == 1, "change fired once");
        check(rec.last.equals("CHANGED path=[R] children=[A] idx=[0]"), "change payload: " + rec.last);
        // nodeChanged(root): no parent exists, so children/indices are null.
        m.nodeChanged(root);
        check(rec.changed == 2, "nodeChanged(root) also fires");
        check(rec.last.equals("CHANGED path=[R] children=null idx=null"), "nodeChanged(root) payload: " + rec.last);
        // nodeChanged(null) is a documented no-op.
        int before = rec.changed;
        m.nodeChanged(null);
        check(rec.changed == before, "nodeChanged(null) is a no-op");

        // reload() and reload(node): STRUCTURE with children/indices null.
        m.reload();
        check(rec.structure == 1 && rec.last.equals("STRUCT path=[R] children=null idx=null"),
                "reload payload: " + rec.last);
        m.reload(a);
        check(rec.structure == 2 && rec.last.equals("STRUCT path=[R, A] children=null idx=null"),
                "reload(node) payload: " + rec.last);
        m.nodeStructureChanged(b);
        check(rec.structure == 3 && rec.last.equals("STRUCT path=[R, B] children=null idx=null"),
                "nodeStructureChanged payload: " + rec.last);

        // Manual nodesWereInserted / nodesWereRemoved.
        m.nodesWereInserted(root, new int[]{0});
        check(rec.inserted == 2 && rec.last.equals("INSERTED path=[R] children=[A] idx=[0]"),
                "nodesWereInserted payload: " + rec.last);
        m.nodesWereRemoved(root, new int[]{0}, new Object[]{a});
        check(rec.removed == 2 && rec.last.equals("REMOVED path=[R] children=[A] idx=[0]"),
                "nodesWereRemoved payload: " + rec.last);
        root.insert(a, 0); // reattach silently for later checks

        // nodesChanged(parent, null) fires with null payload (as nodeChanged(root) does).
        m.nodesChanged(root, null);
        check(rec.last.equals("CHANGED path=[R] children=null idx=null"), "nodesChanged(null idx)");

        // valueForPathChanged sets the user object and fires CHANGE for it.
        m.valueForPathChanged(new TreePath(new Object[]{root, a}), "A2");
        check("A2".equals(a.getUserObject()), "valueForPathChanged sets user object");
        check(rec.last.equals("CHANGED path=[R] children=[A2] idx=[0]"), "valueForPathChanged payload: " + rec.last);

        // setRoot fires STRUCTURE on the new root path.
        DefaultMutableTreeNode nr = new DefaultMutableTreeNode("Y");
        m.setRoot(nr);
        check(m.getRoot() == nr, "setRoot replaces root");
        check(rec.last.equals("STRUCT path=[Y] children=null idx=null"), "setRoot payload: " + rec.last);
        // setRoot(null): allowed; getRoot reports null and a structure event fires.
        m.setRoot(null);
        check(m.getRoot() == null, "setRoot(null) accepted");
        check(rec.structure == 5, "setRoot(null) fires structure event (5th)");
        m.setRoot(root);
        check(m.getRoot() == root, "root restored");
    }

    static void testExceptionsAndNulls() {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("R");
        root.add(new DefaultMutableTreeNode("A"));
        final DefaultTreeModel m = new DefaultTreeModel(root);
        checkThrows(NullPointerException.class, "getChildCount(null)",
                new ThrowingRunnable() { public void run() { m.getChildCount(null); } });
        checkThrows(NullPointerException.class, "getChild(null, 0)",
                new ThrowingRunnable() { public void run() { m.getChild(null, 0); } });
        checkThrows(NullPointerException.class, "isLeaf(null)",
                new ThrowingRunnable() { public void run() { m.isLeaf(null); } });
        checkThrows(IllegalArgumentException.class, "insertNodeInto(null child)",
                new ThrowingRunnable() { public void run() { m.insertNodeInto(null, root, 0); } });
        checkThrows(IllegalArgumentException.class, "removeNodeFromParent(root)",
                new ThrowingRunnable() { public void run() { m.removeNodeFromParent(root); } });
        // getIndexOfChild: null on either side gives -1, not an exception.
        check(m.getIndexOfChild(root, null) == -1, "getIndexOfChild null child is -1");
        check(m.getIndexOfChild(null, root) == -1, "getIndexOfChild null parent is -1");
        check(m.getIndexOfChild(root, new DefaultMutableTreeNode("zz")) == -1, "getIndexOfChild unknown child is -1");
    }

    static void testInterfacePosition() {
        // The interface contract (TreeModel) is exercised through the interface type.
        DefaultMutableTreeNode r = new DefaultMutableTreeNode("R");
        r.add(new DefaultMutableTreeNode("A"));
        TreeModel tm = new DefaultTreeModel(r);
        check(tm.getChildCount(r) == 1, "getChildCount through interface");
        check("A".equals(((DefaultMutableTreeNode) tm.getChild(r, 0)).getUserObject()), "getChild through interface");
        check(tm.getIndexOfChild(r, r.getChildAt(0)) == 0, "getIndexOfChild through interface");
        check(tm.isLeaf(r.getChildAt(0)), "isLeaf through interface");
        check(tm.getRoot() == r, "getRoot through interface");
        tm.valueForPathChanged(new TreePath(r), "X");
        check("X".equals(r.getUserObject()), "valueForPathChanged through interface");
    }
}

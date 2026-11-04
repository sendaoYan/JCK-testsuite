/*
 * @test
 * @summary javax.swing.JTree conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JTreeTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

public class JTreeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JTree");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Scrollable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
        // supplementary structural checks
        check(Class.forName("javax.swing.JTree$DropLocation") != null, "nested DropLocation exists");
        check(c.getDeclaredField("TREE_MODEL_PROPERTY").get(null).equals("model"), "TREE_MODEL_PROPERTY value");
        check(c.getDeclaredField("SELECTION_MODEL_PROPERTY").get(null).equals("selectionModel"), "SELECTION_MODEL_PROPERTY value");
        check(c.getDeclaredMethod("expandPath", javax.swing.tree.TreePath.class).getReturnType() == void.class,
                "expandPath returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame("JTreeTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("c1");
            DefaultMutableTreeNode c1a = new DefaultMutableTreeNode("c1a");
            DefaultMutableTreeNode c1b = new DefaultMutableTreeNode("c1b");
            c1.add(c1a);
            c1.add(c1b);
            DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("c2");
            DefaultMutableTreeNode c3 = new DefaultMutableTreeNode("c3");
            root.add(c1);
            root.add(c2);
            root.add(c3);
            DefaultTreeModel model = new DefaultTreeModel(root);
            JTree tree = new JTree(model);
            frame.add(new JScrollPane(tree));
            frame.setSize(300, 400);
            frame.setVisible(true);

            TreePath rootP = new TreePath(root.getPath());
            TreePath c1P = new TreePath(c1.getPath());
            TreePath c1aP = new TreePath(c1a.getPath());
            TreePath c2P = new TreePath(c2.getPath());
            TreePath c3P = new TreePath(c3.getPath());

            // fixed model wiring and default state (root visible and expanded by default)
            check(tree.getModel() == model, "model same instance");
            check(tree.isRootVisible(), "root visible by default");
            check(tree.getRowCount() == 4, "fresh tree shows root + 3 children");
            check(tree.isExpanded(rootP), "root expanded by default");
            check(!tree.isExpanded(c1P), "child collapsed by default");
            check(!tree.hasBeenExpanded(c1P), "child not yet expanded");

            // collapse/expand root with listener recording
            final List<String> expLog = new ArrayList<>();
            tree.addTreeExpansionListener(new TreeExpansionListener() {
                public void treeExpanded(TreeExpansionEvent e) { expLog.add("exp:" + e.getPath().getLastPathComponent()); }
                public void treeCollapsed(TreeExpansionEvent e) { expLog.add("col:" + e.getPath().getLastPathComponent()); }
            });
            tree.collapsePath(rootP);
            check(tree.getRowCount() == 1, "collapsed root shows 1 row");
            check(tree.isCollapsed(rootP), "root isCollapsed after collapsePath");
            tree.expandPath(rootP);
            check(tree.getRowCount() == 4, "expanded root shows 4 rows");
            check(tree.isVisible(c1P), "child visible after root expand");
            check(expLog.equals(Arrays.asList("col:root", "exp:root")), "collapse then expand root recorded");

            // nested expand and path/row roundtrip
            tree.expandPath(c1P);
            check(tree.getRowCount() == 6, "expanded c1 shows 6 rows");
            check(tree.hasBeenExpanded(c1P), "hasBeenExpanded(c1)");
            check(tree.getRowForPath(c1P) == 1, "row for c1 is 1");
            check(tree.getRowForPath(c1aP) == 2, "row for c1a is 2");
            check(tree.getPathForRow(0).equals(rootP), "row 0 path is root");
            check(tree.getPathForRow(2).equals(c1aP), "row 2 path is c1a");
            check(tree.getPathForRow(0).equals(tree.getPathForRow(tree.getRowForPath(rootP))),
                    "getPathForRow/getRowForPath roundtrip");
            tree.collapsePath(c1P);
            check(tree.getRowCount() == 4, "collapsed c1 back to 4 rows");
            check(expLog.equals(Arrays.asList("col:root", "exp:root", "exp:c1", "col:c1")),
                    "listener sequence col-root/exp-root/exp-c1/col-c1");

            // root visibility
            tree.setRootVisible(false);
            check(tree.getRowCount() == 3, "invisible root hides one row");
            check(tree.getPathForRow(0).equals(c1P), "first row is c1 when root hidden");
            check(tree.isVisible(rootP), "isVisible(root) true even when rootVisible=false (root special case)");
            tree.setRootVisible(true);
            check(tree.getRowCount() == 4, "visible root restores 4 rows");

            // selection with fixed tree (rows: root0 c1=1 c2=2 c3=3)
            final List<TreeSelectionEvent> selLog = new ArrayList<>();
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) { selLog.add(e); }
            });
            tree.setSelectionPath(c2P);
            check(selLog.size() == 1, "selection listener fired once");
            check(selLog.get(0).getNewLeadSelectionPath().getLastPathComponent() == c2,
                    "new lead selection path component is c2 node");
            check(tree.getSelectionPath().equals(c2P), "getSelectionPath is c2");
            check(tree.getSelectionCount() == 1, "selection count 1");
            check(Arrays.equals(tree.getSelectionRows(), new int[]{2}), "selection rows {2}");
            check(tree.getMinSelectionRow() == 2, "min selection row 2");
            check(tree.isPathSelected(c2P), "isPathSelected(c2)");
            check(!tree.isPathSelected(c3P), "!isPathSelected(c3)");
            check(tree.getLastSelectedPathComponent() == c2, "last selected path component c2");
            check(tree.getLeadSelectionPath().equals(c2P), "lead selection path c2");
            tree.setSelectionRow(1);
            check(tree.getSelectionPath().equals(c1P), "setSelectionRow(1) selects c1 path");
            tree.clearSelection();
            check(tree.isSelectionEmpty(), "clearSelection empties");
            check(tree.getLastSelectedPathComponent() == null, "last component null after clear");

            // selection model swap keeps user listeners wired
            DefaultTreeSelectionModel dsm = new DefaultTreeSelectionModel();
            tree.setSelectionModel(dsm);
            check(tree.getSelectionModel() == dsm, "selection model same instance");
            selLog.clear();
            tree.setSelectionPath(c1P);
            check(selLog.size() == 1, "selection listener fires after model swap");
            check(tree.getSelectionPath().equals(c1P), "selection works via new model");

            // model mutation: tree's internal TreeModelListener keeps rows in sync,
            // user TreeModelListener observes the model event itself
            final List<TreeModelEvent> modelLog = new ArrayList<>();
            model.addTreeModelListener(new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent e) { modelLog.add(e); }
                public void treeNodesInserted(TreeModelEvent e) { modelLog.add(e); }
                public void treeNodesRemoved(TreeModelEvent e) { modelLog.add(e); }
                public void treeStructureChanged(TreeModelEvent e) { modelLog.add(e); }
            });
            DefaultMutableTreeNode c4 = new DefaultMutableTreeNode("c4");
            model.insertNodeInto(c4, root, 3);
            check(modelLog.size() == 1, "user model listener got insert event");
            check(modelLog.get(0).getChildIndices()[0] == 3, "inserted at child index 3");
            check(modelLog.get(0).getChildren()[0] == c4, "inserted child is c4");
            check(tree.getRowCount() == 5, "tree row count grew with model insert");
            TreePath c4P = new TreePath(c4.getPath());
            check(tree.getRowForPath(c4P) == 4, "new node visible at row 4");
            check(tree.isVisible(c4P), "inserted node visible");

            // full model swap resets expansion/selection and auto-expands new root
            DefaultMutableTreeNode r2 = new DefaultMutableTreeNode("r2");
            r2.add(new DefaultMutableTreeNode("a"));
            r2.add(new DefaultMutableTreeNode("b"));
            DefaultTreeModel model2 = new DefaultTreeModel(r2);
            tree.setModel(model2);
            check(tree.getModel() == model2, "setModel installs new model");
            check(tree.isSelectionEmpty(), "setModel clears selection");
            check(tree.getRowCount() == 3, "new root auto-expanded: 1 + 2 children");
            check(tree.getPathForRow(0).getLastPathComponent() == r2, "row 0 is new root");
            boolean npe = false;
            try { tree.setModel(null); }
            catch (NullPointerException e) { npe = true; }
            check(!npe, "setModel(null) accepted (tree uses empty model)");
            check(tree.getRowCount() == 0, "null model yields no rows");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JTreeTest::testWhiteBox);
        runOnEDT(JTreeTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}

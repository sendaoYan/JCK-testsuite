/*
 * @test
 * @summary javax.swing.tree.DefaultTreeSelectionModel conformance: selection model (headless-safe):
 *          defaults, single/batch add/remove/set selection semantics, null handling,
 *          selection modes with invalid-value coercion, row mapping via RowMapper,
 *          exact TreeSelectionEvent payloads, lead selection tracking, listener
 *          registry, serialization round-trip.
 * @run main DefaultTreeSelectionModelTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class DefaultTreeSelectionModelTest {
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

    static String paths(TreePath[] ps) {
        if (ps == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (TreePath q : ps) sb.append(Arrays.toString(q.getPath()));
        return sb.toString();
    }

    static class Sel implements TreeSelectionListener {
        int count;
        TreeSelectionEvent last;
        public void valueChanged(TreeSelectionEvent e) { count++; last = e; }
    }

    static DefaultMutableTreeNode root, n1, n2;
    static TreePath pR, p1, p2;

    static void build() {
        root = new DefaultMutableTreeNode("R");
        n1 = new DefaultMutableTreeNode("A");
        n2 = new DefaultMutableTreeNode("B");
        root.add(n1);
        root.add(n2);
        pR = new TreePath(root);
        p1 = pR.pathByAddingChild(n1);
        p2 = pR.pathByAddingChild(n2);
    }

    public static void main(String[] args) throws Exception {
        build();
        testDefaults();
        testSelectionOps();
        testModes();
        testRowMapping();
        testEvents();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testDefaults() {
        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        check(m.getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION, "default mode discontiguous");
        check(m.isSelectionEmpty(), "empty by default");
        check(m.getSelectionPaths() != null && m.getSelectionPaths().length == 0, "no paths by default (empty array)");
        check(m.getSelectionPath() == null, "no single path by default");
        check(m.getSelectionCount() == 0, "count 0");
        check(m.getSelectionRows() != null && m.getSelectionRows().length == 0, "rows empty array without selection");
        check(m.getMinSelectionRow() == -1 && m.getMaxSelectionRow() == -1, "min/max row -1");
        check(m.getLeadSelectionPath() == null, "lead path null");
        check(m.getLeadSelectionRow() == -1, "lead row -1");
        check(m.getRowMapper() == null, "row mapper null");
        check(!m.isPathSelected(p1), "nothing selected");
        check(!m.isRowSelected(0), "no row selected");
        check(m.getTreeSelectionListeners().length == 0, "no listeners");
    }

    static void testSelectionOps() {
        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        m.setSelectionPath(p1);
        check(paths(m.getSelectionPaths()).equals("[R, A]"), "setSelectionPath");
        check(m.isPathSelected(p1) && !m.isPathSelected(p2), "isPathSelected");
        check(m.getSelectionCount() == 1, "count after single set");
        check(Arrays.toString(m.getLeadSelectionPath().getPath()).equals("[R, A]"), "lead follows set");

        m.addSelectionPath(p2);
        check(paths(m.getSelectionPaths()).equals("[R, A][R, B]"), "addSelectionPath accumulates");
        m.removeSelectionPath(p1);
        check(paths(m.getSelectionPaths()).equals("[R, B]"), "removeSelectionPath removes");

        m.setSelectionPaths(new TreePath[]{p1, p2});
        check(paths(m.getSelectionPaths()).equals("[R, A][R, B]") && m.getSelectionCount() == 2,
                "setSelectionPaths batch");
        // Adding an already-selected path keeps it exactly once.
        m.addSelectionPaths(new TreePath[]{p1});
        check(paths(m.getSelectionPaths()).equals("[R, A][R, B]"), "addSelectionPaths dedup");
        // Null arguments are tolerated: batch variants are no-ops, single variants clear.
        m.addSelectionPaths(null);
        check(m.getSelectionCount() == 2, "addSelectionPaths(null) no-op");
        m.removeSelectionPaths(null);
        check(m.getSelectionCount() == 2, "removeSelectionPaths(null) no-op");
        m.setSelectionPaths(null);
        check(m.isSelectionEmpty() && m.getSelectionPaths().length == 0, "setSelectionPaths(null) clears");
        m.setSelectionPath(p1);
        m.setSelectionPath(null);
        check(m.isSelectionEmpty(), "setSelectionPath(null) clears");
        m.addSelectionPath(null);
        check(m.isSelectionEmpty(), "addSelectionPath(null) no-op");
        m.removeSelectionPath(null);
        check(m.isSelectionEmpty(), "removeSelectionPath(null) no-op");

        // clearSelection, and no redundant events.
        m.setSelectionPath(p1);
        m.clearSelection();
        check(m.isSelectionEmpty(), "clearSelection");
        m.clearSelection();
        check(m.isSelectionEmpty(), "clearSelection when empty is harmless");

        // Removing a path that was never selected is a no-op.
        m.setSelectionPath(p1);
        m.removeSelectionPath(new TreePath(new DefaultMutableTreeNode("zz")));
        check(paths(m.getSelectionPaths()).equals("[R, A]"), "removing unknown path is a no-op");
    }

    static void testModes() {
        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        m.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        check(m.getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION, "single mode stored");
        m.setSelectionPath(p1);
        m.addSelectionPath(p2);
        // In SINGLE mode an add replaces the selection.
        check(paths(m.getSelectionPaths()).equals("[R, B]"), "SINGLE mode: add replaces selection");

        // Out-of-range mode values are silently coerced to DISCONTIGUOUS_TREE_SELECTION.
        m.setSelectionMode(99);
        check(m.getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION,
                "invalid mode 99 coerced to DISCONTIGUOUS");
        m.setSelectionMode(-5);
        check(m.getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION,
                "negative mode coerced to DISCONTIGUOUS");
        m.setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        check(m.getSelectionMode() == TreeSelectionModel.CONTIGUOUS_TREE_SELECTION, "contiguous mode stored");
        // Mode constants.
        check(TreeSelectionModel.SINGLE_TREE_SELECTION == 1, "SINGLE_TREE_SELECTION == 1");
        check(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION == 2, "CONTIGUOUS_TREE_SELECTION == 2");
        check(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION == 4, "DISCONTIGUOUS_TREE_SELECTION == 4");
    }

    static void testRowMapping() {
        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        m.setSelectionPaths(new TreePath[]{p1, p2});
        // Without a RowMapper there are no rows: empty array and -1 bounds.
        check(m.getSelectionRows().length == 0, "rows empty without mapper");
        check(m.getMinSelectionRow() == -1 && m.getMaxSelectionRow() == -1, "row bounds -1 without mapper");
        m.setRowMapper(new RowMapper() {
            public int[] getRowsForPaths(TreePath[] ps) {
                int[] rows = new int[ps.length];
                for (int i = 0; i < ps.length; i++) {
                    String s = String.valueOf(ps[i].getLastPathComponent());
                    rows[i] = "A".equals(s) ? 1 : "B".equals(s) ? 2 : -1;
                }
                return rows;
            }
        });
        check(m.getRowMapper() != null, "row mapper stored");
        // Re-selecting refreshes the cached row mapping.
        m.setSelectionPaths(new TreePath[]{p1, p2});
        check(Arrays.toString(m.getSelectionRows()).equals("[1, 2]"), "rows with mapper: " + Arrays.toString(m.getSelectionRows()));
        check(m.getMinSelectionRow() == 1 && m.getMaxSelectionRow() == 2, "row bounds with mapper");
        check(m.getLeadSelectionRow() == 2, "lead row is last selected");
        check(m.isRowSelected(1) && m.isRowSelected(2), "isRowSelected true for selected");
        check(!m.isRowSelected(0), "isRowSelected false outside");
        check(!m.isRowSelected(-1), "isRowSelected(-1) false");
        m.resetRowSelection();
        check(m.getRowMapper() != null, "resetRowSelection keeps the mapper");
        check(m.getMinSelectionRow() == 1 && m.getMaxSelectionRow() == 2,
                "row bounds recomputed after resetRowSelection");
    }

    static void testEvents() {
        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        Sel sel = new Sel();
        m.addTreeSelectionListener(sel);
        m.setSelectionPath(p1);
        check(sel.count == 1, "set fires one event");
        check(paths(sel.last.getPaths()).equals("[R, A]"), "event carries new selection");
        check(sel.last.isAddedPath(0), "event reports path added");
        check(sel.last.isAddedPath(p1), "isAddedPath(TreePath) true for added");
        check(sel.last.getOldLeadSelectionPath() == null, "first selection old lead null");
        check(Arrays.toString(sel.last.getNewLeadSelectionPath().getPath()).equals("[R, A]"), "new lead is the path");

        m.addSelectionPath(p2);
        check(sel.count == 2, "add fires");
        check(paths(sel.last.getPaths()).equals("[R, B]"), "add event carries only the added path");
        check(sel.last.isAddedPath(p2), "add event marks p2 added");
        checkThrows(IllegalArgumentException.class, "isAddedPath with a path not in the event",
                new ThrowingRunnable() { public void run() { sel.last.isAddedPath(p1); } });

        m.removeSelectionPath(p1);
        check(sel.count == 3, "remove fires");
        check(paths(sel.last.getPaths()).equals("[R, A]"), "remove event carries the removed path");
        checkThrows(IllegalArgumentException.class, "isAddedPath for removed path set",
                new ThrowingRunnable() { public void run() { sel.last.isAddedPath(p2); } });

        // Redundant operations must not fire.
        m.setSelectionPaths(new TreePath[]{p2});
        int base = sel.count;
        m.setSelectionPaths(new TreePath[]{p2});
        check(sel.count == base, "identical re-selection fires nothing");
        m.setSelectionPaths(null);
        check(sel.count == base + 1, "clearing a non-empty selection fires");
        m.setSelectionPaths(null);
        check(sel.count == base + 1, "clearing an empty selection fires nothing");

        // Listener registry.
        check(m.getTreeSelectionListeners().length == 1, "one listener");
        m.removeTreeSelectionListener(sel);
        check(m.getTreeSelectionListeners().length == 0, "listener removed");
        m.setSelectionPath(p1);
        check(sel.count == base + 1, "removed listener not notified");
    }

    static void testSerialization() throws Exception {
        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        m.setSelectionPaths(new TreePath[]{p1, p2});
        m.setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(m);
        oos.close();
        DefaultTreeSelectionModel back = (DefaultTreeSelectionModel)
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(paths(back.getSelectionPaths()).equals("[R, A][R, B]"), "round-trip selection");
        check(back.getSelectionCount() == 2, "round-trip count");
        check(back.getSelectionMode() == TreeSelectionModel.CONTIGUOUS_TREE_SELECTION, "round-trip mode");
        // TreePath equality falls through to component identity for DefaultMutableTreeNode,
        // so queries use the deserialized model's own paths.
        check(back.isPathSelected(back.getSelectionPaths()[0]), "round-trip isPathSelected on own path");
        TreePath other = new TreePath(new DefaultMutableTreeNode("Z"));
        back.setSelectionPath(other);
        check(paths(back.getSelectionPaths()).equals("[Z]"), "deserialized model still mutable");
    }
}

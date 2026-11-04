/*
 * @test
 * @summary javax.swing.tree.TreeSelectionModel conformance: white-box structure + black-box behavior.
 * @run main TreeSelectionModelTest
 */

import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class TreeSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeSelectionModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(TreeSelectionModel.SINGLE_TREE_SELECTION == 1, "SINGLE constant 1");
        check(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION == 2, "CONTIGUOUS constant 2");
        check(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION == 4, "DISCONTIGUOUS constant 4");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final TreePath p0 = new TreePath("p0");
        final TreePath p1 = new TreePath("p1");
        final TreePath p2 = new TreePath("p2");

        final List<TreePath> selection = new ArrayList<>();
        final int[] mode = { TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION };
        final RowMapper[] rowMapperRef = new RowMapper[1];
        final List<TreeSelectionListener> selListeners = new ArrayList<>();
        final List<PropertyChangeListener> pcListeners = new ArrayList<>();

        TreeSelectionModel sm = new TreeSelectionModel() {
            private int[] rows() {
                RowMapper rm = rowMapperRef[0];
                return rm == null ? new int[0] : rm.getRowsForPaths(selection.toArray(new TreePath[0]));
            }
            public void setSelectionMode(int m) { mode[0] = m; }
            public int getSelectionMode() { return mode[0]; }
            public void setSelectionPath(TreePath p) { selection.clear(); if (p != null) selection.add(p); }
            public void setSelectionPaths(TreePath[] ps) { selection.clear(); if (ps != null) for (TreePath p : ps) if (p != null) selection.add(p); }
            public void addSelectionPath(TreePath p) { if (p != null && !selection.contains(p)) selection.add(p); }
            public void addSelectionPaths(TreePath[] ps) { if (ps != null) for (TreePath p : ps) if (p != null && !selection.contains(p)) selection.add(p); }
            public void removeSelectionPath(TreePath p) { selection.remove(p); }
            public void removeSelectionPaths(TreePath[] ps) { if (ps != null) for (TreePath p : ps) selection.remove(p); }
            public TreePath getSelectionPath() { return selection.isEmpty() ? null : selection.get(0); }
            public TreePath[] getSelectionPaths() { return selection.toArray(new TreePath[0]); }
            public int getSelectionCount() { return selection.size(); }
            public boolean isPathSelected(TreePath p) { return selection.contains(p); }
            public boolean isSelectionEmpty() { return selection.isEmpty(); }
            public void clearSelection() { selection.clear(); }
            public void setRowMapper(RowMapper rm) { rowMapperRef[0] = rm; }
            public RowMapper getRowMapper() { return rowMapperRef[0]; }
            public int[] getSelectionRows() { return rows(); }
            public int getMinSelectionRow() {
                int[] r = rows(); int min = Integer.MAX_VALUE;
                for (int v : r) min = Math.min(min, v);
                return r.length == 0 ? -1 : min;
            }
            public int getMaxSelectionRow() {
                int[] r = rows(); int max = Integer.MIN_VALUE;
                for (int v : r) max = Math.max(max, v);
                return r.length == 0 ? -1 : max;
            }
            public boolean isRowSelected(int row) {
                for (int r : rows()) if (r == row) return true;
                return false;
            }
            public void resetRowSelection() { }
            public int getLeadSelectionRow() {
                int[] r = rows();
                return r.length == 0 ? -1 : r[r.length - 1];
            }
            public TreePath getLeadSelectionPath() { return selection.isEmpty() ? null : selection.get(selection.size() - 1); }
            public void addPropertyChangeListener(PropertyChangeListener l) { pcListeners.add(l); }
            public void removePropertyChangeListener(PropertyChangeListener l) { pcListeners.remove(l); }
            public void addTreeSelectionListener(TreeSelectionListener l) { selListeners.add(l); }
            public void removeTreeSelectionListener(TreeSelectionListener l) { selListeners.remove(l); }
        };

        // RowMapper + selection mode.
        RowMapper mapper = new RowMapper() {
            public int[] getRowsForPaths(TreePath[] paths) {
                int[] r = new int[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    if (paths[i] == p0) r[i] = 0;
                    else if (paths[i] == p1) r[i] = 2;
                    else if (paths[i] == p2) r[i] = 5;
                    else r[i] = -1;
                }
                return r;
            }
        };
        sm.setRowMapper(mapper);
        check(sm.getRowMapper() == mapper, "getRowMapper round-trip");
        sm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        check(sm.getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION, "setSelectionMode/getSelectionMode");

        // setSelectionPaths + queries.
        sm.setSelectionPaths(new TreePath[] { p0, p1, p2 });
        check(sm.getSelectionCount() == 3, "getSelectionCount 3");
        check(sm.isSelectionEmpty() == false, "isSelectionEmpty false");
        check(sm.isPathSelected(p1) == true, "isPathSelected p1");
        check(sm.getSelectionPath() == p0, "getSelectionPath first");
        check(sm.getSelectionPaths().length == 3, "getSelectionPaths length");
        check(sm.getLeadSelectionPath() == p2, "getLeadSelectionPath last");

        int[] rows = sm.getSelectionRows();
        check(rows != null && rows.length == 3 && rows[0] == 0 && rows[1] == 2 && rows[2] == 5, "getSelectionRows");
        check(sm.getMinSelectionRow() == 0, "getMinSelectionRow");
        check(sm.getMaxSelectionRow() == 5, "getMaxSelectionRow");
        check(sm.isRowSelected(2) == true && sm.isRowSelected(1) == false, "isRowSelected");
        check(sm.getLeadSelectionRow() == 5, "getLeadSelectionRow");
        sm.resetRowSelection();

        // add/remove paths.
        sm.addSelectionPaths(new TreePath[] { p0, p1 });
        check(sm.getSelectionCount() == 3, "addSelectionPaths dedupe");
        sm.removeSelectionPath(p1);
        check(sm.getSelectionCount() == 2 && !sm.isPathSelected(p1), "removeSelectionPath");

        // Listeners.
        TreeSelectionListener sl = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) { }
        };
        sm.addTreeSelectionListener(sl);
        check(selListeners.size() == 1 && selListeners.get(0) == sl, "addTreeSelectionListener");
        sm.removeTreeSelectionListener(sl);
        check(selListeners.size() == 0, "removeTreeSelectionListener");

        PropertyChangeListener pl = new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) { }
        };
        sm.addPropertyChangeListener(pl);
        check(pcListeners.size() == 1, "addPropertyChangeListener");
        sm.removePropertyChangeListener(pl);
        check(pcListeners.size() == 0, "removePropertyChangeListener");

        // clearSelection.
        sm.clearSelection();
        check(sm.isSelectionEmpty() == true && sm.getSelectionCount() == 0, "clearSelection");
        check(sm.getSelectionPath() == null, "getSelectionPath null after clear");

        check(TreeSelectionModel.class.isAssignableFrom(DefaultTreeSelectionModel.class), "DefaultTreeSelectionModel implements TreeSelectionModel");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

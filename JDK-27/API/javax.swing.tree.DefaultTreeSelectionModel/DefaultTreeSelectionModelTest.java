/*
 * @test
 * @summary javax.swing.tree.DefaultTreeSelectionModel conformance: white-box structure + black-box behavior.
 * @run main DefaultTreeSelectionModelTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class DefaultTreeSelectionModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultTreeSelectionModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Cloneable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.tree.TreeSelectionModel"), "interface 2");
    }

    static final class SelRecorder implements TreeSelectionListener {
        final List<TreeSelectionEvent> events = new ArrayList<>();
        public void valueChanged(TreeSelectionEvent e) { events.add(e); }
    }

    static boolean samePaths(TreePath[] a, TreePath... b) {
        if (a == null || a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    static boolean sameInts(int[] a, int... b) {
        if (a == null || a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final TreePath p0 = new TreePath("p0");
        final TreePath p1 = new TreePath("p1");
        final TreePath p2 = new TreePath("p2");

        // RowMapper maps paths to fixed, non-contiguous rows.
        final RowMapper mapper = new RowMapper() {
            public int[] getRowsForPaths(TreePath[] paths) {
                int[] rows = new int[paths.length];
                for (int i = 0; i < paths.length; i++) {
                    if (paths[i] == p0) rows[i] = 0;
                    else if (paths[i] == p1) rows[i] = 2;
                    else if (paths[i] == p2) rows[i] = 5;
                    else rows[i] = -1;
                }
                return rows;
            }
        };

        DefaultTreeSelectionModel m = new DefaultTreeSelectionModel();
        check(m.getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION, "default mode DISCONTIGUOUS");
        check(m.isSelectionEmpty() == true, "initially empty");
        check(m.getSelectionCount() == 0, "initially count 0");
        check(m.getSelectionPath() == null, "initially no path");

        m.setRowMapper(mapper);
        check(m.getRowMapper() == mapper, "getRowMapper round-trip");

        // setSelectionPath / add in DISCONTIGUOUS mode.
        m.setSelectionPath(p0);
        check(m.getSelectionPath() == p0, "setSelectionPath p0");
        check(m.getSelectionCount() == 1, "count 1");
        check(m.isSelectionEmpty() == false, "not empty");
        check(m.isPathSelected(p0) == true, "p0 selected");
        check(m.isPathSelected(p1) == false, "p1 not selected");

        m.addSelectionPath(p1);
        m.addSelectionPath(p2);
        check(m.getSelectionCount() == 3, "DISCONTIGUOUS count 3");
        check(samePaths(m.getSelectionPaths(), p0, p1, p2), "selection order preserved");
        check(sameInts(m.getSelectionRows(), 0, 2, 5), "selection rows from mapper");
        check(m.getMinSelectionRow() == 0, "min row 0");
        check(m.getMaxSelectionRow() == 5, "max row 5");
        check(m.isRowSelected(2) == true, "row 2 selected");
        check(m.isRowSelected(1) == false, "row 1 not selected");
        check(m.getLeadSelectionPath() == p2, "lead path p2");
        check(m.getLeadSelectionRow() == 5, "lead row 5");

        // removeSelectionPath (remaining order is unspecified; assert membership).
        m.removeSelectionPath(p1);
        check(m.getSelectionCount() == 2, "count 2 after remove");
        check(m.isPathSelected(p1) == false, "p1 removed");
        check(m.isPathSelected(p0) == true, "p0 still selected");
        check(m.isPathSelected(p2) == true, "p2 still selected");

        // Listener firing.
        SelRecorder rec = new SelRecorder();
        m.addTreeSelectionListener(rec);
        m.addSelectionPath(p1);
        check(rec.events.size() == 1, "listener fired on add");
        check(rec.events.get(0).isAddedPath(p1) == true, "add event isAddedPath(p1)");
        check(rec.events.get(0).isAddedPath() == true, "add event isAddedPath");
        m.removeSelectionPath(p1);
        check(rec.events.size() == 2, "listener fired on remove");
        check(rec.events.get(1).isAddedPath() == false, "remove event isAddedPath false");
        check(m.getTreeSelectionListeners().length == 1, "getTreeSelectionListeners");
        m.removeTreeSelectionListener(rec);
        check(m.getTreeSelectionListeners().length == 0, "removeTreeSelectionListener");

        // setSelectionMode SINGLE: only first path kept.
        m.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        check(m.getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION, "mode SINGLE");
        m.setSelectionPaths(new TreePath[] { p0, p1 });
        check(m.getSelectionCount() == 1 && m.getSelectionPath() == p0, "SINGLE keeps first");

        // setSelectionMode CONTIGUOUS: non-contiguous -> first path only.
        m.setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        check(m.getSelectionMode() == TreeSelectionModel.CONTIGUOUS_TREE_SELECTION, "mode CONTIGUOUS");
        m.setSelectionPaths(new TreePath[] { p0, p2 });
        check(m.getSelectionCount() == 1 && m.getSelectionPath() == p0, "CONTIGUOUS non-contiguous keeps first");

        // Back to DISCONTIGUOUS, setSelectionPaths with null clears.
        m.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        m.setSelectionPaths(new TreePath[] { p0, p1, p2 });
        check(m.getSelectionCount() == 3, "DISCONTIGUOUS all 3");
        m.setSelectionPath(null);
        check(m.isSelectionEmpty() == true && m.getSelectionCount() == 0, "setSelectionPath(null) clears");

        m.setSelectionPaths(new TreePath[] { p0, p1 });
        check(m.getSelectionCount() == 2, "setSelectionPaths");
        m.clearSelection();
        check(m.isSelectionEmpty() == true, "clearSelection empty");
        check(m.getSelectionCount() == 0, "clearSelection count 0");
        check(m.getSelectionPath() == null, "clearSelection null path");

        // resetRowSelection recomputes rows (no exception, still valid).
        m.setSelectionPath(p2);
        m.resetRowSelection();
        check(m.getSelectionCount() == 1 && m.isPathSelected(p2) == true, "resetRowSelection keeps path");

        // clone() produces an independent selection model.
        try {
            Object clone = m.clone();
            check(clone instanceof DefaultTreeSelectionModel, "clone type");
            check(clone != m, "clone distinct");
            check(((DefaultTreeSelectionModel) clone).getSelectionCount() == 1, "clone copies selection");
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("clone threw " + ex);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

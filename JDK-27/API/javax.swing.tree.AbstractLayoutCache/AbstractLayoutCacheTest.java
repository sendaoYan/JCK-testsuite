/*
 * @test
 * @summary javax.swing.tree.AbstractLayoutCache conformance: white-box structure + black-box behavior.
 * @run main AbstractLayoutCacheTest
 */

import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

public class AbstractLayoutCacheTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.AbstractLayoutCache");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.tree.RowMapper"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final TreePath fixedPath = new TreePath("root");

        AbstractLayoutCache cache = new AbstractLayoutCache() {
            public boolean isExpanded(TreePath path) { return false; }
            public Rectangle getBounds(TreePath path, Rectangle placeIn) {
                if (path == null) return null;
                Rectangle r = (placeIn != null) ? placeIn : new Rectangle();
                r.x = 0; r.y = 0; r.width = 10; r.height = 10;
                return r;
            }
            public TreePath getPathForRow(int row) { return row == 0 ? fixedPath : null; }
            public int getRowForPath(TreePath path) { return path.equals(fixedPath) ? 0 : -1; }
            public TreePath getPathClosestTo(int x, int y) { return fixedPath; }
            public Enumeration<TreePath> getVisiblePathsFrom(TreePath path) {
                return Collections.enumeration(Collections.singletonList(fixedPath));
            }
            public int getVisibleChildCount(TreePath path) { return 0; }
            public void setExpandedState(TreePath path, boolean expanded) { }
            public boolean getExpandedState(TreePath path) { return false; }
            public int getRowCount() { return 1; }
            public void invalidateSizes() { }
            public void invalidatePathBounds(TreePath path) { }
            public void treeNodesChanged(TreeModelEvent e) { }
            public void treeNodesInserted(TreeModelEvent e) { }
            public void treeNodesRemoved(TreeModelEvent e) { }
            public void treeStructureChanged(TreeModelEvent e) { }
        };

        // NodeDimensions round-trip.
        AbstractLayoutCache.NodeDimensions nd = new AbstractLayoutCache.NodeDimensions() {
            public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle placeIn) {
                Rectangle r = (placeIn != null) ? placeIn : new Rectangle();
                r.x = 0; r.y = 0; r.width = 8; r.height = 8;
                return r;
            }
        };
        cache.setNodeDimensions(nd);
        check(cache.getNodeDimensions() == nd, "getNodeDimensions round-trip");

        // Model round-trip.
        DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
        cache.setModel(model);
        check(cache.getModel() == model, "getModel round-trip");

        // Root visible + row height round-trips.
        cache.setRootVisible(true);
        check(cache.isRootVisible() == true, "setRootVisible/isRootVisible");
        cache.setRowHeight(5);
        check(cache.getRowHeight() == 5, "setRowHeight/getRowHeight");

        // Selection model round-trip.
        DefaultTreeSelectionModel sm = new DefaultTreeSelectionModel();
        cache.setSelectionModel(sm);
        check(cache.getSelectionModel() == sm, "getSelectionModel round-trip");

        // RowMapper + abstract methods exercised through concrete calls.
        check(cache.getRowCount() == 1, "getRowCount");
        check(cache.getPathForRow(0).equals(fixedPath), "getPathForRow");
        check(cache.getRowForPath(fixedPath) == 0, "getRowForPath");
        check(cache.getPathClosestTo(3, 4).equals(fixedPath), "getPathClosestTo");
        check(cache.isExpanded(fixedPath) == false, "isExpanded");
        check(cache.getExpandedState(fixedPath) == false, "getExpandedState");
        check(cache.getVisibleChildCount(fixedPath) == 0, "getVisibleChildCount");
        Rectangle b = cache.getBounds(fixedPath, null);
        check(b != null && b.width == 10 && b.height == 10, "getBounds");

        // getPreferredHeight / getPreferredWidth use getRowCount + getBounds.
        check(cache.getPreferredHeight() == 10, "getPreferredHeight 10");
        check(cache.getPreferredWidth(null) == 10, "getPreferredWidth 10");

        // getRowsForPaths (RowMapper contract).
        int[] rows = cache.getRowsForPaths(new TreePath[] { fixedPath });
        check(rows != null && rows.length == 1 && rows[0] == 0, "getRowsForPaths");
        check(cache.getRowsForPaths(null) == null, "getRowsForPaths null");

        // Abstract mutation callbacks are no-ops but must not throw.
        cache.setExpandedState(fixedPath, true);
        cache.invalidateSizes();
        cache.invalidatePathBounds(fixedPath);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

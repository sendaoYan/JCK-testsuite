/*
 * @test
 * @summary javax.swing.tree.VariableHeightLayoutCache conformance: white-box structure + black-box behavior.
 * @run main VariableHeightLayoutCacheTest
 */

import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.VariableHeightLayoutCache;

public class VariableHeightLayoutCacheTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.VariableHeightLayoutCache");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.tree.AbstractLayoutCache"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static List<String> pathNames(Enumeration<TreePath> e) {
        List<String> out = new ArrayList<>();
        while (e.hasMoreElements()) {
            out.add(String.valueOf(e.nextElement().getLastPathComponent()));
        }
        return out;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultMutableTreeNode child0 = new DefaultMutableTreeNode("child0", false);
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("child1", true);
        DefaultMutableTreeNode grand = new DefaultMutableTreeNode("grand", false);
        root.add(child0);
        root.add(child1);
        child1.add(grand);
        DefaultTreeModel model = new DefaultTreeModel(root);

        TreePath rootPath = new TreePath(root);
        TreePath child0Path = rootPath.pathByAddingChild(child0);
        TreePath child1Path = rootPath.pathByAddingChild(child1);
        TreePath grandPath = child1Path.pathByAddingChild(grand);

        // NodeDimensions: width depends on depth, fixed height 15.
        AbstractLayoutCache.NodeDimensions nd = new AbstractLayoutCache.NodeDimensions() {
            public Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle placeIn) {
                Rectangle r = (placeIn != null) ? placeIn : new Rectangle();
                r.x = depth * 10;
                r.y = 0;
                r.width = 20;
                r.height = 15;
                return r;
            }
        };

        VariableHeightLayoutCache cache = new VariableHeightLayoutCache();
        cache.setNodeDimensions(nd);
        check(cache.getNodeDimensions() == nd, "getNodeDimensions round-trip");
        cache.setRootVisible(true);
        check(cache.isRootVisible() == true, "setRootVisible true");
        cache.setModel(model);
        check(cache.getModel() == model, "getModel round-trip");

        // Variable height mode: rowHeight <= 0.
        check(cache.getRowHeight() == 0, "variable mode rowHeight 0");

        check(cache.getRowCount() == 3, "row count 3");
        check(cache.isExpanded(rootPath) == true, "root expanded");
        check(cache.getExpandedState(rootPath) == true, "getExpandedState root");
        check(cache.getExpandedState(child1Path) == false, "child1 collapsed");
        check(cache.getVisibleChildCount(rootPath) == 2, "root visible children 2");
        check(cache.getVisibleChildCount(child1Path) == 0, "collapsed child1 visible children 0");

        check(cache.getRowForPath(rootPath) == 0, "row root 0");
        check(cache.getRowForPath(child0Path) == 1, "row child0 1");
        check(cache.getRowForPath(child1Path) == 2, "row child1 2");
        check(cache.getRowForPath(grandPath) == -1, "grand hidden row -1");
        check(cache.getPathForRow(0).equals(rootPath), "pathForRow 0");
        check(cache.getPathForRow(2).equals(child1Path), "pathForRow 2");
        check(cache.getPathForRow(3) == null, "pathForRow out of range null");

        Rectangle rb = cache.getBounds(rootPath, null);
        check(rb != null && rb.x == 0 && rb.y == 0 && rb.width == 20 && rb.height == 15, "root bounds");
        Rectangle cb = cache.getBounds(child0Path, null);
        check(cb != null && cb.x == 10 && cb.y == 15 && cb.width == 20 && cb.height == 15, "child bounds");
        check(cache.getPreferredHeight() == 45, "preferred height 45");
        check(cache.getPreferredWidth(null) == 30, "preferred width 30");

        int[] rows = cache.getRowsForPaths(new TreePath[] { rootPath, child1Path });
        check(rows != null && rows.length == 2 && rows[0] == 0 && rows[1] == 2, "getRowsForPaths");
        check(cache.getPathClosestTo(0, 0).equals(rootPath), "pathClosestTo root");
        check(cache.getPathClosestTo(0, 100).equals(child1Path), "pathClosestTo last row");
        check(pathNames(cache.getVisiblePathsFrom(rootPath)).equals(java.util.Arrays.asList("root", "child0", "child1")), "visible paths order");

        // Expand child1 -> grand visible.
        cache.setExpandedState(child1Path, true);
        check(cache.getExpandedState(child1Path) == true, "child1 expanded");
        check(cache.getRowCount() == 4, "row count 4 after expand");
        check(cache.getVisibleChildCount(child1Path) == 1, "expanded child1 visible children 1");
        check(cache.getRowForPath(grandPath) == 3, "grand row 3");
        check(cache.getPathForRow(3).equals(grandPath), "pathForRow 3 grand");
        Rectangle gb = cache.getBounds(grandPath, null);
        check(gb != null && gb.x == 20 && gb.y == 45 && gb.width == 20 && gb.height == 15, "grand bounds");
        check(cache.getPreferredHeight() == 60, "preferred height 60 after expand");
        check(cache.getPreferredWidth(null) == 40, "preferred width 40 after expand");

        // Collapse again.
        cache.setExpandedState(child1Path, false);
        check(cache.getRowCount() == 3, "row count 3 after collapse");
        check(cache.getRowForPath(grandPath) == -1, "grand hidden after collapse");

        cache.invalidateSizes();
        cache.invalidatePathBounds(child0Path);

        // setRowHeight round-trip (positive switches to fixed height, 0 back to variable).
        cache.setRowHeight(20);
        check(cache.getRowHeight() == 20, "setRowHeight 20");
        cache.setRowHeight(0);
        check(cache.getRowHeight() == 0, "setRowHeight 0 back to variable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

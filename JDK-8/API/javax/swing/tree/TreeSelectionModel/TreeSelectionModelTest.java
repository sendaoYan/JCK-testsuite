/*
 * @test
 * @summary javax.swing.tree.TreeSelectionModel conformance: type shape via reflection
 *          (headless-safe) including selection-mode constants, plus polymorphic use.
 * @run main TreeSelectionModelTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class TreeSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Minimal implementation used to exercise the interface reference.
    static class SelModel implements TreeSelectionModel {
        TreePath setPath, addedPath, removedPath;
        TreePath[] setPaths, addedPaths, removedPaths;
        int mode = TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
        boolean cleared;
        public void setSelectionMode(int mode) { this.mode = mode; }
        public int getSelectionMode() { return mode; }
        public void setSelectionPath(TreePath path) { setPath = path; }
        public void setSelectionPaths(TreePath[] paths) { setPaths = paths; }
        public void addSelectionPath(TreePath path) { addedPath = path; }
        public void addSelectionPaths(TreePath[] paths) { addedPaths = paths; }
        public void removeSelectionPath(TreePath path) { removedPath = path; }
        public void removeSelectionPaths(TreePath[] paths) { removedPaths = paths; }
        public TreePath getSelectionPath() { return setPath; }
        public TreePath[] getSelectionPaths() { return setPaths; }
        public int getSelectionCount() { return setPaths == null ? 0 : setPaths.length; }
        public boolean isPathSelected(TreePath path) { return path != null && path.equals(setPath); }
        public boolean isSelectionEmpty() { return setPath == null && setPaths == null; }
        public void clearSelection() { cleared = true; setPath = null; setPaths = null; }
        public TreePath getLeadSelectionPath() { return setPath; }
        public int getLeadSelectionRow() { return -1; }
        public int getMaxSelectionRow() { return -1; }
        public int getMinSelectionRow() { return -1; }
        public int[] getSelectionRows() { return new int[0]; }
        public boolean isRowSelected(int row) { return false; }
        public void resetRowSelection() { }
        public void setRowMapper(javax.swing.tree.RowMapper newMapper) { }
        public javax.swing.tree.RowMapper getRowMapper() { return null; }
        public void addTreeSelectionListener(javax.swing.event.TreeSelectionListener x) { }
        public void removeTreeSelectionListener(javax.swing.event.TreeSelectionListener x) { }
        public Object getLastSelectedPathComponent() { return setPath == null ? null : setPath.getLastPathComponent(); }
        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) { }
        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) { }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeSelectionModel", false, TreeSelectionModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSelectionMode"), "method setSelectionMode");
        check(names.contains("getSelectionMode"), "method getSelectionMode");
        check(names.contains("setSelectionPath"), "method setSelectionPath");
        check(names.contains("setSelectionPaths"), "method setSelectionPaths");
        check(names.contains("addSelectionPath"), "method addSelectionPath");
        check(names.contains("addSelectionPaths"), "method addSelectionPaths");
        check(names.contains("removeSelectionPath"), "method removeSelectionPath");
        check(names.contains("removeSelectionPaths"), "method removeSelectionPaths");

        // Selection-mode constants: public static final int with documented values.
        Field single = c.getField("SINGLE_TREE_SELECTION");
        check(single.getInt(null) == 1, "SINGLE_TREE_SELECTION == 1");
        check(Modifier.isStatic(single.getModifiers()) && Modifier.isFinal(single.getModifiers()), "constant modifiers single");
        check(c.getField("CONTIGUOUS_TREE_SELECTION").getInt(null) == 2, "CONTIGUOUS_TREE_SELECTION == 2");
        check(c.getField("DISCONTIGUOUS_TREE_SELECTION").getInt(null) == 4, "DISCONTIGUOUS_TREE_SELECTION == 4");

        // Known implementations.
        Class<?> impl = Class.forName("javax.swing.tree.DefaultTreeSelectionModel", false, TreeSelectionModelTest.class.getClassLoader());
        check(c.isAssignableFrom(impl), "DefaultTreeSelectionModel is a TreeSelectionModel");
        // The SELECTION_MODE_PROPERTY change-support name lives on DefaultTreeSelectionModel.
        check("selectionMode".equals(impl.getField("SELECTION_MODE_PROPERTY").get(null)),
                "SELECTION_MODE_PROPERTY is 'selectionMode'");

        // Polymorphic contract through the interface reference.
        TreeSelectionModel m = new SelModel();
        TreePath p = new TreePath("a");
        m.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        check(m.getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION, "mode through interface");
        m.setSelectionPath(p);
        check(m.getSelectionPath() == p, "set/getSelectionPath through interface");
        check(m.getSelectionCount() == 0, "count through interface");
        check(m.isPathSelected(p), "isPathSelected through interface");
        check(!m.isSelectionEmpty(), "not empty through interface");
        check(c.getMethod("addPropertyChangeListener", java.beans.PropertyChangeListener.class) != null,
                "addPropertyChangeListener declared");
        check(c.getMethod("removePropertyChangeListener", java.beans.PropertyChangeListener.class) != null,
                "removePropertyChangeListener declared");
        check(c.getMethod("getLeadSelectionPath").getReturnType() == TreePath.class, "getLeadSelectionPath returns TreePath");
        check(names.size() == 27, "exactly the 27 specified methods, got " + names.size() + ": " + names);
        TreePath[] batch = new TreePath[]{p, new TreePath("b")};
        m.setSelectionPaths(batch);
        check(((SelModel) m).setPaths == batch, "setSelectionPaths through interface");
        m.addSelectionPath(p);
        check(((SelModel) m).addedPath == p, "addSelectionPath through interface");
        m.addSelectionPaths(batch);
        check(((SelModel) m).addedPaths == batch, "addSelectionPaths through interface");
        m.removeSelectionPath(p);
        check(((SelModel) m).removedPath == p, "removeSelectionPath through interface");
        m.removeSelectionPaths(batch);
        check(((SelModel) m).removedPaths == batch, "removeSelectionPaths through interface");
        m.clearSelection();
        check(m.isSelectionEmpty() && ((SelModel) m).cleared, "clearSelection through interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

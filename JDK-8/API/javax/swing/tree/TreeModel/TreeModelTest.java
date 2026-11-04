/*
 * @test
 * @summary javax.swing.tree.TreeModel conformance: type shape via reflection
 *          (headless-safe), interface completeness, and polymorphic use.
 * @run main TreeModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TreeModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Minimal implementation used to exercise the interface reference.
    static class ArrayModel implements TreeModel {
        Object root = "root";
        int valueChanges, listenerAdds;
        TreeModelListener listener;
        public Object getRoot() { return root; }
        public Object getChild(Object parent, int index) { return "child" + index; }
        public int getChildCount(Object parent) { return 2; }
        public boolean isLeaf(Object node) { return false; }
        public void valueForPathChanged(TreePath path, Object newValue) { valueChanges++; root = newValue; }
        public int getIndexOfChild(Object parent, Object child) { return "child1".equals(child) ? 1 : 0; }
        public void addTreeModelListener(TreeModelListener l) { listenerAdds++; listener = l; }
        public void removeTreeModelListener(TreeModelListener l) { listener = null; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeModel", false, TreeModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRoot"), "method getRoot");
        check(names.contains("getChild"), "method getChild");
        check(names.contains("getChildCount"), "method getChildCount");
        check(names.contains("isLeaf"), "method isLeaf");
        check(names.contains("valueForPathChanged"), "method valueForPathChanged");
        check(names.contains("getIndexOfChild"), "method getIndexOfChild");
        check(names.contains("addTreeModelListener"), "method addTreeModelListener");
        check(names.contains("removeTreeModelListener"), "method removeTreeModelListener");
        check(names.size() == 8, "exactly the 8 specified methods, got " + names.size() + ": " + names);
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract method " + m.getName());
        }
        Method getRoot = c.getMethod("getRoot");
        check(getRoot.getReturnType() == Object.class, "getRoot returns Object");
        Method getChild = c.getMethod("getChild", Object.class, int.class);
        check(getChild.getReturnType() == Object.class, "getChild returns Object");
        check(c.getMethod("getChildCount", Object.class).getReturnType() == int.class, "getChildCount returns int");

        // Known implementations.
        Class<?> impl = Class.forName("javax.swing.tree.DefaultTreeModel", false, TreeModelTest.class.getClassLoader());
        check(c.isAssignableFrom(impl), "DefaultTreeModel is a TreeModel");

        // Polymorphic contract through the interface reference.
        TreeModel m = new ArrayModel();
        check("root".equals(m.getRoot()), "getRoot through interface");
        check("child0".equals(m.getChild("root", 0)) && "child1".equals(m.getChild("root", 1)), "getChild through interface");
        check(m.getChildCount("root") == 2, "getChildCount through interface");
        check(!m.isLeaf("root"), "isLeaf through interface");
        check(m.getIndexOfChild("root", "child1") == 1, "getIndexOfChild through interface");
        m.valueForPathChanged(new TreePath("root"), "changed");
        check("changed".equals(m.getRoot()) && ((ArrayModel) m).valueChanges == 1, "valueForPathChanged through interface");
        TreeModelListener noop = new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) { }
            public void treeNodesInserted(TreeModelEvent e) { }
            public void treeNodesRemoved(TreeModelEvent e) { }
            public void treeStructureChanged(TreeModelEvent e) { }
        };
        m.addTreeModelListener(noop);
        check(((ArrayModel) m).listener == noop && ((ArrayModel) m).listenerAdds == 1, "addTreeModelListener through interface");
        m.removeTreeModelListener(noop);
        check(((ArrayModel) m).listener == null, "removeTreeModelListener through interface");

        // DefaultTreeModel also answers through the same interface shape.
        DefaultMutableTreeNode r = new DefaultMutableTreeNode("R");
        TreeModel dm = new DefaultTreeModel(r);
        check(dm.getRoot() == r && dm.getChildCount(r) == 0 && dm.isLeaf(r), "DefaultTreeModel via interface");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.JTree.AccessibleJTree conformance: type shape via reflection (headless-safe).
 * @run main AccessibleJTreeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleJTreeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JTree$AccessibleJTree", false, AccessibleJTreeTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleSelection"), "implements javax.accessibility.AccessibleSelection");
        check(hasInterface(c, "javax.swing.event.TreeSelectionListener"), "implements javax.swing.event.TreeSelectionListener");
        check(hasInterface(c, "javax.swing.event.TreeModelListener"), "implements javax.swing.event.TreeModelListener");
        check(hasInterface(c, "javax.swing.event.TreeExpansionListener"), "implements javax.swing.event.TreeExpansionListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("valueChanged"), "method valueChanged");
        check(names.contains("fireVisibleDataPropertyChange"), "method fireVisibleDataPropertyChange");
        check(names.contains("treeNodesChanged"), "method treeNodesChanged");
        check(names.contains("treeNodesInserted"), "method treeNodesInserted");
        check(names.contains("treeNodesRemoved"), "method treeNodesRemoved");
        check(names.contains("treeStructureChanged"), "method treeStructureChanged");
        check(names.contains("treeCollapsed"), "method treeCollapsed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

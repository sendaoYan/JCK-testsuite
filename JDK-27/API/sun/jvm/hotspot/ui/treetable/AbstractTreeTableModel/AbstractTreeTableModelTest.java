/*
 * @test
 * @summary sun.jvm.hotspot.ui.treetable.AbstractTreeTableModel conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.treetable:+open
 * @run main AbstractTreeTableModelTest
 */
import java.lang.reflect.*;

public class AbstractTreeTableModelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.treetable.AbstractTreeTableModel", false, AbstractTreeTableModelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.ui.treetable.TreeTableModel"), "interface sun.jvm.hotspot.ui.treetable.TreeTableModel");
        check(hasMethod(c, "getRoot"), "method getRoot");
        check(hasMethod(c, "isLeaf"), "method isLeaf");
        check(hasMethod(c, "valueForPathChanged"), "method valueForPathChanged");
        check(hasMethod(c, "addTreeModelListener"), "method addTreeModelListener");
        check(hasMethod(c, "removeTreeModelListener"), "method removeTreeModelListener");
        check(hasMethod(c, "getColumnClass"), "method getColumnClass");
        check(hasMethod(c, "isCellEditable"), "method isCellEditable");
        check(hasMethod(c, "setValueAt"), "method setValueAt");
        check(hasMethod(c, "getIndexOfChild"), "method getIndexOfChild");
        check(hasMethod(c, "fireTreeStructureChanged"), "method fireTreeStructureChanged");
        check(hasMethod(c, "fireTreeNodesChanged"), "method fireTreeNodesChanged");
        check(hasMethod(c, "fireTreeNodesInserted"), "method fireTreeNodesInserted");
        check(hasMethod(c, "fireTreeNodesRemoved"), "method fireTreeNodesRemoved");
        check(hasField(c, "root"), "field root");
        check(hasField(c, "listenerList"), "field listenerList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

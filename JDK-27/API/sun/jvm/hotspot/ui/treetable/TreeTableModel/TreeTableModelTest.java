/*
 * @test
 * @summary sun.jvm.hotspot.ui.treetable.TreeTableModel conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.treetable:+open
 * @run main TreeTableModelTest
 */
import java.lang.reflect.*;

public class TreeTableModelTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.treetable.TreeTableModel", false, TreeTableModelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "javax.swing.tree.TreeModel"), "interface javax.swing.tree.TreeModel");
        check(hasMethod(c, "getColumnCount"), "method getColumnCount");
        check(hasMethod(c, "getColumnName"), "method getColumnName");
        check(hasMethod(c, "getColumnClass"), "method getColumnClass");
        check(hasMethod(c, "isCellEditable"), "method isCellEditable");
        check(hasMethod(c, "getValueAt"), "method getValueAt");
        check(hasMethod(c, "setValueAt"), "method setValueAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

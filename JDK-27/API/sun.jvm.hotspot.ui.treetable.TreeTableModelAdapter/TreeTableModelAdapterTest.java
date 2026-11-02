/*
 * @test
 * @summary sun.jvm.hotspot.ui.treetable.TreeTableModelAdapter conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.treetable:+open
 * @run main TreeTableModelAdapterTest
 */
import java.lang.reflect.*;

public class TreeTableModelAdapterTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.treetable.TreeTableModelAdapter", false, TreeTableModelAdapterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.table.AbstractTableModel".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getRowCount"), "method getRowCount");
        check(hasMethod(c, "getColumnCount"), "method getColumnCount");
        check(hasMethod(c, "getColumnName"), "method getColumnName");
        check(hasMethod(c, "getColumnClass"), "method getColumnClass");
        check(hasMethod(c, "isCellEditable"), "method isCellEditable");
        check(hasMethod(c, "getValueAt"), "method getValueAt");
        check(hasMethod(c, "setValueAt"), "method setValueAt");
        check(hasMethod(c, "nodeForRow"), "method nodeForRow");
        check(hasMethod(c, "delayedFireTableDataChanged"), "method delayedFireTableDataChanged");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

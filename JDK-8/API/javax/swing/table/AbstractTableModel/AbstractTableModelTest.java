/*
 * @test
 * @summary javax.swing.table.AbstractTableModel conformance: type shape via reflection (headless-safe).
 * @run main AbstractTableModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractTableModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.AbstractTableModel", false, AbstractTableModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.table.TableModel"), "implements javax.swing.table.TableModel");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getColumnName"), "method getColumnName");
        check(names.contains("findColumn"), "method findColumn");
        check(names.contains("getColumnClass"), "method getColumnClass");
        check(names.contains("isCellEditable"), "method isCellEditable");
        check(names.contains("setValueAt"), "method setValueAt");
        check(names.contains("addTableModelListener"), "method addTableModelListener");
        check(names.contains("removeTableModelListener"), "method removeTableModelListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

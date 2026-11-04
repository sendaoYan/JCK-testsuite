/*
 * @test
 * @summary javax.swing.AbstractCellEditor conformance: type shape via reflection (headless-safe).
 * @run main AbstractCellEditorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AbstractCellEditorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractCellEditor", false, AbstractCellEditorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.CellEditor"), "implements javax.swing.CellEditor");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isCellEditable"), "method isCellEditable");
        check(names.contains("shouldSelectCell"), "method shouldSelectCell");
        check(names.contains("stopCellEditing"), "method stopCellEditing");
        check(names.contains("cancelCellEditing"), "method cancelCellEditing");
        check(names.contains("addCellEditorListener"), "method addCellEditorListener");
        check(names.contains("removeCellEditorListener"), "method removeCellEditorListener");
        check(names.contains("getCellEditorListeners"), "method getCellEditorListeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

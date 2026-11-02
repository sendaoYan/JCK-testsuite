/*
 * @test
 * @summary javax.swing.tree.DefaultTreeCellEditor conformance: type shape via reflection (headless-safe).
 * @run main DefaultTreeCellEditorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultTreeCellEditorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultTreeCellEditor", false, DefaultTreeCellEditorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.event.ActionListener"), "implements java.awt.event.ActionListener");
        check(hasInterface(c, "javax.swing.tree.TreeCellEditor"), "implements javax.swing.tree.TreeCellEditor");
        check(hasInterface(c, "javax.swing.event.TreeSelectionListener"), "implements javax.swing.event.TreeSelectionListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setBorderSelectionColor"), "method setBorderSelectionColor");
        check(names.contains("getBorderSelectionColor"), "method getBorderSelectionColor");
        check(names.contains("setFont"), "method setFont");
        check(names.contains("getFont"), "method getFont");
        check(names.contains("getTreeCellEditorComponent"), "method getTreeCellEditorComponent");
        check(names.contains("getCellEditorValue"), "method getCellEditorValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

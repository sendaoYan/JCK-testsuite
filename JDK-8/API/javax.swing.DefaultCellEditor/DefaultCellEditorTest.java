/*
 * @test
 * @summary javax.swing.DefaultCellEditor conformance: type shape via reflection (headless-safe).
 * @run main DefaultCellEditorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultCellEditorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultCellEditor", false, DefaultCellEditorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractCellEditor"), "superclass");
        check(hasInterface(c, "javax.swing.table.TableCellEditor"), "implements javax.swing.table.TableCellEditor");
        check(hasInterface(c, "javax.swing.tree.TreeCellEditor"), "implements javax.swing.tree.TreeCellEditor");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getComponent"), "method getComponent");
        check(names.contains("setClickCountToStart"), "method setClickCountToStart");
        check(names.contains("getClickCountToStart"), "method getClickCountToStart");
        check(names.contains("getCellEditorValue"), "method getCellEditorValue");
        check(names.contains("isCellEditable"), "method isCellEditable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

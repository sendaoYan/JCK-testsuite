/*
 * @test
 * @summary javax.swing.JTable conformance: type shape via reflection (headless-safe).
 * @run main JTableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class JTableTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JTable", false, JTableTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.swing.event.TableModelListener"), "implements javax.swing.event.TableModelListener");
        check(hasInterface(c, "javax.swing.Scrollable"), "implements javax.swing.Scrollable");
        check(hasInterface(c, "javax.swing.event.TableColumnModelListener"), "implements javax.swing.event.TableColumnModelListener");
        check(hasInterface(c, "javax.swing.event.ListSelectionListener"), "implements javax.swing.event.ListSelectionListener");
        check(hasInterface(c, "javax.swing.event.CellEditorListener"), "implements javax.swing.event.CellEditorListener");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        check(hasInterface(c, "javax.swing.event.RowSorterListener"), "implements javax.swing.event.RowSorterListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addNotify"), "method addNotify");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

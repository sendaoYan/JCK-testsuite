/*
 * @test
 * @summary javax.swing.JTable conformance: type shape and supertypes.
 * @run main JTableTest
 */

import java.lang.reflect.Modifier;

public class JTableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JTable");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 7, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.TableModelListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.Scrollable"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.event.TableColumnModelListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.swing.event.ListSelectionListener"), "interface 3");
        check(c.getInterfaces()[4].getName().equals("javax.swing.event.CellEditorListener"), "interface 4");
        check(c.getInterfaces()[5].getName().equals("javax.accessibility.Accessible"), "interface 5");
        check(c.getInterfaces()[6].getName().equals("javax.swing.event.RowSorterListener"), "interface 6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

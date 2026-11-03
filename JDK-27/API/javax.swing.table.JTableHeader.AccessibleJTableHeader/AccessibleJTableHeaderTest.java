/*
 * @test
 * @summary javax.swing.table.JTableHeader.AccessibleJTableHeader conformance: white-box structure + black-box behavior.
 * @run main AccessibleJTableHeaderTest
 */

import java.awt.Point;
import java.lang.reflect.Modifier;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class AccessibleJTableHeaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.JTableHeader$AccessibleJTableHeader");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent$AccessibleJComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTableColumnModel cm = new DefaultTableColumnModel();
        cm.addColumn(new TableColumn(0));
        cm.addColumn(new TableColumn(1));
        cm.addColumn(new TableColumn(2));
        JTableHeader header = new JTableHeader(cm);

        AccessibleContext ac = header.getAccessibleContext();
        check(ac != null, "getAccessibleContext non-null");
        check(ac.getAccessibleRole() == AccessibleRole.PANEL, "accessible role PANEL");
        check(ac.getAccessibleChildrenCount() == 3, "accessible children == column count");

        // children correspond to columns; out-of-range returns null
        Accessible c0 = ac.getAccessibleChild(0);
        Accessible c2 = ac.getAccessibleChild(2);
        check(c0 != null, "accessible child 0 non-null");
        check(c2 != null, "accessible child 2 non-null");
        check(ac.getAccessibleChild(-1) == null, "accessible child -1 null");
        check(ac.getAccessibleChild(3) == null, "accessible child 3 null");

        // getAccessibleAt with an x outside any column returns null (concrete method, invoke reflectively)
        try {
            Object at = ac.getClass().getMethod("getAccessibleAt", Point.class).invoke(ac, new Point(-5, 0));
            check(at == null, "getAccessibleAt negative x null");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("getAccessibleAt invoke failed: " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.table.DefaultTableCellRenderer conformance: white-box structure + black-box behavior.
 * @run main DefaultTableCellRendererTest
 */

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Modifier;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class DefaultTableCellRendererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.DefaultTableCellRenderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JLabel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.table.TableCellRenderer"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        check(r != null, "constructs");
        check(r.isOpaque(), "isOpaque default true");

        // foreground/background round-trip (before rendering overrides them)
        r.setForeground(Color.RED);
        r.setBackground(Color.BLUE);
        check(Color.RED.equals(r.getForeground()), "set/get foreground");
        check(Color.BLUE.equals(r.getBackground()), "set/get background");

        // renderer returns itself as the Component and sets text from value
        JTable table = new JTable();
        Component comp = r.getTableCellRendererComponent(table, "Hello", false, false, 0, 0);
        check(comp == r, "getTableCellRendererComponent returns renderer");
        check("Hello".equals(r.getText()), "renderer sets text from value");
        r.getTableCellRendererComponent(table, Integer.valueOf(42), false, false, 1, 0);
        check("42".equals(r.getText()), "renderer stringifies Integer");
        r.getTableCellRendererComponent(table, null, false, false, 2, 0);
        check("".equals(r.getText()), "renderer null -> empty string");
        r.getTableCellRendererComponent(table, "selected", true, false, 3, 0);
        check("selected".equals(r.getText()), "renderer selected still sets text");

        // polymorphic TableCellRenderer usage
        TableCellRenderer poly = r;
        Component c2 = poly.getTableCellRendererComponent(table, "poly", false, false, 4, 0);
        check(c2 == r, "TableCellRenderer view returns same component");
        check("poly".equals(r.getText()), "TableCellRenderer view sets text");

        // setText/getText (JLabel API)
        r.setText("explicit");
        check("explicit".equals(r.getText()), "setText/getText");

        // updateUI is callable headless
        r.updateUI();
        check(true, "updateUI callable");

        // null table short-circuit returns the renderer without NPE
        Component c3 = r.getTableCellRendererComponent(null, "x", false, false, 0, 0);
        check(c3 == r, "null table returns renderer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

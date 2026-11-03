/*
 * @test
 * @summary javax.swing.table.TableCellRenderer conformance: white-box structure + black-box behavior.
 * @run main TableCellRendererTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TableCellRendererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableCellRenderer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TableCellRenderer renderer = (JTable table, Object value, boolean isSelected,
                                      boolean hasFocus, int row, int column) -> {
            JLabel l = new JLabel();
            l.setText(value == null ? "" : value.toString());
            return l;
        };
        JTable table = new JTable();
        Component c1 = renderer.getTableCellRendererComponent(table, "Hello", false, false, 0, 0);
        check(c1 != null, "returns a component");
        check(c1 instanceof JLabel, "component is JLabel");
        check("Hello".equals(((JLabel) c1).getText()), "text set from value");
        Component c2 = renderer.getTableCellRendererComponent(table, Integer.valueOf(7), true, true, 1, 2);
        check("7".equals(((JLabel) c2).getText()), "Integer stringified");
        Component c3 = renderer.getTableCellRendererComponent(table, null, false, false, 3, 4);
        check("".equals(((JLabel) c3).getText()), "null -> empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

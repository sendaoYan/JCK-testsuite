/*
 * @test
 * @summary javax.swing.table.DefaultTableCellRenderer.UIResource conformance: white-box structure + black-box behavior.
 * @run main UIResourceTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;
import javax.swing.JTable;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;

public class UIResourceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.DefaultTableCellRenderer$UIResource");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.table.DefaultTableCellRenderer"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTableCellRenderer.UIResource r = new DefaultTableCellRenderer.UIResource();
        check(r instanceof DefaultTableCellRenderer, "instance of DefaultTableCellRenderer");
        check(r instanceof UIResource, "instance of UIResource");

        // behaves as a renderer: returns itself and sets text
        JTable table = new JTable();
        Component comp = r.getTableCellRendererComponent(table, "value", false, false, 0, 0);
        check(comp == r, "getTableCellRendererComponent returns itself");
        check("value".equals(r.getText()), "renderer sets text");

        // JLabel text round-trip
        r.setText("label");
        check("label".equals(r.getText()), "setText/getText");

        // foreground/background inherited from JComponent
        r.setForeground(java.awt.Color.GREEN);
        check(java.awt.Color.GREEN.equals(r.getForeground()), "set/get foreground");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

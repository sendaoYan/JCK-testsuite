/*
 * @test
 * @summary javax.swing.tree.TreeCellRenderer conformance: white-box structure + black-box behavior.
 * @run main TreeCellRendererTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class TreeCellRendererTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeCellRenderer");
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
        final JLabel label = new JLabel();
        TreeCellRenderer renderer = new TreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                label.setText(String.valueOf(value));
                return label;
            }
        };

        JTree tree = new JTree();
        Component c = renderer.getTreeCellRendererComponent(tree, "node", true, true, false, 0, true);
        check(c == label, "returns the label");
        check("node".equals(label.getText()), "label text is value");

        // DefaultTreeCellRenderer implements the interface.
        check(TreeCellRenderer.class.isAssignableFrom(DefaultTreeCellRenderer.class), "DefaultTreeCellRenderer implements TreeCellRenderer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

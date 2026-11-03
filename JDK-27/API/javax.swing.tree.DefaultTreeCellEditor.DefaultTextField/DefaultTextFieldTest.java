/*
 * @test
 * @summary javax.swing.tree.DefaultTreeCellEditor.DefaultTextField conformance: white-box structure + black-box behavior.
 * @run main DefaultTextFieldTest
 */

import java.awt.Dimension;
import java.awt.Font;
import java.lang.reflect.Modifier;

import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

public class DefaultTextFieldTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultTreeCellEditor$DefaultTextField");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JTextField"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultTreeCellEditor editor = new DefaultTreeCellEditor(new JTree(), new DefaultTreeCellRenderer());
        Border border = new EmptyBorder(2, 2, 2, 2);
        DefaultTreeCellEditor.DefaultTextField tf = editor.new DefaultTextField(border);

        check(tf != null, "constructed");
        check(tf instanceof javax.swing.JTextField, "is a JTextField");

        // Border round-trip (overridden to remember the supplied border).
        check(tf.getBorder() == border, "getBorder returns supplied border");
        Border border2 = new EmptyBorder(4, 4, 4, 4);
        tf.setBorder(border2);
        check(tf.getBorder() == border2, "setBorder/getBorder round-trip");

        // getFont is non-null.
        Font f = tf.getFont();
        check(f != null, "getFont non-null");

        // getPreferredSize is a valid dimension.
        Dimension d = tf.getPreferredSize();
        check(d != null && d.width >= 0 && d.height >= 0, "getPreferredSize non-negative");

        // Inherited JTextField behavior still works.
        tf.setText("edit me");
        check("edit me".equals(tf.getText()), "JTextField setText/getText");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

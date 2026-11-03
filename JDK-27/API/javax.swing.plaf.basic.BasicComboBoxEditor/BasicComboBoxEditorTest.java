/*
 * @test
 * @summary javax.swing.plaf.basic.BasicComboBoxEditor conformance: white-box structure + black-box behavior.
 * @run main BasicComboBoxEditorTest
 */

import java.lang.reflect.Modifier;

public class BasicComboBoxEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicComboBoxEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.ComboBoxEditor"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.awt.event.FocusListener"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicComboBoxEditor ed = new javax.swing.plaf.basic.BasicComboBoxEditor();
        check(ed.getEditorComponent() instanceof javax.swing.JTextField, "editor is JTextField");
        ed.setItem("hello");
        check("hello".equals(ed.getItem()), "set/get item");
        ed.setItem(null);
        check("".equals(ed.getItem()), "null item becomes empty string");
        javax.swing.JTextField tf = (javax.swing.JTextField) ed.getEditorComponent();
        tf.setText("abc");
        ed.selectAll();
        check(tf.getSelectionStart() == 0 && tf.getSelectionEnd() == 3, "selectAll selects all");
        final int[] fired = {0};
        java.awt.event.ActionListener al = e -> fired[0]++;
        ed.addActionListener(al);
        ed.removeActionListener(al);
        check(true, "add/remove action listener");
        ed.focusGained(new java.awt.event.FocusEvent(tf, java.awt.event.FocusEvent.FOCUS_GAINED));
        ed.focusLost(new java.awt.event.FocusEvent(tf, java.awt.event.FocusEvent.FOCUS_LOST));
        check(true, "focus events callable");
    }
}

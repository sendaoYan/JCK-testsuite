/*
 * @test
 * @summary javax.swing.ComboBoxEditor conformance: white-box structure + black-box behavior.
 * @run main ComboBoxEditorTest
 */

import java.awt.Component;
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;

import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;

public class ComboBoxEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ComboBoxEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final JTextField field = new JTextField();
        final int[] actions = {0};
        ComboBoxEditor ed = new ComboBoxEditor() {
            public Component getEditorComponent() { return field; }
            public void setItem(Object o) { field.setText(String.valueOf(o)); }
            public Object getItem() { return field.getText(); }
            public void selectAll() { field.selectAll(); }
            public void addActionListener(ActionListener l) { actions[0]++; }
            public void removeActionListener(ActionListener l) { actions[0]--; }
        };

        check(ed.getEditorComponent() == field, "getEditorComponent");
        ed.setItem("abc");
        check("abc".equals(ed.getItem()), "setItem/getItem");
        ed.selectAll();
        ed.addActionListener(null);
        ed.removeActionListener(null);
        check(actions[0] == 0, "add/remove action listener");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

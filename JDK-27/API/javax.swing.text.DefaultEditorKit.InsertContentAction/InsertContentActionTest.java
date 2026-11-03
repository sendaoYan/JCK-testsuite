/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.InsertContentAction conformance: white-box structure + black-box behavior.
 * @run main InsertContentActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import javax.swing.JTextArea;
import javax.swing.text.TextAction;

public class InsertContentActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$InsertContentAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.TextAction"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        DefaultEditorKit.InsertContentAction a = new DefaultEditorKit.InsertContentAction();
        check(a instanceof TextAction, "is TextAction");
        check(a instanceof Action, "is Action");
        check(DefaultEditorKit.insertContentAction.equals(a.getValue(Action.NAME)), "name insert-content");
        check(a.isEnabled(), "enabled by default");

        // append content at caret, twice
        JTextArea area = new JTextArea();
        area.setText("");
        area.setCaretPosition(0);
        a.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "AB"));
        check("AB".equals(area.getText()), "inserts first content");
        area.setCaretPosition(2);
        a.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "CD"));
        check("ABCD".equals(area.getText()), "inserts second content at caret");

        // replace selection with content
        JTextArea area2 = new JTextArea();
        area2.setText("abcd");
        area2.select(1, 3);
        a.actionPerformed(new ActionEvent(area2, ActionEvent.ACTION_PERFORMED, "X"));
        check("aXd".equals(area2.getText()), "replaces selection with content");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

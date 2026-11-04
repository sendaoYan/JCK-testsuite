/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.InsertBreakAction conformance: white-box structure + black-box behavior.
 * @run main InsertBreakActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import javax.swing.JTextArea;
import javax.swing.text.TextAction;

public class InsertBreakActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$InsertBreakAction");
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
        DefaultEditorKit.InsertBreakAction a = new DefaultEditorKit.InsertBreakAction();
        check(a instanceof TextAction, "is TextAction");
        check(a instanceof Action, "is Action");
        check(DefaultEditorKit.insertBreakAction.equals(a.getValue(Action.NAME)), "name insert-break");
        check(a.isEnabled(), "enabled by default");

        // insert newline at caret
        JTextArea area = new JTextArea();
        area.setText("ab");
        area.setCaretPosition(1);
        a.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, null));
        check("a\nb".equals(area.getText()), "inserts newline at caret");

        // replace selection with newline
        JTextArea area2 = new JTextArea();
        area2.setText("abcd");
        area2.select(1, 3);
        a.actionPerformed(new ActionEvent(area2, ActionEvent.ACTION_PERFORMED, null));
        check("a\nd".equals(area2.getText()), "replaces selection with newline");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

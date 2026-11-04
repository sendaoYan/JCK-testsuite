/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit.InsertTabAction conformance: white-box structure + black-box behavior.
 * @run main InsertTabActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.DefaultEditorKit;
import javax.swing.JTextArea;
import javax.swing.text.TextAction;

public class InsertTabActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit$InsertTabAction");
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
        DefaultEditorKit.InsertTabAction a = new DefaultEditorKit.InsertTabAction();
        check(a instanceof TextAction, "is TextAction");
        check(a instanceof Action, "is Action");
        check(DefaultEditorKit.insertTabAction.equals(a.getValue(Action.NAME)), "name insert-tab");
        check(a.isEnabled(), "enabled by default");

        // insert tab into empty document
        JTextArea area = new JTextArea();
        area.setText("");
        a.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, null));
        check("\t".equals(area.getText()), "inserts tab into empty");

        // insert tab at caret
        JTextArea area2 = new JTextArea();
        area2.setText("ab");
        area2.setCaretPosition(1);
        a.actionPerformed(new ActionEvent(area2, ActionEvent.ACTION_PERFORMED, null));
        check("a\tb".equals(area2.getText()), "inserts tab at caret");

        // replace selection with tab
        JTextArea area3 = new JTextArea();
        area3.setText("ab");
        area3.select(0, 2);
        a.actionPerformed(new ActionEvent(area3, ActionEvent.ACTION_PERFORMED, null));
        check("\t".equals(area3.getText()), "replaces selection with tab");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.text.StyledEditorKit.AlignmentAction conformance: white-box structure + black-box behavior.
 * @run main AlignmentActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextPane;

public class AlignmentActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit$AlignmentAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit$StyledTextAction"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static int paragraphAlignment(JTextPane pane, int offset) {
        return StyleConstants.getAlignment(((StyledDocument) pane.getDocument()).getParagraphElement(offset).getAttributes());
    }

    static void testBlackBox() {
        StyledEditorKit.AlignmentAction a = new StyledEditorKit.AlignmentAction("center-justify", StyleConstants.ALIGN_CENTER);
        check("center-justify".equals(a.getValue(Action.NAME)), "name center-justify");
        check(a.isEnabled(), "enabled by default");

        // default alignment from constructor
        JTextPane pane = new JTextPane();
        pane.setText("hello world");
        pane.select(0, 5);
        a.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, null));
        check(paragraphAlignment(pane, 0) == StyleConstants.ALIGN_CENTER, "default alignment center");

        // command string overrides alignment
        JTextPane pane2 = new JTextPane();
        pane2.setText("hello world");
        pane2.select(0, 5);
        a.actionPerformed(new ActionEvent(pane2, ActionEvent.ACTION_PERFORMED, String.valueOf(StyleConstants.ALIGN_RIGHT)));
        check(paragraphAlignment(pane2, 0) == StyleConstants.ALIGN_RIGHT, "command override alignment right");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

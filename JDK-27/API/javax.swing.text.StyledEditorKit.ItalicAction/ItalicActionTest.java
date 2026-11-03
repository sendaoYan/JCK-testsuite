/*
 * @test
 * @summary javax.swing.text.StyledEditorKit.ItalicAction conformance: white-box structure + black-box behavior.
 * @run main ItalicActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextPane;

public class ItalicActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit$ItalicAction");
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

    static boolean charItalic(JTextPane pane, int offset) {
        return StyleConstants.isItalic(((StyledDocument) pane.getDocument()).getCharacterElement(offset).getAttributes());
    }

    static void testBlackBox() {
        StyledEditorKit.ItalicAction a = new StyledEditorKit.ItalicAction();
        check("font-italic".equals(a.getValue(Action.NAME)), "name font-italic");
        check(a.isEnabled(), "enabled by default");

        JTextPane pane = new JTextPane();
        pane.setText("hello world");
        pane.select(0, 5);
        a.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, null));
        check(charItalic(pane, 0), "italic applied to selection");
        a.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, null));
        check(!charItalic(pane, 0), "italic toggled off");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

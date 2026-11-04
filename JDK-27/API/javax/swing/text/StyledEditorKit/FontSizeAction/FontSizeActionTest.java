/*
 * @test
 * @summary javax.swing.text.StyledEditorKit.FontSizeAction conformance: white-box structure + black-box behavior.
 * @run main FontSizeActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextPane;

public class FontSizeActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit$FontSizeAction");
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

    static int charSize(JTextPane pane, int offset) {
        return StyleConstants.getFontSize(((StyledDocument) pane.getDocument()).getCharacterElement(offset).getAttributes());
    }

    static void testBlackBox() {
        StyledEditorKit.FontSizeAction a = new StyledEditorKit.FontSizeAction("font-size-24", 24);
        check("font-size-24".equals(a.getValue(Action.NAME)), "name font-size-24");
        check(a.isEnabled(), "enabled by default");

        // default size from constructor
        JTextPane pane = new JTextPane();
        pane.setText("hello world");
        pane.select(0, 5);
        a.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, null));
        check(charSize(pane, 0) == 24, "default size 24");

        // command string overrides size
        JTextPane pane2 = new JTextPane();
        pane2.setText("hello world");
        pane2.select(0, 5);
        a.actionPerformed(new ActionEvent(pane2, ActionEvent.ACTION_PERFORMED, "30"));
        check(charSize(pane2, 0) == 30, "command override size 30");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

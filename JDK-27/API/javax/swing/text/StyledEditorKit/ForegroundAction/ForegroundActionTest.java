/*
 * @test
 * @summary javax.swing.text.StyledEditorKit.ForegroundAction conformance: white-box structure + black-box behavior.
 * @run main ForegroundActionTest
 */

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextPane;

public class ForegroundActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit$ForegroundAction");
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

    static Color charForeground(JTextPane pane, int offset) {
        return StyleConstants.getForeground(((StyledDocument) pane.getDocument()).getCharacterElement(offset).getAttributes());
    }

    static void testBlackBox() {
        StyledEditorKit.ForegroundAction a = new StyledEditorKit.ForegroundAction("foreground-color", Color.RED);
        check("foreground-color".equals(a.getValue(Action.NAME)), "name foreground-color");
        check(a.isEnabled(), "enabled by default");

        // invalid command string keeps default color from constructor
        JTextPane pane = new JTextPane();
        pane.setText("hello world");
        pane.select(0, 5);
        a.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, "not-a-color"));
        check(Color.RED.equals(charForeground(pane, 0)), "default foreground red on bad command");

        // command string overrides color via Color.decode
        JTextPane pane2 = new JTextPane();
        pane2.setText("hello world");
        pane2.select(0, 5);
        a.actionPerformed(new ActionEvent(pane2, ActionEvent.ACTION_PERFORMED, "#00FF00"));
        check(new Color(0, 255, 0).equals(charForeground(pane2, 0)), "command override foreground green");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.text.StyledEditorKit.FontFamilyAction conformance: white-box structure + black-box behavior.
 * @run main FontFamilyActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextPane;

public class FontFamilyActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyledEditorKit$FontFamilyAction");
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

    static String charFamily(JTextPane pane, int offset) {
        return StyleConstants.getFontFamily(((StyledDocument) pane.getDocument()).getCharacterElement(offset).getAttributes());
    }

    static void testBlackBox() {
        StyledEditorKit.FontFamilyAction a = new StyledEditorKit.FontFamilyAction("font-family-Serif", "Serif");
        check("font-family-Serif".equals(a.getValue(Action.NAME)), "name font-family-Serif");
        check(a.isEnabled(), "enabled by default");

        // default family from constructor
        JTextPane pane = new JTextPane();
        pane.setText("hello world");
        pane.select(0, 5);
        a.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, null));
        check("Serif".equals(charFamily(pane, 0)), "default family Serif");

        // command string overrides family
        JTextPane pane2 = new JTextPane();
        pane2.setText("hello world");
        pane2.select(0, 5);
        a.actionPerformed(new ActionEvent(pane2, ActionEvent.ACTION_PERFORMED, "Monospaced"));
        check("Monospaced".equals(charFamily(pane2, 0)), "command override family Monospaced");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

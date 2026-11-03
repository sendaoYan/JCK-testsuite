/*
 * @test
 * @summary javax.swing.JTextPane conformance: white-box structure + black-box behavior.
 * @run main JTextPaneTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class JTextPaneTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JTextPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JEditorPane"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JTextPane pane = new JTextPane();
        check(pane.getStyledDocument() != null, "styled document non-null");
        check(pane.getDocument() instanceof StyledDocument, "document is styled");
        pane.setText("hello");
        check("hello".equals(pane.getText()), "setText/getText");

        Style style = pane.addStyle("myStyle", null);
        check(style != null, "addStyle");
        check(pane.getStyle("myStyle") == style, "getStyle");
        pane.setLogicalStyle(style);
        check(pane.getLogicalStyle() == style, "setLogicalStyle/getLogicalStyle");
        check(pane.getCharacterAttributes() != null, "character attributes");
        check(pane.getParagraphAttributes() != null, "paragraph attributes");
        check(pane.getInputAttributes() != null, "input attributes");

        pane.setCharacterAttributes(new SimpleAttributeSet(), true);
        pane.setParagraphAttributes(new SimpleAttributeSet(), true);
        pane.removeStyle("myStyle");
        check(pane.getStyle("myStyle") == null, "removeStyle");

        JTextPane typed = new JTextPane(new DefaultStyledDocument());
        check(typed.getStyledDocument() != null, "styled doc ctor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit.InsertHTMLTextAction conformance: white-box structure + black-box behavior.
 * @run main InsertHTMLTextActionTest
 */

import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class InsertHTMLTextActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit$InsertHTMLTextAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.swing.text.html.HTMLEditorKit$HTMLTextAction"),
              "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static HTMLEditorKit.InsertHTMLTextAction insertAction(String name, String html,
                                                           HTML.Tag parent, HTML.Tag add) {
        return new HTMLEditorKit.InsertHTMLTextAction(name, html, parent, add);
    }

    static void testBlackBox() throws Exception {
        // constructors + name roundtrip
        HTMLEditorKit.InsertHTMLTextAction a1 =
            insertAction("InsertHello", "<b>hello</b>", HTML.Tag.BODY, HTML.Tag.B);
        check("InsertHello".equals(a1.getValue(Action.NAME)), "4-arg ctor name roundtrip");
        check(a1 instanceof HTMLEditorKit.HTMLTextAction, "is an HTMLTextAction");

        HTMLEditorKit.InsertHTMLTextAction a2 =
            new HTMLEditorKit.InsertHTMLTextAction("InsertAlt", "<b>alt</b>",
                    HTML.Tag.BODY, HTML.Tag.B, HTML.Tag.TABLE, HTML.Tag.TR);
        check("InsertAlt".equals(a2.getValue(Action.NAME)), "6-arg ctor name roundtrip");

        // actionPerformed inserts HTML into a JEditorPane document (headless-safe)
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(kit);
        pane.setDocument(doc);
        pane.setCaretPosition(0);

        a1.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, "cmd"));
        String text = doc.getText(0, doc.getLength());
        check(text.contains("hello"), "actionPerformed inserts html text");
        check(doc.getLength() > 0, "document non-empty after insert");

        // a second insert appends more content into the same document
        a2.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, "cmd"));
        String both = doc.getText(0, doc.getLength());
        check(both.contains("alt"), "second actionPerformed inserts alternate html");

        // action with a non-editor source is a no-op (no exception)
        a1.actionPerformed(new ActionEvent(new Object(), ActionEvent.ACTION_PERFORMED, "cmd"));
        check(true, "actionPerformed tolerates non-JTextComponent source");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

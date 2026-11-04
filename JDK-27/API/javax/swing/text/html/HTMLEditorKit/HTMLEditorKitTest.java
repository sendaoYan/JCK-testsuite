/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit conformance: white-box structure + black-box behavior.
 * @run main HTMLEditorKitTest
 */

import java.awt.Cursor;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;

import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

public class HTMLEditorKitTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit"),
              "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"),
              "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();

        // content type / document / view factory
        check("text/html".equals(kit.getContentType()), "content type is text/html");
        Document doc = kit.createDefaultDocument();
        check(doc instanceof HTMLDocument, "createDefaultDocument returns HTMLDocument");
        check(kit.getViewFactory() != null, "view factory non-null");
        check(kit.getViewFactory() instanceof javax.swing.text.html.HTMLEditorKit.HTMLFactory,
              "view factory is HTMLFactory");

        // style sheet + cursors
        StyleSheet ss = kit.getStyleSheet();
        check(ss != null, "style sheet non-null");
        check(kit.getDefaultCursor() != null, "default cursor non-null");
        check(kit.getDefaultCursor().getType() == Cursor.DEFAULT_CURSOR,
              "default cursor is DEFAULT_CURSOR");
        check(kit.getLinkCursor().getType() == Cursor.HAND_CURSOR,
              "link cursor is HAND_CURSOR");

        // read: parse HTML into a document
        HTMLDocument hdoc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader("<html><body><b>bold</b></body></html>"), hdoc, 0);
        String text = hdoc.getText(0, hdoc.getLength());
        check(text.contains("bold"), "read() parses and stores text");
        check(hdoc.getLength() > 0, "document non-empty after read");

        // write: serialize the document to a writer
        StringWriter out = new StringWriter();
        kit.write(out, hdoc, 0, hdoc.getLength());
        String html = out.toString();
        check(html.contains("<html>"), "write() emits <html>");
        check(html.contains("<b>bold</b>"), "write() emits bold text");

        // actions
        Action[] actions = kit.getActions();
        check(actions.length > 0, "getActions non-empty");
        int htmlTextActions = 0;
        for (Action a : actions) {
            if (a instanceof javax.swing.text.html.HTMLEditorKit.HTMLTextAction) {
                htmlTextActions++;
            }
        }
        check(htmlTextActions > 0, "getActions contains HTMLTextAction instances");

        // input attributes
        check(kit.getInputAttributes() != null, "input attributes non-null");

        // auto form submission (instance state, no global side effects)
        check(kit.isAutoFormSubmission(), "auto form submission default true");
        kit.setAutoFormSubmission(false);
        check(!kit.isAutoFormSubmission(), "setAutoFormSubmission(false) roundtrip");
        kit.setAutoFormSubmission(true);
        check(kit.isAutoFormSubmission(), "setAutoFormSubmission(true) roundtrip");

        // style sheet set/get roundtrip
        StyleSheet replacement = new StyleSheet();
        kit.setStyleSheet(replacement);
        check(kit.getStyleSheet() == replacement, "setStyleSheet/getStyleSheet roundtrip");

        // cursor set/get roundtrip
        Cursor custom = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        kit.setDefaultCursor(custom);
        check(kit.getDefaultCursor() == custom, "setDefaultCursor/getDefaultCursor roundtrip");
        kit.setLinkCursor(custom);
        check(kit.getLinkCursor() == custom, "setLinkCursor/getLinkCursor roundtrip");

        // clone
        HTMLEditorKit clone = (HTMLEditorKit) kit.clone();
        check(clone != kit, "clone is a distinct instance");
        check(clone instanceof HTMLEditorKit, "clone is an HTMLEditorKit");
        check("text/html".equals(clone.getContentType()), "clone keeps content type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

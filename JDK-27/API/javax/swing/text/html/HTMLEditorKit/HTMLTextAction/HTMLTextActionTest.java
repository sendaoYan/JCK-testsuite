/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit.HTMLTextAction conformance: white-box structure + black-box behavior.
 * @run main HTMLTextActionTest
 */

import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.lang.reflect.Modifier;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class HTMLTextActionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Concrete subclass to reach the protected convenience methods.
    static class ProbeAction extends HTMLEditorKit.HTMLTextAction {
        ProbeAction(String name) { super(name); }
        public void actionPerformed(ActionEvent e) { }
        public HTMLDocument doc(JEditorPane e) { return super.getHTMLDocument(e); }
        public HTMLEditorKit kit(JEditorPane e) { return super.getHTMLEditorKit(e); }
        public Element[] elementsAt(HTMLDocument d, int off) { return super.getElementsAt(d, off); }
        public int countToTag(HTMLDocument d, int off, HTML.Tag t) { return super.elementCountToTag(d, off, t); }
        public Element findTag(HTMLDocument d, int off, HTML.Tag t) { return super.findElementMatchingTag(d, off, t); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit$HTMLTextAction");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit$StyledTextAction"),
              "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();

        // concrete instances come out of HTMLEditorKit.getActions()
        int htmlTextActions = 0;
        for (Action a : kit.getActions()) {
            if (a instanceof HTMLEditorKit.HTMLTextAction) htmlTextActions++;
        }
        check(htmlTextActions > 0, "getActions yields HTMLTextAction instances");

        ProbeAction probe = new ProbeAction("probe");
        check("probe".equals(probe.getValue(Action.NAME)), "action name roundtrip");

        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader("<html><body><b>bold</b></body></html>"), doc, 0);

        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(kit);
        pane.setDocument(doc);

        // convenience accessors resolve document/kit from the editor
        check(probe.doc(pane) == doc, "getHTMLDocument resolves document");
        check(probe.kit(pane) == kit, "getHTMLEditorKit resolves kit");

        // element navigation helpers
        Element[] els = probe.elementsAt(doc, 1);
        check(els.length >= 2, "getElementsAt returns root..leaf chain");
        check("html".equals(els[0].getName()), "getElementsAt[0] is root element");

        check(probe.countToTag(doc, 1, HTML.Tag.BODY) >= 0,
              "elementCountToTag(BODY) finds tag");
        check(probe.countToTag(doc, 1, HTML.Tag.TABLE) == -1,
              "elementCountToTag(TABLE) returns -1 for missing tag");

        Element body = probe.findTag(doc, 1, HTML.Tag.BODY);
        check(body != null, "findElementMatchingTag(BODY) non-null");
        check("body".equals(body.getName()), "findElementMatchingTag(BODY) is body");

        // wrong editor type => IllegalArgumentException (exact type)
        JEditorPane styledPane = new JEditorPane();
        styledPane.setEditorKit(new javax.swing.text.StyledEditorKit());
        try {
            probe.doc(styledPane);
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            check(true, "getHTMLDocument throws IAE for non-HTML document");
        }
        try {
            probe.kit(styledPane);
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            check(true, "getHTMLEditorKit throws IAE for non-HTML kit");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

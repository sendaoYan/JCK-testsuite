/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit.LinkController conformance: white-box structure + black-box behavior.
 * @run main LinkControllerTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class LinkControllerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Exposes the protected activateLink entry point.
    static class ExposedLinkController extends HTMLEditorKit.LinkController {
        void doActivateLink(int pos, JEditorPane e) { super.activateLink(pos, e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit$LinkController");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"),
              "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.MouseMotionListener"),
              "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"),
              "interface 1");
        // public no-arg constructor
        check(c.getConstructor().getModifiers() == Modifier.PUBLIC, "public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // instantiable
        HTMLEditorKit.LinkController controller = new HTMLEditorKit.LinkController();
        check(controller != null, "constructor returns instance");

        // build a pane whose document contains a hyperlink
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(
                "<html><body><a href=\"http://example.com/x\">link</a></body></html>"),
                doc, 0);
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(kit);
        pane.setDocument(doc);

        final List<HyperlinkEvent> events = new ArrayList<>();
        pane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) { events.add(e); }
        });

        ExposedLinkController exposed = new ExposedLinkController();
        // activating over the link fires an ACTIVATED event with the href
        exposed.doActivateLink(1, pane);
        check(events.size() == 1, "activateLink fires one HyperlinkEvent");
        HyperlinkEvent ev = events.get(0);
        check(ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED,
              "event type is ACTIVATED");
        check("http://example.com/x".equals(ev.getDescription()),
              "event description carries href");
        check(ev.getSource() == pane, "event source is the editor pane");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

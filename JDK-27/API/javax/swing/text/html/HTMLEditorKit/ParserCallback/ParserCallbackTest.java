/*
 * @test
 * @summary javax.swing.text.html.HTMLEditorKit.ParserCallback conformance: white-box structure + black-box behavior.
 * @run main ParserCallbackTest
 */

import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class ParserCallbackTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Records callbacks so the real parser can drive a concrete subclass.
    static class RecordingCallback extends HTMLEditorKit.ParserCallback {
        final List<String> events = new ArrayList<>();
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) { events.add("start:" + t); }
        public void handleEndTag(HTML.Tag t, int pos) { events.add("end:" + t); }
        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) { events.add("simple:" + t); }
        public void handleText(char[] data, int pos) { events.add("text:" + new String(data)); }
        public void handleComment(char[] data, int pos) { events.add("comment:" + new String(data)); }
    }

    // Exposes the protected getParser() so the real parser can be reached.
    static class KitWithParser extends HTMLEditorKit {
        HTMLEditorKit.Parser parser() { return super.getParser(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLEditorKit$ParserCallback");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        // IMPLIED public static field
        check(c.getField("IMPLIED") != null, "IMPLIED field exists");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // default (no-op) callbacks are safe to invoke directly
        HTMLEditorKit.ParserCallback cb = new HTMLEditorKit.ParserCallback();
        cb.handleText(new char[]{'a'}, 0);
        cb.handleComment(new char[]{'c'}, 0);
        cb.handleStartTag(HTML.Tag.HTML, null, 0);
        cb.handleEndTag(HTML.Tag.HTML, 0);
        cb.handleSimpleTag(HTML.Tag.BR, null, 0);
        cb.handleError("oops", 0);
        cb.handleEndOfLineString("\n");
        cb.flush();
        check(true, "default ParserCallback methods are no-op and do not throw");

        // IMPLIED marker is a non-null object
        check(HTMLEditorKit.ParserCallback.IMPLIED != null, "IMPLIED is non-null");

        // the real parser drives a concrete ParserCallback subclass
        RecordingCallback rec = new RecordingCallback();
        KitWithParser kit = new KitWithParser();
        kit.parser().parse(new StringReader(
                "<html><head></head><body>hi<br><b>x</b></body></html>"), rec, false);
        rec.flush();

        String all = String.join(",", rec.events);
        check(all.indexOf("start:html") == 0, "callback sees start:html first");
        check(all.indexOf("start:body") > 0, "callback sees start:body");
        check(all.indexOf("text:hi") > 0, "callback sees text:hi");
        check(all.indexOf("simple:br") > 0, "callback sees simple:br");
        check(all.indexOf("start:b") > 0, "callback sees start:b");
        check(all.indexOf("text:x") > 0, "callback sees text:x");
        check(all.indexOf("end:b") > 0, "callback sees end:b");
        check(all.endsWith("end:html"), "callback sees end:html last");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

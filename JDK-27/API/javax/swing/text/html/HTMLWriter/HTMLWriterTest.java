/*
 * @test
 * @summary javax.swing.text.html.HTMLWriter conformance: white-box structure + black-box behavior.
 * @run main HTMLWriterTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

public class HTMLWriterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLWriter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.swing.text.AbstractWriter"),
              "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader("<html><body><b>bold</b></body></html>"), doc, 0);

        // 2-arg constructor writes the whole document
        StringWriter out = new StringWriter();
        HTMLWriter writer = new HTMLWriter(out, doc);
        writer.write();
        String html = out.toString();
        check(html.length() > 0, "write() produces non-empty output");
        check(html.contains("<html>"), "output contains <html>");
        check(html.contains("<body>"), "output contains <body>");
        check(html.contains("<b>bold</b>"), "output contains bold text");

        // 4-arg constructor honors the given range
        StringWriter ranged = new StringWriter();
        HTMLWriter rangedWriter = new HTMLWriter(ranged, doc, 0, doc.getLength());
        rangedWriter.write();
        String rangedHtml = ranged.toString();
        check(rangedHtml.contains("bold"), "range write() includes body text");
        check(rangedHtml.contains("<html>"), "range write() emits <html>");
    }

    static HTMLEditorKit deepKit = new HTMLEditorKit();

    static HTMLDocument deepLoad(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) deepKit.createDefaultDocument();
        deepKit.read(new StringReader(html), d, 0);
        return d;
    }

    static final String RICH_HTML =
            "<html><body><div id='d' align='center'><p>Para one</p></div><p>Para two</p>"
            + "<ul id='ul'><li>item one</li><li>item two</li></ul>"
            + "<img src='x.png' alt='ALT' width=20 height=10>"
            + "<form action='/go'><input type='text' name='t1'><input type='submit' value='Go'></form>"
            + "</body></html>";

    static void testTokenOutput() throws Exception {
        HTMLDocument doc = deepLoad(RICH_HTML);
        StringWriter sw = new StringWriter();
        new HTMLWriter(sw, doc).write();
        String out = sw.toString();

        String[] tokens = { "<html", "<body", "<div", "<p", "<ul", "<li", "<img", "<form",
                            "<input", "align=\"center\"", "x.png", "ALT", "Go" };
        for (String t : tokens) check(out.contains(t), "output contains " + t);
        check(out.length() > 300, "rich document produces substantial output: " + out.length());

        // entity re-encoding: parsed entities go back out escaped
        HTMLDocument edoc = deepLoad("<html><body><p>a &amp; b &lt;tag&gt;</p></body></html>");
        sw = new StringWriter();
        new HTMLWriter(sw, edoc).write();
        String eout = sw.toString();
        check(eout.contains("&amp;"), "& re-encoded");
        check(eout.contains("&lt;"), "< re-encoded");
        String stripped = eout.replace("&amp;", "").replace("&lt;", "").replace("&gt;", "").replace("&nbsp;", "");
        check(!stripped.contains("&"), "no raw ampersand left after entity stripping");

        // empty document still writes an html skeleton
        HTMLDocument empty = (HTMLDocument) deepKit.createDefaultDocument();
        sw = new StringWriter();
        new HTMLWriter(sw, empty).write();
        String e = sw.toString();
        check(e.length() > 0, "empty document writes a skeleton");
        check(e.contains("<html") || e.contains("<body") || e.contains("<head"), "skeleton has html tags");
    }

    static void testRangeWriter() throws Exception {
        HTMLDocument doc = deepLoad(RICH_HTML);
        StringWriter sw = new StringWriter();
        new HTMLWriter(sw, doc, 0, doc.getLength() / 2).write();
        String half = sw.toString();
        check(half.length() > 0 && half.length() < 600, "half-range output is a subset");
        check(half.contains("<html") || half.contains("<p"), "half-range keeps structural tokens");

        boolean ok = true;
        try {
            StringWriter sw2 = new StringWriter();
            HTMLWriter hw = new HTMLWriter(sw2, doc, -5, 100);
            hw.write();
        } catch (Throwable t) { ok = false; }
        check(ok, "negative-range constructor and write tolerated");
    }

    static void testNegatives() throws Exception {
        HTMLDocument doc = deepLoad(RICH_HTML);
        boolean ctorOk = true;
        try { new HTMLWriter(null, doc); } catch (Throwable t) { ctorOk = false; }
        check(ctorOk, "HTMLWriter(null, doc) constructor tolerated");
        boolean npe = false;
        try { new HTMLWriter(null, doc).write(); } catch (NullPointerException e) { npe = true; }
        check(npe, "write with null writer NPE");
        boolean npe2 = false;
        try { new HTMLWriter(new StringWriter(), null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "HTMLWriter(w, null) NPE at construction");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTokenOutput();
        testRangeWriter();
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

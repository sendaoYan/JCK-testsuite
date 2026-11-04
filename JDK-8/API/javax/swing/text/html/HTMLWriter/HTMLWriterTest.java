/*
 * @test
 * @summary javax.swing.text.html.HTMLWriter conformance: token output for a
 *          fixed document, entity re-encoding, partial-range writing and
 *          negative cases (headless-safe).
 * @run main HTMLWriterTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLWriter;

public class HTMLWriterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.HTMLWriter", false, HTMLWriterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractWriter"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("write"), "method write");
    }

    static final String RICH_HTML =
            "<html><body><div id='d' align='center'><p>Para one</p></div><p>Para two</p>"
            + "<ul id='ul'><li>item one</li><li>item two</li></ul>"
            + "<img src='x.png' alt='ALT' width=20 height=10>"
            + "<form action='/go'><input type='text' name='t1'><input type='submit' value='Go'></form>"
            + "</body></html>";

    // ---- Part 2: core token output ----
    static void testTokenOutput() throws Exception {
        HTMLDocument doc = load(RICH_HTML);
        StringWriter sw = new StringWriter();
        new HTMLWriter(sw, doc).write();
        String out = sw.toString();

        String[] tokens = { "<html", "<body", "<div", "<p", "<ul", "<li", "<img", "<form",
                            "<input", "align=\"center\"", "x.png", "ALT", "Go" };
        for (String t : tokens) check(out.contains(t), "output contains " + t);
        check(out.length() > 300, "rich document produces substantial output: " + out.length());

        // entity re-encoding: parsed entities go back out escaped
        HTMLDocument edoc = load("<html><body><p>a &amp; b &lt;tag&gt;</p></body></html>");
        sw = new StringWriter();
        new HTMLWriter(sw, edoc).write();
        String eout = sw.toString();
        check(eout.contains("&amp;"), "& re-encoded");
        check(eout.contains("&lt;"), "< re-encoded");
        String stripped = eout.replace("&amp;", "").replace("&lt;", "").replace("&gt;", "").replace("&nbsp;", "");
        check(!stripped.contains("&"), "no raw ampersand left after entity stripping");

        // empty document still writes an html skeleton
        HTMLDocument empty = (HTMLDocument) kit.createDefaultDocument();
        sw = new StringWriter();
        new HTMLWriter(sw, empty).write();
        String e = sw.toString();
        check(e.length() > 0, "empty document writes a skeleton");
        check(e.contains("<html") || e.contains("<body") || e.contains("<head"), "skeleton has html tags");
    }

    // ---- Part 3: partial-range writer ----
    static void testRangeWriter() throws Exception {
        HTMLDocument doc = load(RICH_HTML);
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

    // ---- Part 4: negative cases ----
    static void testNegatives() throws Exception {
        HTMLDocument doc = load(RICH_HTML);
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
        testShape();
        testTokenOutput();
        testRangeWriter();
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.swing.text.html.MinimalHTMLWriter conformance: white-box structure + black-box behavior.
 * @run main MinimalHTMLWriterTest
 */

import java.io.StringWriter;
import java.lang.reflect.Modifier;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.MinimalHTMLWriter;

public class MinimalHTMLWriterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.MinimalHTMLWriter");
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
        DefaultStyledDocument doc = new DefaultStyledDocument();
        Style bold = doc.addStyle("b", null);
        StyleConstants.setBold(bold, true);
        doc.insertString(0, "hello world", null);
        doc.setCharacterAttributes(0, 5, bold, false);

        // 2-arg constructor writes the whole document
        StringWriter out = new StringWriter();
        MinimalHTMLWriter writer = new MinimalHTMLWriter(out, doc);
        writer.write();
        String html = out.toString();
        check(html.length() > 0, "write() produces non-empty output");
        check(html.contains("<html>"), "output contains <html>");
        check(html.contains("<body>"), "output contains <body>");
        check(html.contains("hello world"), "output contains styled text");
        check(html.contains("<b>"), "output expresses bold style");

        // 4-arg constructor honors the given range
        StringWriter ranged = new StringWriter();
        MinimalHTMLWriter rangedWriter = new MinimalHTMLWriter(ranged, doc, 0, doc.getLength());
        rangedWriter.write();
        String rangedHtml = ranged.toString();
        check(rangedHtml.contains("hello world"), "range write() includes text");
        check(rangedHtml.contains("<html>"), "range write() emits <html>");
    }

    static DefaultStyledDocument deepStyledDoc() throws Exception {
        DefaultStyledDocument msd = new DefaultStyledDocument();
        msd.insertString(0, "styled hello", null);
        Style st = msd.addStyle("s1", null);
        StyleConstants.setBold(st, true);
        msd.setParagraphAttributes(0, 1, st, true);
        return msd;
    }

    static void testStyledOutput() throws Exception {
        DefaultStyledDocument msd = deepStyledDoc();
        StringWriter sw = new StringWriter();
        new MinimalHTMLWriter(sw, msd).write();
        String out = sw.toString();

        String[] tokens = { "<html", "<body", "<p", "<b>", "styled hello", "<style" };
        for (String t : tokens) check(out.contains(t), "output contains " + t);
        check(!out.contains("<font"), "no <font> element for plain bold styles");
        check(out.length() > 100, "substantial output: " + out.length());

        // full-range constructor produces the same document
        StringWriter sw2 = new StringWriter();
        new MinimalHTMLWriter(sw2, msd, 0, msd.getLength()).write();
        check(sw2.toString().contains("styled hello"), "range writer keeps the text");
        check(sw2.toString().length() > 0, "range writer output non-empty");

        // out-of-range (pos,len) is tolerated
        boolean ok = true;
        try {
            StringWriter sw3 = new StringWriter();
            new MinimalHTMLWriter(sw3, msd, 5, 3).write();
        } catch (Throwable t) { ok = false; }
        check(ok, "range (5,3) tolerated");
    }

    static void testNegatives() throws Exception {
        DefaultStyledDocument msd = deepStyledDoc();
        boolean ctorOk = true;
        try { new MinimalHTMLWriter(null, msd); } catch (Throwable t) { ctorOk = false; }
        check(ctorOk, "MinimalHTMLWriter(null, doc) constructor tolerated");
        boolean npe = false;
        try { new MinimalHTMLWriter(null, msd).write(); } catch (NullPointerException e) { npe = true; }
        check(npe, "write with null writer NPE");
        boolean npe2 = false;
        try { new MinimalHTMLWriter(new StringWriter(), null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "MinimalHTMLWriter(w, null) NPE at construction");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStyledOutput();
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

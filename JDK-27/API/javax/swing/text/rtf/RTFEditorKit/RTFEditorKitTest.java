/*
 * @test
 * @summary javax.swing.text.rtf.RTFEditorKit conformance: white-box structure + black-box behavior.
 * @run main RTFEditorKitTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Set;
import java.util.HashSet;
import java.io.Reader;
import javax.swing.Action;
import javax.swing.text.DefaultStyledDocument;
import java.lang.reflect.Modifier;

import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

public class RTFEditorKitTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.rtf.RTFEditorKit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RTFEditorKit kit = new RTFEditorKit();

        check("text/rtf".equals(kit.getContentType()), "content type text/rtf");

        Document doc = kit.createDefaultDocument();
        check(doc != null, "createDefaultDocument non-null");
        check(doc instanceof StyledDocument, "document is StyledDocument");

        // read a minimal RTF stream (Reader) into an empty document
        String rtf = "{\\rtf1\\ansi Hello World\\par}";
        kit.read(new StringReader(rtf), doc, 0);
        String text = doc.getText(0, doc.getLength());
        check(text.contains("Hello World"), "reader read captured text");
        check(doc.getLength() > 0, "reader read non-empty document");

        // read (InputStream) into a fresh document
        Document doc2 = kit.createDefaultDocument();
        kit.read(new ByteArrayInputStream("{\\rtf1\\ansi Stream}".getBytes("ASCII")), doc2, 0);
        check(doc2.getText(0, doc2.getLength()).contains("Stream"), "input stream read captured text");

        // read into an existing document at a non-zero offset appends content
        Document doc3 = kit.createDefaultDocument();
        doc3.insertString(0, "AB", null);
        kit.read(new StringReader("{\\rtf1\\ansi CD}"), doc3, 2);
        String doc3text = doc3.getText(0, doc3.getLength());
        check(doc3text.startsWith("AB"), "offset read preserves prefix");
        check(doc3text.contains("CD"), "offset read appends content");

        // write (OutputStream) round-trips the document back to RTF text
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        kit.write(bos, doc, 0, doc.getLength());
        String rtfOut = new String(bos.toByteArray(), "ASCII");
        check(rtfOut.contains("Hello World"), "output stream write round-trip text");

        // write(Writer) is unsupported for the 8-bit RTF format -> IOException
        boolean ioThrown = false;
        try {
            kit.write(new StringWriter(), doc, 0, doc.getLength());
        } catch (IOException expected) {
            ioThrown = true;
        }
        check(ioThrown, "writer write throws IOException");

        // non-RTF text is not accepted: yields an empty document
        Document doc4 = kit.createDefaultDocument();
        kit.read(new StringReader("plain text"), doc4, 0);
        check(doc4.getLength() == 0, "non-RTF input yields empty document");
    }

    static RTFEditorKit deepKit = new RTFEditorKit();

    static void testBasics() throws Exception {
        check("text/rtf".equals(deepKit.getContentType()), "content type text/rtf");
        Document d = deepKit.createDefaultDocument();
        check(d instanceof DefaultStyledDocument, "default document is DefaultStyledDocument");
        check(d.getLength() == 0, "default document empty");
        RTFEditorKit clone = (RTFEditorKit) deepKit.clone();
        check(clone != deepKit, "clone distinct");
        check("text/rtf".equals(clone.getContentType()), "clone keeps content type");
        Action[] acts = deepKit.getActions();
        boolean jdk8 = System.getProperty("java.version").startsWith("1.8");
        check(acts.length == (jdk8 ? 73 : 75),
              "action count " + acts.length + " (" + (jdk8 ? "JDK-8" : "JDK-9+") + ")");
        Set<String> anames = new HashSet<String>();
        for (Action a : acts) anames.add(String.valueOf(a.getValue(Action.NAME)));
        check(anames.size() > 50, "named action table populated");
    }

    static void testRoundTrip() throws Exception {
        DefaultStyledDocument rd = new DefaultStyledDocument();
        rd.insertString(0, "Line1\nLine2", null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        deepKit.write(bos, rd, 0, rd.getLength());
        String rtf = bos.toString("ISO-8859-1");
        check(rtf.startsWith("{\\rtf1"), "output starts with RTF header");
        check(rtf.contains("\\ansi"), "ansi keyword present");
        check(rtf.contains("Line1\\par"), "lines separated by \\par");
        check(rtf.trim().endsWith("}"), "group closed at end");

        Document back = deepKit.createDefaultDocument();
        deepKit.read(new ByteArrayInputStream(bos.toByteArray()), back, 0);
        check(back.getLength() == 12, "round-trip length grows by trailing newline");
        check(back.getText(0, back.getLength()).equals("Line1\nLine2\n"),
              "round-trip text with trailing newline");

        // escapes: hex (apostrophe-h-h) and unicode (backslash-u-N) both decode
        Document esc = deepKit.createDefaultDocument();
        deepKit.read(new StringReader("{\\rtf1\\ansi esc\\'e9 \\u65?}"), esc, 0);
        String t = esc.getText(0, esc.getLength());
        check(t.equals("escé A\n"), "hex and unicode escapes decoded");

        // non-RTF input yields an empty document, no exception
        Document non = deepKit.createDefaultDocument();
        deepKit.read(new ByteArrayInputStream("plain text not rtf".getBytes("ISO-8859-1")), non, 0);
        check(non.getLength() == 0, "non-RTF input produces empty document");

        // 8-bit contract: the Writer overload refuses to write
        boolean ioe = false;
        try { deepKit.write(new StringWriter(), rd, 0, rd.getLength()); }
        catch (java.io.IOException e) { ioe = true; }
        check(ioe, "write(Writer,..) refused: RTF is an 8-bit format");

        // reading at a bad position is tolerated
        boolean ok = true;
        try { deepKit.read(new ByteArrayInputStream("{\\rtf1 x}".getBytes("ISO-8859-1")),
                       deepKit.createDefaultDocument(), 100); } catch (Throwable ex) { ok = false; }
        check(ok, "read at position past end tolerated");
    }

    static void testNegatives() throws Exception {
        boolean npe1 = false;
        try { deepKit.read((InputStream) null, deepKit.createDefaultDocument(), 0); }
        catch (NullPointerException e) { npe1 = true; }
        check(npe1, "read(null InputStream) NPE");

        boolean npe2 = false;
        try { deepKit.read((Reader) null, deepKit.createDefaultDocument(), 0); }
        catch (NullPointerException e) { npe2 = true; }
        check(npe2, "read(null Reader) NPE");

        boolean npe3 = false;
        try { deepKit.write((OutputStream) null, new DefaultStyledDocument(), 0, 0); }
        catch (NullPointerException e) { npe3 = true; }
        check(npe3, "write(null OutputStream) NPE");

        boolean npe4 = false;
        try { deepKit.write(new ByteArrayOutputStream(), null, 0, 0); }
        catch (NullPointerException e) { npe4 = true; }
        check(npe4, "write(null Document) NPE");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBasics();
        testRoundTrip();
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

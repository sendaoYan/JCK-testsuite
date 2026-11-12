/*
 * @test
 * @summary javax.swing.text.rtf.RTFEditorKit conformance: 8-bit write contract,
 *          round-trip with trailing newline, escape decoding, lenient reads
 *          and negative cases (headless-safe).
 * @run main RTFEditorKitTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.Action;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

public class RTFEditorKitTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static RTFEditorKit kit = new RTFEditorKit();

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.rtf.RTFEditorKit", false, RTFEditorKitTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.StyledEditorKit"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getContentType"), "method getContentType");
        check(names.contains("read"), "method read");
        check(names.contains("write"), "method write");
        check(names.contains("read"), "method read");
        check(names.contains("write"), "method write");
    }

    // ---- Part 2: basics ----
    static void testBasics() throws Exception {
        check("text/rtf".equals(kit.getContentType()), "content type text/rtf");
        Document d = kit.createDefaultDocument();
        check(d instanceof DefaultStyledDocument, "default document is DefaultStyledDocument");
        check(d.getLength() == 0, "default document empty");
        RTFEditorKit clone = (RTFEditorKit) kit.clone();
        check(clone != kit, "clone distinct");
        check("text/rtf".equals(clone.getContentType()), "clone keeps content type");
        Action[] acts = kit.getActions();
        boolean jdk8 = System.getProperty("java.version").startsWith("1.8");
        check(acts.length == (jdk8 ? 73 : 75),
              "action count " + acts.length + " (" + (jdk8 ? "JDK-8" : "JDK-9+") + ")");
        Set<String> anames = new HashSet<String>();
        for (Action a : acts) anames.add(String.valueOf(a.getValue(Action.NAME)));
        check(anames.size() > 50, "named action table populated");
    }

    // ---- Part 3: write + round-trip ----
    static void testRoundTrip() throws Exception {
        DefaultStyledDocument rd = new DefaultStyledDocument();
        rd.insertString(0, "Line1\nLine2", null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        kit.write(bos, rd, 0, rd.getLength());
        String rtf = bos.toString("ISO-8859-1");
        check(rtf.startsWith("{\\rtf1"), "output starts with RTF header");
        check(rtf.contains("\\ansi"), "ansi keyword present");
        check(rtf.contains("Line1\\par"), "lines separated by \\par");
        check(rtf.trim().endsWith("}"), "group closed at end");

        Document back = kit.createDefaultDocument();
        kit.read(new ByteArrayInputStream(bos.toByteArray()), back, 0);
        check(back.getLength() == 12, "round-trip length grows by trailing newline");
        check(back.getText(0, back.getLength()).equals("Line1\nLine2\n"),
              "round-trip text with trailing newline");

        // escapes: hex (apostrophe-h-h) and unicode (backslash-u-N) both decode
        Document esc = kit.createDefaultDocument();
        kit.read(new StringReader("{\\rtf1\\ansi esc\\'e9 \\u65?}"), esc, 0);
        String t = esc.getText(0, esc.getLength());
        check(t.equals("esc\u00E9 A\n"), "hex and unicode escapes decoded");

        // non-RTF input yields an empty document, no exception
        Document non = kit.createDefaultDocument();
        kit.read(new ByteArrayInputStream("plain text not rtf".getBytes("ISO-8859-1")), non, 0);
        check(non.getLength() == 0, "non-RTF input produces empty document");

        // 8-bit contract: the Writer overload refuses to write
        boolean ioe = false;
        try { kit.write(new StringWriter(), rd, 0, rd.getLength()); }
        catch (java.io.IOException e) { ioe = true; }
        check(ioe, "write(Writer,..) refused: RTF is an 8-bit format");

        // reading at a bad position is tolerated
        boolean ok = true;
        try { kit.read(new ByteArrayInputStream("{\\rtf1 x}".getBytes("ISO-8859-1")),
                       kit.createDefaultDocument(), 100); } catch (Throwable ex) { ok = false; }
        check(ok, "read at position past end tolerated");
    }

    // ---- Part 4: negative cases ----
    static void testNegatives() throws Exception {
        boolean npe1 = false;
        try { kit.read((InputStream) null, kit.createDefaultDocument(), 0); }
        catch (NullPointerException e) { npe1 = true; }
        check(npe1, "read(null InputStream) NPE");

        boolean npe2 = false;
        try { kit.read((Reader) null, kit.createDefaultDocument(), 0); }
        catch (NullPointerException e) { npe2 = true; }
        check(npe2, "read(null Reader) NPE");

        boolean npe3 = false;
        try { kit.write((OutputStream) null, new DefaultStyledDocument(), 0, 0); }
        catch (NullPointerException e) { npe3 = true; }
        check(npe3, "write(null OutputStream) NPE");

        boolean npe4 = false;
        try { kit.write(new ByteArrayOutputStream(), null, 0, 0); }
        catch (NullPointerException e) { npe4 = true; }
        check(npe4, "write(null Document) NPE");
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        testShape();
        testBasics();
        testRoundTrip();
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

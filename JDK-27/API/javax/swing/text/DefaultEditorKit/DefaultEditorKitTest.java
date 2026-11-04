/*
 * @test
 * @summary javax.swing.text.DefaultEditorKit conformance: white-box structure + black-box behavior.
 * @run main DefaultEditorKitTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class DefaultEditorKitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultEditorKit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.EditorKit"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static boolean hasAction(Action[] actions, String name) {
        for (Action a : actions) {
            if (name.equals(a.getValue(Action.NAME))) return true;
        }
        return false;
    }

    static void testBlackBox() throws Exception {
        DefaultEditorKit kit = new DefaultEditorKit();
        check("text/plain".equals(kit.getContentType()), "content type text/plain");
        check(kit.getViewFactory() == null, "view factory null");
        check(kit.createCaret() == null, "createCaret null");
        check(kit.createDefaultDocument() instanceof PlainDocument, "default document PlainDocument");

        Action[] actions = kit.getActions();
        check(actions.length > 0, "actions non-empty");
        check(hasAction(actions, DefaultEditorKit.insertContentAction), "has insert-content");
        check(hasAction(actions, DefaultEditorKit.insertBreakAction), "has insert-break");
        check(hasAction(actions, DefaultEditorKit.insertTabAction), "has insert-tab");
        check(hasAction(actions, DefaultEditorKit.defaultKeyTypedAction), "has default-typed");
        check(hasAction(actions, DefaultEditorKit.selectAllAction), "has select-all");
        Action[] actions2 = kit.getActions();
        check(actions.length == actions2.length, "getActions stable size");
        check(actions[0] == actions2[0], "getActions same instances");

        // Reader read with "\n"
        Document doc1 = kit.createDefaultDocument();
        kit.read(new StringReader("hello\nworld"), doc1, 0);
        check("hello\nworld".equals(doc1.getText(0, doc1.getLength())), "read LF text");
        check("\n".equals(doc1.getProperty(DefaultEditorKit.EndOfLineStringProperty)), "read LF eol property");

        // Reader read with "\r\n" normalized to "\n"
        Document doc2 = kit.createDefaultDocument();
        kit.read(new StringReader("a\r\nb"), doc2, 0);
        check("a\nb".equals(doc2.getText(0, doc2.getLength())), "read CRLF normalized text");
        check("\r\n".equals(doc2.getProperty(DefaultEditorKit.EndOfLineStringProperty)), "read CRLF eol property");

        // Reader read with "\r" normalized to "\n"
        Document doc3 = kit.createDefaultDocument();
        kit.read(new StringReader("x\ry"), doc3, 0);
        check("x\ny".equals(doc3.getText(0, doc3.getLength())), "read CR normalized text");
        check("\r".equals(doc3.getProperty(DefaultEditorKit.EndOfLineStringProperty)), "read CR eol property");

        // InputStream read (ASCII)
        Document doc4 = kit.createDefaultDocument();
        kit.read(new ByteArrayInputStream("abc".getBytes("US-ASCII")), doc4, 0);
        check("abc".equals(doc4.getText(0, doc4.getLength())), "stream read text");

        // Writer write translating "\n" to explicit eol property
        Document doc5 = kit.createDefaultDocument();
        doc5.insertString(0, "a\nb", null);
        doc5.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\r\n");
        StringWriter sw = new StringWriter();
        kit.write(sw, doc5, 0, doc5.getLength());
        check("a\r\nb".equals(sw.toString()), "write eol translation");

        // Writer write of a substring
        StringWriter sw2 = new StringWriter();
        kit.write(sw2, doc5, 0, 1);
        check("a".equals(sw2.toString()), "write substring");

        // OutputStream write roundtrip (ASCII)
        Document doc6 = kit.createDefaultDocument();
        doc6.insertString(0, "plain", null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        kit.write(bos, doc6, 0, doc6.getLength());
        check("plain".equals(bos.toString("US-ASCII")), "stream write roundtrip");

        // BadLocationException for invalid pos / overflow len
        boolean ble1 = false;
        try { kit.write(new StringWriter(), doc5, -1, 0); } catch (BadLocationException e) { ble1 = true; }
        check(ble1, "write negative pos throws BadLocationException");
        boolean ble2 = false;
        try { kit.write(new StringWriter(), doc5, 0, doc5.getLength() + 1); } catch (BadLocationException e) { ble2 = true; }
        check(ble2, "write overflow len throws BadLocationException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

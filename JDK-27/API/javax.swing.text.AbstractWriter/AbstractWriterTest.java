/*
 * @test
 * @summary javax.swing.text.AbstractWriter conformance: white-box structure + black-box behavior.
 * @run main AbstractWriterTest
 */

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import javax.swing.text.AbstractWriter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.PlainDocument;

public class AbstractWriterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Concrete subclass exposing the protected surface for black-box checks. */
    static class WriterImpl extends AbstractWriter {
        WriterImpl(Writer w, Document doc) { super(w, doc); }
        WriterImpl(Writer w, Document doc, int pos, int len) { super(w, doc, pos, len); }

        public String textOf(Element e) throws BadLocationException { return getText(e); }
        public Document document() { return getDocument(); }
        public ElementIterator elementIterator() { return getElementIterator(); }
        public void emitAll() throws IOException, BadLocationException {
            ElementIterator it = getElementIterator();
            Element e;
            while ((e = it.next()) != null) {
                if (e.isLeaf()) {
                    text(e);
                }
            }
        }
        @Override
        protected void write() throws IOException, BadLocationException {
            emitAll();
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractWriter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PlainDocument doc = new PlainDocument();
        doc.insertString(0, "hello\nworld", null);

        StringWriter sw = new StringWriter();
        WriterImpl w = new WriterImpl(sw, doc);

        // Full-range constructor defaults to [0, document length).
        check(w.getStartOffset() == 0, "default start offset 0");
        check(w.getEndOffset() == doc.getLength(), "default end offset == doc length");
        check(w.document() == doc, "getDocument identity");
        check(w.elementIterator().first() == doc.getDefaultRootElement(), "element iterator rooted at default root");

        // Default line separator is the system line separator.
        check(w.getLineSeparator().equals(System.lineSeparator()), "default line separator");
        w.setLineSeparator("__NL__");
        check(w.getLineSeparator().equals("__NL__"), "setLineSeparator/getLineSeparator roundtrip");

        // Range constructor sets the offsets exactly.
        StringWriter sw2 = new StringWriter();
        WriterImpl range = new WriterImpl(sw2, doc, 1, 4);
        check(range.getStartOffset() == 1, "range start offset");
        check(range.getEndOffset() == 5, "range end offset == pos+len");

        // Writing leaves reproduces the document text (no display needed).
        StringWriter sw3 = new StringWriter();
        WriterImpl emit = new WriterImpl(sw3, doc);
        emit.emitAll();
        check(sw3.toString().equals("hello\nworld"), "emitAll reproduces document text");

        // getText(Element) returns the text of a leaf element.
        Element root = doc.getDefaultRootElement();
        Element leaf0 = root.getElement(0);
        check(emit.textOf(leaf0).equals("hello\n"), "getText leaf0");

        // Zero-length range writes nothing.
        StringWriter sw4 = new StringWriter();
        WriterImpl empty = new WriterImpl(sw4, doc, 3, 0);
        check(empty.getStartOffset() == 3 && empty.getEndOffset() == 3, "zero-length offsets");
        empty.emitAll();
        check(sw4.toString().equals(""), "zero-length range writes nothing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

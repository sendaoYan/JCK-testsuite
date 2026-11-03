/*
 * @test
 * @summary javax.swing.text.Document conformance: white-box structure + black-box behavior.
 * @run main DocumentTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;

public class DocumentTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Document");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Contract constants.
        check("stream".equals(Document.StreamDescriptionProperty), "StreamDescriptionProperty");
        check("title".equals(Document.TitleProperty), "TitleProperty");

        // The interface contract is verified through a concrete Document.
        Document doc = new PlainDocument();
        check(doc.getLength() == 0, "initial length 0");

        doc.insertString(0, "one two three", null);
        check(doc.getLength() == 13, "length after insert");
        check("one two three".equals(doc.getText(0, doc.getLength())), "getText round-trip");
        doc.remove(3, 4);
        check("one three".equals(doc.getText(0, doc.getLength())), "remove round-trip");

        Position start = doc.getStartPosition();
        Position end = doc.getEndPosition();
        Position mid = doc.createPosition(4);
        check(start.getOffset() == 0, "start position 0");
        check(end.getOffset() == doc.getLength() + 1, "end position tracks content end");
        doc.insertString(0, "AB", null);
        check(mid.getOffset() == 6, "createPosition tracks insert before it");
        check(end.getOffset() == doc.getLength() + 1, "end position after insert");

        Element[] roots = doc.getRootElements();
        check(roots.length == 2, "getRootElements length 2");
        check(roots[0] == doc.getDefaultRootElement(), "getRootElements[0]");
        check(doc.getDefaultRootElement() != null, "getDefaultRootElement non-null");

        doc.putProperty("p", "q");
        check("q".equals(doc.getProperty("p")), "put/get property");
        doc.putProperty("p", null);
        check(doc.getProperty("p") == null, "putProperty null removes");

        final boolean[] rendered = new boolean[1];
        doc.render(new Runnable() { public void run() { rendered[0] = true; } });
        check(rendered[0], "render executes");

        final int[] updates = new int[1];
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updates[0]++; }
            public void removeUpdate(DocumentEvent e) { updates[0]++; }
            public void changedUpdate(DocumentEvent e) {}
        };
        doc.addDocumentListener(dl);
        doc.insertString(doc.getLength(), "z", null);
        doc.remove(doc.getLength() - 1, 1);
        check(updates[0] == 2, "insert+remove both notify");
        doc.removeDocumentListener(dl);
        doc.insertString(doc.getLength(), "q", null);
        check(updates[0] == 2, "removed listener no longer notified");

        expectThrows(BadLocationException.class, () -> doc.insertString(doc.getLength() + 1, "x", null), "insert past end");
        expectThrows(BadLocationException.class, () -> doc.getText(0, -1), "getText negative length");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

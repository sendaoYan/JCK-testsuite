/*
 * @test
 * @summary javax.swing.text.AbstractDocument conformance: white-box structure + black-box behavior.
 * @run main AbstractDocumentTest
 */

import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;

public class AbstractDocumentTest {
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
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Document"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PlainDocument doc = new PlainDocument();

        // length / text / insert / remove round-trips through AbstractDocument API
        check(doc.getLength() == 0, "initial length 0");
        doc.insertString(0, "Hello", null);
        check(doc.getLength() == 5, "length after insert 5");
        check("Hello".equals(doc.getText(0, 5)), "getText round-trip");
        check("Hello".equals(doc.getText(0, doc.getLength())), "full getText");
        doc.insertString(doc.getLength(), " World", null);
        check("Hello World".equals(doc.getText(0, doc.getLength())), "append via insertString");
        doc.remove(5, 6);
        check("Hello".equals(doc.getText(0, doc.getLength())), "remove round-trip");
        doc.replace(5, 0, "!", null);
        check("Hello!".equals(doc.getText(0, doc.getLength())), "replace as insert");

        // element tree traversal via the abstract accessors
        Element root = doc.getDefaultRootElement();
        check(root != null, "default root non-null");
        check(root.getElementCount() == 1, "single paragraph");
        Element para = doc.getParagraphElement(0);
        check(para == root.getElement(0), "paragraph is first child of root");
        check(para.getStartOffset() == 0 && para.getEndOffset() == doc.getLength() + 1, "paragraph range");

        // position tracking
        Position p = doc.createPosition(2);
        doc.insertString(0, "AB", null);
        check(p.getOffset() == 4, "createPosition tracks insert before it");
        check(doc.getStartPosition().getOffset() == 0, "start position");
        check(doc.getEndPosition().getOffset() == doc.getLength() + 1, "end position");

        // root elements list
        Element[] roots = doc.getRootElements();
        check(roots.length == 2, "getRootElements length 2");
        check(roots[0] == doc.getDefaultRootElement(), "getRootElements[0] default root");
        check(roots[1] == doc.getBidiRootElement(), "getRootElements[1] bidi root");

        // document properties
        check(doc.getProperty("absent") == null, "absent property null");
        doc.putProperty("k", "v");
        check("v".equals(doc.getProperty("k")), "put/get property");
        doc.putProperty("k", null);
        check(doc.getProperty("k") == null, "putProperty null removes");
        check(doc.getDocumentProperties() != null, "document properties non-null");
        Dictionary<Object,Object> origProps = doc.getDocumentProperties();
        Dictionary<Object,Object> dict = new Hashtable<Object,Object>();
        doc.setDocumentProperties(dict);
        check(doc.getDocumentProperties() == dict, "setDocumentProperties replaces");
        // Restore defaults; later insertString reads the "i18n" property.
        doc.setDocumentProperties(origProps);
        check(doc.getDocumentProperties() == origProps, "document properties restored");

        // asynchronous load priority
        check(doc.getAsynchronousLoadPriority() == -1, "default async priority -1");
        doc.setAsynchronousLoadPriority(7);
        check(doc.getAsynchronousLoadPriority() == 7, "set async priority");
        doc.setAsynchronousLoadPriority(-1);
        check(doc.getAsynchronousLoadPriority() == -1, "reset async priority");

        // document filter
        check(doc.getDocumentFilter() == null, "default filter null");
        DocumentFilter f = new DocumentFilter();
        doc.setDocumentFilter(f);
        check(doc.getDocumentFilter() == f, "set/get document filter");
        doc.setDocumentFilter(null);

        // render executes runnable
        final boolean[] ran = new boolean[1];
        doc.render(new Runnable() { public void run() { ran[0] = true; } });
        check(ran[0], "render executes runnable");

        // document listener add/remove/notify
        final int[] ins = new int[1];
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { ins[0]++; }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        };
        doc.addDocumentListener(dl);
        doc.insertString(0, "x", null);
        check(ins[0] == 1, "document listener notified on insert");
        doc.removeDocumentListener(dl);
        doc.insertString(0, "y", null);
        check(ins[0] == 1, "document listener removed");

        // undo via UndoableEditListener
        final UndoableEdit[] cap = new UndoableEdit[1];
        doc.addUndoableEditListener(e -> cap[0] = e.getEdit());
        String before = doc.getText(0, doc.getLength());
        doc.insertString(doc.getLength(), "ZZZ", null);
        check(doc.getText(0, doc.getLength()).equals(before + "ZZZ"), "inserted before undo");
        cap[0].undo();
        check(doc.getText(0, doc.getLength()).equals(before), "undoable edit undo reverts insert");
        cap[0].redo();
        check(doc.getText(0, doc.getLength()).equals(before + "ZZZ"), "undoable edit redo reapplies");

        // exceptions
        expectThrows(BadLocationException.class, () -> doc.getText(0, -1), "getText negative length");
        expectThrows(BadLocationException.class, () -> doc.insertString(doc.getLength() + 1, "x", null), "insert past end");
        expectThrows(BadLocationException.class, () -> doc.remove(0, doc.getLength() + 1), "remove past end");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

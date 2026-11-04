/*
 * @test
 * @summary javax.swing.text.AbstractDocument.DefaultDocumentEvent conformance: white-box structure + black-box behavior.
 * @run main DefaultDocumentEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;

public class DefaultDocumentEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$DefaultDocumentEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.CompoundEdit"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.DocumentEvent"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        PlainDocument pd = new PlainDocument();

        // Directly construct (non-static inner class) to inspect getters.
        AbstractDocument.DefaultDocumentEvent dde =
            pd.new DefaultDocumentEvent(3, 2, DocumentEvent.EventType.INSERT);
        check(dde.getType() == DocumentEvent.EventType.INSERT, "getType INSERT");
        check(dde.getOffset() == 3, "getOffset");
        check(dde.getLength() == 2, "getLength");
        check(dde.getDocument() == pd, "getDocument");
        check(dde.isSignificant(), "isSignificant true");
        check(dde.getPresentationName() != null, "getPresentationName non-null");
        check(dde.getUndoPresentationName() != null, "getUndoPresentationName non-null");
        check(dde.getRedoPresentationName() != null, "getRedoPresentationName non-null");
        check(dde.toString() != null, "toString non-null");

        AbstractDocument.DefaultDocumentEvent rm =
            pd.new DefaultDocumentEvent(0, 1, DocumentEvent.EventType.REMOVE);
        check(rm.getType() == DocumentEvent.EventType.REMOVE, "getType REMOVE");

        // Capture a real DefaultDocumentEvent from an insertion that changes
        // element structure (newline inserted).
        final AbstractDocument.DefaultDocumentEvent[] cap = new AbstractDocument.DefaultDocumentEvent[1];
        pd.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                cap[0] = (AbstractDocument.DefaultDocumentEvent) e;
            }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        });
        pd.insertString(0, "a\nb", null);
        check(cap[0] != null, "insert fires DefaultDocumentEvent");
        check(cap[0].getType() == DocumentEvent.EventType.INSERT, "captured event INSERT");
        check(cap[0].getOffset() == 0, "captured offset 0");
        check(cap[0].getLength() == 3, "captured length 3");
        check(cap[0].getDocument() == pd, "captured document");

        Element root = pd.getDefaultRootElement();
        DocumentEvent.ElementChange chg = cap[0].getChange(root);
        check(chg != null, "getChange(root) non-null when structure changes");
        check(chg.getElement() == root, "element change element is root");

        // No element change for a single-line insert.
        PlainDocument pd2 = new PlainDocument();
        final AbstractDocument.DefaultDocumentEvent[] cap3 = new AbstractDocument.DefaultDocumentEvent[1];
        pd2.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { cap3[0] = (AbstractDocument.DefaultDocumentEvent) e; }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        });
        pd2.insertString(0, "plain", null);
        check(cap3[0].getChange(pd2.getDefaultRootElement()) == null,
              "getChange null when no structure change");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

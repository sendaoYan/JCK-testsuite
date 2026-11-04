/*
 * @test
 * @summary javax.swing.text.AbstractDocument.ElementEdit conformance: ElementChange payload getters, undo/redo element swaps, event-derived changes (headless-safe).
 * @run main ElementEditTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.undo.UndoableEdit;

public class ElementEditTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static class OpenDoc extends PlainDocument {
        Element makeBranch(Element parent, AttributeSet a) {
            writeLock();
            try { return ((AbstractDocument) this).new BranchElement(parent, a); }
            finally { writeUnlock(); }
        }
        Element makeLeaf(Element parent, AttributeSet a, int o0, int o1) {
            writeLock();
            try { return ((AbstractDocument) this).new LeafElement(parent, a, o0, o1); }
            finally { writeUnlock(); }
        }
        void branchReplace(Element b, int i, int n, Element[] e) {
            writeLock();
            try { ((AbstractDocument.BranchElement) b).replace(i, n, e); }
            finally { writeUnlock(); }
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$ElementEdit", false, ElementEditTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass().getName().equals("javax.swing.undo.AbstractUndoableEdit"), "superclass");
        check(hasInterface(c, "javax.swing.event.DocumentEvent$ElementChange"), "implements DocumentEvent.ElementChange");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getElement"), "method getElement");
        check(names.contains("getIndex"), "method getIndex");
        check(names.contains("getChildrenRemoved"), "method getChildrenRemoved");
        check(names.contains("getChildrenAdded"), "method getChildrenAdded");
        check(names.contains("undo"), "method undo");
        check(names.contains("redo"), "method redo");

        testGettersAndUndoRedo();
        testFromEvent();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testGettersAndUndoRedo() throws Exception {
        OpenDoc d = new OpenDoc();
        d.insertString(0, "abcdef", null);
        Element root = d.getDefaultRootElement();
        Element branch = d.makeBranch(root, null);
        Element l1 = d.makeLeaf(branch, SimpleAttributeSet.EMPTY, 0, 3);
        Element l2 = d.makeLeaf(branch, SimpleAttributeSet.EMPTY, 3, 6);
        d.branchReplace(branch, 0, 0, new Element[] { l1, l2 });
        check(branch.getElement(0).getStartOffset() == 0, "initial first child starts at 0");

        // record the swap of l1 -> l4
        Element l4 = d.makeLeaf(branch, SimpleAttributeSet.EMPTY, 9, 12);
        AbstractDocument.ElementEdit edit =
                new AbstractDocument.ElementEdit(branch, 0, new Element[] { l1 }, new Element[] { l4 });
        check(edit.getElement() == branch, "getElement identity");
        check(edit.getIndex() == 0, "getIndex");
        check(edit.getChildrenRemoved().length == 1 && edit.getChildrenRemoved()[0] == l1, "removed recorded");
        check(edit.getChildrenAdded().length == 1 && edit.getChildrenAdded()[0] == l4, "added recorded");
        check(edit instanceof UndoableEdit, "is an undoable edit");
        check(edit.isSignificant(), "significant");
        check(edit.canUndo() && !edit.canRedo(), "fresh edit can undo only");

        // the edit starts in the "done" state: undo first, then redo, then undo
        edit.undo();
        check(branch.getElement(0) == l1, "undo while already reverted is positionally stable");
        edit.redo();
        check(branch.getElement(0) == l4 && branch.getElement(0).getStartOffset() == 9, "redo installs the added element");
        check(branch.getElementCount() == 2, "sibling untouched by redo");
        edit.undo();
        check(branch.getElement(0) == l1 && branch.getElement(0).getStartOffset() == 0, "undo restores the removed element");

        // empty edits are legal records
        AbstractDocument.ElementEdit empty =
                new AbstractDocument.ElementEdit(branch, 0, new Element[0], new Element[0]);
        check(empty.getChildrenRemoved().length == 0 && empty.getChildrenAdded().length == 0, "empty record");
        check(empty.getElement() == branch && empty.getIndex() == 0, "empty record metadata");
    }

    static void testFromEvent() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        final DocumentEvent[] last = new DocumentEvent[1];
        d.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { last[0] = e; }
            public void removeUpdate(DocumentEvent e) { }
            public void changedUpdate(DocumentEvent e) { }
        });
        d.insertString(0, "ab\ncd", null);
        DocumentEvent.ElementChange ch = last[0].getChange(d.getDefaultRootElement());
        check(ch != null, "section change captured");
        check(ch instanceof AbstractDocument.ElementEdit, "change is an ElementEdit");
        AbstractDocument.ElementEdit ee = (AbstractDocument.ElementEdit) ch;
        check(ee.getElement() == d.getDefaultRootElement(), "event edit element");
        check(ee.getIndex() == 1, "event edit index");
        check(ee.getChildrenAdded().length == 1, "event edit added one paragraph");
        check(ee.getChildrenRemoved().length == 0, "event edit removed none");
        // a document with no structural change reports no ElementEdit
        PlainDocument p = new PlainDocument();
        final DocumentEvent[] plast = new DocumentEvent[1];
        p.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { plast[0] = e; }
            public void removeUpdate(DocumentEvent e) { }
            public void changedUpdate(DocumentEvent e) { }
        });
        p.insertString(0, "one line", null);
        check(plast[0].getChange(p.getDefaultRootElement()) == null, "plain single-line insert has no element change");
    }
}

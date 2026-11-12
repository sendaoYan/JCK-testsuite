/*
 * @test
 * @summary javax.swing.text.AbstractDocument.DefaultDocumentEvent conformance: event payload (offset/length/type/document), presentation names, undo/redo through the event, element changes (headless-safe).
 * @run main DefaultDocumentEventTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

public class DefaultDocumentEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static DocumentEvent[] lastEvent = new DocumentEvent[1];

    static DocumentListener recorder() {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { lastEvent[0] = e; }
            public void removeUpdate(DocumentEvent e) { lastEvent[0] = e; }
            public void changedUpdate(DocumentEvent e) { lastEvent[0] = e; }
        };
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$DefaultDocumentEvent", false, DefaultDocumentEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.CompoundEdit"), "superclass");
        check(hasInterface(c, "javax.swing.event.DocumentEvent"), "implements javax.swing.event.DocumentEvent");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("toString"), "method toString");
        check(names.contains("addEdit"), "method addEdit");
        check(names.contains("redo"), "method redo");
        check(names.contains("undo"), "method undo");
        check(names.contains("isSignificant"), "method isSignificant");
        check(names.contains("getPresentationName"), "method getPresentationName");
        check(names.contains("getUndoPresentationName"), "method getUndoPresentationName");

        testInsertEvent();
        testRemoveEvent();
        testUndoThroughEvent();
        testElementChange();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testInsertEvent() throws Exception {
        PlainDocument d = new PlainDocument();
        d.addDocumentListener(recorder());
        d.insertString(0, "hello", null);
        DocumentEvent e = lastEvent[0];
        check(e != null, "event captured");
        check(e.getClass().getName().equals("javax.swing.text.AbstractDocument$DefaultDocumentEvent"),
                "event is a DefaultDocumentEvent");
        check(e.getOffset() == 0 && e.getLength() == 5, "offset/length");
        check(e.getType() == DocumentEvent.EventType.INSERT, "type INSERT");
        check(e.getDocument() == d, "document identity");
        check(e.getChange(d.getDefaultRootElement()) == null, "no structural change on a plain single-line insert");
        check(e.toString() != null, "toString non-null");
    }

    static void testRemoveEvent() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "abcdef", null);
        d.addDocumentListener(recorder());
        d.remove(2, 3);
        DocumentEvent e = lastEvent[0];
        check(e.getOffset() == 2 && e.getLength() == 3, "remove offset/length");
        check(e.getType() == DocumentEvent.EventType.REMOVE, "type REMOVE");
        check(e.getDocument() == d, "remove document identity");
        // event types enum
        check(DocumentEvent.EventType.INSERT.toString().equals("INSERT"), "INSERT enum name");
        check(DocumentEvent.EventType.REMOVE.toString().equals("REMOVE"), "REMOVE enum name");
        check(DocumentEvent.EventType.CHANGE.toString().equals("CHANGE"), "CHANGE enum name");
        check(DocumentEvent.EventType.INSERT != DocumentEvent.EventType.REMOVE
                && DocumentEvent.EventType.REMOVE != DocumentEvent.EventType.CHANGE, "three distinct event types");
    }

    static void testUndoThroughEvent() throws Exception {
        PlainDocument d = new PlainDocument();
        d.addDocumentListener(recorder());
        d.insertString(0, "hello", null);
        DocumentEvent e = lastEvent[0];
        // the event doubles as a CompoundEdit: undoing it reverts the insertion
        CompoundEdit edit = (CompoundEdit) e;
        check(edit.isSignificant(), "document edit is significant");
        check(edit.canUndo(), "fresh edit canUndo");
        check("addition".equals(edit.getPresentationName()), "insert presentation name");
        edit.undo();
        check(d.getLength() == 0, "undo through the event reverts the insert");
        check(!edit.canUndo() && edit.canRedo(), "post-undo state");
        check("deletion".equals(edit.getUndoPresentationName()) || edit.getUndoPresentationName() != null,
                "undo presentation name present");
        edit.redo();
        check(d.getLength() == 5 && "hello".equals(d.getText(0, 5)), "redo restores content");

        // the same edit object is published to undoable listeners as a single edit
        PlainDocument d2 = new PlainDocument();
        final UndoableEdit[] published = new UndoableEdit[1];
        d2.addUndoableEditListener(ev -> published[0] = ev.getEdit());
        d2.insertString(0, "x", null);
        check(published[0] != null && published[0].isSignificant(), "insert publishes one significant edit");
    }

    static void testElementChange() throws Exception {
        DefaultStyledDocument d = new DefaultStyledDocument();
        d.addDocumentListener(recorder());
        // inserting two paragraphs reports structural changes
        d.insertString(0, "ab\ncd", null);
        DocumentEvent e = lastEvent[0];
        Element sec = d.getDefaultRootElement();
        DocumentEvent.ElementChange ch = e.getChange(sec);
        check(ch != null, "section reports an element change");
        check(ch.getElement() == sec, "change names the section");
        check(ch.getIndex() == 1, "paragraph inserted at index 1");
        check(ch.getChildrenAdded().length == 1, "one paragraph added");
        check(ch.getChildrenRemoved().length == 0, "nothing removed at section level");
        // the affected paragraph replaced its content element
        Element para0 = d.getParagraphElement(0);
        DocumentEvent.ElementChange pch = e.getChange(para0);
        check(pch != null, "paragraph reports an element change");
        check(pch.getElement() == para0, "change names the paragraph");
        check(pch.getIndex() == 0, "content replaced at index 0");
        check(pch.getChildrenAdded().length == 1 && pch.getChildrenRemoved().length == 1,
                "content run swapped");
        // an element not structurally involved reports nothing
        check(e.getChange(d.getBidiRootElement()) == null, "uninvolved element reports no change");
        // ElementChange is also an UndoableEdit
        check(ch instanceof UndoableEdit, "ElementChange doubles as UndoableEdit");
    }
}

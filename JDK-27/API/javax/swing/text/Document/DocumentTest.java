/*
 * @test
 * @summary javax.swing.text.Document conformance: interface shape + Document contract umbrella over PlainDocument(GapContent), PlainDocument(StringContent), DefaultStyledDocument (headless-safe).
 * @run main DocumentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StringContent;

public class DocumentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static interface Throwing { Object run() throws Exception; }
    static void expectBLE(Throwing r, String m, int expectedOffset) throws Exception {
        try {
            Object o = r.run();
            throw new AssertionError(m + ": expected BadLocationException but returned " + o);
        } catch (BadLocationException e) {
            check(e.offsetRequested() == expectedOffset, m + " offsetRequested==" + expectedOffset + " (got " + e.offsetRequested() + ")");
        }
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Document", false, DocumentTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(!c.isEnum() && !c.isAnnotation(), "not enum/annotation");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLength"), "method getLength");
        check(names.contains("addDocumentListener"), "method addDocumentListener");
        check(names.contains("removeDocumentListener"), "method removeDocumentListener");
        check(names.contains("addUndoableEditListener"), "method addUndoableEditListener");
        check(names.contains("removeUndoableEditListener"), "method removeUndoableEditListener");
        check(names.contains("getProperty"), "method getProperty");
        check(names.contains("putProperty"), "method putProperty");
        check(names.contains("remove"), "method remove");
        check(names.contains("insertString"), "method insertString");
        check(names.contains("getText"), "method getText");
        check(names.contains("getStartPosition"), "method getStartPosition");
        check(names.contains("getEndPosition"), "method getEndPosition");
        check(names.contains("createPosition"), "method createPosition");
        check(names.contains("getRootElements"), "method getRootElements");
        check(names.contains("getDefaultRootElement"), "method getDefaultRootElement");
        check(names.contains("render"), "method render");
        // well-known property keys
        java.lang.reflect.Field f1 = c.getField("StreamDescriptionProperty");
        check(f1.get(null) instanceof String, "StreamDescriptionProperty is String");
        java.lang.reflect.Field f2 = c.getField("TitleProperty");
        check(f2.get(null) instanceof String, "TitleProperty is String");
        check(!"TitleProperty".equals(f1.get(null)), "distinct key values");
    }

    static Vector<String> docEvents = new Vector<String>();

    static void exercise(Document d, String tag) throws Exception {
        // empty state
        check(d.getLength() == 0, tag + " empty length 0");
        check("".equals(d.getText(0, 0)), tag + " empty getText");
        check(d.getRootElements().length >= 1, tag + " has root elements");
        check(d.getRootElements()[0] == d.getDefaultRootElement(), tag + " root 0 is default root");
        check(d.getDefaultRootElement().getStartOffset() == 0, tag + " root starts at 0");
        check(d.getDefaultRootElement().getEndOffset() == 1, tag + " empty root ends at 1 (implicit newline)");
        check(d.getProperty("noSuchKey") == null, tag + " missing property null");
        check(d.getStartPosition().getOffset() == 0, tag + " start position 0");

        // insert / read back
        d.insertString(0, "hello", null);
        check(d.getLength() == 5, tag + " length after insert");
        check("hello".equals(d.getText(0, 5)), tag + " read back");
        check("ell".equals(d.getText(1, 3)), tag + " middle slice");
        check("".equals(d.getText(5, 0)), tag + " empty slice at end");
        d.insertString(2, "XX", null);
        check("heXXllo".equals(d.getText(0, 7)), tag + " middle insert");

        // Segment variant
        Segment seg = new Segment();
        d.getText(1, 2, seg);
        check("eX".equals(seg.toString()), tag + " getText into Segment");

        // remove
        d.remove(2, 2);
        check("hello".equals(d.getText(0, 5)), tag + " remove middle");
        d.remove(0, 5);
        check(d.getLength() == 0, tag + " remove all");

        // properties
        d.putProperty("pk", "pv");
        check("pv".equals(d.getProperty("pk")), tag + " put/getProperty");
        d.putProperty("pk", null);
        check(d.getProperty("pk") == null, tag + " null value removes property");

        // positions
        d.insertString(0, "abcde", null);
        Position p = d.createPosition(2);
        check(p.getOffset() == 2, tag + " position offset");
        d.insertString(2, "Z", null);
        check(p.getOffset() == 3, tag + " position tracks insert");
        check(d.getEndPosition().getOffset() == 7, tag + " end position at content end");

        // render executes without a display
        final int[] ran = { 0 };
        d.render(new Runnable() { public void run() { ran[0] += d.getLength(); } });
        check(ran[0] == 6, tag + " render runs runnable");

        // listener events with exact offsets/lengths/types
        docEvents.clear();
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { docEvents.add("I" + e.getOffset() + ":" + e.getLength() + ":" + e.getType()); }
            public void removeUpdate(DocumentEvent e) { docEvents.add("R" + e.getOffset() + ":" + e.getLength() + ":" + e.getType()); }
            public void changedUpdate(DocumentEvent e) { docEvents.add("C" + e.getOffset() + ":" + e.getLength() + ":" + e.getType()); }
        };
        d.addDocumentListener(dl);
        d.insertString(0, "Q", null);
        check(docEvents.size() == 1 && docEvents.get(0).equals("I0:1:INSERT"), tag + " insert event exact");
        d.remove(0, 1);
        check(docEvents.size() == 2 && docEvents.get(1).equals("R0:1:REMOVE"), tag + " remove event exact");
        d.removeDocumentListener(dl);
        d.insertString(0, "W", null);
        check(docEvents.size() == 2, tag + " no events after removeDocumentListener");
        d.remove(0, 1);

        // shared negative contract: negative length getText rejected
        expectBLE(() -> d.getText(0, -1), tag + " negative length", -1);
        // full range including the implicit newline is readable
        check(d.getText(0, d.getLength() + 1).length() == d.getLength() + 1, tag + " text incl newline");
    }

    static void testNegativeOffsetGetText() throws Exception {
        // negative offset surfaces differently per Content implementation:
        // GapContent rejects it with BLE(-1); StringContent lets String construction fail
        PlainDocument gap = new PlainDocument();
        gap.insertString(0, "hello", null);
        expectBLE(() -> gap.getText(-1, 1), "gap negative offset", -1);
        PlainDocument str = new PlainDocument(new StringContent());
        str.insertString(0, "hello", null);
        try {
            str.getText(-1, 1);
            check(false, "string-content negative offset should throw");
        } catch (StringIndexOutOfBoundsException e) {
            check(true, "string-content negative offset SIOOBE");
        }
    }

    static void testInsertBounds() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "hello", null);
        // insertString with null/empty string is a no-op regardless of offset
        d.insertString(999, null, null);
        check(d.getLength() == 5, "null string insert no-op");
        d.insertString(999, "", null);
        check(d.getLength() == 5, "empty string insert no-op");
        d.insertString(-1, "", null);
        check(d.getLength() == 5, "negative offset empty insert no-op");
        // negative offset with content throws
        expectBLE(() -> { d.insertString(-1, "x", null); return null; }, "negative offset insert", 6);
    }

    static void testRemoveBounds() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "hello", null);
        // zero-length removals never throw (short-circuit)
        d.remove(999, 0);
        d.remove(-1, 0);
        check(d.getLength() == 5, "zero-length removes are no-ops");
        // offs < 0 with len > 0
        expectBLE(() -> { d.remove(-1, 1); return null; }, "negative remove", 6);
        // offs + len > getLength()
        expectBLE(() -> { d.remove(4, 2); return null; }, "remove past end", 6);
        expectBLE(() -> { d.remove(5, 1); return null; }, "remove at end", 6);
        // removing everything including offset getLength() works
        d.remove(0, 5);
        check(d.getLength() == 0, "remove all content");
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        exercise(new PlainDocument(), "plain-gap");
        exercise(new PlainDocument(new StringContent()), "plain-string");
        exercise(new DefaultStyledDocument(), "styled");
        testNegativeOffsetGetText();
        testInsertBounds();
        testRemoveBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

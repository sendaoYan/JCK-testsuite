/*
 * @test
 * @summary javax.swing.text.StringContent conformance: Content contract + exact BadLocationException matrix + positions + undo/redo + serialization (headless-safe).
 * @run main StringContentTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StringContent;
import javax.swing.undo.UndoableEdit;

public class StringContentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Throwing { Object run() throws Exception; }
    static void expectBLE(Throwing r, String m, int expectedOffset) throws Exception {
        try {
            Object o = r.run();
            throw new AssertionError(m + ": expected BadLocationException but returned " + o);
        } catch (BadLocationException e) {
            check(e.offsetRequested() == expectedOffset, m + " offsetRequested==" + expectedOffset + " (got " + e.offsetRequested() + ")");
        }
    }

    static void testInitial() {
        StringContent s = new StringContent();
        check(s.length() == 1, "default initial length 1");
        StringContent s10 = new StringContent(10);
        check(s10.length() == 1, "sized initial length 1");
        StringContent s0 = new StringContent(0);
        check(s0.length() == 1, "size < 1 clamped");
        StringContent sNeg = new StringContent(-7);
        check(sNeg.length() == 1, "negative size clamped");
    }

    static void testInsert() throws Exception {
        StringContent s = new StringContent();
        s.insertString(0, "hi");
        check(s.length() == 3, "length after insert");
        check("hi".equals(s.getString(0, 2)), "content");
        check("\n".equals(s.getString(2, 1)), "implicit newline at end");

        s.insertString(1, "X"); // hXi
        check("hXi".equals(s.getString(0, 3)), "mid insert");

        // bounds: where < 0 or where >= length() is invalid. Inserting just before
        // the trailing newline (length()-1) is the legal end position; inserting
        // right at length() (after the implicit newline) is refused.
        StringContent b = new StringContent();
        b.insertString(0, "abc"); // length 4
        expectBLE(() -> b.insertString(b.length(), "z"), "insert at length()", b.length());
    }

    static void testInsertBounds() throws Exception {
        StringContent b = new StringContent();
        b.insertString(0, "abc");
        // inserting just before the trailing newline is the legal end position
        b.insertString(3, "d");
        check("abcd".equals(b.getString(0, 4)), "insert at end of user content");
        expectBLE(() -> b.insertString(-1, "z"), "insert negative", b.length());
        expectBLE(() -> b.insertString(5, "z"), "insert beyond", b.length());
        int len = b.length();
        b.insertString(0, "");
        check(b.length() == len, "empty insert no-op");
    }

    static void testRemove() throws Exception {
        StringContent s = new StringContent();
        s.insertString(0, "abcdef");
        s.remove(0, 2);
        check("cdef".equals(s.getString(0, 4)), "remove front");
        s.remove(2, 2);
        check("cd".equals(s.getString(0, 2)), "remove end");
        // where + nitems must be < length(): the trailing newline cannot be removed
        expectBLE(() -> s.remove(2, 1), "remove trailing newline", s.length());
        expectBLE(() -> s.remove(0, 3), "remove past end", s.length());
        // negative offsets fall through to String construction and surface as
        // StringIndexOutOfBoundsException rather than BadLocationException
        try {
            s.remove(-1, 1);
            check(false, "remove negative should throw");
        } catch (StringIndexOutOfBoundsException e) {
            check(true, "remove negative SIOOBE");
        }
        expectBLE(() -> s.remove(3, 0), "remove(3,0) past end", s.length());
        s.remove(0, 0);
        check("cd".equals(s.getString(0, 2)), "remove(.,0) no-op");
    }

    static void testGetStringGetChars() throws Exception {
        StringContent s = new StringContent();
        s.insertString(0, "abcdefgh");
        check("".equals(s.getString(0, 0)), "empty range");
        check("abc".equals(s.getString(0, 3)), "prefix");
        expectBLE(() -> s.getString(0, 10), "getString past end", s.length());
        check("".equals(s.getString(s.length(), 0)), "getString at end zero length ok");
        expectBLE(() -> s.getString(10, 0), "getString offset past end", s.length());
        try {
            s.getString(-1, 0);
            check(false, "getString negative should throw");
        } catch (StringIndexOutOfBoundsException e) {
            check(true, "getString negative SIOOBE");
        }

        Segment seg = new Segment();
        s.getChars(2, 3, seg);
        check(seg.count == 3, "segment count");
        check("cde".equals(seg.toString()), "segment content");
        expectBLE(() -> { s.getChars(2, 100, seg); return null; }, "getChars past end", s.length());
    }

    static void testPositions() throws Exception {
        StringContent s = new StringContent();
        s.insertString(0, "hello");
        Position p0 = s.createPosition(0);
        Position p2 = s.createPosition(2);
        Position p5 = s.createPosition(5);
        check(p0.getOffset() == 0, "p0");
        check(p2.getOffset() == 2, "p2");
        check(p5.getOffset() == 5, "p5");

        s.insertString(2, "XX");
        check(p2.getOffset() == 4, "p2 shifts");
        check(p5.getOffset() == 7, "p5 shifts");
        s.remove(2, 2);
        check(p2.getOffset() == 2, "p2 back");
        s.remove(0, 3);
        check(p2.getOffset() == 0, "p2 collapses to remove start");
        check(p0.getOffset() == 0, "p0 pinned");

        // inserting at offset 0 does not drag existing marks forward
        StringContent t = new StringContent();
        t.insertString(0, "ab");
        Position at0 = t.createPosition(0);
        t.insertString(0, "z");
        check(at0.getOffset() == 0, "insert at 0 keeps mark at 0");
    }

    static void testUndoRedo() throws Exception {
        StringContent s = new StringContent();
        UndoableEdit ins = s.insertString(0, "hello");
        check(ins != null, "insert returns edit");
        ins.undo();
        check(s.length() == 1, "undo insert");
        ins.redo();
        check("hello".equals(s.getString(0, 5)), "redo insert");

        Position p2 = s.createPosition(2);
        UndoableEdit rm = s.remove(1, 2); // hlo
        check("hlo".equals(s.getString(0, 3)), "after remove");
        check(p2.getOffset() == 1, "position adjusted");
        rm.undo();
        check("hello".equals(s.getString(0, 5)), "undo remove");
        check(p2.getOffset() == 2, "position restored");
        rm.redo();
        check("hlo".equals(s.getString(0, 3)), "redo remove");
    }

    static void testSerialization() throws Exception {
        StringContent s = new StringContent();
        s.insertString(0, "round trip");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(s);
        oos.close();
        StringContent r = (StringContent) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(r != s, "round-trip distinct");
        check(r.length() == s.length(), "round-trip length");
        check("round trip".equals(r.getString(0, 10)), "round-trip content");
        r.insertString(0, "X");
        check("Xround trip".equals(r.getString(0, 11)), "round-trip mutable");
    }

    public static void main(String[] args) throws Exception {
        testInitial();
        testInsert();
        testInsertBounds();
        testRemove();
        testGetStringGetChars();
        testPositions();
        testUndoRedo();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

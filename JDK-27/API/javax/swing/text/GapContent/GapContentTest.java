/*
 * @test
 * @summary javax.swing.text.GapContent conformance: gap-buffer Content contract + exact BadLocationException matrix + positions + undo/redo + serialization (headless-safe).
 * @run main GapContentTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

public class GapContentTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Throwing { Object run() throws Exception; }
    static Object expectBLE(Throwing r, String m, int expectedOffset) throws Exception {
        try {
            Object o = r.run();
            throw new AssertionError(m + ": expected BadLocationException but returned " + o);
        } catch (BadLocationException e) {
            check(e.offsetRequested() == expectedOffset, m + " offsetRequested==" + expectedOffset + " (got " + e.offsetRequested() + ")");
            return e;
        }
    }

    static void testInitial() {
        GapContent g = new GapContent();
        check(g.length() == 1, "default initial length 1 (implicit newline)");
        GapContent g10 = new GapContent(10);
        check(g10.length() == 1, "sized initial length 1");
        GapContent g0 = new GapContent(0);
        check(g0.length() == 1, "size < 1 clamped to 1");
        GapContent gNeg = new GapContent(-5);
        check(gNeg.length() == 1, "negative size clamped to 1");
    }

    static void testInsert() throws Exception {
        GapContent g = new GapContent();
        g.insertString(0, "hello");
        check(g.length() == 6, "length after insert");
        check("hello".equals(g.getString(0, 5)), "content");
        check("\n".equals(g.getString(5, 1)), "implicit newline at end");

        g.insertString(2, "XX"); // heXXllo
        check(g.length() == 8, "length after mid insert");
        check("heXXllo".equals(g.getString(0, 7)), "mid insert content");

        // inserting just before the trailing newline is the legal end position
        g.insertString(7, "!");
        check("!".equals(g.getString(7, 1)), "insert at end of user content");

        // inserting at 0 repeatedly
        g.insertString(0, "a");
        check("a".equals(g.getString(0, 1)), "insert at 0");

        // empty insert anywhere legal is a no-op
        int len = g.length();
        g.insertString(0, "");
        check(g.length() == len, "empty insert no-op");

        // bounds: where < 0 or where > length(). Unlike StringContent,
        // GapContent also accepts where == length() (right after the implicit newline).
        GapContent b = new GapContent();
        b.insertString(0, "abc"); // length 4
        b.insertString(b.length(), "z");
        check(b.length() == 5, "insert at length() accepted by GapContent");
        expectBLE(() -> b.insertString(b.length() + 1, "z"), "insert beyond length()", b.length());
        expectBLE(() -> b.insertString(-1, "z"), "insert negative", b.length());
        // empty string does not bypass the insert bounds check
        expectBLE(() -> b.insertString(-1, ""), "insert negative empty string", b.length());
    }

    static void testRemove() throws Exception {
        GapContent g = new GapContent();
        g.insertString(0, "abcdef");
        g.remove(0, 2);
        check("cdef".equals(g.getString(0, 4)), "remove at front");
        g.remove(2, 2);
        check("cd".equals(g.getString(0, 2)), "remove at end of user content");
        // where + nitems must stay < length(): removing the trailing newline is refused
        expectBLE(() -> g.remove(2, 1), "remove trailing newline", g.length() + 1);
        expectBLE(() -> g.remove(0, 3), "remove beyond end", g.length() + 1);
        // negative offsets are rejected by the underlying getString with offs=-1
        expectBLE(() -> g.remove(-1, 1), "remove negative offset", -1);
        expectBLE(() -> g.remove(-1, 0), "remove negative offset zero length", -1);
        // len == 0 at a boundary offset still trips the where+nitems >= length() rule
        expectBLE(() -> g.remove(3, 0), "remove(3,0) beyond", g.length() + 1);
        g.remove(0, 0);
        check("cd".equals(g.getString(0, 2)), "remove(.,0) no-op");
        // remove everything except the newline
        g.remove(0, 2);
        check(g.length() == 1, "back to bare newline");
    }

    static void testGetStringGetChars() throws Exception {
        GapContent g = new GapContent();
        g.insertString(0, "abcdefgh");
        check("".equals(g.getString(0, 0)), "empty range");
        check("abc".equals(g.getString(0, 3)), "prefix");
        check("fgh".equals(g.getString(5, 3)), "suffix");
        // the full content including the trailing newline is readable
        check("abcdefgh\n".equals(g.getString(0, 9)), "full content including newline");
        expectBLE(() -> g.getString(0, 10), "getString beyond end", g.length() + 1);
        expectBLE(() -> g.getString(10, 0), "getString offset beyond", g.length() + 1);
        expectBLE(() -> g.getString(-1, 1), "getString negative offset", -1);
        expectBLE(() -> g.getString(-1, 0), "getString negative offset zero length", -1);
        // empty string does not bypass the insert bounds check

        Segment s = new Segment();
        g.getChars(2, 3, s);
        check(s.count == 3, "segment count");
        check("cde".equals(s.toString()), "segment content");
        g.getChars(0, 0, s);
        check(s.count == 0, "segment empty");
        expectBLE(() -> { g.getChars(2, 100, s); return null; }, "getChars beyond end", g.length() + 1);
        g.getChars(0, 9, s);
        check(s.count == 9, "getChars full content");
        expectBLE(() -> { g.getChars(-1, 1, s); return null; }, "getChars negative offset", -1);
    }

    static void testPositions() throws Exception {
        GapContent g = new GapContent();
        g.insertString(0, "hello");
        Position p0 = g.createPosition(0);
        Position p2 = g.createPosition(2);
        Position p5 = g.createPosition(5);
        check(p0.getOffset() == 0 && p2.getOffset() == 2 && p5.getOffset() == 5, "initial offsets");

        g.insertString(2, "XX");
        check(p0.getOffset() == 0, "pos0 unaffected");
        check(p2.getOffset() == 4, "pos2 shifts right");
        check(p5.getOffset() == 7, "pos5 shifts right");

        g.remove(2, 2);
        check(p2.getOffset() == 2, "pos2 shifts back");
        check(p5.getOffset() == 5, "pos5 shifts back");

        g.remove(0, 3);
        check(p0.getOffset() == 0, "pos0 pinned");
        check(p2.getOffset() == 0, "pos inside removed range collapses");

        // GapContent interns marks: two positions at the same offset share the instance
        Position a = g.createPosition(1);
        Position b = g.createPosition(1);
        check(a == b, "same-offset positions interned to one instance");
        check(a.getOffset() == b.getOffset(), "same offset");

        // GapContent.createPosition performs no range validation itself
        Position wild = g.createPosition(10000);
        check(wild.getOffset() == 10000, "out-of-range offset stored verbatim");
    }

    static void testUndoRedo() throws Exception {
        GapContent g = new GapContent();
        UndoableEdit ins = g.insertString(0, "hello");
        check(ins != null, "insert returns edit");
        check(ins.canUndo(), "insert canUndo");
        check(!ins.canRedo(), "fresh insert cannot redo");
        ins.undo();
        check(g.length() == 1, "undo restores bare newline");
        check(!ins.canUndo() && ins.canRedo(), "after undo canRedo");
        ins.redo();
        check("hello".equals(g.getString(0, 5)), "redo restores content");

        Position p2 = g.createPosition(2);
        UndoableEdit rm = g.remove(1, 2); // "hlo"
        check("hlo".equals(g.getString(0, 3)), "content after remove");
        check(p2.getOffset() == 1, "position adjusted by remove");
        rm.undo();
        check("hello".equals(g.getString(0, 5)), "undo remove restores content");
        check(p2.getOffset() == 2, "position restored by undo");
        rm.redo();
        check("hlo".equals(g.getString(0, 3)), "redo remove again");
        check(p2.getOffset() == 1, "position adjusted again by redo");

        // undoing an insertion removes exactly the inserted text
        GapContent g2 = new GapContent();
        UndoableEdit i1 = g2.insertString(0, "abc");
        UndoableEdit i2 = g2.insertString(3, "def");
        i2.undo();
        check("abc".equals(g2.getString(0, 3)), "second insert undone");
        i1.undo();
        check(g2.length() == 1, "first insert undone");
        i1.redo();
        i2.redo();
        check("abcdef".equals(g2.getString(0, 6)), "both redone in order");
    }

    static void testSerialization() throws Exception {
        GapContent g = new GapContent();
        g.insertString(0, "serialize me");
        Position p = g.createPosition(4);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(g);
        oos.close();
        GapContent r = (GapContent) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(r != g, "round-trip distinct");
        check(r.length() == g.length(), "round-trip length");
        check("serialize me".equals(r.getString(0, 12)), "round-trip content");
        check(r.createPosition(0).getOffset() == 0, "round-trip usable");
        r.insertString(0, "X");
        check("Xserialize me".equals(r.getString(0, 13)), "round-trip mutable");
    }

    public static void main(String[] args) throws Exception {
        testInitial();
        testInsert();
        testRemove();
        testGetStringGetChars();
        testPositions();
        testUndoRedo();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

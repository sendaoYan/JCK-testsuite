/*
 * @test
 * @summary javax.swing.text.Position conformance: interface + createPosition / tracking / start-end (headless-safe).
 * @run main PositionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;

public class PositionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    interface Throwing { void run() throws Throwable; }
    static void expectEx(Class<? extends Throwable> ex, Throwing r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t, t);
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception thrown");
    }

    static void testTypeShape() {
        Class<?> c = Position.class;
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOffset"), "method getOffset");
    }

    static void testCreatePosition() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "hello", null);
        Position p0 = d.createPosition(0);
        Position p2 = d.createPosition(2);
        Position p5 = d.createPosition(d.getLength());
        check(p0.getOffset() == 0, "position 0");
        check(p2.getOffset() == 2, "position 2");
        check(p5.getOffset() == 5, "position at end of user content");
        // The start position sits at offset 0; the end position sits at the very
        // end of the backing content, i.e. after the implicit trailing newline
        // (content length = getLength() + 1).
        check(d.getStartPosition().getOffset() == 0, "start position 0");
        check(d.getEndPosition().getOffset() == 6, "end position after implicit newline");
        // Positions may also be created on an empty document.
        PlainDocument e = new PlainDocument();
        check(e.createPosition(0).getOffset() == 0, "position 0 on empty doc");
        check(e.getEndPosition().getOffset() == 1, "end position of empty doc is content end");
    }

    static void testPositionTracksChanges() throws Exception {
        PlainDocument d = new PlainDocument();
        d.insertString(0, "hello", null);
        Position p0 = d.createPosition(0);
        Position p2 = d.createPosition(2);
        Position p5 = d.createPosition(5);

        d.insertString(1, "XX", null); // hXXello
        check(p0.getOffset() == 0, "start position unaffected by insert after");
        check(p2.getOffset() == 4, "position shifted by insert before");
        check(p5.getOffset() == 7, "end position shifted by insert");

        d.remove(1, 2); // back to hello
        check(p2.getOffset() == 2, "position shifted back after remove");
        check(p5.getOffset() == 5, "end position shifted back");

        d.remove(0, 2); // llo
        check(p0.getOffset() == 0, "start position pinned at 0");
        check(p2.getOffset() == 0, "position in removed range collapses to start");
    }

    static void testStartEndTrack() throws Exception {
        PlainDocument d = new PlainDocument();
        Position start = d.getStartPosition();
        Position end = d.getEndPosition();
        check(start.getOffset() == 0, "empty doc start 0");
        check(end.getOffset() == 1, "empty doc end at implicit newline");
        d.insertString(0, "abc", null);
        check(start.getOffset() == 0, "start stays 0");
        check(end.getOffset() == 4, "end tracks to content end");
        d.remove(0, 1);
        check(end.getOffset() == 3, "end tracks back after remove");
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        testCreatePosition();
        testPositionTracksChanges();
        testStartEndTrack();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

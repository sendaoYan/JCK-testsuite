/*
 * @test
 * @summary javax.swing.text.AbstractDocument.Content conformance: white-box structure + black-box behavior.
 * @run main ContentTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.StringContent;
import javax.swing.undo.UndoableEdit;

public class ContentTest {
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
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$Content");
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
        // Exercise the interface contract through GapContent (primary impl).
        AbstractDocument.Content c = new GapContent();

        check(c.length() == 1, "fresh content length 1 (implied newline)");
        check("\n".equals(c.getString(0, 1)), "fresh content is a newline");

        UndoableEdit ue = c.insertString(1, "abc");
        check(ue != null, "insertString returns an undoable edit");
        check(c.length() == 4, "length after insert");
        check("\nabc".equals(c.getString(0, 4)), "content after insert");

        Segment seg = new Segment();
        c.getChars(0, 4, seg);
        check("\nabc".equals(new String(seg.array, seg.offset, seg.count)), "getChars fills segment");

        Position p = c.createPosition(2);
        c.insertString(0, "XY");
        check(p.getOffset() == 4, "position tracks insert before it");

        c.remove(0, 2);
        check("\nabc".equals(c.getString(0, c.length())), "remove restores original content");

        expectThrows(BadLocationException.class, () -> c.insertString(-1, "x"), "insert negative");
        expectThrows(BadLocationException.class, () -> c.getString(0, c.length() + 1), "getString past end");
        expectThrows(BadLocationException.class, () -> c.remove(0, c.length()), "remove whole content");

        // Same contract holds for StringContent.
        AbstractDocument.Content sc = new StringContent();
        check(sc.length() == 1, "StringContent fresh length 1");
        sc.insertString(0, "hello");
        check("hello\n".equals(sc.getString(0, sc.length())), "StringContent insert/getString");
        Position sp = sc.createPosition(2);
        sc.insertString(0, "XY");
        check(sp.getOffset() == 4, "StringContent position tracks insert");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

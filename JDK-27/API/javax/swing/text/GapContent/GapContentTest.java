/*
 * @test
 * @summary javax.swing.text.GapContent conformance: white-box structure + black-box behavior.
 * @run main GapContentTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.BadLocationException;
import javax.swing.text.GapContent;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

public class GapContentTest {
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
        Class<?> c = Class.forName("javax.swing.text.GapContent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.GapVector"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.AbstractDocument$Content"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        GapContent c = new GapContent();
        check(c.length() == 1, "initial length 1");
        check("\n".equals(c.getString(0, 1)), "initial implied newline");

        // insert / getString / getChars
        UndoableEdit ins = c.insertString(0, "abc");
        check(ins != null, "insertString returns edit");
        check(c.length() == 4, "length after insert");
        check("abc\n".equals(c.getString(0, c.length())), "content after insert");

        Segment seg = new Segment();
        c.getChars(1, 2, seg);
        check("bc".equals(new String(seg.array, seg.offset, seg.count)), "getChars subrange");

        // position tracking
        Position p = c.createPosition(1);
        c.insertString(0, "XY");
        check(p.getOffset() == 3, "position tracks insert before it");
        c.remove(0, 2);
        check("abc\n".equals(c.getString(0, c.length())), "content after remove");

        // insert undo / redo
        ins.undo();
        check(c.length() == 1 && "\n".equals(c.getString(0, 1)), "insert undo restores newline");
        ins.redo();
        check("abc\n".equals(c.getString(0, c.length())), "insert redo reapplies");

        // remove undo / redo
        UndoableEdit rm = c.remove(0, 3);
        check(c.length() == 1, "remove shortens");
        rm.undo();
        check("abc\n".equals(c.getString(0, c.length())), "remove undo restores");
        rm.redo();
        check("\n".equals(c.getString(0, c.length())), "remove redo reapplies");

        // sized constructor
        GapContent sized = new GapContent(3);
        check(sized.length() == 1, "sized constructor length 1");
        sized.insertString(0, "hello");
        check("hello\n".equals(sized.getString(0, sized.length())), "sized constructor usable");

        // bounds
        expectThrows(BadLocationException.class, () -> c.insertString(-1, "x"), "insert negative");
        expectThrows(BadLocationException.class, () -> c.remove(0, c.length()), "remove whole content");
        expectThrows(BadLocationException.class, () -> c.getString(0, c.length() + 1), "getString past end");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

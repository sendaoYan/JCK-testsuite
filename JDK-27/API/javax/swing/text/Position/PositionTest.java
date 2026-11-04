/*
 * @test
 * @summary javax.swing.text.Position conformance: white-box structure + black-box behavior.
 * @run main PositionTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.PlainDocument;
import javax.swing.text.Position;

public class PositionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Position");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getDeclaredMethods().length == 1, "single getOffset method");
    }

    static void testBlackBox() throws Exception {
        PlainDocument doc = new PlainDocument();
        doc.insertString(0, "abcdef", null);

        // A Position tracks edits made before its location.
        Position p = doc.createPosition(3);
        check(p.getOffset() == 3, "initial offset 3");

        doc.insertString(0, "xx", null);
        check(p.getOffset() == 5, "offset shifts after insert before position");

        doc.insertString(5, "YY", null); // insert exactly at position (forward bias)
        check(p.getOffset() == 7, "forward bias moves position past inserted text");

        doc.insertString(0, "z", null);
        check(p.getOffset() == 8, "further shift after more leading inserts");

        // Position 0 is a special case that never moves.
        Position zero = doc.createPosition(0);
        check(zero.getOffset() == 0, "zero position starts at 0");
        doc.insertString(0, "abc", null);
        check(zero.getOffset() == 0, "zero position stays at 0");

        // A position at the end tracks the document length.
        Position end = doc.createPosition(doc.getLength());
        check(end.getOffset() == doc.getLength(), "end position equals length");
        doc.insertString(doc.getLength(), "tail", null);
        check(end.getOffset() == doc.getLength(), "end position tracks growth");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

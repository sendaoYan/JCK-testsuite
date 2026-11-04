/*
 * @test
 * @summary javax.swing.text.Utilities conformance: white-box structure + black-box behavior.
 * @run main UtilitiesTest
 */

import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Utilities;

public class UtilitiesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Utilities");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Two-line document: "hello world\n" [0,12), "foo bar baz\n" [12,24).
        JTextArea ta = new JTextArea("hello world\nfoo bar baz\n");
        ta.setLocale(Locale.US);
        ta.setSize(800, 200); // non-zero size so modelToView is available

        // Row boundaries are delimited by newlines, independent of font metrics.
        check(Utilities.getRowStart(ta, 0) == 0, "getRowStart(0)");
        check(Utilities.getRowStart(ta, 6) == 0, "getRowStart(6) same row");
        check(Utilities.getRowStart(ta, 13) == 12, "getRowStart(13) second row");
        check(Utilities.getRowEnd(ta, 0) == 11, "getRowEnd(0) first row end");
        check(Utilities.getRowEnd(ta, 13) == 23, "getRowEnd(13) second row end");

        // Word boundaries (BreakIterator on ASCII, Locale.US).
        check(Utilities.getWordStart(ta, 6) == 6, "getWordStart(6)");
        check(Utilities.getWordEnd(ta, 6) == 11, "getWordEnd(6)");
        check(Utilities.getWordStart(ta, 0) == 0, "getWordStart(0)");
        check(Utilities.getWordEnd(ta, 0) == 5, "getWordEnd(0)");
        check(Utilities.getNextWord(ta, 0) == 6, "getNextWord(0)");
        check(Utilities.getPreviousWord(ta, 11) == 6, "getPreviousWord(11)");

        // Vertical navigation across rows (x=0 maps to row start).
        check(Utilities.getPositionAbove(ta, 13, 0) == 0, "getPositionAbove(13)");
        check(Utilities.getPositionBelow(ta, 0, 0) == 12, "getPositionBelow(0)");

        // Paragraph element lookup.
        Element p0 = Utilities.getParagraphElement(ta, 0);
        check(p0 != null && p0.getStartOffset() == 0, "paragraph(0) start");
        Element p1 = Utilities.getParagraphElement(ta, 13);
        check(p1 != null && p1.getStartOffset() == 12, "paragraph(13) start");

        // Out-of-range offset throws BadLocationException with the offset.
        try {
            Utilities.getWordStart(ta, ta.getDocument().getLength() + 5);
            check(false, "getWordStart out of range throws");
        } catch (BadLocationException e) {
            check(e.offsetRequested() == ta.getDocument().getLength() + 5, "BadLocationException offset");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

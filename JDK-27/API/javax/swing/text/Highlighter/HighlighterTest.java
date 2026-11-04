/*
 * @test
 * @summary javax.swing.text.Highlighter conformance: white-box structure + black-box behavior.
 * @run main HighlighterTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class HighlighterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Highlighter");
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
        JTextArea area = new JTextArea("0123456789");
        Highlighter h = new DefaultHighlighter();
        check(h.getHighlights().length == 0, "empty initially");

        h.install(area);
        Highlighter.HighlightPainter p = DefaultHighlighter.DefaultPainter;
        Object tag = h.addHighlight(2, 5, p);
        Highlighter.Highlight[] hs = h.getHighlights();
        check(hs.length == 1, "one highlight after add");
        check(hs[0].getStartOffset() == 2 && hs[0].getEndOffset() == 5, "offsets");
        check(hs[0].getPainter() == p, "painter identity");

        h.changeHighlight(tag, 3, 7);
        check(h.getHighlights()[0].getStartOffset() == 3 && h.getHighlights()[0].getEndOffset() == 7, "changed offsets");

        h.removeHighlight(tag);
        check(h.getHighlights().length == 0, "removed");

        h.addHighlight(0, 1, p);
        h.addHighlight(2, 3, p);
        check(h.getHighlights().length == 2, "two highlights");
        h.removeAllHighlights();
        check(h.getHighlights().length == 0, "removeAll empties");

        boolean neg = false;
        try { h.addHighlight(-1, 2, p); } catch (BadLocationException e) { neg = true; }
        check(neg, "negative start throws BadLocationException");
        boolean badRange = false;
        try { h.addHighlight(5, 4, p); } catch (BadLocationException e) { badRange = true; }
        check(badRange, "end before start throws BadLocationException");

        h.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

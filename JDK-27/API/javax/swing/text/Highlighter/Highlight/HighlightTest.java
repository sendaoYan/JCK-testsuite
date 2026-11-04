/*
 * @test
 * @summary javax.swing.text.Highlighter.Highlight conformance: white-box structure + black-box behavior.
 * @run main HighlightTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class HighlightTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Highlighter$Highlight");
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
        JTextArea area = new JTextArea("abcdefgh");
        DefaultHighlighter h = new DefaultHighlighter();
        h.install(area);

        Highlighter.HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
        Object tag = h.addHighlight(1, 4, p);
        Highlighter.Highlight hl = h.getHighlights()[0];
        check(hl.getStartOffset() == 1, "getStartOffset");
        check(hl.getEndOffset() == 4, "getEndOffset");
        check(hl.getPainter() == p, "getPainter identity");
        check(hl.getStartOffset() <= hl.getEndOffset(), "start <= end");

        h.changeHighlight(tag, 2, 6);
        Highlighter.Highlight hl2 = h.getHighlights()[0];
        check(hl2.getStartOffset() == 2 && hl2.getEndOffset() == 6, "changeHighlight reflected in Highlight");

        Object tag2 = h.addHighlight(5, 7, p);
        Highlighter.Highlight[] hs = h.getHighlights();
        check(hs.length == 2, "two highlights");
        check(hs[0].getStartOffset() != hs[1].getStartOffset(), "distinct highlights");
        check(hs[0].getPainter() == hs[1].getPainter(), "same painter for both");

        h.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

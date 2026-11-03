/*
 * @test
 * @summary javax.swing.text.DefaultHighlighter conformance: white-box structure + black-box behavior.
 * @run main DefaultHighlighterTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class DefaultHighlighterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultHighlighter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.LayeredHighlighter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JTextArea area = new JTextArea();
        area.setText("0123456789");
        DefaultHighlighter h = new DefaultHighlighter();

        check(h.getDrawsLayeredHighlights(), "drawsLayeredHighlights default true");
        check(h.getHighlights().length == 0, "no highlights initially");
        h.setDrawsLayeredHighlights(false);
        check(!h.getDrawsLayeredHighlights(), "drawsLayeredHighlights false");
        h.setDrawsLayeredHighlights(true);
        check(h.getDrawsLayeredHighlights(), "drawsLayeredHighlights true again");

        h.install(area);
        Highlighter.HighlightPainter p1 = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
        Highlighter.HighlightPainter p2 = DefaultHighlighter.DefaultPainter;
        Object tag1 = h.addHighlight(1, 4, p1);
        Object tag2 = h.addHighlight(6, 8, p2);

        Highlighter.Highlight[] hs = h.getHighlights();
        check(hs.length == 2, "two highlights added");
        check(hs[0].getStartOffset() == 1 && hs[0].getEndOffset() == 4, "first highlight offsets");
        check(hs[0].getPainter() == p1, "first highlight painter");
        check(hs[1].getStartOffset() == 6 && hs[1].getEndOffset() == 8, "second highlight offsets");
        check(hs[1].getPainter() == p2, "second highlight painter");

        h.changeHighlight(tag1, 2, 5);
        hs = h.getHighlights();
        check(hs[0].getStartOffset() == 2 && hs[0].getEndOffset() == 5, "changeHighlight updates offsets");

        h.removeHighlight(tag2);
        check(h.getHighlights().length == 1, "removeHighlight removes one");

        boolean negStart = false;
        try { h.addHighlight(-1, 3, p1); } catch (BadLocationException e) { negStart = true; }
        check(negStart, "negative start throws BadLocationException");
        boolean endBeforeStart = false;
        try { h.addHighlight(5, 2, p1); } catch (BadLocationException e) { endBeforeStart = true; }
        check(endBeforeStart, "end before start throws BadLocationException");

        h.addHighlight(0, 2, p1);
        check(h.getHighlights().length == 2, "two before removeAll");
        h.removeAllHighlights();
        check(h.getHighlights().length == 0, "removeAllHighlights empties");

        h.addHighlight(0, 3, p1);
        h.install(area);
        check(h.getHighlights().length == 0, "install clears existing highlights");

        check(DefaultHighlighter.DefaultPainter != null, "DefaultPainter static non-null");
        h.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

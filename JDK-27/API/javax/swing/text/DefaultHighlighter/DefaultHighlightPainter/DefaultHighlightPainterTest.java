/*
 * @test
 * @summary javax.swing.text.DefaultHighlighter.DefaultHighlightPainter conformance: white-box structure + black-box behavior.
 * @run main DefaultHighlightPainterTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.LayeredHighlighter;

public class DefaultHighlightPainterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.DefaultHighlighter$DefaultHighlightPainter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.LayeredHighlighter$LayerPainter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Color color = new Color(0x11, 0x22, 0x33);
        DefaultHighlighter.DefaultHighlightPainter p =
                new DefaultHighlighter.DefaultHighlightPainter(color);
        check(p.getColor() == color, "getColor returns constructor color");
        check(p instanceof Highlighter.HighlightPainter, "implements HighlightPainter");
        check(p instanceof LayeredHighlighter.LayerPainter, "extends LayerPainter");

        DefaultHighlighter.DefaultHighlightPainter nullPainter =
                new DefaultHighlighter.DefaultHighlightPainter(null);
        check(nullPainter.getColor() == null, "null color constructor");

        check(DefaultHighlighter.DefaultPainter != null, "DefaultPainter static non-null");
        check(DefaultHighlighter.DefaultPainter instanceof DefaultHighlighter.DefaultHighlightPainter,
                "DefaultPainter is DefaultHighlightPainter");
        check(((DefaultHighlighter.DefaultHighlightPainter) DefaultHighlighter.DefaultPainter).getColor() == null,
                "DefaultPainter has null color");

        // use the painter in a real highlighter and read it back
        JTextArea area = new JTextArea("abcdefgh");
        DefaultHighlighter h = new DefaultHighlighter();
        h.install(area);
        Object tag = h.addHighlight(2, 5, p);
        Highlighter.Highlight[] hs = h.getHighlights();
        check(hs.length == 1, "one highlight");
        check(hs[0].getPainter() == p, "painter stored by identity");
        check(hs[0].getStartOffset() == 2 && hs[0].getEndOffset() == 5, "offsets via painter");
        h.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

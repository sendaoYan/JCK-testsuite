/*
 * @test
 * @summary javax.swing.text.Highlighter.HighlightPainter conformance: white-box structure + black-box behavior.
 * @run main HighlightPainterTest
 */

import java.awt.Color;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class HighlightPainterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Highlighter$HighlightPainter");
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
        // custom painter records paint invocation with its arguments
        final int[] calls = {0};
        final int[] seenOffs = new int[2];
        Highlighter.HighlightPainter custom = (g, p0, p1, bounds, c) -> {
            calls[0]++;
            seenOffs[0] = p0;
            seenOffs[1] = p1;
        };
        custom.paint(null, 3, 8, null, null);
        check(calls[0] == 1, "paint invoked once");
        check(seenOffs[0] == 3 && seenOffs[1] == 8, "paint receives offsets");

        // DefaultHighlightPainter is a HighlightPainter
        DefaultHighlighter.DefaultHighlightPainter dp =
                new DefaultHighlighter.DefaultHighlightPainter(Color.BLUE);
        check(dp instanceof Highlighter.HighlightPainter, "DefaultHighlightPainter is a HighlightPainter");
        check(dp.getColor() == Color.BLUE, "painter color");

        // the painter is stored and read back by the highlighter
        JTextArea area = new JTextArea("abcdef");
        DefaultHighlighter h = new DefaultHighlighter();
        h.install(area);
        Object tag = h.addHighlight(1, 3, dp);
        check(h.getHighlights()[0].getPainter() == dp, "painter roundtrip through highlighter");
        h.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

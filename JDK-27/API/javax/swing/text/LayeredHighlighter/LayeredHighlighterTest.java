/*
 * @test
 * @summary javax.swing.text.LayeredHighlighter conformance: white-box structure + black-box behavior.
 * @run main LayeredHighlighterTest
 */

import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.LayeredHighlighter;

public class LayeredHighlighterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.LayeredHighlighter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Highlighter"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        LayeredHighlighter h = new DefaultHighlighter();
        check(h instanceof Highlighter, "LayeredHighlighter is a Highlighter");

        JTextArea area = new JTextArea("abcdef");
        h.install(area);
        // paintLayeredHighlights with no highlights is a no-op (null view/graphics safe)
        h.paintLayeredHighlights(null, 0, 0, null, area, null);
        check(true, "paintLayeredHighlights no-op with no highlights");

        h.addHighlight(1, 3, DefaultHighlighter.DefaultPainter);
        check(h.getHighlights().length == 1, "addHighlight via LayeredHighlighter");
        check(((DefaultHighlighter) h).getDrawsLayeredHighlights(), "layered drawing on by default");

        h.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

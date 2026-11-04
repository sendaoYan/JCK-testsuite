/*
 * @test
 * @summary javax.swing.text.LayeredHighlighter.LayerPainter conformance: white-box structure + black-box behavior.
 * @run main LayerPainterTest
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.Modifier;
import javax.swing.JTextArea;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.View;

public class LayerPainterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.LayeredHighlighter$LayerPainter");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Highlighter$HighlightPainter"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Rectangle result = new Rectangle(1, 2, 3, 4);
        LayeredHighlighter.LayerPainter lp = new LayeredHighlighter.LayerPainter() {
            @Override
            public Shape paintLayer(Graphics g, int p0, int p1, Shape bounds, JTextComponent c, View view) {
                return result;
            }
            @Override
            public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
            }
        };
        check(lp instanceof Highlighter.HighlightPainter, "LayerPainter is a HighlightPainter");
        Shape s = lp.paintLayer(null, 0, 1, null, null, null);
        check(s == result, "paintLayer returns the produced shape");

        // DefaultHighlightPainter is a concrete LayerPainter
        DefaultHighlighter.DefaultHighlightPainter dp =
                new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
        check(dp instanceof LayeredHighlighter.LayerPainter, "DefaultHighlightPainter is a LayerPainter");

        // a LayerPainter highlight participates in layered drawing
        JTextArea area = new JTextArea("abcdef");
        DefaultHighlighter h = new DefaultHighlighter();
        h.install(area);
        h.addHighlight(1, 4, dp);
        check(h.getHighlights().length == 1, "LayerPainter used as highlight painter");
        check(h.getDrawsLayeredHighlights(), "layered highlight drawing active");
        h.deinstall(area);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

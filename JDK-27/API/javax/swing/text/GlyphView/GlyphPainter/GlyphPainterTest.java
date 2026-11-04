/*
 * @test
 * @summary javax.swing.text.GlyphView.GlyphPainter conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main GlyphPainterTest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.Position;
import javax.swing.text.TabExpander;

public class GlyphPainterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class DummyPainter extends GlyphView.GlyphPainter {
        public float getSpan(GlyphView v, int p0, int p1, TabExpander e, float x) { return 0f; }
        public float getHeight(GlyphView v) { return 0f; }
        public float getAscent(GlyphView v) { return 0f; }
        public float getDescent(GlyphView v) { return 0f; }
        public void paint(GlyphView v, Graphics g, Shape a, int p0, int p1) { }
        public Shape modelToView(GlyphView v, int pos, Position.Bias bias, Shape a) { return null; }
        public int viewToModel(GlyphView v, float x, float y, Shape a, Position.Bias[] biasReturn) { return 0; }
        public int getBoundedPosition(GlyphView v, int p0, float x, float len) { return p0; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.GlyphView$GlyphPainter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Element leaf = ta.getDocument().getDefaultRootElement().getElement(0);
            GlyphView gv = new GlyphView(leaf);
            DummyPainter p = new DummyPainter();

            // stateless painter returns itself for any range
            check(p.getPainter(gv, 0, 5) == p, "getPainter returns this by default");

            Shape alloc = new Rectangle(0, 0, 200, 30);
            Position.Bias[] bias = new Position.Bias[1];

            // EAST advances within the run (view spans [0, end))
            int end = gv.getEndOffset();
            check(p.getNextVisualPositionFrom(gv, 0, Position.Bias.Forward, alloc, SwingConstants.EAST, bias) == 1, "EAST from 0 -> 1");
            check(bias[0] == Position.Bias.Forward, "EAST forward bias");
            check(p.getNextVisualPositionFrom(gv, end - 1, Position.Bias.Forward, alloc, SwingConstants.EAST, bias) == -1, "EAST at last char -> -1");
            check(p.getNextVisualPositionFrom(gv, end - 2, Position.Bias.Forward, alloc, SwingConstants.EAST, bias) == end - 1, "EAST from second-to-last -> last");

            // WEST retreats within the run
            check(p.getNextVisualPositionFrom(gv, 5, Position.Bias.Forward, alloc, SwingConstants.WEST, bias) == 4, "WEST from 5 -> 4");
            check(p.getNextVisualPositionFrom(gv, 0, Position.Bias.Forward, alloc, SwingConstants.WEST, bias) == -1, "WEST at start -> -1");
            check(p.getNextVisualPositionFrom(gv, -1, Position.Bias.Forward, alloc, SwingConstants.WEST, bias) == end - 1, "WEST auto -> end-1");

            // NORTH/SOUTH on a single-line view are not contained
            check(p.getNextVisualPositionFrom(gv, 0, Position.Bias.Forward, alloc, SwingConstants.NORTH, bias) == -1, "NORTH -> -1");
            check(p.getNextVisualPositionFrom(gv, 0, Position.Bias.Forward, alloc, SwingConstants.SOUTH, bias) == -1, "SOUTH -> -1");

            boolean threw = false;
            try { p.getNextVisualPositionFrom(gv, 0, Position.Bias.Forward, alloc, 999, bias); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "invalid direction -> IllegalArgumentException");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(GlyphPainterTest::testWhiteBox);
        runOnEDT(GlyphPainterTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}

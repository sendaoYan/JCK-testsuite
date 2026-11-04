/*
 * @test
 * @summary javax.swing.text.GlyphView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main GlyphViewTest
 */

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.View;

public class GlyphViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class Host extends View {
        final Container c;
        Host(Container c) { super(null); this.c = c; }
        public float getPreferredSpan(int axis) { return 0; }
        public void paint(Graphics g, Shape a) { }
        public Shape modelToView(int pos, Shape a, Position.Bias b) { return null; }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] b) { return 0; }
        public Container getContainer() { return c; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.GlyphView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.TabableView"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.lang.Cloneable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Document doc = ta.getDocument();
            Element leaf = doc.getDefaultRootElement().getElement(0);

            GlyphView gv = new GlyphView(leaf);
            check(gv.getElement() == leaf, "getElement returns ctor element");
            check(gv.getStartOffset() == leaf.getStartOffset() && gv.getEndOffset() == leaf.getEndOffset(), "start/end offsets from element");
            check(gv.getDocument() == doc, "getDocument from element");

            // glyph painter lifecycle
            check(gv.getGlyphPainter() == null, "no painter until needed");
            GlyphView.GlyphPainter p = gv.getGlyphPainter();
            gv.setGlyphPainter(null);
            check(gv.getGlyphPainter() == null, "setGlyphPainter(null) clears");

            // attribute / font queries on a plain document
            check(gv.getBackground() == null, "no background on plain document");
            check(gv.getTabExpander() == null, "no tab expander initially");
            check(!gv.isUnderline() && !gv.isStrikeThrough() && !gv.isSubscript() && !gv.isSuperscript(), "no text decorations by default");

            gv.setParent(new Host(ta));
            check(gv.getFont() != null, "font from container");
            check(gv.getForeground() != null, "foreground from container");

            // text and spans
            Segment seg = gv.getText(0, 5);
            check("hello".equals(seg.toString()), "getText returns requested range");
            float xspan = gv.getPreferredSpan(View.X_AXIS);
            float yspan = gv.getPreferredSpan(View.Y_AXIS);
            check(xspan >= 0f, "X preferred span >= 0");
            check(yspan > 0f, "Y preferred span > 0");
            check(gv.getMinimumSpan(View.X_AXIS) <= xspan, "X minimum <= preferred");
            float align = gv.getAlignment(View.Y_AXIS);
            check(align >= 0f && align <= 1f, "Y alignment within [0,1]");
            check(gv.getTabbedSpan(0f, null) >= 0f, "tabbed span >= 0");
            check(gv.getPartialSpan(0, 5) >= 0f, "partial span >= 0");

            // fragmenting
            View frag = gv.createFragment(0, 5);
            check(frag instanceof GlyphView && frag != gv, "createFragment returns a distinct GlyphView");
            check(frag.getStartOffset() == 0 && frag.getEndOffset() == 5, "fragment offsets");
            check(gv.breakView(View.X_AXIS, 0, 0f, 100f) != null, "breakView non-null");
            check(gv.getBreakWeight(View.X_AXIS, 0f, 100f) >= View.BadBreakWeight, "break weight within valid range");

            // model/view translation
            Rectangle alloc = new Rectangle(0, 0, 200, 30);
            Shape s0 = gv.modelToView(0, alloc, Position.Bias.Forward);
            Shape s6 = gv.modelToView(6, alloc, Position.Bias.Forward);
            check(s0 != null && s6 != null, "modelToView non-null");
            check(s6.getBounds().x >= s0.getBounds().x, "x monotonic with position");
            Position.Bias[] bias = new Position.Bias[1];
            check(gv.viewToModel(0f, (float) alloc.y, alloc, bias) == 0, "viewToModel maps left edge to start");
            check(bias[0] == Position.Bias.Forward, "forward bias");

            // next visual position
            Position.Bias[] bias2 = new Position.Bias[1];
            int east = gv.getNextVisualPositionFrom(0, Position.Bias.Forward, alloc, SwingConstants.EAST, bias2);
            check(east == 1, "EAST advances one position");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(GlyphViewTest::testWhiteBox);
        runOnEDT(GlyphViewTest::testBlackBox);
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

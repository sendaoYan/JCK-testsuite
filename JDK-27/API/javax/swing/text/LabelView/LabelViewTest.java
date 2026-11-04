/*
 * @test
 * @summary javax.swing.text.LabelView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main LabelViewTest
 */

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

public class LabelViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.LabelView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.GlyphView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.TabableView"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextPane tp = new JTextPane();
        tp.setText("hello world");
        frame.add(tp);
        frame.pack();
        try {
            StyledDocument doc = tp.getStyledDocument();
            Element leaf = doc.getDefaultRootElement().getElement(0).getElement(0);

            LabelView lv = new LabelView(leaf);
            check(lv.getElement() == leaf, "getElement returns ctor element");
            check(lv.getStartOffset() == leaf.getStartOffset() && lv.getEndOffset() == leaf.getEndOffset(), "offsets from element");
            check(lv.getDocument() == doc, "getDocument from element");

            // styled attributes are resolved from the StyledDocument
            check(lv.getFont() != null, "font resolved from StyledDocument");
            check(lv.getForeground() != null, "foreground resolved from StyledDocument");
            check(lv.getBackground() == null, "background undefined by default");
            check(!lv.isUnderline() && !lv.isStrikeThrough() && !lv.isSubscript() && !lv.isSuperscript(), "no decorations by default");

            // glyph painter lifecycle (inherited from GlyphView)
            check(lv.getGlyphPainter() == null, "no painter before first measurement");
            LabelView.GlyphPainter p = lv.getGlyphPainter();
            lv.setGlyphPainter(null);
            check(lv.getGlyphPainter() == null, "setGlyphPainter(null) clears");

            lv.setParent(new Host(tp));
            float xspan = lv.getPreferredSpan(View.X_AXIS);
            float yspan = lv.getPreferredSpan(View.Y_AXIS);
            check(xspan >= 0f, "X preferred span >= 0");
            check(yspan > 0f, "Y preferred span > 0");
            check(lv.getTabbedSpan(0f, null) >= 0f, "TabableView.getTabbedSpan >= 0");
            check(lv.getPartialSpan(0, 5) >= 0f, "TabableView.getPartialSpan >= 0");

            // model/view translation
            Rectangle alloc = new Rectangle(0, 0, 200, 30);
            Shape s0 = lv.modelToView(0, alloc, Position.Bias.Forward);
            Shape s6 = lv.modelToView(6, alloc, Position.Bias.Forward);
            check(s0 != null && s6 != null, "modelToView non-null");
            check(s6.getBounds().x >= s0.getBounds().x, "x monotonic with position");

            // style changes are reflected after the element is re-read
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setUnderline(sas, true);
            StyleConstants.setStrikeThrough(sas, true);
            doc.setCharacterAttributes(0, doc.getLength(), sas, false);
            Element leaf2 = doc.getDefaultRootElement().getElement(0).getElement(0);
            LabelView lv2 = new LabelView(leaf2);
            check(lv2.isUnderline(), "underline style reflected");
            check(lv2.isStrikeThrough(), "strike-through style reflected");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(LabelViewTest::testWhiteBox);
        runOnEDT(LabelViewTest::testBlackBox);
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

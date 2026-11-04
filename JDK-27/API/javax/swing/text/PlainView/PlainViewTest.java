/*
 * @test
 * @summary javax.swing.text.PlainView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main PlainViewTest
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
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Position;
import javax.swing.text.View;

public class PlainViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.PlainView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.TabExpander"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Document doc = ta.getDocument();
            Element root = doc.getDefaultRootElement();

            PlainView pv = new PlainView(root);
            check(pv.getElement() == root, "getElement returns ctor element");
            check(pv.getStartOffset() == root.getStartOffset() && pv.getEndOffset() == root.getEndOffset(), "start/end offsets from element");
            check(pv.getDocument() == doc, "getDocument from element");
            check(pv.getViewCount() == 0, "PlainView has no child views");
            check(pv.getView(0) == null, "PlainView getView null");

            pv.setParent(new Host(ta));
            // spans: non-negative, and height matches a single font line
            float xspan = pv.getPreferredSpan(View.X_AXIS);
            float yspan = pv.getPreferredSpan(View.Y_AXIS);
            check(xspan >= 0f, "X preferred span >= 0");
            check(yspan > 0f, "Y preferred span > 0 (one font line)");
            check(pv.getMinimumSpan(View.Y_AXIS) == yspan, "minimum Y == preferred (weight 0)");
            check(pv.getMaximumSpan(View.Y_AXIS) == yspan, "maximum Y == preferred (weight 0)");

            // nextTabStop: 8-character tab grid, monotonic and periodic
            float ts = pv.nextTabStop(0f, 0);
            check(ts > 0f, "first tab stop positive");
            check(pv.nextTabStop(ts, 0) == 2f * ts, "tab stop advances by one grid");
            check(pv.nextTabStop(2f * ts, 0) == 3f * ts, "tab stop advances by one grid (2)");
            check(pv.nextTabStop(1000f, 0) >= 1000f, "tab stop beyond reference");

            // modelToView / viewToModel on a single line
            Rectangle alloc = new Rectangle(0, 0, 200, 30);
            Shape s0 = pv.modelToView(0, alloc, Position.Bias.Forward);
            Shape s6 = pv.modelToView(6, alloc, Position.Bias.Forward);
            check(s0 != null && s6 != null, "modelToView non-null");
            check(s0.getBounds().x == alloc.x, "position 0 maps to left edge");
            check(s0.getBounds().width == 1, "single-column caret shape");
            check(s6.getBounds().x >= s0.getBounds().x, "x monotonic with position");
            Position.Bias[] bias = new Position.Bias[1];
            int m = pv.viewToModel(0f, (float) alloc.y, alloc, bias);
            check(m == 0, "left edge maps to start offset");
            check(bias[0] == Position.Bias.Forward, "forward bias reported");

            // actual component root view is a PlainView when not wrapping
            View real = ta.getUI().getRootView(ta).getView(0);
            check(real instanceof PlainView, "JTextArea root child is a PlainView");
            check(real.getPreferredSpan(View.Y_AXIS) > 0f, "real view has positive height");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(PlainViewTest::testWhiteBox);
        runOnEDT(PlainViewTest::testBlackBox);
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

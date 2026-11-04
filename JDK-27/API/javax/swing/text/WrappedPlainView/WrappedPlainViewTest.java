/*
 * @test
 * @summary javax.swing.text.WrappedPlainView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main WrappedPlainViewTest
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
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.WrappedPlainView;

public class WrappedPlainViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.WrappedPlainView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.TabExpander"), "interface 0");
        check(c.getConstructor(Element.class) != null, "one-arg ctor present");
        check(c.getConstructor(Element.class, boolean.class) != null, "two-arg ctor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("aaa bbb ccc ddd eee");
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        frame.add(ta);
        frame.pack();
        try {
            Document doc = ta.getDocument();
            Element root = doc.getDefaultRootElement();

            // both constructors
            WrappedPlainView wpv = new WrappedPlainView(root);
            WrappedPlainView wpvWord = new WrappedPlainView(root, true);
            check(wpv.getElement() == root, "one-arg ctor element");
            check(wpvWord.getElement() == root, "two-arg ctor element");
            check(wpv.getViewCount() == 0, "no children before parent set");

            wpv.setParent(new Host(ta));
            // spans non-negative with span ordering
            float xp = wpv.getPreferredSpan(View.X_AXIS);
            float xmin = wpv.getMinimumSpan(View.X_AXIS);
            float xmax = wpv.getMaximumSpan(View.X_AXIS);
            float yp = wpv.getPreferredSpan(View.Y_AXIS);
            check(xp >= 0f && yp >= 0f, "spans non-negative");
            check(xmin <= xp && xp <= xmax, "X min <= preferred <= max");
            check(wpv.getMinimumSpan(View.Y_AXIS) <= yp && yp <= wpv.getMaximumSpan(View.Y_AXIS), "Y min <= preferred <= max");

            // nextTabStop after metrics are primed
            float ts = wpv.nextTabStop(0f, 0);
            check(ts > 0f, "first tab stop positive");
            check(wpv.nextTabStop(ts, 0) == 2f * ts, "tab stop advances one grid");
            check(wpv.nextTabStop(5000f, 0) >= 5000f, "tab stop beyond reference");

            // real component view is a WrappedPlainView with line children
            View real = ta.getUI().getRootView(ta).getView(0);
            check(real instanceof WrappedPlainView, "JTextArea(lineWrap) root child is WrappedPlainView");
            check(real.getViewCount() > 0, "wrapped view loads line children");
            float rp = real.getPreferredSpan(View.Y_AXIS);
            check(rp > 0f, "real wrapped view has positive height");
            check(real.getMinimumSpan(View.Y_AXIS) <= rp && rp <= real.getMaximumSpan(View.Y_AXIS), "real Y span ordering");
            check(real.getView(0) != null && real.getView(0).getParent() == real, "line child parented to wrapped view");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(WrappedPlainViewTest::testWhiteBox);
        runOnEDT(WrappedPlainViewTest::testBlackBox);
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

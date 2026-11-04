/*
 * @test
 * @summary javax.swing.text.BoxView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main BoxViewTest
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
import javax.swing.text.BoxView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class BoxViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsIAE(Runnable r) {
        try { r.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
    }

    // fixed-span child so layout requirements are deterministic
    static final class FixedView extends View {
        FixedView(Element e) { super(e); }
        public float getPreferredSpan(int axis) { return 20f; }
        public float getMinimumSpan(int axis) { return 10f; }
        public float getMaximumSpan(int axis) { return 30f; }
        public void paint(Graphics g, Shape a) { }
        public Shape modelToView(int pos, Shape a, Position.Bias b) { return new Rectangle(0, 0, 1, 1); }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] b) { b[0] = Position.Bias.Forward; return 0; }
    }
    static final ViewFactory FIXED = FixedView::new;

    static final class Host extends View {
        final Container c; final ViewFactory f;
        Host(Container c, ViewFactory f) { super(null); this.c = c; this.f = f; }
        public float getPreferredSpan(int axis) { return 0; }
        public void paint(Graphics g, Shape a) { }
        public Shape modelToView(int pos, Shape a, Position.Bias b) { return null; }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] b) { return 0; }
        public Container getContainer() { return c; }
        public ViewFactory getViewFactory() { return f; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.BoxView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.CompositeView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextPane tp = new JTextPane();
        tp.setText("alpha\nbeta\ngamma");
        frame.add(tp);
        frame.pack();
        try {
            Document doc = tp.getDocument();
            Element root = doc.getDefaultRootElement();

            // axis handling on an empty box
            BoxView x = new BoxView(root, View.X_AXIS);
            check(x.getAxis() == View.X_AXIS, "ctor axis X_AXIS");
            BoxView y = new BoxView(root, View.Y_AXIS);
            check(y.getAxis() == View.Y_AXIS, "ctor axis Y_AXIS");
            x.setAxis(View.Y_AXIS);
            check(x.getAxis() == View.Y_AXIS, "setAxis round trip");
            x.setAxis(View.X_AXIS);
            check(x.getAxis() == View.X_AXIS, "setAxis back");
            x.layoutChanged(View.X_AXIS); // public invalidation, must not throw

            // spans of an empty box are zero
            check(x.getPreferredSpan(View.X_AXIS) == 0f, "empty preferred span 0");
            check(x.getMinimumSpan(View.X_AXIS) == 0f, "empty minimum span 0");
            check(x.getMaximumSpan(View.X_AXIS) == 0f, "empty maximum span 0");
            check(x.getAlignment(View.X_AXIS) == 0.5f, "empty alignment 0.5");
            check(x.getViewCount() == 0, "empty child count");
            check(throwsIAE(() -> x.getPreferredSpan(2)), "getPreferredSpan invalid axis -> IAE");
            check(throwsIAE(() -> x.getMinimumSpan(2)), "getMinimumSpan invalid axis -> IAE");
            check(throwsIAE(() -> x.getMaximumSpan(2)), "getMaximumSpan invalid axis -> IAE");

            // setSize / width / height round trip
            x.setSize(100f, 50f);
            check(x.getWidth() == 100 && x.getHeight() == 50, "setSize -> getWidth/getHeight");

            // children loaded through a factory; deterministic fixed spans
            BoxView bx = new BoxView(root, View.X_AXIS);
            bx.setParent(new Host(tp, FIXED));
            check(bx.getViewCount() == root.getElementCount(), "loadChildren creates one child per element");
            check(root.getElementCount() == 3, "three child elements");
            check(bx.getView(0) instanceof FixedView, "factory used for children");
            check(bx.getView(0).getParent() == bx, "child parent back-reference");

            // tiled (major axis X) sums, minor axis is max
            check(bx.getPreferredSpan(View.X_AXIS) == 60f, "major preferred sums children");
            check(bx.getMinimumSpan(View.X_AXIS) == 30f, "major minimum sums children");
            check(bx.getMaximumSpan(View.X_AXIS) == 90f, "major maximum sums children");
            check(bx.getPreferredSpan(View.Y_AXIS) == 20f, "minor preferred is max child");
            check(bx.getMinimumSpan(View.Y_AXIS) == 10f, "minor minimum is max child");
            check(bx.getMaximumSpan(View.Y_AXIS) == Integer.MAX_VALUE, "minor maximum unbounded");

            // layout then child allocation
            bx.setSize(90f, 30f);
            Shape ca = bx.getChildAllocation(0, new Rectangle(0, 0, 90, 30));
            check(ca != null, "child allocation non-null after layout");
            check(ca.getBounds().width > 0, "child allocation has positive width");

            // replace / append / remove on a box
            View extra = new FixedView(root.getElement(0));
            bx.append(extra);
            check(bx.getViewCount() == 4, "append increments count");
            check(bx.getView(3) == extra && extra.getParent() == bx, "appended parent set");
            bx.remove(3);
            check(bx.getViewCount() == 3, "remove decrements count");
            bx.replace(0, 1, null);
            check(bx.getViewCount() == 2, "replace(length>0, null) removes");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(BoxViewTest::testWhiteBox);
        runOnEDT(BoxViewTest::testBlackBox);
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

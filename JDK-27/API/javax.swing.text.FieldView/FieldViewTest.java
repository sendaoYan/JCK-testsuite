/*
 * @test
 * @summary javax.swing.text.FieldView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FieldViewTest
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
import javax.swing.text.FieldView;
import javax.swing.text.Position;
import javax.swing.text.View;

public class FieldViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.FieldView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.PlainView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
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

            FieldView fv = new FieldView(root);
            check(fv.getElement() == root, "getElement returns ctor element");
            check(fv.getStartOffset() == root.getStartOffset() && fv.getEndOffset() == root.getEndOffset(), "offsets from element");
            fv.setParent(new Host(ta));

            // FieldView is horizontally resizable but not vertically
            check(fv.getResizeWeight(View.X_AXIS) == 1, "resize weight X_AXIS == 1");
            check(fv.getResizeWeight(View.Y_AXIS) == 0, "resize weight Y_AXIS == 0");

            float xspan = fv.getPreferredSpan(View.X_AXIS);
            float yspan = fv.getPreferredSpan(View.Y_AXIS);
            check(xspan >= 0f, "X preferred span >= 0");
            check(yspan > 0f, "Y preferred span > 0");
            check(fv.getMinimumSpan(View.X_AXIS) == 0f, "X minimum == 0 (resizable)");
            check(fv.getMaximumSpan(View.X_AXIS) == Integer.MAX_VALUE, "X maximum == MAX_VALUE (resizable)");

            // modelToView/viewToModel through adjusted allocation
            Rectangle alloc = new Rectangle(0, 0, 200, 40);
            Shape s0 = fv.modelToView(0, alloc, Position.Bias.Forward);
            Shape s6 = fv.modelToView(6, alloc, Position.Bias.Forward);
            check(s0 != null && s6 != null, "modelToView non-null");
            check(s0.getBounds().x == alloc.x, "position 0 maps to left edge");
            check(s0.getBounds().width == 1, "caret is one column wide");
            check(s6.getBounds().x >= s0.getBounds().x, "x monotonic with position");
            Position.Bias[] bias = new Position.Bias[1];
            int m = fv.viewToModel(0f, (float) s0.getBounds().y, alloc, bias);
            check(m == 0, "left edge maps to start offset");
            check(bias[0] == Position.Bias.Forward, "forward bias");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(FieldViewTest::testWhiteBox);
        runOnEDT(FieldViewTest::testBlackBox);
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

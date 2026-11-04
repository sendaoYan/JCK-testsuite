/*
 * @test
 * @summary javax.swing.text.ComponentView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ComponentViewTest
 */

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

public class ComponentViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsBLE(ThrowingRunnable r) {
        try { r.run(); return false; }
        catch (BadLocationException e) { return true; }
        catch (Exception e) { throw new RuntimeException(e); }
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
        Class<?> c = Class.forName("javax.swing.text.ComponentView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextPane tp = new JTextPane();
        frame.add(tp);
        frame.pack();
        try {
            StyledDocument doc = tp.getStyledDocument();
            JLabel label = new JLabel("embedded");
            Style compStyle = tp.addStyle("comp", null);
            StyleConstants.setComponent(compStyle, label);
            doc.insertString(0, "C", compStyle);

            Element para = doc.getDefaultRootElement().getElement(0);
            Element compElem = para.getElement(0);
            check("component".equals(compElem.getName()), "component element name");

            ComponentView cv = new ComponentView(compElem);
            check(cv.getElement() == compElem, "getElement returns ctor element");
            check(cv.getStartOffset() == 0 && cv.getEndOffset() == 1, "component occupies one character");
            check(cv.getComponent() == null, "component not created before parenting");

            cv.setParent(new Host(tp));
            check(cv.getComponent() == label, "getComponent returns embedded component");

            // spans follow the embedded component size
            float xp = cv.getPreferredSpan(View.X_AXIS);
            float yp = cv.getPreferredSpan(View.Y_AXIS);
            check(xp >= 0f && yp >= 0f, "spans non-negative");
            check(cv.getMinimumSpan(View.X_AXIS) <= xp && xp <= cv.getMaximumSpan(View.X_AXIS), "X span ordering");
            float ax = cv.getAlignment(View.X_AXIS);
            float ay = cv.getAlignment(View.Y_AXIS);
            check(ax >= 0f && ax <= 1f && ay >= 0f && ay <= 1f, "alignment within [0,1]");

            // modelToView / viewToModel
            Rectangle alloc = new Rectangle(10, 20, 80, 40);
            Shape s0 = cv.modelToView(0, alloc, Position.Bias.Forward);
            Shape s1 = cv.modelToView(1, alloc, Position.Bias.Forward);
            check(s0.getBounds().x == alloc.x && s0.getBounds().width == 0, "start -> left edge");
            check(s1.getBounds().x == alloc.x + alloc.width && s1.getBounds().width == 0, "end -> right edge");
            check(throwsBLE(() -> cv.modelToView(2, alloc, Position.Bias.Forward)), "modelToView out of range -> BadLocationException");
            Position.Bias[] bias = new Position.Bias[1];
            check(cv.viewToModel(alloc.x - 1f, 0f, alloc, bias) == 0, "left half -> start");
            check(cv.viewToModel(alloc.x + alloc.width + 1f, 0f, alloc, bias) == 1, "right half -> end");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ComponentViewTest::testWhiteBox);
        runOnEDT(ComponentViewTest::testBlackBox);
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

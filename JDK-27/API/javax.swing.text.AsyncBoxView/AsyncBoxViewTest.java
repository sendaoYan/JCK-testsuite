/*
 * @test
 * @summary javax.swing.text.AsyncBoxView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main AsyncBoxViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.AsyncBoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

public class AsyncBoxViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AsyncBoxView");
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
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Element root = ta.getDocument().getDefaultRootElement();
            AsyncBoxView v = new AsyncBoxView(root, View.X_AXIS);
            check(v.getMajorAxis() == View.X_AXIS, "major axis stored from constructor");
            check(v.getMinorAxis() == View.Y_AXIS, "minor axis is orthogonal");
            check(v.getStartOffset() == 0 && v.getEndOffset() == root.getEndOffset(), "offsets from element");

            // empty view: no children, no layout ever queued
            check(v.getViewCount() == 0, "no children");
            check(v.getView(0) == null, "getView on empty returns null");

            // insets default to zero and round-trip
            check(v.getTopInset() == 0f && v.getBottomInset() == 0f && v.getLeftInset() == 0f && v.getRightInset() == 0f, "default insets zero");
            v.setTopInset(1f); v.setBottomInset(2f); v.setLeftInset(3f); v.setRightInset(4f);
            check(v.getTopInset() == 1f && v.getBottomInset() == 2f && v.getLeftInset() == 3f && v.getRightInset() == 4f, "inset round-trip");

            // spans follow documented default (major = majorSpan + inset; minor unknown -> margin + 30)
            check(v.getPreferredSpan(View.X_AXIS) == 7f, "major preferred == majorSpan(0) + left+right(7)");
            check(v.getPreferredSpan(View.Y_AXIS) == 33f, "minor preferred == margin(3) + 30");
            check(v.getMinimumSpan(View.X_AXIS) == 7f, "major minimum == major preferred");
            check(v.getMinimumSpan(View.Y_AXIS) == 8f, "minor minimum == insets(3) + 5");
            check(v.getMaximumSpan(View.X_AXIS) == 7f, "major maximum == major preferred");
            check(v.getMaximumSpan(View.Y_AXIS) == Integer.MAX_VALUE, "minor maximum unbounded");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(AsyncBoxViewTest::testWhiteBox);
        runOnEDT(AsyncBoxViewTest::testBlackBox);
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

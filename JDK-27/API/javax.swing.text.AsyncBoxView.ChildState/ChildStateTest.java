/*
 * @test
 * @summary javax.swing.text.AsyncBoxView.ChildState conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ChildStateTest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.AsyncBoxView;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;

public class ChildStateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // minimal child view; zero alignment keeps minor-offset math deterministic
    static final class PlainChild extends View {
        PlainChild() { super(null); }
        public float getPreferredSpan(int axis) { return 0f; }
        public void paint(Graphics g, Shape a) { }
        public Shape modelToView(int pos, Shape a, Position.Bias b) { return null; }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] b) { return 0; }
        public float getAlignment(int axis) { return 0f; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AsyncBoxView$ChildState");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.lang.Runnable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Element root = ta.getDocument().getDefaultRootElement();
            AsyncBoxView box = new AsyncBoxView(root, View.X_AXIS);
            PlainChild child = new PlainChild();
            AsyncBoxView.ChildState cs = box.new ChildState(child);

            check(cs.getChildView() == child, "getChildView returns the child");
            check(child.getParent() == box, "constructor parents the child to the box");

            // fresh state: nothing laid out, zero spans and offsets
            check(cs.isLayoutValid() == false, "layout invalid before update");
            check(cs.getMajorSpan() == 0f, "major span initially zero");
            check(cs.getMajorOffset() == 0f, "major offset initially zero");
            check(cs.getMinorSpan() == 0f, "minor span reports child maximum when under target");
            check(cs.getMinorOffset() == 0f, "minor offset zero with zero-alignment child");

            cs.setMajorOffset(42f);
            check(cs.getMajorOffset() == 42f, "setMajorOffset round-trip");

            // marking preferences changed must not throw and keeps layout invalid
            cs.preferenceChanged(true, true);
            check(cs.isLayoutValid() == false, "preferenceChanged invalidates (still invalid)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ChildStateTest::testWhiteBox);
        runOnEDT(ChildStateTest::testBlackBox);
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

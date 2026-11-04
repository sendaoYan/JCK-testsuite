/*
 * @test
 * @summary javax.swing.text.FlowView.FlowStrategy conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main FlowStrategyTest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.Element;
import javax.swing.text.FlowView;
import javax.swing.text.Position;
import javax.swing.text.View;

public class FlowStrategyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class Row extends View {
        Row(Element e) { super(e); }
        public float getPreferredSpan(int axis) { return 0f; }
        public void paint(Graphics g, Shape a) { }
        public Shape modelToView(int pos, Shape a, Position.Bias b) { return null; }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] b) { return 0; }
    }

    static final class TestFlowView extends FlowView {
        View pool;
        TestFlowView(Element e) { super(e, View.X_AXIS); }
        protected View createRow() { return new Row(getElement()); }
        void setPool(View v) { layoutPool = v; }
    }

    // expose protected getLogicalView for direct observation
    static final class ExposingStrategy extends FlowView.FlowStrategy {
        View logical(FlowView fv) { return getLogicalView(fv); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.FlowView$FlowStrategy");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
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
            Element leaf = ta.getDocument().getDefaultRootElement().getElement(0);
            TestFlowView fv = new TestFlowView(leaf);
            ExposingStrategy strategy = new ExposingStrategy();

            check(strategy != null, "FlowStrategy constructs");

            // getLogicalView hands back the flow's layout pool
            View pool = new Row(leaf);
            fv.setPool(pool);
            check(strategy.logical(fv) == pool, "getLogicalView returns the layout pool");

            // a synthetic insertUpdate with null event/allocation is tolerated
            strategy.insertUpdate(fv, null, null);
            check(true, "insertUpdate(null, null) does not throw");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(FlowStrategyTest::testWhiteBox);
        runOnEDT(FlowStrategyTest::testBlackBox);
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

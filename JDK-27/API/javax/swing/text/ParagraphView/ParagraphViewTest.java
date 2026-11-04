/*
 * @test
 * @summary javax.swing.text.ParagraphView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ParagraphViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

public class ParagraphViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ParagraphView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.FlowView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.TabExpander"), "interface 0");
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
            Element para = doc.getDefaultRootElement().getElement(0);

            ParagraphView pv = new ParagraphView(para);
            check(pv.getElement() == para, "getElement returns ctor element");
            check(pv.getStartOffset() == para.getStartOffset() && pv.getEndOffset() == para.getEndOffset(), "offsets from element");
            check(pv.getAxis() == View.Y_AXIS, "paragraph tiles rows vertically");
            check(pv.getFlowAxis() == View.X_AXIS, "text flows horizontally");

            check(pv.getFlowSpan(0) == Integer.MAX_VALUE, "default flow span unbounded");
            check(pv.getFlowStart(0) == 0, "default flow start at origin");

            // default left-justified paragraph: a tab stop every 72 points
            check(pv.nextTabStop(0f, 0) == 72f, "first tab stop at 72");
            check(pv.nextTabStop(100f, 0) == 144f, "second tab stop at 144");

            check(pv.getAlignment(View.X_AXIS) == 0.5f, "X alignment centered");
            check(pv.getAlignment(View.Y_AXIS) == 0.5f, "Y alignment centered when empty");
            boolean threw = false;
            try { pv.getAlignment(2); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "invalid alignment axis -> IllegalArgumentException");

            check(pv.getBreakWeight(View.Y_AXIS, 100f) == View.BadBreakWeight, "Y break weight bad");
            check(pv.getBreakWeight(View.X_AXIS, 100f) == View.BadBreakWeight, "X break weight bad");
            check(pv.breakView(View.Y_AXIS, 100f, null) == pv, "breakView returns itself");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ParagraphViewTest::testWhiteBox);
        runOnEDT(ParagraphViewTest::testBlackBox);
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

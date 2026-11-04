/*
 * @test
 * @summary javax.swing.text.View conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ViewTest
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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;

public class ViewTest {
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

    // A minimal concrete View so the abstract View API can be exercised
    // through its documented default implementations.
    static final class SimpleView extends View {
        SimpleView(Element e) { super(e); }
        public float getPreferredSpan(int axis) { return 42f; }
        public void paint(Graphics g, Shape a) { }
        public Shape modelToView(int pos, Shape a, Position.Bias b) {
            return new Rectangle(0, 0, 1, 1);
        }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] b) {
            b[0] = Position.Bias.Forward;
            return 0;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.View");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
        // constant values
        check(View.X_AXIS == 0, "X_AXIS constant");
        check(View.Y_AXIS == 1, "Y_AXIS constant");
        check(View.BadBreakWeight == 0, "BadBreakWeight");
        check(View.GoodBreakWeight == 1000, "GoodBreakWeight");
        check(View.ExcellentBreakWeight == 2000, "ExcellentBreakWeight");
        check(View.ForcedBreakWeight == 3000, "ForcedBreakWeight");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world\nsecond line");
        frame.add(ta);
        frame.pack();
        try {
            Document doc = ta.getDocument();
            Element leaf = doc.getDefaultRootElement().getElement(0);

            // element / document mapping
            SimpleView v = new SimpleView(leaf);
            check(v.getElement() == leaf, "getElement returns ctor element");
            check(v.getDocument() == doc, "getDocument delegates to element");
            check(v.getStartOffset() == leaf.getStartOffset(), "getStartOffset");
            check(v.getEndOffset() == leaf.getEndOffset(), "getEndOffset");
            check(v.getAttributes() == leaf.getAttributes(), "getAttributes defaults to element attributes");

            // parent chaining
            check(v.getParent() == null, "no parent initially");
            SimpleView parent = new SimpleView(doc.getDefaultRootElement());
            v.setParent(parent);
            check(v.getParent() == parent, "setParent/getParent round trip");
            v.setParent(null);
            check(v.getParent() == null, "setParent(null) clears parent");

            // default leaf-view behavior
            check(v.isVisible(), "default view visible");
            check(v.getViewCount() == 0, "default view count 0");
            check(v.getView(0) == null, "default getView null");
            check(v.getChildAllocation(0, new Rectangle(0, 0, 10, 10)) == null, "default child allocation null");
            check(v.getViewIndex(0, Position.Bias.Forward) == -1, "default getViewIndex -1");
            check(v.getAlignment(View.X_AXIS) == 0.5f, "default alignment 0.5");
            check(v.getResizeWeight(View.X_AXIS) == 0, "default resize weight 0");
            check(v.getMinimumSpan(View.X_AXIS) == 42f, "min span == preferred when weight 0");
            check(v.getMaximumSpan(View.X_AXIS) == 42f, "max span == preferred when weight 0");

            // break / fragment defaults
            check(v.breakView(View.X_AXIS, 0, 0f, 0f) == v, "default breakView returns this");
            check(v.createFragment(0, 1) == v, "default createFragment returns this");
            check(v.getBreakWeight(View.X_AXIS, 0f, 100f) == View.GoodBreakWeight, "break weight good when len > span");
            check(v.getBreakWeight(View.X_AXIS, 0f, 10f) == View.BadBreakWeight, "break weight bad when len <= span");

            // container / factory / tooltip with no parent
            check(v.getContainer() == null, "container null without parent");
            check(v.getViewFactory() == null, "factory null without parent");
            check(v.getToolTipText(0f, 0f, new Rectangle(0, 0, 10, 10)) == null, "tooltip null");
            check(v.getViewIndex(0f, 0f, new Rectangle(0, 0, 10, 10)) == -1, "getViewIndex(x,y) -1");

            // preferenceChanged with no parent is a no-op
            v.preferenceChanged(v, true, true);

            // real model/view translation through a live JTextArea
            Rectangle r0 = ta.modelToView(0);
            Rectangle r6 = ta.modelToView(6);
            check(r0 != null && r6 != null, "modelToView non-null");
            check(r0.x <= r6.x, "x coordinate monotonic with position");
            int pos = ta.viewToModel(new java.awt.Point(r0.x, r0.y));
            check(pos >= 0 && pos <= doc.getLength(), "viewToModel within document range");
            check(throwsBLE(() -> ta.modelToView(-1)), "modelToView(-1) -> BadLocationException");
            check(throwsBLE(() -> ta.modelToView(doc.getLength() + 2)), "modelToView past end -> BadLocationException");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ViewTest::testWhiteBox);
        runOnEDT(ViewTest::testBlackBox);
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

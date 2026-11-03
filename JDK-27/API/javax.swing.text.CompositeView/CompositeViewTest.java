/*
 * @test
 * @summary javax.swing.text.CompositeView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main CompositeViewTest
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
import javax.swing.text.CompositeView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class CompositeViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class MyComposite extends CompositeView {
        MyComposite(Element e) { super(e); }
        protected boolean isBefore(int a, int b, Rectangle r) { return false; }
        protected boolean isAfter(int a, int b, Rectangle r) { return false; }
        protected View getViewAtPoint(int x, int y, Rectangle r) { return null; }
        protected void childAllocation(int i, Rectangle r) { r.setBounds(0, 0, 10, 10); }
        public float getPreferredSpan(int axis) { return 0; }
        public void paint(Graphics g, Shape a) { }
    }

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
        Class<?> c = Class.forName("javax.swing.text.CompositeView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.View"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
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
            ViewFactory vf = tp.getEditorKit().getViewFactory();
            check(vf != null, "styled view factory non-null");

            MyComposite mc = new MyComposite(root);
            check(mc.getElement() == root, "getElement returns root");
            check(mc.getViewCount() == 0, "no children before setParent");
            check(mc.getParent() == null, "no parent before setParent");

            mc.setParent(new Host(tp, vf));
            check(mc.getViewCount() == root.getElementCount(), "loadChildren creates a child per element");
            check(root.getElementCount() == 3, "three paragraph children");
            check(mc.getView(0) instanceof ParagraphView, "factory created ParagraphView for paragraph element");
            for (int i = 0; i < mc.getViewCount(); i++) {
                check(mc.getView(i).getParent() == mc, "child parent back-reference");
            }

            // getViewIndex over model offsets
            int len = doc.getLength();
            check(mc.getViewIndex(0, Position.Bias.Forward) == 0, "view index at 0");
            check(mc.getViewIndex(len - 1, Position.Bias.Forward) == mc.getViewCount() - 1, "view index at last offset");
            check(mc.getViewIndex(mc.getEndOffset(), Position.Bias.Forward) == -1, "view index at end offset -> -1");

            // child allocation
            Shape ca = mc.getChildAllocation(0, new Rectangle(5, 6, 100, 40));
            check(ca instanceof Rectangle, "child allocation is a Rectangle");
            check(ca.getBounds().width == 10 && ca.getBounds().height == 10, "childAllocation applied");

            // replace / append / remove round trips
            View first = mc.getView(0);
            View replacement = vf.create(root.getElement(1));
            mc.replace(0, 1, new View[] { replacement });
            check(mc.getViewCount() == 3, "replace keeps count");
            check(mc.getView(0) == replacement, "replace installed new view");
            check(replacement.getParent() == mc, "replacement parent set");
            check(first.getParent() == null, "removed view detached");

            mc.removeAll();
            check(mc.getViewCount() == 0, "removeAll empties children");

            View appended = vf.create(root.getElement(0));
            mc.append(appended);
            check(mc.getViewCount() == 1, "append adds one");
            check(mc.getView(0) == appended && appended.getParent() == mc, "appended parent set");
            mc.remove(0);
            check(mc.getViewCount() == 0, "remove(index) drops child");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(CompositeViewTest::testWhiteBox);
        runOnEDT(CompositeViewTest::testBlackBox);
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

/*
 * @test
 * @summary javax.swing.text.ViewFactory conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ViewFactoryTest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Shape;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class ViewFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class StubView extends View {
        StubView(Element e) { super(e); }
        public float getPreferredSpan(int axis) { return 0f; }
        public void paint(Graphics g, Shape a) { }
        public Shape modelToView(int pos, Shape a, Position.Bias b) { return null; }
        public int viewToModel(float x, float y, Shape a, Position.Bias[] b) { return 0; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ViewFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ViewFactory");

        check(c.getDeclaredMethods().length == 1, "one abstract method");
        Method create = c.getMethod("create", Element.class);
        check(create.getReturnType() == View.class, "create returns View");

        // a conforming factory maps an element to a view
        final View made = new StubView(null);
        ViewFactory f = new ViewFactory() {
            public View create(Element elem) { return made; }
        };
        check(f.create(null) == made, "create contract exercised");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ViewFactoryTest::testWhiteBox);
        runOnEDT(ViewFactoryTest::testBlackBox);
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

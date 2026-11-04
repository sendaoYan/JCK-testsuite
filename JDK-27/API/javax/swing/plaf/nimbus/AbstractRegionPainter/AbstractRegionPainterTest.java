/*
 * @test
 * @summary javax.swing.plaf.nimbus.AbstractRegionPainter conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main AbstractRegionPainterTest
 */

import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.nimbus.AbstractRegionPainter;

public class AbstractRegionPainterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    static class Painter extends AbstractRegionPainter {
        boolean doPaintCalled;
        protected PaintContext getPaintContext() { return null; }
        protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
            doPaintCalled = true;
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(AbstractRegionPainterTest::testWhiteBox);
        runOnEDT(AbstractRegionPainterTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.AbstractRegionPainter");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.Painter"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        Painter p = new Painter();
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        JComponent c = new JPanel();
        p.paint(g, c, 0, 50);
        check(p.doPaintCalled == false, "zero width skips doPaint");
        p.paint(g, c, 100, 50);
        check(p.doPaintCalled == true, "paint invokes doPaint");
        g.dispose();
    }
}

/*
 * @test
 * @summary javax.swing.plaf.metal.MetalScrollButton conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalScrollButtonTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.metal.MetalScrollButton;

public class MetalScrollButtonTest {
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

    static void assertNoThrow(ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) { throw new AssertionError(msg + ": " + t, t); }
        passed++;
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(MetalScrollButtonTest::testWhiteBox);
        runOnEDT(MetalScrollButtonTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalScrollButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicArrowButton"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        int[] dirs = new int[] { SwingConstants.NORTH, SwingConstants.SOUTH,
                                 SwingConstants.EAST, SwingConstants.WEST };
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        for (int dir : dirs) {
            MetalScrollButton b = new MetalScrollButton(dir, 30, false);
            check(b.getButtonWidth() == 30, "button width 30");
            Dimension pref = b.getPreferredSize();
            Dimension min = b.getMinimumSize();
            Dimension max = b.getMaximumSize();
            check(pref != null && pref.width >= 0 && pref.height >= 0, "preferred size sane");
            check(min != null && min.width >= 0 && min.height >= 0, "minimum size sane");
            check(max != null && max.width >= 0 && max.height >= 0, "maximum size sane");
            assertNoThrow(() -> b.setFreeStanding(true), "setFreeStanding(true)");
            assertNoThrow(() -> b.setFreeStanding(false), "setFreeStanding(false)");
            // MetalScrollButton.paint reads getParent().isEnabled(), so the
            // button must have a container parent before it can be painted.
            new JPanel().add(b);
            b.setSize(30, 30);
            assertNoThrow(() -> {
                Graphics g = img.createGraphics();
                g.setClip(0, 0, 100, 100);
                try { b.paint(g); } finally { g.dispose(); }
            }, "paint");
        }
    }
}

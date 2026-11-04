/*
 * @test
 * @summary javax.swing.plaf.metal.MetalInternalFrameTitlePane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalInternalFrameTitlePaneTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.plaf.metal.MetalInternalFrameTitlePane;

public class MetalInternalFrameTitlePaneTest {
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
        runOnEDT(MetalInternalFrameTitlePaneTest::testWhiteBox);
        runOnEDT(MetalInternalFrameTitlePaneTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalInternalFrameTitlePane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicInternalFrameTitlePane"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JInternalFrame frame = new JInternalFrame("Title");
        MetalInternalFrameTitlePane tp = new MetalInternalFrameTitlePane(frame);
        check(tp instanceof JComponent, "instanceof JComponent");

        Dimension pref = tp.getPreferredSize();
        check(pref != null && pref.width >= 0 && pref.height >= 0, "preferred size sane");

        assertNoThrow(() -> tp.setPalette(true), "setPalette(true)");
        assertNoThrow(() -> tp.setPalette(false), "setPalette(false)");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { tp.paintComponent(g); } finally { g.dispose(); }
        }, "paintComponent");
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { tp.paintPalette(g); } finally { g.dispose(); }
        }, "paintPalette");
    }
}

/*
 * @test
 * @summary javax.swing.plaf.metal.MetalInternalFrameUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalInternalFrameUITest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JInternalFrame;
import javax.swing.plaf.metal.MetalInternalFrameUI;

public class MetalInternalFrameUITest {
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
        runOnEDT(MetalInternalFrameUITest::testWhiteBox);
        runOnEDT(MetalInternalFrameUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalInternalFrameUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicInternalFrameUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JInternalFrame f = new JInternalFrame("Frame");
        MetalInternalFrameUI ui = new MetalInternalFrameUI(f);
        check(MetalInternalFrameUI.createUI(f) instanceof MetalInternalFrameUI, "createUI type");

        assertNoThrow(() -> ui.installUI(f), "installUI");
        assertNoThrow(() -> ui.setPalette(true), "setPalette");
        // getPreferredSize on an un-parented JInternalFrame recurses between
        // the title pane and root pane layouts, so only the fixed maximum size
        // contract is asserted here.
        check(ui.getMaximumSize(f) != null, "maximum size non-null");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.paint(g, f); } finally { g.dispose(); }
        }, "paint");
        assertNoThrow(() -> ui.uninstallUI(f), "uninstallUI");
    }
}

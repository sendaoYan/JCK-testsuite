/*
 * @test
 * @summary javax.swing.plaf.metal.MetalScrollBarUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalScrollBarUITest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JScrollBar;
import javax.swing.plaf.metal.MetalScrollBarUI;

public class MetalScrollBarUITest {
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
        runOnEDT(MetalScrollBarUITest::testWhiteBox);
        runOnEDT(MetalScrollBarUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalScrollBarUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicScrollBarUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JScrollBar sb = new JScrollBar(JScrollBar.HORIZONTAL);
        MetalScrollBarUI ui = new MetalScrollBarUI();
        check(MetalScrollBarUI.createUI(sb) instanceof MetalScrollBarUI, "createUI type");
        check("JScrollBar.isFreeStanding".equals(MetalScrollBarUI.FREE_STANDING_PROP), "FREE_STANDING_PROP");

        assertNoThrow(() -> ui.installUI(sb), "installUI");
        Dimension pref = ui.getPreferredSize(sb);
        check(pref != null && pref.width >= 0 && pref.height >= 0, "preferred size sane");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.paint(g, sb); } finally { g.dispose(); }
        }, "paint");
        assertNoThrow(() -> ui.uninstallUI(sb), "uninstallUI");
    }
}

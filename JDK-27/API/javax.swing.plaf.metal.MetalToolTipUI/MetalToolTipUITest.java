/*
 * @test
 * @summary javax.swing.plaf.metal.MetalToolTipUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalToolTipUITest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JToolTip;
import javax.swing.plaf.metal.MetalToolTipUI;

public class MetalToolTipUITest {
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
        runOnEDT(MetalToolTipUITest::testWhiteBox);
        runOnEDT(MetalToolTipUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalToolTipUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicToolTipUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        MetalToolTipUI ui = new MetalToolTipUI();
        check(ui != null, "ui instance");

        JToolTip tip = new JToolTip();
        tip.setTipText("hello");
        tip.setSize(100, 100);
        check(MetalToolTipUI.createUI(tip) instanceof MetalToolTipUI, "createUI returns MetalToolTipUI");

        assertNoThrow(() -> ui.installUI(tip), "installUI");
        check(tip.getWidth() > 0, "width positive");
        check(tip.getHeight() > 0, "height positive");

        Dimension d = ui.getPreferredSize(tip);
        check(d != null, "preferred size non-null");
        check(d.width > 0 && d.height > 0, "preferred size positive");
        check("".equals(ui.getAcceleratorString()), "accelerator string empty");
        check(MetalToolTipUI.padSpaceBetweenStrings == 12, "padSpaceBetweenStrings is 12");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.paint(g, tip); } finally { g.dispose(); }
        }, "paint");

        assertNoThrow(() -> ui.uninstallUI(tip), "uninstallUI");
    }
}

/*
 * @test
 * @summary javax.swing.plaf.metal.MetalTabbedPaneUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalTabbedPaneUITest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public class MetalTabbedPaneUITest {
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
        runOnEDT(MetalTabbedPaneUITest::testWhiteBox);
        runOnEDT(MetalTabbedPaneUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalTabbedPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        MetalTabbedPaneUI ui = new MetalTabbedPaneUI();
        check(ui != null, "ui instance");

        JTabbedPane tp = new JTabbedPane();
        tp.addTab("a", new JLabel());
        tp.setSize(100, 100);
        check(MetalTabbedPaneUI.createUI(tp) instanceof MetalTabbedPaneUI, "createUI returns MetalTabbedPaneUI");

        assertNoThrow(() -> ui.installUI(tp), "installUI");
        check(tp.getWidth() > 0, "width positive");
        check(tp.getHeight() > 0, "height positive");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.paint(g, tp); } finally { g.dispose(); }
        }, "paint");
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.update(g, tp); } finally { g.dispose(); }
        }, "update");

        assertNoThrow(() -> ui.uninstallUI(tp), "uninstallUI");
    }
}

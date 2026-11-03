/*
 * @test
 * @summary javax.swing.plaf.metal.MetalMenuBarUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalMenuBarUITest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.plaf.metal.MetalMenuBarUI;

public class MetalMenuBarUITest {
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
        runOnEDT(MetalMenuBarUITest::testWhiteBox);
        runOnEDT(MetalMenuBarUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalMenuBarUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicMenuBarUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JMenuBar bar = new JMenuBar();
        bar.add(new JMenu("File"));
        MetalMenuBarUI ui = new MetalMenuBarUI();
        check(MetalMenuBarUI.createUI(bar) instanceof MetalMenuBarUI, "createUI type");

        assertNoThrow(() -> ui.installUI(bar), "installUI");
        // BasicMenuBarUI does not provide a preferred/minimum/maximum size;
        // the inherited defaults return null, which is the observable contract.
        assertNoThrow(() -> ui.getPreferredSize(bar), "getPreferredSize");
        check(ui.getMinimumSize(bar) == null, "minimum size null");
        check(ui.getMaximumSize(bar) == null, "maximum size null");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.update(g, bar); } finally { g.dispose(); }
        }, "update");
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.paint(g, bar); } finally { g.dispose(); }
        }, "paint");
        assertNoThrow(() -> ui.uninstallUI(bar), "uninstallUI");
        check(bar.getMenuCount() == 1, "menu count preserved");
    }
}

/*
 * @test
 * @summary javax.swing.plaf.metal.MetalTextFieldUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalTextFieldUITest
 */

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.Modifier;
import javax.swing.JTextField;
import javax.swing.plaf.metal.MetalTextFieldUI;

public class MetalTextFieldUITest {
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
        runOnEDT(MetalTextFieldUITest::testWhiteBox);
        runOnEDT(MetalTextFieldUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalTextFieldUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicTextFieldUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        MetalTextFieldUI ui = new MetalTextFieldUI();
        check(ui != null, "ui instance");

        JTextField tf = new JTextField("hi");
        tf.setSize(100, 100);
        check(MetalTextFieldUI.createUI(tf) instanceof MetalTextFieldUI, "createUI returns MetalTextFieldUI");

        assertNoThrow(() -> ui.installUI(tf), "installUI");
        check(tf.getWidth() > 0, "width positive");
        check(tf.getHeight() > 0, "height positive");

        assertNoThrow(() -> ui.propertyChange(new PropertyChangeEvent(tf, "jckProp", null, null)), "propertyChange");

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        assertNoThrow(() -> {
            Graphics g = img.createGraphics();
            g.setClip(0, 0, 100, 100);
            try { ui.paint(g, tf); } finally { g.dispose(); }
        }, "paint");

        assertNoThrow(() -> ui.uninstallUI(tf), "uninstallUI");
        check("hi".equals(tf.getText()), "text preserved after UI cycle");
    }
}

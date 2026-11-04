/*
 * @test
 * @summary javax.swing.plaf.metal.MetalIconFactory.TreeControlIcon conformance: white-box structure + black-box behavior.
 * @run main TreeControlIconTest
 */

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.plaf.metal.MetalIconFactory;

public class TreeControlIconTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void assertNoThrow(ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) { throw new AssertionError(msg + ": " + t, t); }
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalIconFactory$TreeControlIcon");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Icon"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        MetalIconFactory.TreeControlIcon light = new MetalIconFactory.TreeControlIcon(false);
        MetalIconFactory.TreeControlIcon dark = new MetalIconFactory.TreeControlIcon(true);
        check(light instanceof Icon, "instanceof Icon");
        check(dark instanceof Serializable, "instanceof Serializable");
        check(light.getIconWidth() >= 0, "light iconWidth non-negative");
        check(light.getIconHeight() >= 0, "light iconHeight non-negative");
        check(dark.getIconWidth() >= 0, "dark iconWidth non-negative");
        check(dark.getIconHeight() >= 0, "dark iconHeight non-negative");
        assertNoThrow(() -> {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            light.paintIcon(new JLabel(), img.createGraphics(), 0, 0);
        }, "light paintIcon");
        assertNoThrow(() -> {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            dark.paintMe(new JLabel(), img.createGraphics(), 0, 0);
        }, "dark paintMe");
    }
}

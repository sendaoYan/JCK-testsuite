/*
 * @test
 * @summary javax.swing.plaf.metal.MetalIconFactory.PaletteCloseIcon conformance: white-box structure + black-box behavior.
 * @run main PaletteCloseIconTest
 */

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalIconFactory;

public class PaletteCloseIconTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalIconFactory$PaletteCloseIcon");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Icon"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.plaf.UIResource"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
    }

    static void testBlackBox() throws Exception {
        MetalIconFactory.PaletteCloseIcon icon = new MetalIconFactory.PaletteCloseIcon();
        check(icon instanceof Icon, "instanceof Icon");
        check(icon instanceof UIResource, "instanceof UIResource");
        check(icon instanceof Serializable, "instanceof Serializable");
        check(icon.getIconWidth() >= 0, "iconWidth non-negative");
        check(icon.getIconHeight() >= 0, "iconHeight non-negative");
        assertNoThrow(() -> {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(new JButton(), img.createGraphics(), 0, 0);
        }, "paintIcon");
    }
}

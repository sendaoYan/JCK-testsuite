/*
 * @test
 * @summary javax.swing.plaf.metal.MetalIconFactory.TreeLeafIcon conformance: white-box structure + black-box behavior.
 * @run main TreeLeafIconTest
 */

import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.plaf.metal.MetalIconFactory;

public class TreeLeafIconTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalIconFactory$TreeLeafIcon");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.metal.MetalIconFactory$FileIcon16"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        MetalIconFactory.TreeLeafIcon icon = new MetalIconFactory.TreeLeafIcon();
        check(icon instanceof MetalIconFactory.FileIcon16, "instanceof FileIcon16");
        check(icon instanceof Icon, "instanceof Icon");
        check(icon.getShift() >= 0, "shift non-negative");
        check(icon.getAdditionalHeight() >= 0, "additionalHeight non-negative");
        check(icon.getIconWidth() >= 0, "iconWidth non-negative");
        check(icon.getIconHeight() >= 0, "iconHeight non-negative");
        assertNoThrow(() -> {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(new JLabel(), img.createGraphics(), 0, 0);
        }, "paintIcon");
    }
}

/*
 * @test
 * @summary javax.swing.ImageIcon conformance: white-box structure + black-box behavior.
 * @run main ImageIconTest
 */

import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ImageIconTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ImageIcon");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Icon"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.accessibility.Accessible"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BufferedImage img = new BufferedImage(16, 8, BufferedImage.TYPE_INT_ARGB);
        ImageIcon icon = new ImageIcon(img);
        check(icon.getIconWidth() == 16, "getIconWidth");
        check(icon.getIconHeight() == 8, "getIconHeight");
        check(icon.getImage() == img, "getImage");
        check(icon.getImageLoadStatus() == MediaTracker.COMPLETE, "image loaded");

        icon.setDescription("my icon");
        check("my icon".equals(icon.getDescription()), "setDescription/getDescription");

        // paint smoke into an offscreen buffer
        BufferedImage canvas = new BufferedImage(16, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        icon.paintIcon(new JPanel(), g, 0, 0);
        g.dispose();
        check(true, "paintIcon smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

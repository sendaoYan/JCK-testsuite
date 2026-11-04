/*
 * @test
 * @summary javax.swing.plaf.metal.MetalComboBoxIcon conformance: white-box structure + black-box behavior.
 * @run main MetalComboBoxIconTest
 */
import java.lang.reflect.Modifier;

public class MetalComboBoxIconTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalComboBoxIcon");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Icon"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");

        check(hasDeclaredMethod(c, "paintIcon"), "declares method paintIcon");
        check(hasDeclaredMethod(c, "getIconWidth"), "declares method getIconWidth");
        check(hasDeclaredMethod(c, "getIconHeight"), "declares method getIconHeight");
    }

    static void testBlackBox() throws Exception {
        javax.swing.Icon icon = new javax.swing.plaf.metal.MetalComboBoxIcon();
        check(icon.getIconWidth() == 10, "icon width 10");
        check(icon.getIconHeight() == 5, "icon height 5");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        icon.paintIcon(new javax.swing.JComboBox<Object>(), g, 2, 2);
        g.dispose();
    }

}

/*
 * @test
 * @summary javax.swing.plaf.metal.MetalBorders.MenuItemBorder conformance: white-box structure + black-box behavior.
 * @run main MenuItemBorderTest
 */
import java.lang.reflect.Modifier;

public class MenuItemBorderTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalBorders$MenuItemBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.AbstractBorder"), "superclass javax.swing.border.AbstractBorder");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");

        check(hasDeclaredMethod(c, "paintBorder"), "declares method paintBorder");
        check(hasDeclaredMethod(c, "getBorderInsets"), "declares method getBorderInsets");
    }

    static void testBlackBox() throws Exception {
        javax.swing.border.Border b = new javax.swing.plaf.metal.MetalBorders.MenuItemBorder();
        check(b instanceof javax.swing.plaf.UIResource, "UIResource marker");
        java.awt.Insets ins = b.getBorderInsets(null);
        check(ins != null, "insets non-null");
        check(!b.isBorderOpaque(), "not opaque by default");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(40, 40, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        b.paintBorder(new java.awt.Component(){}, g, 0, 0, 40, 40);
        g.dispose();
    }

}

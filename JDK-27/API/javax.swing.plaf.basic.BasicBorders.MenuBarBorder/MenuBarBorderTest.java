/*
 * @test
 * @summary javax.swing.plaf.basic.BasicBorders.MenuBarBorder conformance: white-box structure + black-box behavior.
 * @run main MenuBarBorderTest
 */

import java.lang.reflect.Modifier;

public class MenuBarBorderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicBorders$MenuBarBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.AbstractBorder"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.UIResource"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicBorders.MenuBarBorder b =
            new javax.swing.plaf.basic.BasicBorders.MenuBarBorder(java.awt.Color.GRAY, java.awt.Color.WHITE);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        java.awt.Insets ins = new java.awt.Insets(0, 0, 0, 0);
        check(b.getBorderInsets(new javax.swing.JMenuBar(), ins) == ins, "returns provided insets");
        java.awt.Graphics g = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        b.paintBorder(new javax.swing.JMenuBar(), g, 0, 0, 20, 20);
        g.dispose();
        check(true, "paintBorder ok");
    }
}

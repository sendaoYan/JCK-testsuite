/*
 * @test
 * @summary javax.swing.plaf.basic.BasicBorders.SplitPaneBorder conformance: white-box structure + black-box behavior.
 * @run main SplitPaneBorderTest
 */

import java.lang.reflect.Modifier;

public class SplitPaneBorderTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicBorders$SplitPaneBorder");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.border.Border"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.plaf.UIResource"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        javax.swing.plaf.basic.BasicBorders.SplitPaneBorder b =
            new javax.swing.plaf.basic.BasicBorders.SplitPaneBorder(java.awt.Color.GRAY, java.awt.Color.WHITE);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JSplitPane());
        check(ins != null && ins.top >= 0, "insets non-null");
        check(b.isBorderOpaque() == true, "splitpane border opaque");
        java.awt.Graphics g = new java.awt.image.BufferedImage(20, 20, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        b.paintBorder(new javax.swing.JSplitPane(), g, 0, 0, 20, 20);
        g.dispose();
        check(true, "paintBorder ok");
    }
}

/*
 * @test
 * @summary javax.swing.plaf.basic.BasicGraphicsUtils conformance: white-box structure + black-box behavior.
 * @run main BasicGraphicsUtilsTest
 */

import java.lang.reflect.Modifier;

public class BasicGraphicsUtilsTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicGraphicsUtils");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        java.awt.Insets e = javax.swing.plaf.basic.BasicGraphicsUtils.getEtchedInsets();
        check(e != null && e.top == 2 && e.left == 2 && e.bottom == 2 && e.right == 2, "etched insets 2");
        java.awt.Insets gr = javax.swing.plaf.basic.BasicGraphicsUtils.getGrooveInsets();
        check(gr != null && gr.top == 2 && gr.left == 2 && gr.bottom == 2 && gr.right == 2, "groove insets 2");
        java.awt.Graphics g = new java.awt.image.BufferedImage(40, 40, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        javax.swing.plaf.basic.BasicGraphicsUtils.drawEtchedRect(g, 0, 0, 20, 20, java.awt.Color.GRAY, java.awt.Color.WHITE, java.awt.Color.GRAY, java.awt.Color.WHITE);
        javax.swing.plaf.basic.BasicGraphicsUtils.drawGroove(g, 0, 0, 20, 20, java.awt.Color.GRAY, java.awt.Color.WHITE);
        javax.swing.plaf.basic.BasicGraphicsUtils.drawBezel(g, 0, 0, 20, 20, false, false, java.awt.Color.GRAY, java.awt.Color.WHITE, java.awt.Color.GRAY, java.awt.Color.WHITE);
        javax.swing.plaf.basic.BasicGraphicsUtils.drawLoweredBezel(g, 0, 0, 20, 20, java.awt.Color.GRAY, java.awt.Color.WHITE, java.awt.Color.GRAY, java.awt.Color.WHITE);
        javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g, 0, 0, 20, 20);
        javax.swing.plaf.basic.BasicGraphicsUtils.drawString(g, "abc", 5, 5, -1);
        g.dispose();
        check(true, "draw methods callable");
        javax.swing.JButton btn = new javax.swing.JButton("X");
        java.awt.Dimension d = javax.swing.plaf.basic.BasicGraphicsUtils.getPreferredButtonSize(btn, 4);
        check(d != null && d.width >= 0 && d.height >= 0, "preferred button size");
        javax.swing.JLabel lab = new javax.swing.JLabel();
        java.awt.FontMetrics fm = lab.getFontMetrics(lab.getFont());
        check("hello".equals(javax.swing.plaf.basic.BasicGraphicsUtils.getClippedString(lab, fm, "hello", 100)), "not clipped when fits");
        check(javax.swing.plaf.basic.BasicGraphicsUtils.getStringWidth(lab, fm, "hello") >= 0, "string width non-negative");
    }
}

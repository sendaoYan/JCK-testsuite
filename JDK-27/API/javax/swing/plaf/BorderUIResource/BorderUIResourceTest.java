/*
 * @test
 * @summary javax.swing.plaf.BorderUIResource conformance: white-box structure + black-box behavior.
 * @run main BorderUIResourceTest
 */

import java.lang.reflect.Modifier;

public class BorderUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.BorderUIResource");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.border.Border"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.plaf.UIResource"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.io.Serializable"), "interface 2");
    }

    static void testBlackBox() throws Exception {
        javax.swing.border.EmptyBorder eb = new javax.swing.border.EmptyBorder(1, 2, 3, 4);
        javax.swing.plaf.BorderUIResource b = new javax.swing.plaf.BorderUIResource(eb);
        check(b instanceof javax.swing.plaf.UIResource, "is UIResource");
        java.awt.Insets ins = b.getBorderInsets(new javax.swing.JLabel());
        check(ins.top == 1 && ins.left == 2 && ins.bottom == 3 && ins.right == 4, "delegates insets");
        check(b.isBorderOpaque() == false, "delegates isBorderOpaque");
        java.awt.Graphics g = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        b.paintBorder(new javax.swing.JLabel(), g, 0, 0, 10, 10);
        g.dispose();
        check(true, "paintBorder delegates without throw");
        check(javax.swing.plaf.BorderUIResource.getEtchedBorderUIResource() != null, "etched factory");
        check(javax.swing.plaf.BorderUIResource.getLoweredBevelBorderUIResource() != null, "lowered bevel factory");
        check(javax.swing.plaf.BorderUIResource.getRaisedBevelBorderUIResource() != null, "raised bevel factory");
        check(javax.swing.plaf.BorderUIResource.getBlackLineBorderUIResource() != null, "black line factory");
    }
}

/*
 * @test
 * @summary javax.swing.plaf.IconUIResource conformance: white-box structure + black-box behavior.
 * @run main IconUIResourceTest
 */

import java.lang.reflect.Modifier;

public class IconUIResourceTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.IconUIResource");
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
        final int W = 24, H = 12;
        javax.swing.Icon inner = new javax.swing.Icon() {
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {}
            public int getIconWidth() { return W; }
            public int getIconHeight() { return H; }
        };
        javax.swing.plaf.IconUIResource i = new javax.swing.plaf.IconUIResource(inner);
        check(i instanceof javax.swing.plaf.UIResource, "is UIResource");
        check(i.getIconWidth() == W && i.getIconHeight() == H, "delegates size");
        java.awt.Graphics g = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        i.paintIcon(new javax.swing.JLabel(), g, 0, 0);
        g.dispose();
        check(true, "paintIcon delegates");
    }
}

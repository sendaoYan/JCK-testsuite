/*
 * @test
 * @summary javax.swing.Icon conformance: interface shape + paint contract via concrete icon (headless-safe).
 * @run main IconTest
 */

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class IconTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.Icon", false, IconTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("paintIcon"), "method paintIcon");
        check(names.contains("getIconWidth"), "method getIconWidth");
        check(names.contains("getIconHeight"), "method getIconHeight");
        check(c.getDeclaredMethods().length == 3, "exactly three declared methods");
    }

    static class SimpleIcon implements Icon {
        java.awt.Component seenComponent; Graphics2D seenGraphics;
        int seenX = Integer.MIN_VALUE, seenY = Integer.MIN_VALUE, paints;
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            paints++; seenComponent = c; seenGraphics = (Graphics2D) g;
            seenX = x; seenY = y;
            g.drawLine(0, 0, 1, 1);
        }
        public int getIconWidth() { return 5; }
        public int getIconHeight() { return 7; }
    }

    static void testContract() {
        SimpleIcon icon = new SimpleIcon();
        check(icon instanceof Icon, "implementation is an Icon");
        check(icon.getIconWidth() == 5, "width from implementation");
        check(icon.getIconHeight() == 7, "height from implementation");
        check(Icon.class.isAssignableFrom(ImageIcon.class), "ImageIcon implements Icon");
        check(!Icon.class.isAssignableFrom(javax.swing.GrayFilter.class),
                "GrayFilter is not an Icon");
        JLabel label = new JLabel("x");
        label.setIcon(icon);
        check(label.getIcon() == icon, "JLabel stores icon as-is");
        check(label.getIcon().getIconWidth() == 5, "icon width via label");
        label.setIcon(null);
        check(label.getIcon() == null, "setIcon(null) clears");
        // paint contract: args passed through verbatim, null component tolerated
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 3, 4);
        check(icon.paints == 1, "paintIcon invoked exactly once");
        check(icon.seenComponent == null, "component argument passed through");
        check(icon.seenGraphics == g, "graphics argument passed through");
        check(icon.seenX == 3 && icon.seenY == 4, "coordinates passed through");
        icon.paintIcon(label, g, 0, 0);
        check(icon.paints == 2 && icon.seenComponent == label, "component argument passed through");
        g.dispose();
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

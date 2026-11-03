/*
 * @test
 * @summary javax.swing.border.MatteBorder conformance: white-box structure + black-box behavior.
 * @run main MatteBorderTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.Icon;
import javax.swing.border.MatteBorder;

public class MatteBorderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean insetsEq(Insets in, int top, int left, int bottom, int right) {
        return in != null && in.top == top && in.left == left && in.bottom == bottom && in.right == right;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.border.MatteBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.EmptyBorder"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // color matte: insets round-trip, color stored, no icon, opaque
        Color red = Color.RED;
        MatteBorder m1 = new MatteBorder(1, 2, 3, 4, red);
        check(insetsEq(m1.getBorderInsets(), 1, 2, 3, 4), "color matte insets");
        check(m1.getMatteColor() == red, "matte color round-trip");
        check(m1.getTileIcon() == null, "no icon for color matte");
        check(m1.isBorderOpaque(), "color matte opaque");

        // Insets + color constructor round-trips
        MatteBorder m2 = new MatteBorder(new Insets(4, 3, 2, 1), Color.BLUE);
        check(insetsEq(m2.getBorderInsets(), 4, 3, 2, 1), "insets+color round-trip");
        check(m2.getMatteColor() == Color.BLUE, "blue color");

        // icon-only constructor derives insets from the icon's width/height
        Icon icon = new Icon() {
            public int getIconWidth() { return 7; }
            public int getIconHeight() { return 5; }
            public void paintIcon(Component c, Graphics g, int x, int y) {}
        };
        MatteBorder m3 = new MatteBorder(icon);
        check(insetsEq(m3.getBorderInsets(), 5, 7, 5, 7), "icon-computed insets");
        check(m3.getTileIcon() == icon, "icon round-trip");
        check(m3.getMatteColor() == null, "no color for icon matte");
        check(!m3.isBorderOpaque(), "icon matte not opaque");

        // explicit insets + icon constructor keeps the insets and stores the icon
        MatteBorder m4 = new MatteBorder(2, 3, 4, 5, icon);
        check(insetsEq(m4.getBorderInsets(), 2, 3, 4, 5), "explicit insets with icon");
        check(m4.getTileIcon() == icon, "icon kept");

        // paintBorder smoke for both the color and icon variants
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        m1.paintBorder(new Component() {}, g, 0, 0, 24, 24);
        m3.paintBorder(new Component() {}, g, 0, 0, 24, 24);
        g.dispose();
        check(true, "paintBorder smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

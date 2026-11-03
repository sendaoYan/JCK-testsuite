/*
 * @test
 * @summary javax.swing.border.EtchedBorder conformance: white-box structure + black-box behavior.
 * @run main EtchedBorderTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.EtchedBorder;

public class EtchedBorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.EtchedBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.AbstractBorder"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");

        check(EtchedBorder.RAISED == 0 && EtchedBorder.LOWERED == 1, "etch type constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // default is lowered with null derived colors
        EtchedBorder e0 = new EtchedBorder();
        check(e0.getEtchType() == EtchedBorder.LOWERED, "default lowered");
        check(e0.getHighlightColor() == null && e0.getShadowColor() == null, "default null colors");
        check(insetsEq(e0.getBorderInsets(null), 2, 2, 2, 2), "insets 2");
        check(e0.isBorderOpaque(), "opaque");

        // explicit etch type round-trip
        EtchedBorder e1 = new EtchedBorder(EtchedBorder.RAISED);
        check(e1.getEtchType() == EtchedBorder.RAISED, "raised type");

        // color constructor defaults to LOWERED and stores colors
        Color hi = Color.WHITE;
        Color sh = Color.BLACK;
        EtchedBorder e2 = new EtchedBorder(hi, sh);
        check(e2.getEtchType() == EtchedBorder.LOWERED, "color ctor lowered");
        check(e2.getHighlightColor() == hi && e2.getShadowColor() == sh, "colors round-trip");

        // four-arg constructor round-trips both colors and the type
        EtchedBorder e3 = new EtchedBorder(EtchedBorder.RAISED, hi, sh);
        check(e3.getEtchType() == EtchedBorder.RAISED && e3.getHighlightColor() == hi && e3.getShadowColor() == sh, "4-arg round-trip");

        // derived colors come from the component background
        Component comp = new Component() {};
        comp.setBackground(Color.WHITE);
        check(e0.getHighlightColor(comp).equals(Color.WHITE.brighter()), "derived highlight");
        check(e0.getShadowColor(comp).equals(Color.WHITE.darker()), "derived shadow");
        check(e2.getHighlightColor(comp) == hi, "explicit highlight wins");
        check(e2.getShadowColor(comp) == sh, "explicit shadow wins");

        // paintBorder smoke with a real component background
        BufferedImage img = new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        e3.paintBorder(comp, g, 0, 0, 12, 12);
        g.dispose();
        check(true, "paintBorder smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.awt.font.ImageGraphicAttribute conformance: white-box structure plus black-box behavior.
 * @run main ImageGraphicAttributeTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;

public class ImageGraphicAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.ImageGraphicAttribute");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(GraphicAttribute.class.isAssignableFrom(c), "extends GraphicAttribute");
        check(hasMethod(c, "getAscent"), "getAscent()");
        check(hasMethod(c, "getDescent"), "getDescent()");
        check(hasMethod(c, "getAdvance"), "getAdvance()");
        check(hasMethod(c, "getBounds"), "getBounds()");
        check(hasMethod(c, "draw", Graphics2D.class, float.class, float.class), "draw");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BufferedImage img = new BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB);

        // (image, alignment): advance = width, ascent/descent derive from alignment + height
        ImageGraphicAttribute top = new ImageGraphicAttribute(img, GraphicAttribute.TOP_ALIGNMENT);
        check(top.getAdvance() == 10.0f, "advance == image width");
        check(top.getAscent() == 0.0f && top.getDescent() == 20.0f, "TOP_ALIGNMENT ascent/descent");
        check(top.getAlignment() == GraphicAttribute.TOP_ALIGNMENT, "getAlignment");
        Rectangle2D b = top.getBounds();
        check(b != null && b.getWidth() == 10.0 && b.getHeight() == 20.0, "bounds width/height");

        ImageGraphicAttribute bottom = new ImageGraphicAttribute(img, GraphicAttribute.BOTTOM_ALIGNMENT);
        check(bottom.getAscent() == 0.0f && bottom.getDescent() == 20.0f, "BOTTOM_ALIGNMENT origin (0,0) ascent/descent");

        // explicit (image, alignment, originX, originY)
        ImageGraphicAttribute origin = new ImageGraphicAttribute(img, GraphicAttribute.ROMAN_BASELINE, 2.0f, 3.0f);
        check(origin.getAdvance() == 8.0f, "origin ctor advance == width - originX");
        check(origin.getAscent() == 3.0f && origin.getDescent() == 17.0f, "origin ctor ascent/descent");

        // equals / hashCode
        ImageGraphicAttribute other = new ImageGraphicAttribute(img, GraphicAttribute.TOP_ALIGNMENT);
        check(top.equals(other), "equal attributes");
        check(top.hashCode() == other.hashCode(), "equal attributes same hashCode");
        check(!top.equals(new ImageGraphicAttribute(img, GraphicAttribute.BOTTOM_ALIGNMENT)), "different alignment not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

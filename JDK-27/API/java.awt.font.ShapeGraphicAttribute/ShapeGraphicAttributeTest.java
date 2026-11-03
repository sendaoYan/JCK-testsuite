/*
 * @test
 * @summary java.awt.font.ShapeGraphicAttribute conformance: white-box structure plus black-box behavior.
 * @run main ShapeGraphicAttributeTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.lang.reflect.*;

public class ShapeGraphicAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.ShapeGraphicAttribute");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(GraphicAttribute.class.isAssignableFrom(c), "extends GraphicAttribute");
        for (String s : new String[]{"STROKE", "FILL"}) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == boolean.class, "constant " + s + " boolean");
        }
        check(hasMethod(c, "getAscent"), "getAscent()");
        check(hasMethod(c, "getAdvance"), "getAdvance()");
        check(hasMethod(c, "getBounds"), "getBounds()");
        check(hasMethod(c, "getOutline", AffineTransform.class), "getOutline(AffineTransform)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(ShapeGraphicAttribute.STROKE && !ShapeGraphicAttribute.FILL, "STROKE/FILL constant values");

        Shape shape = new Rectangle2D.Double(0, 0, 10, 5);
        ShapeGraphicAttribute sga = new ShapeGraphicAttribute(shape, GraphicAttribute.ROMAN_BASELINE, ShapeGraphicAttribute.STROKE);
        check(sga.getAdvance() == 10.0f, "advance == shape width");
        check(sga.getAscent() == 0.0f && sga.getDescent() == 5.0f, "ROMAN_BASELINE ascent/descent");
        check(sga.getAlignment() == GraphicAttribute.ROMAN_BASELINE, "getAlignment");
        Rectangle2D b = sga.getBounds();
        check(b != null && b.getWidth() == 11.0 && b.getHeight() == 6.0, "STROKE bounds add 1px");
        check(sga.getOutline(null) != null, "getOutline non-null");

        ShapeGraphicAttribute fill = new ShapeGraphicAttribute(shape, GraphicAttribute.TOP_ALIGNMENT, ShapeGraphicAttribute.FILL);
        check(fill.getAscent() == 0.0f && fill.getDescent() == 5.0f, "TOP_ALIGNMENT ascent/descent");
        Rectangle2D fb = fill.getBounds();
        check(fb.getWidth() == 10.0 && fb.getHeight() == 5.0, "FILL bounds no expansion");

        // equals / hashCode
        ShapeGraphicAttribute same = new ShapeGraphicAttribute(shape, GraphicAttribute.ROMAN_BASELINE, ShapeGraphicAttribute.STROKE);
        check(sga.equals(same) && sga.hashCode() == same.hashCode(), "equal attributes equal");
        check(!sga.equals(fill), "different stroke/fill not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

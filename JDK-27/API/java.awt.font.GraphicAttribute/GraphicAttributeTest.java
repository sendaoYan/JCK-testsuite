/*
 * @test
 * @summary java.awt.font.GraphicAttribute conformance: white-box structure plus black-box behavior.
 * @run main GraphicAttributeTest
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.lang.reflect.*;

public class GraphicAttributeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.GraphicAttribute");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        for (String s : new String[]{"TOP_ALIGNMENT","BOTTOM_ALIGNMENT","ROMAN_BASELINE","CENTER_BASELINE","HANGING_BASELINE"}) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == int.class, "constant " + s);
        }
        check(hasMethod(c, "getAscent"), "getAscent()");
        check(hasMethod(c, "getDescent"), "getDescent()");
        check(hasMethod(c, "getAdvance"), "getAdvance()");
        check(hasMethod(c, "getAlignment"), "getAlignment()");
        check(hasMethod(c, "getBounds"), "getBounds()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(GraphicAttribute.TOP_ALIGNMENT == -1 && GraphicAttribute.BOTTOM_ALIGNMENT == -2
              && GraphicAttribute.ROMAN_BASELINE == 0 && GraphicAttribute.CENTER_BASELINE == 1
              && GraphicAttribute.HANGING_BASELINE == 2, "alignment constant values");

        // anonymous subclass: fixed ascent/descent/advance
        GraphicAttribute ga = new GraphicAttribute(GraphicAttribute.TOP_ALIGNMENT) {
            public float getAscent() { return 5.0f; }
            public float getDescent() { return 3.0f; }
            public float getAdvance() { return 8.0f; }
            public void draw(Graphics2D g, float x, float y) { }
        };
        check(ga.getAlignment() == GraphicAttribute.TOP_ALIGNMENT, "getAlignment");
        check(ga.getAscent() == 5.0f && ga.getDescent() == 3.0f && ga.getAdvance() == 8.0f,
              "ascent/descent/advance from subclass");
        check(ga.getBounds() != null, "getBounds non-null");
        check(ga.getOutline(null) != null, "getOutline non-null");
        // default justification info
        GlyphJustificationInfo info = ga.getJustificationInfo();
        check(info.weight == 8.0f && !info.growAbsorb && info.growPriority == GlyphJustificationInfo.PRIORITY_INTERCHAR
              && !info.shrinkAbsorb && info.shrinkPriority == GlyphJustificationInfo.PRIORITY_WHITESPACE,
              "default justification info uses advance as weight");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

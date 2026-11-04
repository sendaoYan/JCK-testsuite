/*
 * @test
 * @summary java.awt.font.GlyphJustificationInfo conformance: white-box structure plus black-box behavior.
 * @run main GlyphJustificationInfoTest
 */

import java.awt.font.GlyphJustificationInfo;
import java.lang.reflect.*;

public class GlyphJustificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.GlyphJustificationInfo");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        for (String s : new String[]{"PRIORITY_KASHIDA","PRIORITY_WHITESPACE","PRIORITY_INTERCHAR","PRIORITY_NONE"}) {
            Field f = c.getField(s);
            check(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                  && f.getType() == int.class, "constant " + s);
        }
        check(c.getConstructor(float.class, boolean.class, int.class, float.class, float.class,
              boolean.class, int.class, float.class, float.class) != null, "9-arg constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(GlyphJustificationInfo.PRIORITY_KASHIDA == 0 && GlyphJustificationInfo.PRIORITY_WHITESPACE == 1
              && GlyphJustificationInfo.PRIORITY_INTERCHAR == 2 && GlyphJustificationInfo.PRIORITY_NONE == 3,
              "priority constant values");

        GlyphJustificationInfo g = new GlyphJustificationInfo(0.5f, true, 1, 0.1f, 0.2f, false, 2, 0.3f, 0.4f);
        check(g.weight == 0.5f, "weight field");
        check(g.growAbsorb && g.growPriority == 1, "grow fields");
        check(close(g.growLeftLimit, 0.1f) && close(g.growRightLimit, 0.2f), "grow limits");
        check(!g.shrinkAbsorb && g.shrinkPriority == 2, "shrink fields");
        check(close(g.shrinkLeftLimit, 0.3f) && close(g.shrinkRightLimit, 0.4f), "shrink limits");

        // two instances with same values are not equal (no equals override) but fields match
        GlyphJustificationInfo g2 = new GlyphJustificationInfo(0.5f, true, 1, 0.1f, 0.2f, false, 2, 0.3f, 0.4f);
        check(g2.weight == g.weight && g2.growPriority == g.growPriority, "independent instance same fields");
    }

    static boolean close(float a, float b) { return Math.abs(a - b) < 1e-6; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

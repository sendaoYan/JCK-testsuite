/*
 * @test
 * @summary java.awt.font.TextHitInfo conformance: white-box structure plus black-box behavior.
 * @run main TextHitInfoTest
 */

import java.awt.font.TextHitInfo;
import java.lang.reflect.*;

public class TextHitInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.TextHitInfo");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(hasMethod(c, "leading", int.class), "leading(int)");
        check(hasMethod(c, "trailing", int.class), "trailing(int)");
        check(hasMethod(c, "beforeOffset", int.class), "beforeOffset(int)");
        check(hasMethod(c, "afterOffset", int.class), "afterOffset(int)");
        check(hasMethod(c, "getCharIndex"), "getCharIndex()");
        check(hasMethod(c, "isLeadingEdge"), "isLeadingEdge()");
        check(hasMethod(c, "getInsertionIndex"), "getInsertionIndex()");
        check(hasMethod(c, "getOtherHit"), "getOtherHit()");
        check(hasMethod(c, "getOffsetHit", int.class), "getOffsetHit(int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TextHitInfo lead = TextHitInfo.leading(3);
        check(lead.getCharIndex() == 3 && lead.isLeadingEdge() && lead.getInsertionIndex() == 3,
              "leading(3) char/edge/insertion");

        TextHitInfo trail = TextHitInfo.trailing(3);
        check(trail.getCharIndex() == 3 && !trail.isLeadingEdge() && trail.getInsertionIndex() == 4,
              "trailing(3) char/edge/insertion");

        TextHitInfo before = TextHitInfo.beforeOffset(3);
        check(before.getCharIndex() == 2 && !before.isLeadingEdge(), "beforeOffset(3) char 2 trailing");
        TextHitInfo after = TextHitInfo.afterOffset(3);
        check(after.getCharIndex() == 3 && after.isLeadingEdge(), "afterOffset(3) char 3 leading");

        // getOtherHit flips to other side of insertion point
        TextHitInfo other = lead.getOtherHit();
        check(other.getCharIndex() == 2 && !other.isLeadingEdge(), "leading(3).getOtherHit == trailing(2)");
        TextHitInfo other2 = trail.getOtherHit();
        check(other2.getCharIndex() == 4 && other2.isLeadingEdge(), "trailing(3).getOtherHit == leading(4)");

        // getOffsetHit shifts char index, keeps edge
        TextHitInfo shifted = lead.getOffsetHit(5);
        check(shifted.getCharIndex() == 8 && shifted.isLeadingEdge(), "getOffsetHit(5) shifts index");

        // equals / hashCode / toString
        check(lead.equals(TextHitInfo.leading(3)), "equal hits equal");
        check(!lead.equals(trail), "leading != trailing same index");
        check(lead.hashCode() == 3, "hashCode == charIndex");
        check("TextHitInfo[3L]".equals(lead.toString()), "toString leading");
        check("TextHitInfo[3T]".equals(trail.toString()), "toString trailing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

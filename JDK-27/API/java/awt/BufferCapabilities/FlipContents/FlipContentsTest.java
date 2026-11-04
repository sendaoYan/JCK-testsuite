/*
 * @test
 * @summary java.awt.BufferCapabilities.FlipContents conformance: white-box structure + black-box behavior.
 * @run main FlipContentsTest
 */

import java.lang.reflect.*;
import java.awt.BufferCapabilities;

public class FlipContentsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.BufferCapabilities$FlipContents", false, FlipContentsTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.BufferCapabilities.FlipContents");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(c.getEnumConstants() == null, "not a java.lang.Enum");
        check(c.getSuperclass().getName().equals("java.awt.AttributeValue"), "extends java.awt.AttributeValue");
        for (String n : new String[] {"UNDEFINED", "BACKGROUND", "PRIOR", "COPIED"})
            check(c.getField(n).getType() == BufferCapabilities.FlipContents.class, "public static final " + n);
        check(c.getMethod("toString") != null, "toString present");
        check(c.getMethod("hashCode") != null, "hashCode present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BufferCapabilities.FlipContents u = BufferCapabilities.FlipContents.UNDEFINED;
        BufferCapabilities.FlipContents b = BufferCapabilities.FlipContents.BACKGROUND;
        BufferCapabilities.FlipContents p = BufferCapabilities.FlipContents.PRIOR;
        BufferCapabilities.FlipContents c = BufferCapabilities.FlipContents.COPIED;

        check(u != null && b != null && p != null && c != null, "all four constants non-null");
        check(u != b && b != p && p != c, "constants distinct");

        // AttributeValue uses the ordinal int as hashCode and name as toString
        check(u.hashCode() == 0, "UNDEFINED hashCode 0");
        check(b.hashCode() == 1, "BACKGROUND hashCode 1");
        check(p.hashCode() == 2, "PRIOR hashCode 2");
        check(c.hashCode() == 3, "COPIED hashCode 3");

        check(u.toString().equals("undefined"), "UNDEFINED toString");
        check(b.toString().equals("background"), "BACKGROUND toString");
        check(p.toString().equals("prior"), "PRIOR toString");
        check(c.toString().equals("copied"), "COPIED toString");

        // deterministic across repeated reads
        check(BufferCapabilities.FlipContents.BACKGROUND == b, "singleton identity");
        check(BufferCapabilities.FlipContents.PRIOR.toString().equals(p.toString()), "toString deterministic");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

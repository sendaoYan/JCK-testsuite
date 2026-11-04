/*
 * @test
 * @summary java.awt.ComponentOrientation conformance: white-box structure + black-box behavior.
 * @run main ComponentOrientationTest
 */

import java.lang.reflect.*;
import java.awt.ComponentOrientation;
import java.util.Locale;

public class ComponentOrientationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.ComponentOrientation", false, ComponentOrientationTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.ComponentOrientation");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isFinal(m), "public final class");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getField("LEFT_TO_RIGHT").getType() == ComponentOrientation.class, "LEFT_TO_RIGHT field");
        check(c.getField("RIGHT_TO_LEFT").getType() == ComponentOrientation.class, "RIGHT_TO_LEFT field");
        check(c.getField("UNKNOWN").getType() == ComponentOrientation.class, "UNKNOWN field");
        for (String n : new String[] {"isHorizontal", "isLeftToRight"})
            check(c.getMethod(n) != null, "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(ComponentOrientation.LEFT_TO_RIGHT.isHorizontal(), "LTR is horizontal");
        check(ComponentOrientation.LEFT_TO_RIGHT.isLeftToRight(), "LTR is left-to-right");

        check(ComponentOrientation.RIGHT_TO_LEFT.isHorizontal(), "RTL is horizontal");
        check(!ComponentOrientation.RIGHT_TO_LEFT.isLeftToRight(), "RTL is not left-to-right");

        check(ComponentOrientation.UNKNOWN.isHorizontal(), "UNKNOWN is horizontal");
        check(ComponentOrientation.UNKNOWN.isLeftToRight(), "UNKNOWN defaults left-to-right");

        check(ComponentOrientation.LEFT_TO_RIGHT != ComponentOrientation.RIGHT_TO_LEFT, "LTR != RTL");
        check(ComponentOrientation.UNKNOWN != ComponentOrientation.LEFT_TO_RIGHT, "UNKNOWN != LTR");

        // locale mapping (language-tag based)
        check(ComponentOrientation.getOrientation(Locale.US) == ComponentOrientation.LEFT_TO_RIGHT,
              "en locale -> LTR");
        check(ComponentOrientation.getOrientation(Locale.of("ar")) == ComponentOrientation.RIGHT_TO_LEFT,
              "ar locale -> RTL");
        check(ComponentOrientation.getOrientation(Locale.of("he")) == ComponentOrientation.RIGHT_TO_LEFT,
              "he locale -> RTL");
        check(ComponentOrientation.getOrientation(Locale.of("zh")) == ComponentOrientation.LEFT_TO_RIGHT,
              "zh locale -> LTR");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

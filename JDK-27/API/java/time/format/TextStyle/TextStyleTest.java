/*
 * @test
 * @summary java.time.format.TextStyle conformance: white-box structure + black-box behavior.
 * @run main TextStyleTest
 */

import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class TextStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.TextStyle");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(Enum.class.isAssignableFrom(c), "extends Enum");
        check(c.getEnumConstants().length == 6, "six constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(TextStyle.values().length == 6, "six styles");
        check(TextStyle.valueOf("FULL") == TextStyle.FULL, "valueOf FULL");
        check(TextStyle.valueOf("FULL_STANDALONE") == TextStyle.FULL_STANDALONE,
              "valueOf FULL_STANDALONE");
        check(TextStyle.valueOf("SHORT") == TextStyle.SHORT, "valueOf SHORT");
        check(TextStyle.valueOf("NARROW") == TextStyle.NARROW, "valueOf NARROW");
        check(TextStyle.valueOf("NARROW_STANDALONE") == TextStyle.NARROW_STANDALONE,
              "valueOf NARROW_STANDALONE");

        check(!TextStyle.FULL.isStandalone(), "FULL not standalone");
        check(TextStyle.FULL_STANDALONE.isStandalone(), "FULL_STANDALONE standalone");
        check(!TextStyle.SHORT.isStandalone(), "SHORT not standalone");
        check(TextStyle.SHORT_STANDALONE.isStandalone(), "SHORT_STANDALONE standalone");
        check(!TextStyle.NARROW.isStandalone(), "NARROW not standalone");
        check(TextStyle.NARROW_STANDALONE.isStandalone(), "NARROW_STANDALONE standalone");

        check(TextStyle.FULL.asStandalone() == TextStyle.FULL_STANDALONE,
              "FULL asStandalone");
        check(TextStyle.FULL_STANDALONE.asNormal() == TextStyle.FULL, "FULL_STANDALONE asNormal");
        check(TextStyle.NARROW.asStandalone() == TextStyle.NARROW_STANDALONE,
              "NARROW asStandalone");

        check("Jan".equals(Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.US)),
              "January short");
        check("January".equals(Month.JANUARY.getDisplayName(TextStyle.FULL, Locale.US)),
              "January full");
        check("J".equals(Month.JANUARY.getDisplayName(TextStyle.NARROW, Locale.US)),
              "January narrow");
        check("Monday".equals(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL, Locale.US)),
              "Monday full");
        check("Mon".equals(DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, Locale.US)),
              "Monday short");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

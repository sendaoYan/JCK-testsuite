/*
 * @test
 * @summary java.time.format.FormatStyle conformance: white-box structure + black-box behavior.
 * @run main FormatStyleTest
 */

import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class FormatStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.FormatStyle");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(Enum.class.isAssignableFrom(c), "extends Enum");
        check(c.getEnumConstants().length == 4, "four constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(FormatStyle.values().length == 4, "four styles");
        check(FormatStyle.valueOf("FULL") == FormatStyle.FULL, "valueOf FULL");
        check(FormatStyle.valueOf("LONG") == FormatStyle.LONG, "valueOf LONG");
        check(FormatStyle.valueOf("MEDIUM") == FormatStyle.MEDIUM, "valueOf MEDIUM");
        check(FormatStyle.valueOf("SHORT") == FormatStyle.SHORT, "valueOf SHORT");

        // FormatStyle feeds DateTimeFormatter.ofLocalizedDate; assert only
        // that the formatter is produced and carries a locale (no localized text).
        DateTimeFormatter full = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
        check(full != null, "ofLocalizedDate FULL non-null");
        check(full.getLocale() != null, "ofLocalizedDate FULL has locale");

        DateTimeFormatter shortF = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        check(shortF != null, "ofLocalizedDate SHORT non-null");
        check(shortF.getLocale() != null, "ofLocalizedDate SHORT has locale");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

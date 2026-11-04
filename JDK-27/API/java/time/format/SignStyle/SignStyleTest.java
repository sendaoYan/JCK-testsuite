/*
 * @test
 * @summary java.time.format.SignStyle conformance: white-box structure + black-box behavior.
 * @run main SignStyleTest
 */

import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

public class SignStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static DateTimeFormatter signFormatter(SignStyle style, int minWidth, int maxWidth) {
        return new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, minWidth, maxWidth, style)
                .toFormatter();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.SignStyle");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(Enum.class.isAssignableFrom(c), "extends Enum");
        check(c.getEnumConstants().length == 5, "five constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(SignStyle.values().length == 5, "five styles");
        check(SignStyle.valueOf("NORMAL") == SignStyle.NORMAL, "valueOf NORMAL");
        check(SignStyle.valueOf("ALWAYS") == SignStyle.ALWAYS, "valueOf ALWAYS");
        check(SignStyle.valueOf("NEVER") == SignStyle.NEVER, "valueOf NEVER");
        check(SignStyle.valueOf("NOT_NEGATIVE") == SignStyle.NOT_NEGATIVE, "valueOf NOT_NEGATIVE");
        check(SignStyle.valueOf("EXCEEDS_PAD") == SignStyle.EXCEEDS_PAD, "valueOf EXCEEDS_PAD");

        DateTimeFormatter normal = signFormatter(SignStyle.NORMAL, 4, 10);
        check("2024".equals(normal.format(Year.of(2024))), "NORMAL positive");
        check("-2024".equals(normal.format(Year.of(-2024))), "NORMAL negative");

        DateTimeFormatter always = signFormatter(SignStyle.ALWAYS, 4, 10);
        check("+2024".equals(always.format(Year.of(2024))), "ALWAYS positive");
        check("-2024".equals(always.format(Year.of(-2024))), "ALWAYS negative");

        DateTimeFormatter never = signFormatter(SignStyle.NEVER, 4, 10);
        check("2024".equals(never.format(Year.of(2024))), "NEVER positive");
        check("2024".equals(never.format(Year.of(-2024))), "NEVER negative drops sign");

        DateTimeFormatter notNeg = signFormatter(SignStyle.NOT_NEGATIVE, 4, 10);
        check("2024".equals(notNeg.format(Year.of(2024))), "NOT_NEGATIVE positive");
        expectThrows(DateTimeException.class,
                     () -> notNeg.format(Year.of(-2024)),
                     "NOT_NEGATIVE rejects negative");

        DateTimeFormatter exceeds = signFormatter(SignStyle.EXCEEDS_PAD, 6, 10);
        check("002024".equals(exceeds.format(Year.of(2024))), "EXCEEDS_PAD positive padded");
        check("-002024".equals(exceeds.format(Year.of(-2024))), "EXCEEDS_PAD negative padded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

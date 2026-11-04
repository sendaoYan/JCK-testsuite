/*
 * @test
 * @summary java.time.format.ResolverStyle conformance: white-box structure + black-box behavior.
 * @run main ResolverStyleTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class ResolverStyleTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.ResolverStyle");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(Enum.class.isAssignableFrom(c), "extends Enum");
        check(c.getEnumConstants().length == 3, "three constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(ResolverStyle.values().length == 3, "three styles");
        check(ResolverStyle.valueOf("STRICT") == ResolverStyle.STRICT, "valueOf STRICT");
        check(ResolverStyle.valueOf("SMART") == ResolverStyle.SMART, "valueOf SMART");
        check(ResolverStyle.valueOf("LENIENT") == ResolverStyle.LENIENT, "valueOf LENIENT");

        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // STRICT rejects an invalid day-of-month.
        expectThrows(DateTimeParseException.class,
                     () -> f.withResolverStyle(ResolverStyle.STRICT)
                            .parse("2024-02-30", LocalDate::from),
                     "STRICT rejects 2024-02-30");

        // SMART resolves the invalid day to the last valid day of the month.
        check(f.withResolverStyle(ResolverStyle.SMART)
                .parse("2024-02-30", LocalDate::from).equals(LocalDate.of(2024, 2, 29)),
              "SMART resolves 2024-02-30 to Feb 29");

        // LENIENT rolls the overflow into the next month.
        check(f.withResolverStyle(ResolverStyle.LENIENT)
                .parse("2024-02-30", LocalDate::from).equals(LocalDate.of(2024, 3, 1)),
              "LENIENT resolves 2024-02-30 to Mar 1");

        // Day overflow across a month boundary.
        expectThrows(DateTimeParseException.class,
                     () -> f.withResolverStyle(ResolverStyle.STRICT)
                            .parse("2024-06-40", LocalDate::from),
                     "STRICT rejects 2024-06-40");
        check(f.withResolverStyle(ResolverStyle.LENIENT)
                .parse("2024-06-40", LocalDate::from).equals(LocalDate.of(2024, 7, 10)),
              "LENIENT rolls 2024-06-40 to Jul 10");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

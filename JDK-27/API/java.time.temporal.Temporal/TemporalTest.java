/*
 * @test
 * @summary java.time.temporal.Temporal conformance: white-box structure + black-box behavior.
 * @run main TemporalTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

public class TemporalTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.Temporal");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        boolean extendsAccessor = false;
        for (Class<?> i : c.getInterfaces()) if (i == TemporalAccessor.class) extendsAccessor = true;
        check(extendsAccessor, "extends TemporalAccessor");
        check(c.getMethod("with", TemporalField.class, long.class).getReturnType() == Temporal.class, "with(TemporalField,long) returns Temporal");
        check(c.getMethod("plus", long.class, TemporalUnit.class).getReturnType() == Temporal.class, "plus(long,TemporalUnit) returns Temporal");
        check(c.getMethod("until", Temporal.class, TemporalUnit.class).getReturnType() == long.class, "until returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Temporal t = LocalDate.of(2024, 6, 15);
        check(t instanceof LocalDate, "LocalDate is a Temporal");

        // field/unit support
        check(t.isSupported(ChronoField.YEAR), "isSupported(TemporalField YEAR)");
        check(t.isSupported(ChronoUnit.DAYS), "isSupported(TemporalUnit DAYS)");
        check(!t.isSupported(ChronoUnit.HOURS), "not supported HOURS on LocalDate");

        // with / plus / minus via fields and amounts
        check(t.with(ChronoField.YEAR, 2025).equals(LocalDate.of(2025, 6, 15)), "with year");
        check(t.plus(1, ChronoUnit.DAYS).equals(LocalDate.of(2024, 6, 16)), "plus 1 day");
        check(t.minus(1, ChronoUnit.DAYS).equals(LocalDate.of(2024, 6, 14)), "minus 1 day");
        check(t.plus(Period.ofDays(3)).equals(LocalDate.of(2024, 6, 18)), "plus(TemporalAmount Period)");
        check(t.minus(Period.ofDays(3)).equals(LocalDate.of(2024, 6, 12)), "minus(TemporalAmount Period)");

        // with(TemporalAdjuster)
        TemporalAdjuster firstOfMonth = ta -> ta.with(ChronoField.DAY_OF_MONTH, 1);
        check(t.with(firstOfMonth).equals(LocalDate.of(2024, 6, 1)), "with(TemporalAdjuster)");

        // until
        check(t.until(LocalDate.of(2024, 6, 20), ChronoUnit.DAYS) == 5, "until in days");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

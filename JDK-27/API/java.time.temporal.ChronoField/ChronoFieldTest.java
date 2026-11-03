/*
 * @test
 * @summary java.time.temporal.ChronoField conformance: white-box structure + black-box behavior.
 * @run main ChronoFieldTest
 */

import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;

public class ChronoFieldTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String m) {
        try { r.run(); } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.ChronoField");
        check(c.isEnum(), "is an enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        boolean implementsTemporalField = false;
        for (Class<?> i : c.getInterfaces()) if (i == TemporalField.class) implementsTemporalField = true;
        check(implementsTemporalField, "implements TemporalField");
        check(c.getEnumConstants().length == 30, "30 constants");
        check(ChronoField.valueOf("YEAR") == ChronoField.YEAR, "valueOf YEAR round-trip");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constant ranges
        check(ChronoField.MONTH_OF_YEAR.range().equals(ValueRange.of(1, 12)), "MONTH_OF_YEAR range 1..12");
        check(ChronoField.DAY_OF_MONTH.range().equals(ValueRange.of(1, 28, 31)), "DAY_OF_MONTH range 1..28/31");
        check(ChronoField.YEAR.range().equals(ValueRange.of(-999999999, 999999999)), "YEAR range");
        check(ChronoField.MINUTE_OF_HOUR.range().equals(ValueRange.of(0, 59)), "MINUTE_OF_HOUR range 0..59");

        // date/time classification
        check(ChronoField.YEAR.isDateBased() && !ChronoField.YEAR.isTimeBased(), "YEAR date-based only");
        check(ChronoField.HOUR_OF_DAY.isTimeBased() && !ChronoField.HOUR_OF_DAY.isDateBased(), "HOUR_OF_DAY time-based only");

        // base/range units
        check(ChronoField.MONTH_OF_YEAR.getBaseUnit() == ChronoUnit.MONTHS, "MONTH_OF_YEAR base MONTHS");
        check(ChronoField.MONTH_OF_YEAR.getRangeUnit() == ChronoUnit.YEARS, "MONTH_OF_YEAR range YEARS");
        check(ChronoField.DAY_OF_MONTH.getBaseUnit() == ChronoUnit.DAYS, "DAY_OF_MONTH base DAYS");
        check(ChronoField.DAY_OF_MONTH.getRangeUnit() == ChronoUnit.MONTHS, "DAY_OF_MONTH range MONTHS");

        // support on concrete temporals
        check(ChronoField.MONTH_OF_YEAR.isSupportedBy(LocalDate.of(2024, 3, 1)), "MONTH_OF_YEAR supported by LocalDate");
        check(!ChronoField.HOUR_OF_DAY.isSupportedBy(LocalDate.of(2024, 3, 1)), "HOUR_OF_DAY not supported by LocalDate");

        // adjustInto / getFrom
        check(ChronoField.MONTH_OF_YEAR.adjustInto(LocalDate.of(2024, 3, 1), 6).equals(LocalDate.of(2024, 6, 1)), "adjustInto month -> June");
        check(ChronoField.MINUTE_OF_HOUR.getFrom(LocalTime.of(10, 30)) == 30, "getFrom minute-of-hour");
        check(ChronoField.DAY_OF_WEEK.getFrom(LocalDate.of(2024, 6, 15)) == 6, "DAY_OF_WEEK of 2024-06-15 is Saturday(6)");

        // validity checks + exceptions
        check(ChronoField.MONTH_OF_YEAR.checkValidIntValue(5) == 5, "checkValidIntValue 5");
        check(ChronoField.MONTH_OF_YEAR.checkValidValue(12) == 12, "checkValidValue 12");
        expectThrows(DateTimeException.class, () -> ChronoField.MONTH_OF_YEAR.checkValidValue(13), "checkValidValue 13");
        expectThrows(DateTimeException.class, () -> ChronoField.MONTH_OF_YEAR.checkValidIntValue(0), "checkValidIntValue 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

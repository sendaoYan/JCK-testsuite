/*
 * @test
 * @summary java.time.temporal.IsoFields conformance: white-box structure + black-box behavior.
 * @run main IsoFieldsTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

public class IsoFieldsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.IsoFields");
        check(Modifier.isFinal(c.getModifiers()), "is final utility class");
        check(c.getConstructors().length == 0, "no public constructor");
        check(IsoFields.QUARTER_OF_YEAR instanceof TemporalField, "QUARTER_OF_YEAR is a TemporalField");
        check(IsoFields.DAY_OF_QUARTER instanceof TemporalField, "DAY_OF_QUARTER is a TemporalField");
        check(IsoFields.WEEK_OF_WEEK_BASED_YEAR instanceof TemporalField, "WEEK_OF_WEEK_BASED_YEAR is a TemporalField");
        check(IsoFields.WEEK_BASED_YEAR instanceof TemporalField, "WEEK_BASED_YEAR is a TemporalField");
        check(IsoFields.WEEK_BASED_YEARS instanceof TemporalUnit, "WEEK_BASED_YEARS is a TemporalUnit");
        check(IsoFields.QUARTER_YEARS instanceof TemporalUnit, "QUARTER_YEARS is a TemporalUnit");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // quarter of year
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2024, 1, 1)) == 1, "QUARTER_OF_YEAR Jan = 1");
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2024, 7, 1)) == 3, "QUARTER_OF_YEAR Jul = 3");
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2024, 10, 1)) == 4, "QUARTER_OF_YEAR Oct = 4");
        check(IsoFields.QUARTER_OF_YEAR.range().equals(ValueRange.of(1, 4)), "QUARTER_OF_YEAR range 1..4");

        // day of quarter (leap vs non-leap)
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2024, 1, 1)) == 1, "DAY_OF_QUARTER first day");
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2024, 3, 31)) == 91, "DAY_OF_QUARTER end of Q1 2024 (leap) = 91");
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2023, 3, 31)) == 90, "DAY_OF_QUARTER end of Q1 2023 (non-leap) = 90");
        check(IsoFields.DAY_OF_QUARTER.range().getMinimum() == 1, "DAY_OF_QUARTER range min 1");
        check(IsoFields.DAY_OF_QUARTER.range().getSmallestMaximum() == 90, "DAY_OF_QUARTER smallest max 90");
        check(IsoFields.DAY_OF_QUARTER.range().getMaximum() == 92, "DAY_OF_QUARTER max 92");

        // week-based-year fields (2024-01-01 is a Monday, ISO week-based-year 2024)
        check(IsoFields.WEEK_OF_WEEK_BASED_YEAR.getFrom(LocalDate.of(2024, 1, 1)) == 1, "WOWBY 2024-01-01 = 1");
        check(IsoFields.WEEK_BASED_YEAR.getFrom(LocalDate.of(2024, 1, 1)) == 2024, "WBY 2024-01-01 = 2024");
        check(IsoFields.WEEK_BASED_YEAR.getFrom(LocalDate.of(2024, 12, 30)) == 2025, "WBY 2024-12-30 = 2025");

        // quarter-years unit arithmetic
        check(IsoFields.QUARTER_YEARS.addTo(LocalDate.of(2024, 1, 1), 1).equals(LocalDate.of(2024, 4, 1)), "QUARTER_YEARS.addTo +1 -> Apr");
        check(IsoFields.QUARTER_YEARS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 7, 1)) == 2, "QUARTER_YEARS.between = 2");
        check(IsoFields.WEEK_BASED_YEARS.isDateBased(), "WEEK_BASED_YEARS is date-based");
        check(IsoFields.WEEK_BASED_YEARS.isDurationEstimated(), "WEEK_BASED_YEARS duration estimated");
        check(IsoFields.QUARTER_YEARS.isDurationEstimated(), "QUARTER_YEARS duration estimated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepIsoFields();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepIsoFields() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(IsoFields.QUARTER_OF_YEAR.getFrom(d) == 1, "QoY Feb=1");
        check(IsoFields.DAY_OF_QUARTER.getFrom(d) == 60, "DAY_OF_QUARTER Feb29=60");
        LocalDate d2 = LocalDate.of(2024, 5, 1);
        check(IsoFields.QUARTER_OF_YEAR.getFrom(d2) == 2, "QoY May=2");
        check(IsoFields.DAY_OF_QUARTER.getFrom(d2) == 31, "DAY_OF_QUARTER May1=31");
        check(LocalDate.of(2024, 12, 31).getLong(IsoFields.DAY_OF_QUARTER) == 92, "DOQ Dec31=92");
        check(LocalDate.of(2023, 12, 31).getLong(IsoFields.DAY_OF_QUARTER) == 92, "DOQ non-leap Dec31=92");
        xeq(IsoFields.QUARTER_OF_YEAR.range().toString(), "1 - 4", "QoY range fixed");
        xeq(IsoFields.QUARTER_YEARS.getDuration().toString(), "PT2191H27M18S", "QUARTER_YEARS duration");
        check(LocalDate.of(2024, 1, 1).plus(1, IsoFields.QUARTER_YEARS).toString().equals("2024-04-01"), "plus QUARTER_YEARS");
        check(LocalDate.of(2024, 5, 15).minus(1, IsoFields.QUARTER_YEARS).toString().equals("2024-02-15"), "minus QUARTER_YEARS");
        check(IsoFields.WEEK_BASED_YEARS.between(LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1)) == 2, "WBY between 2023-01-01..2024-01-01=2");
        check(IsoFields.WEEK_BASED_YEARS.between(LocalDate.of(2023, 1, 2), LocalDate.of(2024, 1, 1)) == 1, "WBY between ordinary pair=1");
        xeq(LocalDate.of(2024, 1, 1).with(IsoFields.WEEK_BASED_YEAR, 2025).toString(), "2024-12-30", "with WBY keeps week+dow");
        xeq(LocalDate.of(2024, 2, 29).with(IsoFields.WEEK_BASED_YEAR, 2023).toString(), "2023-03-02", "with WBY Feb29 -> W9 Thu of 2023");
        check(LocalDate.of(2024, 2, 29).getLong(IsoFields.WEEK_BASED_YEAR) == 2024L, "WBY of Feb29-2024");
        check(LocalDate.of(2025, 1, 1).getLong(IsoFields.WEEK_BASED_YEAR) == 2025L, "WBY of 2025-01-01 is 2025");
        xact(() -> IsoFields.QUARTER_OF_YEAR.getFrom(LocalTime.NOON), UnsupportedTemporalTypeException.class,
            "Unsupported field: QuarterOfYear", "QoY on LocalTime");
        xact(() -> LocalTime.NOON.getLong(IsoFields.DAY_OF_QUARTER), UnsupportedTemporalTypeException.class,
            "Unsupported field: DayOfQuarter", "DOQ on LocalTime");
        xeq(LocalDate.of(2024, 1, 1).plus(1, IsoFields.WEEK_BASED_YEARS).toString(), "2024-12-30", "plus WBY unit = +52/53 weeks");
        xact(() -> LocalTime.NOON.plus(1, IsoFields.WEEK_BASED_YEARS), UnsupportedTemporalTypeException.class,
            "Unsupported field: WeekBasedYear", "WBY unit on time reports the field");
        check(!IsoFields.QUARTER_OF_YEAR.isSupportedBy(LocalTime.NOON), "QoY not supported by time");
        check(IsoFields.QUARTER_OF_YEAR.isSupportedBy(d), "QoY supported by date");
        check(IsoFields.QUARTER_OF_YEAR.isDateBased(), "QoY date based");
        check(IsoFields.WEEK_BASED_YEARS.isDateBased(), "WBY unit is date based");
        check(IsoFields.WEEK_BASED_YEARS.isDurationEstimated(), "WBY estimated");
        check(IsoFields.QUARTER_OF_YEAR.getBaseUnit() == IsoFields.QUARTER_YEARS, "QoY base QUARTER_YEARS");
        check(IsoFields.QUARTER_OF_YEAR.getRangeUnit() == ChronoUnit.YEARS, "QoY range unit YEARS");
    }

}

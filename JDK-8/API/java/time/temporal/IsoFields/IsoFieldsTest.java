/*
 * @test
 * @summary java.time.temporal.IsoFields conformance: QUARTER/DAY/WEEK based fields.
 * @run main IsoFieldsTest
 */
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

public class IsoFieldsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2020, 7, 1)) == 3, "QUARTER_OF_YEAR");
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2020, 1, 1)) == 1, "DAY_OF_QUARTER");
        check(IsoFields.WEEK_OF_WEEK_BASED_YEAR.getFrom(LocalDate.of(2020, 1, 1)) == 1, "WEEK_OF_WEEK_BASED_YEAR");
        check(IsoFields.WEEK_BASED_YEAR.getFrom(LocalDate.of(2020, 1, 1)) == 2020, "WEEK_BASED_YEAR");
        check(IsoFields.QUARTER_YEARS != null, "QUARTER_YEARS");
        check(IsoFields.WEEK_BASED_YEARS != null, "WEEK_BASED_YEARS");
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

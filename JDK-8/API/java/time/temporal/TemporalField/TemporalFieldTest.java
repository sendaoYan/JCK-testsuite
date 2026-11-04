/*
 * @test
 * @summary java.time.temporal.TemporalField conformance: via ChronoField.DAY_OF_MONTH.
 * @run main TemporalFieldTest
 */

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.temporal.UnsupportedTemporalTypeException;

public class TemporalFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalField() {
        TemporalField f = ChronoField.DAY_OF_MONTH;
        check(f.isDateBased(), "isDateBased");
        check(!f.isTimeBased(), "not isTimeBased");
        check(f.isSupportedBy(LocalDate.now()), "isSupportedBy LocalDate");
        check(f.getBaseUnit() == ChronoUnit.DAYS, "getBaseUnit DAYS");
        check(f.getRangeUnit() == ChronoUnit.MONTHS, "getRangeUnit MONTHS");
        check(f.range().getMinimum() == 1, "range min 1");
        check(f.range().getMaximum() == 31, "range max 31");
        check(f.rangeRefinedBy(LocalDate.of(2020, 2, 1)).getMaximum() == 29, "rangeRefinedBy Feb 2020");
        check(f.getFrom(LocalDate.of(2020, 1, 15)) == 15, "getFrom");
        check(f.adjustInto(LocalDate.of(2020, 1, 1), 20).equals(LocalDate.of(2020, 1, 20)),
              "adjustInto");
    }

    public static void main(String[] args) {
        testTemporalField();
        testDeepTemporalField();
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


    static void testDeepTemporalField() {
        TemporalField f = ChronoField.DAY_OF_MONTH;
        xeq(f.getBaseUnit().toString(), "Days", "base unit");
        xeq(f.getRangeUnit().toString(), "Months", "range unit");
        xeq(f.range().toString(), "1 - 28/31", "range");
        check(f.isDateBased(), "date based");
        check(!f.isTimeBased(), "not time based");
        check(f.isSupportedBy(LocalDate.of(2024, 2, 29)), "supported by date");
        check(!f.isSupportedBy(LocalTime.NOON), "not supported by time");
        check(f.getFrom(LocalDate.of(2024, 2, 29)) == 29, "getFrom");
        xeq(f.rangeRefinedBy(LocalDate.of(2024, 2, 1)).toString(), "1 - 29", "rangeRefinedBy");
        xact(() -> f.rangeRefinedBy(LocalTime.NOON), UnsupportedTemporalTypeException.class,
            "Unsupported field: DayOfMonth", "rangeRefinedBy time");
        xact(() -> f.getFrom(LocalTime.NOON), UnsupportedTemporalTypeException.class,
            "Unsupported field: DayOfMonth", "getFrom time");
        TemporalField y = ChronoField.YEAR;
        check(y.getFrom(LocalDate.of(-44, 1, 1)) == -44, "negative proleptic year");
        check(ChronoField.ERA.getFrom(LocalDate.of(2024, 1, 1)) == 1, "era CE");
        check(ChronoField.ERA.getFrom(LocalDate.of(-1, 1, 1)) == 0, "era BCE");
        check(ChronoField.YEAR_OF_ERA.getFrom(LocalDate.of(-44, 1, 1)) == 45, "YearOfEra BCE = -year+1");
        check(ChronoField.YEAR_OF_ERA.getFrom(LocalDate.of(2024, 1, 1)) == 2024, "YearOfEra CE");
        xeq(ChronoField.HOUR_OF_AMPM.getBaseUnit().toString(), "Hours", "base unit");
        xact(() -> ChronoField.DAY_OF_MONTH.checkValidValue(0L), DateTimeException.class,
            "Invalid value for DayOfMonth (valid values 1 - 28/31): 0", "checkValidValue single arg");
    }

}

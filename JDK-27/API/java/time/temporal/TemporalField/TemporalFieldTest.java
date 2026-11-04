/*
 * @test
 * @summary java.time.temporal.TemporalField conformance: white-box structure + black-box behavior.
 * @run main TemporalFieldTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.temporal.UnsupportedTemporalTypeException;

public class TemporalFieldTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalField");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        for (String n : new String[] {"getBaseUnit", "getRangeUnit", "range", "isDateBased", "isTimeBased",
                "isSupportedBy", "rangeRefinedBy", "getFrom", "adjustInto", "resolve", "toString"}) {
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)), "method " + n);
        }
        check(c.getMethod("getFrom", java.time.temporal.TemporalAccessor.class).getReturnType() == long.class, "getFrom returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TemporalField dom = ChronoField.DAY_OF_MONTH;

        // classification
        check(dom.isDateBased(), "DAY_OF_MONTH isDateBased");
        check(!dom.isTimeBased(), "DAY_OF_MONTH not isTimeBased");

        // support + units
        check(dom.isSupportedBy(LocalDate.of(2024, 3, 1)), "isSupportedBy LocalDate");
        check(!dom.isSupportedBy(java.time.LocalTime.of(10, 0)), "not isSupportedBy LocalTime");
        check(dom.getBaseUnit() == ChronoUnit.DAYS, "getBaseUnit DAYS");
        check(dom.getRangeUnit() == ChronoUnit.MONTHS, "getRangeUnit MONTHS");

        // range + refined range
        check(dom.range().equals(ValueRange.of(1, 28, 31)), "range() 1..28/31");
        check(dom.rangeRefinedBy(LocalDate.of(2020, 2, 1)).getMaximum() == 29, "rangeRefinedBy Feb 2020 max 29");
        check(dom.rangeRefinedBy(LocalDate.of(2021, 2, 1)).getMaximum() == 28, "rangeRefinedBy Feb 2021 max 28");

        // getFrom / adjustInto
        check(dom.getFrom(LocalDate.of(2020, 1, 15)) == 15, "getFrom");
        check(dom.adjustInto(LocalDate.of(2020, 1, 1), 20).equals(LocalDate.of(2020, 1, 20)), "adjustInto");

        // display name
        check(dom.getDisplayName(Locale.US) != null, "getDisplayName non-null");

        // month-of-year field via the interface
        TemporalField moy = ChronoField.MONTH_OF_YEAR;
        check(moy.getBaseUnit() == ChronoUnit.MONTHS && moy.getRangeUnit() == ChronoUnit.YEARS, "MONTH_OF_YEAR units");
        check(moy.getFrom(LocalDate.of(2024, 7, 1)) == 7, "MONTH_OF_YEAR getFrom");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

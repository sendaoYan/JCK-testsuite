/*
 * @test
 * @summary java.time.temporal.ChronoField API conformance: field range and
 *          date/time classification.
 * @run main ChronoFieldTest
 */

import java.time.temporal.ChronoField;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class ChronoFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoField() {
        ChronoField dom = ChronoField.DAY_OF_MONTH;
        check(dom.range().getMinimum() == 1, "day-of-month min");
        check(dom.range().getMaximum() == 31, "day-of-month max");
        check(dom.isDateBased(), "day-of-month date-based");
        check(!dom.isTimeBased(), "day-of-month not time-based");

        ChronoField hod = ChronoField.HOUR_OF_DAY;
        check(hod.range().getMaximum() == 23, "hour-of-day max");
        check(hod.isTimeBased(), "hour-of-day time-based");
    }

    public static void main(String[] args) {
        testChronoField();
        testDeepChronoField();
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


    static void testDeepChronoField() {
        // exact range table (probe-locked both sides)
        String[][] tbl = {
            {"NANO_OF_SECOND","0 - 999999999"}, {"NANO_OF_DAY","0 - 86399999999999"},
            {"MICRO_OF_SECOND","0 - 999999"}, {"MICRO_OF_DAY","0 - 86399999999"},
            {"MILLI_OF_SECOND","0 - 999"}, {"MILLI_OF_DAY","0 - 86399999"},
            {"SECOND_OF_MINUTE","0 - 59"}, {"SECOND_OF_DAY","0 - 86399"},
            {"MINUTE_OF_HOUR","0 - 59"}, {"MINUTE_OF_DAY","0 - 1439"},
            {"HOUR_OF_AMPM","0 - 11"}, {"CLOCK_HOUR_OF_AMPM","1 - 12"},
            {"HOUR_OF_DAY","0 - 23"}, {"CLOCK_HOUR_OF_DAY","1 - 24"},
            {"AMPM_OF_DAY","0 - 1"}, {"DAY_OF_WEEK","1 - 7"},
            {"ALIGNED_DAY_OF_WEEK_IN_MONTH","1 - 7"}, {"ALIGNED_DAY_OF_WEEK_IN_YEAR","1 - 7"},
            {"DAY_OF_MONTH","1 - 28/31"}, {"DAY_OF_YEAR","1 - 365/366"},
            {"ALIGNED_WEEK_OF_MONTH","1 - 4/5"}, {"ALIGNED_WEEK_OF_YEAR","1 - 53"},
            {"MONTH_OF_YEAR","1 - 12"}, {"PROLEPTIC_MONTH","-11999999988 - 11999999999"},
            {"YEAR_OF_ERA","1 - 999999999/1000000000"}, {"YEAR","-999999999 - 999999999"},
            {"ERA","0 - 1"}, {"OFFSET_SECONDS","-64800 - 64800"},
        };
        for (String[] e : tbl)
            xeq(ChronoField.valueOf(e[0]).range().toString(), e[1], "range " + e[0].toLowerCase());
         xeq(ChronoField.EPOCH_DAY.range().toString(), "-365249999634 - 365249999634", "EpochDay range JDK8");
         xeq(ChronoField.INSTANT_SECONDS.range().toString(), "-9223372036854775808 - 9223372036854775807", "InstantSeconds range JDK8 full long");
        LocalDate d = LocalDate.of(2024, 2, 29);
        LocalTime t = LocalTime.of(10, 15, 30, 123);
        check(ChronoField.NANO_OF_SECOND.getFrom(t) == 123, "getFrom nano");
        check(ChronoField.DAY_OF_MONTH.getFrom(d) == 29, "getFrom DOM");
        check(ChronoField.YEAR.getFrom(d) == 2024, "getFrom YEAR");
        check(ChronoField.MONTH_OF_YEAR.getFrom(d) == 2, "getFrom MONTH");
        check(ChronoField.EPOCH_DAY.getFrom(d) == 19782L, "getFrom EPOCH_DAY");
        check(ChronoField.MINUTE_OF_DAY.getFrom(t) == 615, "getFrom MINUTE_OF_DAY");
        check(ChronoField.INSTANT_SECONDS.getFrom(Instant.ofEpochSecond(5)) == 5L, "getFrom INSTANT_SECONDS");
        check(ChronoField.DAY_OF_YEAR.isSupportedBy(d) && !ChronoField.HOUR_OF_DAY.isSupportedBy(d), "isSupportedBy LocalDate");
        check(ChronoField.NANO_OF_SECOND.isSupportedBy(t) && !ChronoField.DAY_OF_MONTH.isSupportedBy(t), "isSupportedBy LocalTime");
        xeq(ChronoField.DAY_OF_MONTH.rangeRefinedBy(d).toString(), "1 - 29", "rangeRefinedBy Feb leap");
        xeq(ChronoField.DAY_OF_MONTH.rangeRefinedBy(LocalDate.of(2024, 4, 2)).toString(), "1 - 30", "rangeRefinedBy Apr");
        xeq(ChronoField.DAY_OF_YEAR.rangeRefinedBy(LocalDate.of(2023, 6, 1)).toString(), "1 - 365", "rangeRefinedBy DOY non-leap");
        check(ChronoField.HOUR_OF_DAY.getBaseUnit() == ChronoUnit.HOURS, "HOUR base unit");
        check(ChronoField.DAY_OF_MONTH.getBaseUnit() == ChronoUnit.DAYS, "DOM base unit DAYS");
        check(ChronoField.MONTH_OF_YEAR.getBaseUnit() == ChronoUnit.MONTHS, "MONTH base unit");
        check(ChronoField.ALIGNED_WEEK_OF_YEAR.getRangeUnit() == ChronoUnit.YEARS, "AWOY range unit");
        check(ChronoField.YEAR.getRangeUnit() == ChronoUnit.FOREVER, "YEAR range unit FOREVER");
        check(ChronoField.NANO_OF_SECOND.isTimeBased() && !ChronoField.NANO_OF_SECOND.isDateBased(), "timeBased");
        check(ChronoField.DAY_OF_WEEK.isDateBased() && !ChronoField.DAY_OF_WEEK.isTimeBased(), "dateBased");
        check(!ChronoField.INSTANT_SECONDS.isDateBased() && !ChronoField.INSTANT_SECONDS.isTimeBased(), "instant neither");
        check(ChronoField.YEAR.checkValidIntValue(2024) == 2024, "checkValidIntValue ok");
        xact(() -> ChronoField.DAY_OF_MONTH.checkValidValue(32L), DateTimeException.class,
            "Invalid value for DayOfMonth (valid values 1 - 28/31): 32", "checkValidValue 32");
        xact(() -> ChronoField.DAY_OF_WEEK.checkValidValue(8L), DateTimeException.class,
            "Invalid value for DayOfWeek (valid values 1 - 7): 8", "checkValidValue DOW 8");
        xact(() -> ChronoField.NANO_OF_SECOND.checkValidIntValue(1234567890), DateTimeException.class,
            "Invalid value for NanoOfSecond (valid values 0 - 999999999): 1234567890", "checkValidIntValue nano");
        check(ChronoField.valueOf("DAY_OF_MONTH") == ChronoField.DAY_OF_MONTH, "valueOf");
        xact(() -> ChronoField.valueOf("BOGUS"), IllegalArgumentException.class, null, "valueOf bogus");
        check(ChronoField.values().length == 30, "30 chrono fields");
        xeq(ChronoField.values()[0].toString(), "NanoOfSecond", "ordinal order starts NanoOfSecond");
    }

}

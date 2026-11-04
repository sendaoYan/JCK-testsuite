/*
 * @test
 * @summary java.time.MonthDay API conformance: of/getters, isValidYear, and atYear.
 * @run main MonthDayTest
 */

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.DateTimeException;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQueries;

public class MonthDayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMonthDay() {
        MonthDay md = MonthDay.of(Month.FEBRUARY, 29);
        check(md.getMonth() == Month.FEBRUARY && md.getDayOfMonth() == 29, "of/getters");
        check(md.isValidYear(2024) && !md.isValidYear(2023), "isValidYear");
        check(md.atYear(2024).equals(LocalDate.of(2024, 2, 29)), "atYear");
    }

    public static void main(String[] args) {
        testMonthDay();
        testDeepMonthDay();
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


    static void testDeepMonthDay() {
        MonthDay md = MonthDay.of(2, 29);
        xeq(md.toString(), "--02-29", "toString");
        check(md.getMonthValue() == 2 && md.getDayOfMonth() == 29, "getters");
        check(md.getMonth() == Month.FEBRUARY, "getMonth");
        check(!md.isValidYear(2023) && md.isValidYear(2024), "isValidYear");
        check(MonthDay.of(2, 28).isValidYear(2023), "02-28 valid any year");
        xeq(md.atYear(2024).toString(), "2024-02-29", "atYear");
        xeq(MonthDay.of(2, 29).atYear(2023).toString(), "2023-02-28", "atYear clamps");
        xeq(MonthDay.of(1, 31).atYear(2024).toString(), "2024-01-31", "atYear Jan31");
        xeq(md.with(Month.MARCH).toString(), "--03-29", "with(Month)");
        xeq(md.withDayOfMonth(28).toString(), "--02-28", "withDayOfMonth");
        xact(() -> md.withDayOfMonth(30), DateTimeException.class,
            "Illegal value for DayOfMonth field, value 30 is not valid for month FEBRUARY", "withDayOfMonth 30 Feb");
        xact(() -> MonthDay.of(4, 31), DateTimeException.class,
            "Illegal value for DayOfMonth field, value 31 is not valid for month APRIL", "of Apr 31");
        xact(() -> MonthDay.of(13, 1), DateTimeException.class, "Invalid value for MonthOfYear: 13", "of month 13");
        xact(() -> MonthDay.of(0, 1), DateTimeException.class,
            "Invalid value for MonthOfYear: 0", "of month 0");
        check(MonthDay.parse("--02-29").equals(md), "parse");
        check(MonthDay.parse("--12-31").toString().equals("--12-31"), "parse Dec31");
        xact(() -> MonthDay.parse("02-29"), DateTimeParseException.class, null, "parse no dashes");
        xact(() -> MonthDay.parse("--02-30"), DateTimeParseException.class, null, "parse invalid day");
        check(MonthDay.from(LocalDate.of(2024, 2, 29)).equals(md), "from");
        check(md.compareTo(MonthDay.of(3, 1)) < 0, "compareTo");
        check(md.isAfter(MonthDay.of(2, 28)) && md.isBefore(MonthDay.of(3, 1)), "isAfter/isBefore");
        check(md.format(DateTimeFormatter.ofPattern("--MM-dd")).equals("--02-29"), "format");
        check(md.query(TemporalQueries.precision()) == null, "precision null");
        check(md.query(TemporalQueries.chronology()) == IsoChronology.INSTANCE, "chronology ISO");
        xeq(md.adjustInto(LocalDate.of(2024, 2, 15)).toString(), "2024-02-29", "adjustInto");
        xeq(md.adjustInto(LocalDate.of(2023, 2, 15)).toString(), "2023-02-28", "adjustInto clamps");
        check(md.equals(MonthDay.of(2, 29)), "equals");
        check(md.hashCode() == MonthDay.parse("--02-29").hashCode(), "hash");
        check(MonthDay.of(2, 29).isSupported(ChronoField.DAY_OF_MONTH), "support DOM");
        check(!MonthDay.of(2, 29).isSupported(ChronoField.YEAR), "no YEAR");
        check(MonthDay.of(2, 29).get(ChronoField.MONTH_OF_YEAR) == 2, "get month");
    }

}

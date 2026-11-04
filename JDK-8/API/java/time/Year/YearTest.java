/*
 * @test
 * @summary java.time.Year API conformance: getValue, isLeap, length, and atDay.
 * @run main YearTest
 */

import java.time.LocalDate;
import java.time.Year;
import java.time.DateTimeException;
import java.time.Month;
import java.time.MonthDay;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class YearTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testYear() {
        Year y = Year.of(2024);
        check(y.getValue() == 2024, "getValue");
        check(y.isLeap() && !Year.of(2023).isLeap(), "isLeap");
        check(y.length() == 366 && Year.of(2023).length() == 365, "length");
        check(y.atDay(60).equals(LocalDate.of(2024, 2, 29)), "atDay");
    }

    public static void main(String[] args) {
        testYear();
        testDeepYear();
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


    static void testDeepYear() {
        Year y = Year.of(2024);
        check(y.getValue() == 2024, "getValue");
        check(y.isLeap() && Year.of(2023).isLeap() == false, "isLeap");
        check(Year.isLeap(0) && !Year.isLeap(100) && Year.isLeap(400), "static isLeap");
        check(y.length() == 366 && Year.of(2023).length() == 365, "length");
        check(y.isValidMonthDay(MonthDay.of(2, 29)) && !Year.of(2023).isValidMonthDay(MonthDay.of(2, 29)), "isValidMonthDay");
        xeq(y.atMonth(2).toString(), "2024-02", "atMonth int");
        xeq(y.atMonth(Month.FEBRUARY).toString(), "2024-02", "atMonth enum");
        xeq(y.atMonthDay(MonthDay.of(2, 29)).toString(), "2024-02-29", "atMonthDay");
        xeq(y.atDay(1).toString(), "2024-01-01", "atDay 1");
        xeq(y.atDay(366).toString(), "2024-12-31", "atDay 366 leap");
        xact(() -> y.atDay(367), DateTimeException.class,
            "Invalid value for DayOfYear (valid values 1 - 365/366): 367", "atDay 367");
        xact(() -> Year.of(2023).atDay(366), DateTimeException.class,
            "Invalid date 'DayOfYear 366' as '2023' is not a leap year", "atDay 366 non-leap");
        xeq(y.plusYears(1).toString(), "2025", "plusYears");
        xeq(y.minusYears(24).toString(), "2000", "minusYears");
        xeq(y.plus(1, ChronoUnit.DECADES).toString(), "2034", "plus DECADES");
        xeq(y.minus(1, ChronoUnit.CENTURIES).toString(), "1924", "minus CENTURIES");
        xeq(y.plus(1, ChronoUnit.MILLENNIA).toString(), "3024", "plus MILLENNIA");
        check(y.until(Year.of(2026), ChronoUnit.YEARS) == 2, "until YEARS");
        check(y.until(Year.of(2024), ChronoUnit.YEARS) == 0, "until self 0");
        xact(() -> y.until(Year.of(2026), ChronoUnit.MONTHS), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Months", "until MONTHS");
        xact(() -> y.plus(1, ChronoUnit.DAYS), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Days", "plus DAYS");
        check(y.isSupported(ChronoUnit.YEARS), "support YEARS");
        check(!y.isSupported(ChronoUnit.DAYS), "no DAYS");
        check(y.query(TemporalQueries.precision()) == ChronoUnit.YEARS, "precision Years");
        check(y.query(TemporalQueries.chronology()) == IsoChronology.INSTANCE, "chronology ISO");
        xact(() -> Year.of(Year.MAX_VALUE + 1), DateTimeException.class,
            "Invalid value for Year (valid values -999999999 - 999999999): 1000000000", "of overflow");
        xeq(Year.of(Year.MIN_VALUE).toString(), "-999999999", "MIN toString");
        xeq(Year.of(Year.MAX_VALUE).toString(), "999999999", "MAX toString no sign");
        check(Year.parse("2024").equals(y), "parse");
        check(Year.parse("0044").getValue() == 44, "parse padded");
        xact(() -> Year.parse("abc"), DateTimeParseException.class,
            "Text 'abc' could not be parsed at index 0", "parse bad");
         xact(() -> Year.parse("+2024"), DateTimeParseException.class,
             "Text '+2024' could not be parsed at index 0", "parse plus sign JDK8");
        check(Year.parse("-0044").getValue() == -44, "parse negative");
        check(y.format(DateTimeFormatter.ofPattern("yyyy")).equals("2024"), "format");
        check(Year.from(LocalDate.of(2024, 6, 1)).equals(y), "from");
        check(y.compareTo(Year.of(2023)) > 0, "compareTo");
        check(y.equals(Year.of(2024)) && y.hashCode() == Year.of(2024).hashCode(), "equals/hash");
        xeq(y.adjustInto(LocalDate.of(2023, 6, 1)).toString(), "2024-06-01", "adjustInto");
        xeq(y.isBefore(Year.of(2025)) + "/" + y.isAfter(Year.of(2023)), "true/true", "isBefore/isAfter");
        xeq(y.toString(), "2024", "toString");
    }

}

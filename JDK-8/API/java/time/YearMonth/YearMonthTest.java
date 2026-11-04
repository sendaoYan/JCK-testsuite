/*
 * @test
 * @summary java.time.YearMonth API conformance: of/getters, lengthOfMonth, isLeapYear, and
 *          atDay.
 * @run main YearMonthTest
 */

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.DateTimeException;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class YearMonthTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testYearMonth() {
        YearMonth ym = YearMonth.of(2024, 2);
        check(ym.getYear() == 2024 && ym.getMonth() == Month.FEBRUARY, "of/getters");
        check(ym.lengthOfMonth() == 29, "lengthOfMonth leap");
        check(ym.isLeapYear(), "isLeapYear");
        check(YearMonth.of(2023, 2).lengthOfMonth() == 28, "lengthOfMonth non-leap");
        check(ym.atDay(15).equals(LocalDate.of(2024, 2, 15)), "atDay");
    }

    public static void main(String[] args) {
        testYearMonth();
        testDeepYearMonth();
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


    static void testDeepYearMonth() {
        YearMonth ym = YearMonth.of(2024, 2);
        check(ym.getYear() == 2024 && ym.getMonthValue() == 2, "getters");
        check(ym.getMonth() == Month.FEBRUARY, "getMonth");
        check(ym.isLeapYear(), "isLeapYear");
        check(ym.lengthOfYear() == 366 && ym.lengthOfMonth() == 29, "lengths leap");
        check(YearMonth.of(2023, 2).lengthOfMonth() == 28, "lengthOfMonth non-leap");
        check(YearMonth.of(2024, 4).lengthOfMonth() == 30, "April 30");
        xeq(ym.plusMonths(13).toString(), "2025-03", "plusMonths 13");
        xeq(ym.minusMonths(1).toString(), "2024-01", "minusMonths");
        xeq(ym.plusYears(1).toString(), "2025-02", "plusYears");
        xeq(ym.minusYears(24).toString(), "2000-02", "minusYears");
        xeq(ym.withYear(2025).toString(), "2025-02", "withYear");
        xeq(ym.withMonth(3).toString(), "2024-03", "withMonth");
        xeq(YearMonth.of(2024, 1).plusMonths(13).toString(), "2025-02", "year rollover plus");
        xeq(YearMonth.of(2024, 1).minusMonths(2).toString(), "2023-11", "year rollover minus");
        xeq(ym.atDay(29).toString(), "2024-02-29", "atDay leap");
        xeq(YearMonth.of(2024, 4).atEndOfMonth().toString(), "2024-04-30", "atEndOfMonth");
        xeq(YearMonth.of(2023, 2).atEndOfMonth().toString(), "2023-02-28", "atEndOfMonth non-leap");
        xeq(ym.atEndOfMonth().toString(), "2024-02-29", "atEndOfMonth leap");
        xeq(ym.atDay(1).toString(), "2024-02-01", "atDay 1");
        xact(() -> ym.atDay(30), DateTimeException.class, "Invalid date 'FEBRUARY 30'", "atDay 30 Feb");
        xact(() -> YearMonth.of(2024, 4).atDay(31), DateTimeException.class, "Invalid date 'APRIL 31'", "atDay 31 Apr");
        check(ym.isValidDay(29) && !YearMonth.of(2023, 2).isValidDay(29), "isValidDay");
        check(ym.until(YearMonth.of(2026, 3), ChronoUnit.MONTHS) == 25, "until MONTHS");
        check(ym.until(YearMonth.of(2026, 3), ChronoUnit.YEARS) == 2, "until YEARS trunc");
        xact(() -> ym.plus(1, ChronoUnit.DAYS), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Days", "plus DAYS");
        check(ym.query(TemporalQueries.precision()) == ChronoUnit.MONTHS, "precision Months");
        check(ym.isSupported(ChronoUnit.MONTHS) && !ym.isSupported(ChronoUnit.DAYS), "support units");
        check(ym.isSupported(ChronoField.YEAR) && ym.isSupported(ChronoField.MONTH_OF_YEAR), "support fields");
        xact(() -> YearMonth.of(2024, 13), DateTimeException.class,
            "Invalid value for MonthOfYear (valid values 1 - 12): 13", "of month 13");
        xact(() -> YearMonth.of(2024, 2).withMonth(13), DateTimeException.class,
            "Invalid value for MonthOfYear (valid values 1 - 12): 13", "withMonth 13");
        check(YearMonth.parse("2024-02").equals(ym), "parse");
        check(YearMonth.from(LocalDate.of(2024, 2, 29)).equals(ym), "from");
        xeq(YearMonth.from(Year.of(2024).atMonth(2)).toString(), "2024-02", "from YearMonth");
        check(ym.compareTo(YearMonth.of(2024, 3)) < 0, "compareTo");
        check(ym.isBefore(YearMonth.of(2024, 3)) && ym.isAfter(YearMonth.of(2024, 1)), "isBefore/isAfter");
        check(ym.format(DateTimeFormatter.ofPattern("yyyy/MM")).equals("2024/02"), "format");
        xeq(ym.adjustInto(LocalDate.of(2024, 5, 10)).toString(), "2024-02-10", "adjustInto");
    }

}

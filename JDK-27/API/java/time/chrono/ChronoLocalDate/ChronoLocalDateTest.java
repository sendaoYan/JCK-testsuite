/*
 * @test
 * @summary java.time.chrono.ChronoLocalDate conformance: white-box structure + black-box behavior.
 * @run main ChronoLocalDateTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChronoLocalDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.ChronoLocalDate");
        check(c.isInterface(), "ChronoLocalDate is an interface");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "extends Temporal");
        check(TemporalAdjuster.class.isAssignableFrom(c), "extends TemporalAdjuster");
        check(Comparable.class.isAssignableFrom(c), "extends Comparable");
        for (String m : new String[]{"timeLineOrder", "from", "getChronology", "getEra", "isLeapYear",
                "lengthOfMonth", "lengthOfYear", "isSupported", "with", "plus", "minus", "query",
                "adjustInto", "until", "format", "atTime", "toEpochDay", "compareTo", "isAfter",
                "isBefore", "isEqual"}) {
            check(hasMethod(c, m), "ChronoLocalDate has method " + m);
        }
    }

    static void testBlackBox() {
        ChronoLocalDate d = LocalDate.of(2020, 1, 1);
        check(d.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(d.getEra() == IsoEra.CE, "getEra CE");
        check(LocalDate.of(0, 1, 1).getEra() == IsoEra.BCE, "year 0 is BCE");

        // Field access via Temporal interface.
        check(d.isSupported(ChronoField.DAY_OF_MONTH), "isSupported DAY_OF_MONTH");
        check(!d.isSupported(ChronoField.HOUR_OF_DAY), "!isSupported HOUR_OF_DAY");
        check(d.get(ChronoField.DAY_OF_MONTH) == 1, "get DAY_OF_MONTH");
        check(d.get(ChronoField.MONTH_OF_YEAR) == 1, "get MONTH_OF_YEAR");
        check(d.getLong(ChronoField.YEAR) == 2020L, "getLong YEAR");
        check(d.getLong(ChronoField.EPOCH_DAY) == 18262L, "getLong EPOCH_DAY");

        // Length / leap queries.
        check(d.lengthOfMonth() == 31, "lengthOfMonth Jan");
        check(d.lengthOfYear() == 366, "lengthOfYear leap");
        check(LocalDate.of(2019, 2, 1).lengthOfMonth() == 28, "lengthOfMonth Feb non-leap");
        check(d.isLeapYear(), "2020 isLeapYear");
        check(!LocalDate.of(1900, 1, 1).isLeapYear(), "1900 !isLeapYear");
        check(LocalDate.of(2000, 1, 1).isLeapYear(), "2000 isLeapYear");

        // toEpochDay round-trip.
        check(d.toEpochDay() == 18262L, "toEpochDay 2020-01-01");
        check(LocalDate.of(1970, 1, 1).toEpochDay() == 0L, "toEpochDay epoch");
        check(LocalDate.ofEpochDay(18262L).equals(d), "ofEpochDay round-trip");

        // atTime / plus / minus / with.
        check(d.atTime(LocalTime.NOON).toLocalDate().equals(d), "atTime keeps date");
        check(d.plus(1, ChronoUnit.DAYS).equals(LocalDate.of(2020, 1, 2)), "plus day");
        check(d.plus(1, ChronoUnit.MONTHS).equals(LocalDate.of(2020, 2, 1)), "plus month");
        check(d.minus(1, ChronoUnit.DAYS).equals(LocalDate.of(2019, 12, 31)), "minus day crosses year");
        check(d.with(ChronoField.YEAR, 2021).equals(LocalDate.of(2021, 1, 1)), "with YEAR");
        check(d.with(ChronoField.DAY_OF_YEAR, 60).equals(LocalDate.of(2020, 2, 29)), "with DAY_OF_YEAR leap");

        // until (long) and until (ChronoPeriod).
        check(d.until(LocalDate.of(2020, 1, 5), ChronoUnit.DAYS) == 4, "until DAYS");
        check(d.until(LocalDate.of(2021, 1, 1), ChronoUnit.DAYS) == 366L, "until DAYS across leap year");
        ChronoPeriod period = d.until(LocalDate.of(2021, 2, 3));
        check(period.get(ChronoUnit.YEARS) == 1 && period.get(ChronoUnit.MONTHS) == 1
              && period.get(ChronoUnit.DAYS) == 2, "until ChronoPeriod 1y1m2d");

        // Ordering.
        check(d.isBefore(LocalDate.of(2020, 1, 2)), "isBefore");
        check(d.isAfter(LocalDate.of(2019, 12, 31)), "isAfter");
        check(d.isEqual(LocalDate.of(2020, 1, 1)), "isEqual");
        check(d.compareTo(LocalDate.of(2020, 1, 2)) < 0, "compareTo < 0");

        // timeLineOrder comparator sorts chronologically.
        List<ChronoLocalDate> dates = new ArrayList<ChronoLocalDate>();
        dates.add(LocalDate.of(2020, 2, 1));
        dates.add(LocalDate.of(2020, 1, 1));
        dates.add(LocalDate.of(2019, 12, 31));
        Collections.sort(dates, ChronoLocalDate.timeLineOrder());
        check(dates.get(0).equals(LocalDate.of(2019, 12, 31))
              && dates.get(1).equals(LocalDate.of(2020, 1, 1))
              && dates.get(2).equals(LocalDate.of(2020, 2, 1)), "timeLineOrder sort");

        // format.
        check(d.format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2020-01-01"), "format ISO_LOCAL_DATE");

        // query.
        check(d.query(java.time.temporal.TemporalQueries.localDate()).equals(d), "query localDate");

        // Exception path: setting an unsupported field.
        expectThrows(java.time.temporal.UnsupportedTemporalTypeException.class,
              () -> d.with(ChronoField.HOUR_OF_DAY, 5),
              "with HOUR_OF_DAY throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.temporal.TemporalQueries;

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
        testDeepChronoLocalDate();
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


    static void testDeepChronoLocalDate() {
        ChronoLocalDate jp = JapaneseDate.of(2024, 6, 15);
        ChronoLocalDate mg = MinguoDate.of(113, 6, 15);
        ChronoLocalDate iso = java.time.LocalDate.of(2024, 6, 15);
        xeq(jp.compareTo(mg) + "", "-3", "cross-chrono compareTo by id");
        xeq(jp.compareTo(iso) + "", "1", "jp vs iso-1d compareTo");
        check(ChronoLocalDate.timeLineOrder().compare(jp, iso) == 0, "timeLine same instant equal");
        check(ChronoLocalDate.timeLineOrder().compare(jp, java.time.LocalDate.of(2024, 6, 14)) > 0, "timeLine later");
        check(ChronoLocalDate.timeLineOrder().compare(mg, java.time.LocalDate.of(2024, 6, 16)) < 0, "timeLine earlier");
        check(jp.isAfter(java.time.LocalDate.of(2024, 6, 14)), "jp isAfter iso-1d");
        check(jp.isBefore(java.time.LocalDate.of(2024, 6, 16)), "jp isBefore iso+1d");
        check(jp.isEqual(iso), "jp isEqual same timeline");
        check(!jp.equals(iso), "jp not equals iso");
        check(ChronoLocalDate.from(java.time.LocalDate.of(2024, 6, 15)).equals(iso), "from LocalDate");
        check(ChronoLocalDate.from(java.time.LocalDateTime.of(2024, 6, 15, 10, 0)).equals(iso), "from LocalDateTime");
        check(ChronoLocalDate.from(java.time.ZonedDateTime.of(2024, 6, 15, 10, 0, 0, 0, java.time.ZoneOffset.UTC)).equals(iso),
            "from ZonedDateTime");
        xact(() -> ChronoLocalDate.from(null), NullPointerException.class, "temporal", "from null");
        check(jp.toEpochDay() == 19889L, "jp epochDay");
        check(mg.toEpochDay() == 19889L, "mg epochDay same");
        check(jp.format(java.time.format.DateTimeFormatter.ofPattern("G y-MM-d", java.util.Locale.ENGLISH))
            .equals("Reiwa 6-06-15"), "jp format era pattern");
        check(jp.query(java.time.temporal.TemporalQueries.localDate()).equals(java.time.LocalDate.of(2024, 6, 15)),
            "jp query localDate");
        check(jp.getChronology().equals(JapaneseChronology.INSTANCE), "jp chronology");
    }

}

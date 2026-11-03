/*
 * @test
 * @summary java.time.chrono.Chronology conformance: white-box structure + black-box behavior.
 * @run main ChronologyTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChronologyTest {
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
        Class<?> c = Class.forName("java.time.chrono.Chronology");
        check(c.isInterface(), "Chronology is an interface");
        check(Comparable.class.isAssignableFrom(c), "extends Comparable");
        for (String m : new String[]{"from", "ofLocale", "of", "getAvailableChronologies", "getId",
                "getCalendarType", "date", "dateYearDay", "dateEpochDay", "dateNow", "localDateTime",
                "zonedDateTime", "isLeapYear", "prolepticYear", "eraOf", "eras", "range",
                "getDisplayName", "resolveDate", "period", "epochSecond", "isIsoBased", "compareTo"}) {
            check(hasMethod(c, m), "Chronology has method " + m);
        }
    }

    static void testBlackBox() {
        Chronology c = IsoChronology.INSTANCE;
        check("ISO".equals(c.getId()), "getId");
        check("iso8601".equals(c.getCalendarType()), "getCalendarType");
        check(c.isLeapYear(2020), "isLeapYear 2020");
        check(!c.isLeapYear(2019), "isLeapYear 2019 false");
        check(!c.isLeapYear(1900), "isLeapYear 1900 false");
        check(c.isLeapYear(2000), "isLeapYear 2000 true");

        // Era conversion.
        check(c.prolepticYear(IsoEra.CE, 2020) == 2020, "prolepticYear CE");
        check(c.prolepticYear(IsoEra.BCE, 1) == 0, "prolepticYear BCE");
        check(c.eraOf(1) == IsoEra.CE, "eraOf 1 CE");
        check(c.eraOf(0) == IsoEra.BCE, "eraOf 0 BCE");
        check(c.eras().size() == 2, "two eras");
        check(c.eras().contains(IsoEra.CE) && c.eras().contains(IsoEra.BCE), "eras contain CE/BCE");

        // Date factories.
        check(c.date(2020, 1, 1).equals(LocalDate.of(2020, 1, 1)), "date");
        check(c.dateYearDay(2020, 60).equals(LocalDate.of(2020, 2, 29)), "dateYearDay 60 -> Feb 29");
        check(c.dateEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "dateEpochDay 0");
        check(c.dateEpochDay(18262).equals(LocalDate.of(2020, 1, 1)), "dateEpochDay 18262");
        check(c.dateNow() != null, "dateNow non-null");

        // Display name (stable English).
        check("ISO".equals(c.getDisplayName(TextStyle.FULL, Locale.ENGLISH)), "getDisplayName FULL ISO");
        check("ISO".equals(c.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)), "getDisplayName SHORT ISO");

        // period / epochSecond / isIsoBased.
        check(c.period(1, 2, 3).get(java.time.temporal.ChronoUnit.YEARS) == 1, "period years");
        check(c.epochSecond(1970, 1, 1, 0, 0, 0, ZoneOffset.UTC) == 0L, "epochSecond epoch");
        check(c.isIsoBased(), "isIsoBased true");

        // compareTo.
        check(c.compareTo(IsoChronology.INSTANCE) == 0, "compareTo self");
        check(c.compareTo(Chronology.of("ThaiBuddhist")) < 0, "compareTo ThaiBuddhist < 0");

        // Static factories.
        check(Chronology.of("ISO") == IsoChronology.INSTANCE, "Chronology.of ISO == INSTANCE");
        check(Chronology.from(LocalDate.of(2020, 1, 1)) == IsoChronology.INSTANCE, "Chronology.from LocalDate");
        check(Chronology.ofLocale(Locale.US) == IsoChronology.INSTANCE, "Chronology.ofLocale US == ISO");

        // getAvailableChronologies: sorted ids must contain the known calendars.
        Set<Chronology> avail = Chronology.getAvailableChronologies();
        List<String> ids = new ArrayList<String>();
        for (Chronology ch : avail) {
            ids.add(ch.getId());
        }
        Collections.sort(ids);
        check(ids.contains("ISO"), "available contains ISO");
        check(ids.contains("Japanese"), "available contains Japanese");
        check(ids.contains("Minguo"), "available contains Minguo");
        check(ids.contains("ThaiBuddhist"), "available contains ThaiBuddhist");
        check(ids.contains("Hijrah-umalqura"), "available contains Hijrah-umalqura");

        // localDateTime / zonedDateTime conversion round-trip.
        check(c.localDateTime(LocalDateTime.of(2020, 1, 1, 12, 0))
                .equals(LocalDateTime.of(2020, 1, 1, 12, 0)), "localDateTime round-trip");
        check(c.zonedDateTime(java.time.ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, java.time.ZoneId.of("UTC")))
                .toInstant().equals(java.time.Instant.parse("2020-01-01T12:00:00Z")), "zonedDateTime round-trip");

        // Exception paths.
        expectThrows(java.time.DateTimeException.class,
              () -> Chronology.of("NoSuchChronology"), "Chronology.of unknown throws");
        expectThrows(java.time.DateTimeException.class,
              () -> c.date(java.time.LocalTime.NOON), "date(TemporalAccessor) LocalTime throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

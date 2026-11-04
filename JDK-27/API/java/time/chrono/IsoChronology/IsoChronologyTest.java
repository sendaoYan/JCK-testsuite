/*
 * @test
 * @summary java.time.chrono.IsoChronology conformance: white-box structure + black-box behavior.
 * @run main IsoChronologyTest
 */

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.AbstractChronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.util.Map;
import java.time.DateTimeException;
import java.time.chrono.Chronology;

public class IsoChronologyTest {
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
        Class<?> c = Class.forName("java.time.chrono.IsoChronology");
        check(Modifier.isFinal(c.getModifiers()), "IsoChronology is final");
        check(!c.isInterface(), "IsoChronology is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "IsoChronology is not abstract");
        check(c.getSuperclass() == AbstractChronology.class, "extends AbstractChronology");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");

        Field inst = c.getField("INSTANCE");
        check(Modifier.isPublic(inst.getModifiers()) && Modifier.isStatic(inst.getModifiers())
              && Modifier.isFinal(inst.getModifiers()), "INSTANCE is public static final");
        check(inst.get(null) == IsoChronology.INSTANCE, "INSTANCE field == INSTANCE");

        for (String m : new String[]{"getId", "getCalendarType", "date", "dateYearDay", "dateEpochDay",
                "epochSecond", "localDateTime", "zonedDateTime", "dateNow", "isLeapYear", "prolepticYear",
                "eraOf", "eras", "resolveDate", "range", "period", "isIsoBased"}) {
            check(hasMethod(c, m), "IsoChronology has method " + m);
        }
    }

    static void testBlackBox() {
        IsoChronology iso = IsoChronology.INSTANCE;
        check(iso == IsoChronology.INSTANCE, "INSTANCE singleton");
        check("ISO".equals(iso.getId()), "id");
        check("iso8601".equals(iso.getCalendarType()), "calendarType");

        // Leap years (proleptic Gregorian rules).
        check(iso.isLeapYear(2020), "2020 leap");
        check(!iso.isLeapYear(2019), "2019 not leap");
        check(!iso.isLeapYear(1900), "1900 not leap");
        check(iso.isLeapYear(2000), "2000 leap");
        check(iso.isLeapYear(0), "year 0 leap");

        // Era conversion.
        check(iso.prolepticYear(IsoEra.CE, 2020) == 2020, "prolepticYear CE");
        check(iso.prolepticYear(IsoEra.BCE, 1) == 0, "prolepticYear BCE");
        check(iso.eraOf(1) == IsoEra.CE, "eraOf CE");
        check(iso.eraOf(0) == IsoEra.BCE, "eraOf BCE");
        check(iso.eras().size() == 2, "two eras");
        check(iso.eras().contains(IsoEra.CE) && iso.eras().contains(IsoEra.BCE), "eras contain CE/BCE");

        // Date factories.
        check(iso.date(2020, 1, 1).equals(LocalDate.of(2020, 1, 1)), "date");
        check(iso.date(IsoEra.BCE, 1, 1, 1).equals(LocalDate.of(0, 1, 1)), "date BCE year 0");
        check(iso.dateYearDay(2020, 60).equals(LocalDate.of(2020, 2, 29)), "dateYearDay leap day");
        check(iso.dateYearDay(IsoEra.CE, 2020, 60).equals(LocalDate.of(2020, 2, 29)), "dateYearDay Era");
        check(iso.dateEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "dateEpochDay 0");
        check(iso.dateEpochDay(18262).equals(LocalDate.of(2020, 1, 1)), "dateEpochDay 18262");
        check(iso.date(LocalDate.of(2020, 1, 1)).equals(LocalDate.of(2020, 1, 1)), "date(TemporalAccessor)");

        // period / epochSecond / isIsoBased / range.
        check(iso.period(1, 2, 3).equals(Period.of(1, 2, 3)), "period");
        check(iso.epochSecond(1970, 1, 1, 0, 0, 0, ZoneOffset.UTC) == 0L, "epochSecond epoch");
        check(iso.isIsoBased(), "isIsoBased");
        check(iso.range(ChronoField.YEAR).getMinimum() == -999999999, "range YEAR min");
        check(iso.range(ChronoField.YEAR).getMaximum() == 999999999, "range YEAR max");

        // localDateTime / zonedDateTime conversion.
        check(iso.localDateTime(LocalDateTime.of(2020, 1, 1, 12, 0))
                .equals(LocalDateTime.of(2020, 1, 1, 12, 0)), "localDateTime round-trip");
        ZonedDateTime zdt = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
        check(iso.zonedDateTime(zdt).equals(zdt), "zonedDateTime round-trip");
        check(iso.zonedDateTime(Instant.parse("2020-01-01T12:00:00Z"), ZoneId.of("UTC"))
                .toInstant().equals(Instant.parse("2020-01-01T12:00:00Z")), "zonedDateTime Instant");

        // resolveDate STRICT.
        Map<TemporalField, Long> map = new HashMap<TemporalField, Long>();
        map.put(ChronoField.YEAR, 2020L);
        map.put(ChronoField.MONTH_OF_YEAR, 1L);
        map.put(ChronoField.DAY_OF_MONTH, 1L);
        check(iso.resolveDate(map, ResolverStyle.STRICT).equals(LocalDate.of(2020, 1, 1)),
              "resolveDate STRICT");

        // dateNow non-null (no exact value).
        check(iso.dateNow() != null, "dateNow non-null");

        // Exception paths.
        expectThrows(java.time.DateTimeException.class,
              () -> iso.eraOf(2), "eraOf 2 throws DateTimeException");
        Map<TemporalField, Long> invalid = new HashMap<TemporalField, Long>();
        invalid.put(ChronoField.YEAR, 2020L);
        invalid.put(ChronoField.MONTH_OF_YEAR, 2L);
        invalid.put(ChronoField.DAY_OF_MONTH, 30L);
        expectThrows(java.time.DateTimeException.class,
              () -> iso.resolveDate(invalid, ResolverStyle.STRICT),
              "resolveDate invalid Feb 30 throws DateTimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepIsoChronology();
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


    static void testDeepIsoChronology() {
        check(IsoChronology.INSTANCE.equals(Chronology.of("ISO")), "of ISO equals");
        xeq(IsoChronology.INSTANCE.getId() + "|" + IsoChronology.INSTANCE.getCalendarType(), "ISO|iso8601", "ids");
        check(IsoChronology.INSTANCE.isLeapYear(2024), "2024 leap");
        check(!IsoChronology.INSTANCE.isLeapYear(2023), "2023 not leap");
        check(IsoChronology.INSTANCE.isLeapYear(2000), "2000 leap");
        check(!IsoChronology.INSTANCE.isLeapYear(1900), "1900 not leap");
        check(IsoChronology.INSTANCE.dateYearDay(2024, 167).toString().equals("2024-06-15"), "dateYearDay");
        check(IsoChronology.INSTANCE.dateEpochDay(19889L).toString().equals("2024-06-15"), "dateEpochDay");
        check(IsoChronology.INSTANCE.date(java.time.LocalDate.of(2024, 6, 15)).toString().equals("2024-06-15"), "date");
        check(IsoChronology.INSTANCE.localDateTime(java.time.LocalDateTime.of(2024, 6, 15, 10, 0)).toString()
            .equals("2024-06-15T10:00"), "localDateTime factory");
        check(IsoChronology.INSTANCE.zonedDateTime(java.time.Instant.parse("2024-06-15T10:15:30Z"), java.time.ZoneOffset.UTC)
            .toString().equals("2024-06-15T10:15:30Z"), "zonedDateTime factory");
        xact(() -> IsoChronology.INSTANCE.localDateTime(null), NullPointerException.class, "temporal", "localDateTime null");
        xeq(IsoChronology.INSTANCE.period(1, 2, 3).toString(), "P1Y2M3D", "period toString");
        xeq(IsoChronology.INSTANCE.eraOf(0).toString(), "BCE", "eraOf BCE");
        xeq(IsoChronology.INSTANCE.eraOf(1).toString(), "CE", "eraOf CE");
        xact(() -> IsoChronology.INSTANCE.eraOf(5), java.time.DateTimeException.class, "Invalid era: 5", "eraOf 5");
        check(IsoChronology.INSTANCE.eras().size() == 2, "two eras");
    }

}

/*
 * @test
 * @summary java.time.chrono.IsoChronology conformance: id/calendar/leap/era.
 * @run main IsoChronologyTest
 */

import java.time.LocalDate;
import java.time.Period;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;

public class IsoChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIsoChronology() {
        IsoChronology iso = IsoChronology.INSTANCE;
        check("ISO".equals(iso.getId()), "id");
        check("iso8601".equals(iso.getCalendarType()), "calendarType");
        check(iso.isLeapYear(2020), "2020 leap");
        check(!iso.isLeapYear(2019), "2019 not leap");
        check(iso.prolepticYear(IsoEra.CE, 2020) == 2020, "prolepticYear CE");
        check(iso.prolepticYear(IsoEra.BCE, 1) == 0, "prolepticYear BCE");
        check(iso.eraOf(1) == IsoEra.CE, "eraOf CE");
        check(iso.eraOf(0) == IsoEra.BCE, "eraOf BCE");
        check(iso.eras().size() == 2, "two eras");
        check(iso.date(2020, 1, 1).equals(LocalDate.of(2020, 1, 1)), "date");
        check(iso.dateEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "dateEpochDay");
        check(iso.period(1, 2, 3).equals(Period.of(1, 2, 3)), "period");
    }

    public static void main(String[] args) {
        testIsoChronology();
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

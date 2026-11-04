/*
 * @test
 * @summary java.time.temporal.JulianFields conformance: white-box structure + black-box behavior.
 * @run main JulianFieldsTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.temporal.JulianFields;
import java.time.temporal.TemporalField;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

public class JulianFieldsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.JulianFields");
        check(Modifier.isFinal(c.getModifiers()), "is final utility class");
        check(c.getConstructors().length == 0, "no public constructor");
        check(JulianFields.JULIAN_DAY instanceof TemporalField, "JULIAN_DAY is a TemporalField");
        check(JulianFields.MODIFIED_JULIAN_DAY instanceof TemporalField, "MODIFIED_JULIAN_DAY is a TemporalField");
        check(JulianFields.RATA_DIE instanceof TemporalField, "RATA_DIE is a TemporalField");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LocalDate epoch = LocalDate.of(1970, 1, 1);

        // well-known epoch values
        check(JulianFields.JULIAN_DAY.getFrom(epoch) == 2440588L, "JULIAN_DAY of 1970-01-01 = 2440588");
        check(JulianFields.MODIFIED_JULIAN_DAY.getFrom(epoch) == 40587L, "MODIFIED_JULIAN_DAY of 1970-01-01 = 40587");
        check(JulianFields.RATA_DIE.getFrom(epoch) == 719163L, "RATA_DIE of 1970-01-01 = 719163");

        // increment by one day
        check(JulianFields.JULIAN_DAY.getFrom(LocalDate.of(1970, 1, 2)) == 2440589L, "JULIAN_DAY increments by day");
        check(JulianFields.MODIFIED_JULIAN_DAY.getFrom(LocalDate.of(1970, 1, 2)) == 40588L, "MJD increments by day");
        check(JulianFields.RATA_DIE.getFrom(LocalDate.of(1970, 1, 2)) == 719164L, "RATA_DIE increments by day");

        // classification and support
        check(JulianFields.JULIAN_DAY.isDateBased(), "JULIAN_DAY is date-based");
        check(!JulianFields.JULIAN_DAY.isTimeBased(), "JULIAN_DAY is not time-based");
        check(JulianFields.JULIAN_DAY.isSupportedBy(LocalDate.of(1970, 1, 1)), "JULIAN_DAY supported by LocalDate");
        check(JulianFields.MODIFIED_JULIAN_DAY.isSupportedBy(LocalDate.of(1970, 1, 1)), "MJD supported by LocalDate");
        check(JulianFields.RATA_DIE.isSupportedBy(LocalDate.of(1970, 1, 1)), "RATA_DIE supported by LocalDate");

        // adjustInto round-trip
        LocalDate rt = JulianFields.JULIAN_DAY.adjustInto(LocalDate.of(2000, 1, 1), 2440588L);
        check(rt.equals(epoch), "JULIAN_DAY.adjustInto round-trip to 1970-01-01");
        LocalDate rt2 = JulianFields.RATA_DIE.adjustInto(LocalDate.of(2000, 1, 1), 719163L);
        check(rt2.equals(epoch), "RATA_DIE.adjustInto round-trip to 1970-01-01");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepJulianFields();
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


    static void testDeepJulianFields() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.getLong(JulianFields.JULIAN_DAY) == 2460370L, "JULIAN_DAY value");
        check(d.getLong(JulianFields.MODIFIED_JULIAN_DAY) == 60369L, "MJD value");
        check(d.getLong(JulianFields.RATA_DIE) == 738945L, "RATA_DIE value");
        check(LocalDate.of(1970, 1, 1).getLong(JulianFields.JULIAN_DAY) == 2440588L, "epoch JULIAN_DAY");
        check(LocalDate.of(1970, 1, 1).getLong(JulianFields.MODIFIED_JULIAN_DAY) == 40587L, "epoch MJD 40587");
        check(LocalDate.of(1970, 1, 1).getLong(JulianFields.RATA_DIE) == 719163L, "epoch RATA_DIE");
        xeq(JulianFields.JULIAN_DAY.range().toString(), "-365240778574 - 365244221059", "JULIAN_DAY range");
        xeq(JulianFields.MODIFIED_JULIAN_DAY.range().toString(), "-365243178575 - 365241821058", "MJD range");
        xeq(JulianFields.RATA_DIE.range().toString(), "-365242499999 - 365242499634", "RATA_DIE range");
        check(JulianFields.JULIAN_DAY.isDateBased(), "date based");
        check(!JulianFields.JULIAN_DAY.isTimeBased(), "not time based");
        check(LocalDate.from(d.with(JulianFields.JULIAN_DAY, 2460370L)).equals(d), "JD round trip");
        check(LocalDate.from(d.with(JulianFields.MODIFIED_JULIAN_DAY, 60369L)).equals(d), "MJD round trip");
        check(LocalDate.from(d.with(JulianFields.RATA_DIE, 738945L)).equals(d), "RD round trip");
        check(JulianFields.JULIAN_DAY.getBaseUnit() == ChronoUnit.DAYS, "JD base DAYS");
        check(JulianFields.JULIAN_DAY.getRangeUnit() == ChronoUnit.FOREVER, "JD range FOREVER");
        xact(() -> JulianFields.JULIAN_DAY.getFrom(LocalTime.NOON), UnsupportedTemporalTypeException.class,
            "Unsupported field: EpochDay", "JD on LocalTime reports EpochDay");
        xact(() -> LocalTime.NOON.getLong(JulianFields.RATA_DIE), UnsupportedTemporalTypeException.class,
            "Unsupported field: EpochDay", "RD on LocalTime reports EpochDay");
        check(!JulianFields.JULIAN_DAY.isSupportedBy(LocalTime.NOON), "JD not supported by time");
        check(JulianFields.JULIAN_DAY.isSupportedBy(d), "JD supported by date");
        check(LocalDate.of(2024, 2, 29).plusDays(1).getLong(JulianFields.JULIAN_DAY) == 2460371L, "JD + 1 day");
    }

}

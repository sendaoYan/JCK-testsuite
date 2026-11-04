/*
 * @test
 * @summary java.time.temporal.JulianFields conformance: JULIAN_DAY/MJD/RATA_DIE values.
 * @run main JulianFieldsTest
 */

import java.time.LocalDate;
import java.time.temporal.JulianFields;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

public class JulianFieldsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJulianFields() {
        check(JulianFields.JULIAN_DAY.isDateBased(), "JULIAN_DAY isDateBased");
        check(!JulianFields.JULIAN_DAY.isTimeBased(), "JULIAN_DAY not timeBased");
        check(JulianFields.JULIAN_DAY.isSupportedBy(LocalDate.now()), "JULIAN_DAY supported");

        LocalDate epoch = LocalDate.of(1970, 1, 1);
        check(JulianFields.JULIAN_DAY.getFrom(epoch) == 2440588L, "JULIAN_DAY 1970-01-01");
        check(JulianFields.MODIFIED_JULIAN_DAY.getFrom(epoch) == 40587L, "MODIFIED_JULIAN_DAY 1970-01-01");
        check(JulianFields.RATA_DIE.getFrom(epoch) == 719163L, "RATA_DIE 1970-01-01");
    }

    public static void main(String[] args) {
        testJulianFields();
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

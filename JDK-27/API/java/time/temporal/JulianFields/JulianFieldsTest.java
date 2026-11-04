/*
 * @test
 * @summary java.time.temporal.JulianFields conformance: white-box structure + black-box behavior.
 * @run main JulianFieldsTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.temporal.JulianFields;
import java.time.temporal.TemporalField;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

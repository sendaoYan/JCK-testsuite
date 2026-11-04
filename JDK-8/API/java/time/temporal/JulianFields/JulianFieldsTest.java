/*
 * @test
 * @summary java.time.temporal.JulianFields conformance: JULIAN_DAY/MJD/RATA_DIE values.
 * @run main JulianFieldsTest
 */

import java.time.LocalDate;
import java.time.temporal.JulianFields;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

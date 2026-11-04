/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistDate conformance: era/chronology/from.
 * @run main ThaiBuddhistDateTest
 */

import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.chrono.ThaiBuddhistEra;
import java.time.temporal.ChronoField;

public class ThaiBuddhistDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThaiBuddhistDate() {
        ThaiBuddhistDate td = ThaiBuddhistDate.of(2563, 1, 1);
        check(td.getEra() == ThaiBuddhistEra.BE, "era");
        check(td.getChronology() == ThaiBuddhistChronology.INSTANCE, "chronology");
        check(td.getLong(ChronoField.YEAR) == 2563, "year");
        check(td.equals(ThaiBuddhistDate.from(LocalDate.of(2020, 1, 1))), "from 2020-01-01");
        check(td.lengthOfMonth() == 31, "lengthOfMonth January");
    }

    public static void main(String[] args) {
        testThaiBuddhistDate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

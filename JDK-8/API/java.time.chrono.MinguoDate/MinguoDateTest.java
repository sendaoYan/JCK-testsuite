/*
 * @test
 * @summary java.time.chrono.MinguoDate conformance: era/chronology/from.
 * @run main MinguoDateTest
 */

import java.time.LocalDate;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;
import java.time.chrono.MinguoEra;
import java.time.temporal.ChronoField;

public class MinguoDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMinguoDate() {
        MinguoDate md = MinguoDate.of(1, 1, 1);
        check(md.getEra() == MinguoEra.ROC, "era");
        check(md.getChronology() == MinguoChronology.INSTANCE, "chronology");
        check(md.getLong(ChronoField.YEAR) == 1, "year");
        check(md.equals(MinguoDate.from(LocalDate.of(1912, 1, 1))), "from 1912-01-01");
        check(md.lengthOfMonth() == 31, "lengthOfMonth January");
        check(md.toEpochDay() == LocalDate.of(1912, 1, 1).toEpochDay(), "toEpochDay");
    }

    public static void main(String[] args) {
        testMinguoDate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

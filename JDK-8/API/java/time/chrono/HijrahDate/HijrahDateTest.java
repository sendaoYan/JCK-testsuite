/*
 * @test
 * @summary java.time.chrono.HijrahDate conformance: era/chronology/fields/leap.
 * @run main HijrahDateTest
 */

import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.chrono.HijrahEra;
import java.time.temporal.ChronoField;

public class HijrahDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHijrahDate() {
        HijrahDate hd = HijrahDate.of(1437, 1, 1);
        check(hd.getEra() == HijrahEra.AH, "era");
        check(hd.getChronology() == HijrahChronology.INSTANCE, "chronology");
        check(hd.getLong(ChronoField.YEAR) == 1437, "year");
        check(hd.getLong(ChronoField.MONTH_OF_YEAR) == 1, "month");
        check(hd.getLong(ChronoField.DAY_OF_MONTH) == 1, "day");
        check(hd.lengthOfYear() >= 354 && hd.lengthOfYear() <= 355, "lengthOfYear");
        check(hd.isLeapYear() == (hd.lengthOfYear() == 355), "isLeapYear consistent");
    }

    public static void main(String[] args) {
        testHijrahDate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

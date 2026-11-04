/*
 * @test
 * @summary java.time.chrono.HijrahChronology conformance: id/calendarType/era.
 * @run main HijrahChronologyTest
 */

import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahEra;

public class HijrahChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHijrahChronology() {
        HijrahChronology hc = HijrahChronology.INSTANCE;
        check("Hijrah-umalqura".equals(hc.getId()), "id");
        check("islamic-umalqura".equals(hc.getCalendarType()), "calendarType");
        check(hc.eras().size() == 1, "one era");
        check(hc.eraOf(1) == HijrahEra.AH, "eraOf AH");
        check(hc.date(1437, 1, 1) != null, "date");
    }

    public static void main(String[] args) {
        testHijrahChronology();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

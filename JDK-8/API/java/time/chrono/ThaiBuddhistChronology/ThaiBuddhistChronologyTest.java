/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistChronology conformance: id/calendarType/era.
 * @run main ThaiBuddhistChronologyTest
 */

import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistEra;

public class ThaiBuddhistChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThaiBuddhistChronology() {
        ThaiBuddhistChronology tc = ThaiBuddhistChronology.INSTANCE;
        check("ThaiBuddhist".equals(tc.getId()), "id");
        check("buddhist".equals(tc.getCalendarType()), "calendarType");
        check(tc.eras().size() == 2, "two eras");
        check(tc.eraOf(1) == ThaiBuddhistEra.BE, "eraOf BE");
        check(tc.date(2563, 1, 1) != null, "date 2563");
    }

    public static void main(String[] args) {
        testThaiBuddhistChronology();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

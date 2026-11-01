/*
 * @test
 * @summary java.time.chrono.MinguoChronology conformance: id/calendarType/era.
 * @run main MinguoChronologyTest
 */

import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoEra;

public class MinguoChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMinguoChronology() {
        MinguoChronology mc = MinguoChronology.INSTANCE;
        check("Minguo".equals(mc.getId()), "id");
        check("roc".equals(mc.getCalendarType()), "calendarType");
        check(mc.eras().size() == 2, "two eras");
        check(mc.eraOf(1) == MinguoEra.ROC, "eraOf ROC");
        check(mc.date(1, 1, 1) != null, "date ROC 1");
    }

    public static void main(String[] args) {
        testMinguoChronology();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

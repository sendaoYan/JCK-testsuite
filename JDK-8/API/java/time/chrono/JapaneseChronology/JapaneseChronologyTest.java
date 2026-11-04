/*
 * @test
 * @summary java.time.chrono.JapaneseChronology conformance: id/calendarType/era.
 * @run main JapaneseChronologyTest
 */

import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseEra;

public class JapaneseChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJapaneseChronology() {
        JapaneseChronology jc = JapaneseChronology.INSTANCE;
        check("Japanese".equals(jc.getId()), "id");
        check("japanese".equals(jc.getCalendarType()), "calendarType");
        check(jc.eraOf(2) == JapaneseEra.HEISEI, "eraOf HEISEI");
        check(jc.date(JapaneseEra.HEISEI, 1, 1, 8) != null, "date by era");
        check(jc.isLeapYear(2020), "2020 leap");
    }

    public static void main(String[] args) {
        testJapaneseChronology();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

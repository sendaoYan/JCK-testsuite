/*
 * @test
 * @summary java.time.chrono.JapaneseDate conformance: era/chronology/fields/from.
 * @run main JapaneseDateTest
 */

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.JapaneseEra;
import java.time.temporal.ChronoField;

public class JapaneseDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJapaneseDate() {
        JapaneseDate jd = JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 8);
        check(jd.getEra() == JapaneseEra.HEISEI, "era");
        check(jd.getChronology() == JapaneseChronology.INSTANCE, "chronology");
        check(jd.getLong(ChronoField.YEAR_OF_ERA) == 1, "yearOfEra");
        check(jd.getLong(ChronoField.MONTH_OF_YEAR) == 1, "month");
        check(jd.getLong(ChronoField.DAY_OF_MONTH) == 8, "day");
        check(jd.equals(JapaneseDate.from(LocalDate.of(1989, 1, 8))), "from LocalDate");
        check(jd.lengthOfYear() > 0, "lengthOfYear");
        check(jd.atTime(LocalTime.NOON) != null, "atTime");
    }

    public static void main(String[] args) {
        testJapaneseDate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

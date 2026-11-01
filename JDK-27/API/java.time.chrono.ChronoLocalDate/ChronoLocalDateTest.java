/*
 * @test
 * @summary java.time.chrono.ChronoLocalDate conformance: via LocalDate.
 * @run main ChronoLocalDateTest
 */

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class ChronoLocalDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoLocalDate() {
        ChronoLocalDate d = LocalDate.of(2020, 1, 1);
        check(d.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(d.getEra() == IsoEra.CE, "getEra");
        check(d.isSupported(ChronoField.DAY_OF_MONTH), "isSupported");
        check(d.get(ChronoField.DAY_OF_MONTH) == 1, "get DAY_OF_MONTH");
        check(d.getLong(ChronoField.YEAR) == 2020, "getLong YEAR");
        check(d.lengthOfMonth() == 31, "lengthOfMonth");
        check(d.lengthOfYear() == 366, "lengthOfYear leap");
        check(d.isLeapYear(), "isLeapYear");
        check(d.toEpochDay() == 18262L, "toEpochDay");
        check(d.atTime(LocalTime.NOON) != null, "atTime");
        check(d.plus(1, ChronoUnit.DAYS).equals(LocalDate.of(2020, 1, 2)), "plus");
        check(d.until(LocalDate.of(2020, 1, 5), ChronoUnit.DAYS) == 4, "until");
    }

    public static void main(String[] args) {
        testChronoLocalDate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

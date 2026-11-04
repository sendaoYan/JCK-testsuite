/*
 * @test
 * @summary java.time.chrono.IsoChronology conformance: id/calendar/leap/era.
 * @run main IsoChronologyTest
 */

import java.time.LocalDate;
import java.time.Period;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;

public class IsoChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIsoChronology() {
        IsoChronology iso = IsoChronology.INSTANCE;
        check("ISO".equals(iso.getId()), "id");
        check("iso8601".equals(iso.getCalendarType()), "calendarType");
        check(iso.isLeapYear(2020), "2020 leap");
        check(!iso.isLeapYear(2019), "2019 not leap");
        check(iso.prolepticYear(IsoEra.CE, 2020) == 2020, "prolepticYear CE");
        check(iso.prolepticYear(IsoEra.BCE, 1) == 0, "prolepticYear BCE");
        check(iso.eraOf(1) == IsoEra.CE, "eraOf CE");
        check(iso.eraOf(0) == IsoEra.BCE, "eraOf BCE");
        check(iso.eras().size() == 2, "two eras");
        check(iso.date(2020, 1, 1).equals(LocalDate.of(2020, 1, 1)), "date");
        check(iso.dateEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "dateEpochDay");
        check(iso.period(1, 2, 3).equals(Period.of(1, 2, 3)), "period");
    }

    public static void main(String[] args) {
        testIsoChronology();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

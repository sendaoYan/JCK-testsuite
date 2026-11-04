/*
 * @test
 * @summary java.time.chrono.Chronology conformance: via IsoChronology.
 * @run main ChronologyTest
 */

import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.format.TextStyle;
import java.util.Locale;

public class ChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronology() {
        Chronology c = IsoChronology.INSTANCE;
        check("ISO".equals(c.getId()), "getId");
        check("iso8601".equals(c.getCalendarType()), "getCalendarType");
        check(c.isLeapYear(2020), "isLeapYear 2020");
        check(!c.isLeapYear(2019), "isLeapYear 2019 false");
        check(c.prolepticYear(IsoEra.CE, 2020) == 2020, "prolepticYear CE");
        check(c.prolepticYear(IsoEra.BCE, 1) == 0, "prolepticYear BCE");
        check(c.eraOf(1) == IsoEra.CE, "eraOf 1");
        check(c.eras().size() == 2, "two eras");
        check(c.date(2020, 1, 1).equals(LocalDate.of(2020, 1, 1)), "date");
        check(c.dateEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "dateEpochDay");
        check(c.dateNow() != null, "dateNow");
        check(c.getDisplayName(TextStyle.FULL, Locale.ENGLISH) != null, "getDisplayName");
        check(c.period(1, 2, 3) != null, "period");
        check(c.compareTo(IsoChronology.INSTANCE) == 0, "compareTo");
        check(Chronology.of("ISO") == IsoChronology.INSTANCE, "Chronology.of ISO");
    }

    public static void main(String[] args) {
        testChronology();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

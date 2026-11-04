/*
 * @test
 * @summary java.util.GregorianCalendar API conformance: isLeapYear, the (year, month, day)
 *          constructor, and epoch field values in UTC.
 * @run main GregorianCalendarTest
 */

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class GregorianCalendarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLeapYear() {
        GregorianCalendar gc = new GregorianCalendar();
        check(gc.isLeapYear(2000), "2000 is leap");
        check(gc.isLeapYear(2004), "2004 is leap");
        check(!gc.isLeapYear(1900), "1900 is not leap (century)");
        check(!gc.isLeapYear(2023), "2023 is not leap");
    }

    static void testConstructor() {
        GregorianCalendar g = new GregorianCalendar(2024, Calendar.MARCH, 1);
        check(g.get(Calendar.YEAR) == 2024, "(y,m,d) YEAR");
        check(g.get(Calendar.MONTH) == Calendar.MARCH, "(y,m,d) MONTH");
        check(g.get(Calendar.DAY_OF_MONTH) == 1, "(y,m,d) DAY_OF_MONTH");
    }

    static void testEpoch() {
        GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        g.setTimeInMillis(0L);
        check(g.get(Calendar.YEAR) == 1970 && g.get(Calendar.MONTH) == Calendar.JANUARY,
              "epoch fields in UTC");
    }

    public static void main(String[] args) {
        testLeapYear();
        testConstructor();
        testEpoch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.util.Calendar.Builder conformance: set/build date and time.
 * @run main CalendarBuilderTest
 */

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() {
        Calendar cal = new Calendar.Builder()
            .set(Calendar.YEAR, 2020)
            .set(Calendar.MONTH, Calendar.JANUARY)
            .set(Calendar.DAY_OF_MONTH, 15)
            .build();
        check(cal.get(Calendar.YEAR) == 2020, "year");
        check(cal.get(Calendar.MONTH) == Calendar.JANUARY, "month");
        check(cal.get(Calendar.DAY_OF_MONTH) == 15, "day");

        Calendar c2 = new Calendar.Builder().setDate(2021, Calendar.FEBRUARY, 20).build();
        check(c2.get(Calendar.YEAR) == 2021 && c2.get(Calendar.DAY_OF_MONTH) == 20, "setDate");

        Calendar c3 = new Calendar.Builder().setTimeOfDay(10, 30, 45).build();
        check(c3.get(Calendar.HOUR_OF_DAY) == 10 && c3.get(Calendar.MINUTE) == 30
              && c3.get(Calendar.SECOND) == 45, "setTimeOfDay");

        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar c4 = new Calendar.Builder().setTimeZone(tz).setLocale(Locale.US)
            .setLenient(false).build();
        check(c4.getTimeZone().equals(tz), "setTimeZone");
        check(!c4.isLenient(), "setLenient false");
    }

    public static void main(String[] args) {
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

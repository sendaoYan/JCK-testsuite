/*
 * @test
 * @summary java.util.Calendar API conformance: epoch field values in UTC, getTime,
 *          set/add of fields, and before/after.
 * @run main CalendarTest
 */

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Calendar utc() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
    }

    static void testEpoch() {
        Calendar cal = utc();
        cal.setTimeInMillis(0L);
        check(cal.get(Calendar.YEAR) == 1970, "epoch YEAR");
        check(cal.get(Calendar.MONTH) == Calendar.JANUARY, "epoch MONTH");
        check(cal.get(Calendar.DAY_OF_MONTH) == 1, "epoch DAY_OF_MONTH");
        check(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0,
              "epoch HOUR_OF_DAY/MINUTE");
        check(cal.getTime().getTime() == 0L, "getTime == epoch millis");
    }

    static void testFieldOps() {
        Calendar cal = utc();
        cal.setTimeInMillis(0L);
        cal.set(Calendar.YEAR, 2000);
        check(cal.get(Calendar.YEAR) == 2000, "set YEAR");
        cal.add(Calendar.YEAR, 1);
        check(cal.get(Calendar.YEAR) == 2001, "add YEAR");

        Calendar later = utc();
        later.setTimeInMillis(0L);
        later.set(Calendar.YEAR, 2002);
        check(later.after(cal) && cal.before(later), "before/after");
    }

    public static void main(String[] args) {
        testEpoch();
        testFieldOps();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

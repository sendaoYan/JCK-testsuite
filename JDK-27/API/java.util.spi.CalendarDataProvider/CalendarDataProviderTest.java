/*
 * @test
 * @summary java.util.spi.CalendarDataProvider conformance: first day / minimal days.
 * @run main CalendarDataProviderTest
 */

import java.util.Calendar;
import java.util.Locale;
import java.util.spi.CalendarDataProvider;
import java.util.spi.LocaleServiceProvider;

public class CalendarDataProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCalendarDataProvider() {
        CalendarDataProvider p = new CalendarDataProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public int getFirstDayOfWeek(Locale l) {
                return Calendar.SUNDAY;
            }
            public int getMinimalDaysInFirstWeek(Locale l) {
                return 1;
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getFirstDayOfWeek(Locale.US) == Calendar.SUNDAY, "getFirstDayOfWeek");
        check(p.getMinimalDaysInFirstWeek(Locale.US) == 1, "getMinimalDaysInFirstWeek");
    }

    public static void main(String[] args) {
        testCalendarDataProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

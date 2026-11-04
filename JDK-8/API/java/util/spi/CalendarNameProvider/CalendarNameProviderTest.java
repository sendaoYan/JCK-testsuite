/*
 * @test
 * @summary java.util.spi.CalendarNameProvider conformance: display names.
 * @run main CalendarNameProviderTest
 */

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.spi.CalendarNameProvider;
import java.util.spi.LocaleServiceProvider;

public class CalendarNameProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCalendarNameProvider() {
        CalendarNameProvider p = new CalendarNameProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public String getDisplayName(String calendarType, int field, int value, int style,
                                         Locale l) {
                return "name";
            }
            public Map<String, Integer> getDisplayNames(String calendarType, int field, int style,
                                                        Locale l) {
                return new HashMap<String, Integer>();
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check("name".equals(p.getDisplayName("gregory", Calendar.MONTH, Calendar.JANUARY,
                                             Calendar.LONG, Locale.US)), "getDisplayName");
        check(p.getDisplayNames("gregory", Calendar.MONTH, Calendar.LONG, Locale.US) != null,
              "getDisplayNames");
    }

    public static void main(String[] args) {
        testCalendarNameProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.text.spi.DateFormatProvider conformance: hierarchy and instance methods.
 * @run main DateFormatProviderTest
 */

import java.text.DateFormat;
import java.text.spi.DateFormatProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DateFormatProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateFormatProvider() {
        DateFormatProvider p = new DateFormatProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DateFormat getTimeInstance(int style, Locale l) {
                return DateFormat.getTimeInstance(style, Locale.US);
            }
            public DateFormat getDateInstance(int style, Locale l) {
                return DateFormat.getDateInstance(style, Locale.US);
            }
            public DateFormat getDateTimeInstance(int ds, int ts, Locale l) {
                return DateFormat.getDateTimeInstance(ds, ts, Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getTimeInstance(DateFormat.SHORT, Locale.US) != null, "getTimeInstance");
        check(p.getDateInstance(DateFormat.SHORT, Locale.US) != null, "getDateInstance");
        check(p.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US) != null,
              "getDateTimeInstance");
    }

    public static void main(String[] args) {
        testDateFormatProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

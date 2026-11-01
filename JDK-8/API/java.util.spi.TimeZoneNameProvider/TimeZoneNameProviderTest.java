/*
 * @test
 * @summary java.util.spi.TimeZoneNameProvider conformance: getDisplayName.
 * @run main TimeZoneNameProviderTest
 */

import java.util.Locale;
import java.util.TimeZone;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;

public class TimeZoneNameProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTimeZoneNameProvider() {
        TimeZoneNameProvider p = new TimeZoneNameProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public String getDisplayName(String ID, boolean daylight, int style, Locale l) {
                return "TZ";
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check("TZ".equals(p.getDisplayName("America/New_York", false, TimeZone.LONG, Locale.US)),
              "getDisplayName");
        check(p.getGenericDisplayName("America/New_York", TimeZone.LONG, Locale.US) == null,
              "getGenericDisplayName default null");
    }

    public static void main(String[] args) {
        testTimeZoneNameProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

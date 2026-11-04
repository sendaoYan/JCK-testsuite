/*
 * @test
 * @summary java.text.spi.DateFormatSymbolsProvider conformance: hierarchy and getInstance.
 * @run main DateFormatSymbolsProviderTest
 */

import java.text.DateFormatSymbols;
import java.text.spi.DateFormatSymbolsProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DateFormatSymbolsProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateFormatSymbolsProvider() {
        DateFormatSymbolsProvider p = new DateFormatSymbolsProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DateFormatSymbols getInstance(Locale l) {
                return DateFormatSymbols.getInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getInstance(Locale.US) != null, "getInstance");
    }

    public static void main(String[] args) {
        testDateFormatSymbolsProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.text.spi.DecimalFormatSymbolsProvider conformance: hierarchy and getInstance.
 * @run main DecimalFormatSymbolsProviderTest
 */

import java.text.DecimalFormatSymbols;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DecimalFormatSymbolsProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDecimalFormatSymbolsProvider() {
        DecimalFormatSymbolsProvider p = new DecimalFormatSymbolsProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DecimalFormatSymbols getInstance(Locale l) {
                return DecimalFormatSymbols.getInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getInstance(Locale.US) != null, "getInstance");
    }

    public static void main(String[] args) {
        testDecimalFormatSymbolsProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

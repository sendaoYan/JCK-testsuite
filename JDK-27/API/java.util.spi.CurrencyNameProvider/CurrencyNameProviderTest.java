/*
 * @test
 * @summary java.util.spi.CurrencyNameProvider conformance: getSymbol and getDisplayName.
 * @run main CurrencyNameProviderTest
 */

import java.util.Locale;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleServiceProvider;

public class CurrencyNameProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCurrencyNameProvider() {
        CurrencyNameProvider p = new CurrencyNameProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public String getSymbol(String currencyCode, Locale l) {
                return "$";
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check("$".equals(p.getSymbol("USD", Locale.US)), "getSymbol");
        check(p.getDisplayName("USD", Locale.US) == null, "getDisplayName default null");
        boolean threw = false;
        try {
            p.getDisplayName("US", Locale.US);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        check(threw, "getDisplayName validates currency code");
    }

    public static void main(String[] args) {
        testCurrencyNameProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

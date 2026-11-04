/*
 * @test
 * @summary java.util.spi.LocaleServiceProvider conformance: getAvailableLocales/isSupportedLocale.
 * @run main LocaleServiceProviderTest
 */

import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class LocaleServiceProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLocaleServiceProvider() {
        LocaleServiceProvider p = new LocaleServiceProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US, Locale.FRANCE};
            }
        };
        check(p.getAvailableLocales().length == 2, "getAvailableLocales length");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE");
        check(!p.isSupportedLocale(Locale.JAPAN), "isSupportedLocale JAPAN false");
    }

    public static void main(String[] args) {
        testLocaleServiceProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

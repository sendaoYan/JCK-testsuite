/*
 * @test
 * @summary java.util.spi.LocaleNameProvider conformance: language/country/variant names.
 * @run main LocaleNameProviderTest
 */

import java.util.Locale;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;

public class LocaleNameProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLocaleNameProvider() {
        LocaleNameProvider p = new LocaleNameProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public String getDisplayLanguage(String languageCode, Locale l) {
                return "lang";
            }
            public String getDisplayCountry(String countryCode, Locale l) {
                return "country";
            }
            public String getDisplayVariant(String variant, Locale l) {
                return "variant";
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check("lang".equals(p.getDisplayLanguage("en", Locale.US)), "getDisplayLanguage");
        check("country".equals(p.getDisplayCountry("US", Locale.US)), "getDisplayCountry");
        check("variant".equals(p.getDisplayVariant("POSIX", Locale.US)), "getDisplayVariant");
        check(p.getDisplayScript("Latn", Locale.US) == null, "getDisplayScript default null");
    }

    public static void main(String[] args) {
        testLocaleNameProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

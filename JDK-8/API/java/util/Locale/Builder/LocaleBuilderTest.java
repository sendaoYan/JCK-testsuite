/*
 * @test
 * @summary java.util.Locale.Builder conformance: language/region/script/variant/extensions.
 * @run main LocaleBuilderTest
 */

import java.util.Locale;

public class LocaleBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() {
        Locale l = new Locale.Builder().setLanguage("en").setRegion("US").build();
        check("en".equals(l.getLanguage()) && "US".equals(l.getCountry()), "language/region");

        Locale l2 = new Locale.Builder().setLocale(Locale.FRANCE).build();
        check(Locale.FRANCE.equals(l2), "setLocale");

        Locale l3 = new Locale.Builder().setLanguage("zh").setScript("Hans").setRegion("CN").build();
        check("zh".equals(l3.getLanguage()) && "Hans".equals(l3.getScript())
              && "CN".equals(l3.getCountry()), "script");

        Locale l4 = new Locale.Builder().setLanguage("en").setVariant("POSIX").build();
        check("POSIX".equals(l4.getVariant()), "variant");

        Locale l5 = new Locale.Builder().setLanguage("en").setExtension('u', "ca-japanese").build();
        check(l5.getExtension('u') != null, "extension");

        Locale l6 = new Locale.Builder().setLanguage("en").addUnicodeLocaleAttribute("foobar").build();
        check(l6.getUnicodeLocaleAttributes().contains("foobar"), "unicode attribute");

        Locale l7 = new Locale.Builder().setLanguage("en").setUnicodeLocaleKeyword("nu", "thai").build();
        check("thai".equals(l7.getUnicodeLocaleType("nu")), "unicode keyword");

        Locale l8 = new Locale.Builder().setLanguage("en").clear().setLanguage("fr").build();
        check("fr".equals(l8.getLanguage()), "clear");
    }

    public static void main(String[] args) {
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

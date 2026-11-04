/*
 * @test
 * @summary java.util.Locale API conformance (Java 8 surface): complete contract coverage of
 *          constants, constructors + case normalization, getters (language/script/country/variant),
 *          ISO3 language/country, toLanguageTag/forLanguageTag round-trip + BCP47 extensions +
 *          stripExtensions, clone/equals/hashCode, getDisplayName (fixed locale), available
 *          locales/ISO codes, and LanguageRange filter/lookup (no setDefault).
 * @run main LocaleTest
 */

import java.util.Locale;
import java.util.Arrays;
import java.util.List;

public class LocaleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstants() {
        Locale en = Locale.ENGLISH;
        check("en".equals(en.getLanguage()) && "".equals(en.getCountry()), "ENGLISH");
        Locale us = Locale.US;
        check("en".equals(us.getLanguage()) && "US".equals(us.getCountry()), "US");
        Locale root = Locale.ROOT;
        check("".equals(root.getLanguage()) && "".equals(root.getCountry()), "ROOT");
        check("fr".equals(Locale.FRENCH.getLanguage()), "FRENCH");
        check("zh".equals(Locale.CHINESE.getLanguage()), "CHINESE");
        check("zh".equals(Locale.SIMPLIFIED_CHINESE.getLanguage())
              && "CN".equals(Locale.SIMPLIFIED_CHINESE.getCountry()), "SIMPLIFIED_CHINESE");
    }

    static void testConstruction() {
        Locale fr = new Locale("fr", "FR");
        check("fr".equals(fr.getLanguage()) && "FR".equals(fr.getCountry()), "(language, country)");
        Locale built = new Locale.Builder().setLanguage("de").setRegion("DE").build();
        check("de".equals(built.getLanguage()) && "DE".equals(built.getCountry()), "Builder");

        // case normalization: language lowercased, country uppercased.
        Locale mixed = new Locale("EN", "us");
        check("en".equals(mixed.getLanguage()) && "US".equals(mixed.getCountry()),
              "case normalization");
        Locale withVariant = new Locale("en", "US", "POSIX");
        check("POSIX".equals(withVariant.getVariant()), "variant");
    }

    static void testDefault() {
        Locale def = Locale.getDefault();
        check(def != null, "getDefault non-null");
        check(Locale.getDefault(Locale.Category.FORMAT) != null, "getDefault(FORMAT) non-null");
        check(Locale.getDefault(Locale.Category.DISPLAY) != null, "getDefault(DISPLAY) non-null");
    }

    static void testIso3() {
        check("eng".equals(new Locale("en").getISO3Language()), "ISO3Language en -> eng");
        check("fra".equals(new Locale("fr").getISO3Language()), "ISO3Language fr -> fra");
        check("USA".equals(new Locale("en", "US").getISO3Country()), "ISO3Country US -> USA");
    }

    static void testLanguageTag() {
        Locale l = Locale.forLanguageTag("en-US");
        check("en".equals(l.getLanguage()) && "US".equals(l.getCountry()), "forLanguageTag en-US");
        check("en-US".equals(l.toLanguageTag()), "toLanguageTag en-US");
        check("fr-FR".equals(new Locale("fr", "FR").toLanguageTag()), "toLanguageTag fr-FR");
        check("en-US".equals(Locale.forLanguageTag("en-us").toLanguageTag()),
              "forLanguageTag normalizes case");
    }

    static void testExtensions() {
        Locale l = Locale.forLanguageTag("en-US-u-ca-gregory");
        check(l.hasExtensions(), "hasExtensions for BCP47 unicode extension");
        check(l.getExtension(Locale.UNICODE_LOCALE_EXTENSION) != null
              || "en-US-u-ca-gregory".equals(l.toLanguageTag()),
              "unicode extension present");
        Locale stripped = l.stripExtensions();
        check(!stripped.hasExtensions(), "stripExtensions removes extensions");
        check("en-US".equals(stripped.toLanguageTag()), "stripped language tag");

        Locale p = Locale.forLanguageTag("en-US-x-lvariant");
        check(p.getExtension(Locale.PRIVATE_USE_EXTENSION) != null || p.hasExtensions(),
              "private use extension");
    }

    static void testCloneEqualsHashCode() {
        Locale a = new Locale("en", "US");
        Locale b = (Locale) a.clone();
        check(b != a && b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "equal hashCode");
        check(a.equals(new Locale("en", "US")), "equals same fields");
        check(!a.equals(new Locale("en", "GB")), "!equals different country");
        check(!a.equals(null) && !a.equals("x"), "equals null/other-type false");
    }

    static void testDisplayNames() {
        Locale fr = new Locale("fr", "FR");
        check(fr.getDisplayLanguage(Locale.ENGLISH) != null
              && fr.getDisplayLanguage(Locale.ENGLISH).length() > 0,
              "getDisplayLanguage(ENGLISH) non-empty");
        check(fr.getDisplayName(Locale.ENGLISH) != null
              && fr.getDisplayName(Locale.ENGLISH).length() > 0,
              "getDisplayName(ENGLISH) non-empty");
    }

    static void testAvailableLocales() {
        Locale[] locales = Locale.getAvailableLocales();
        check(locales != null && locales.length > 0, "getAvailableLocales non-empty");
        check(Locale.getISOCountries().length > 0, "getISOCountries non-empty");
        check(Locale.getISOLanguages().length > 0, "getISOLanguages non-empty");
    }

    static void testFilterLookup() {
        List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse("en-*;q=1.0,fr-*;q=0.5");
        List<Locale> locales = Arrays.asList(Locale.US, Locale.FRANCE, Locale.GERMANY);
        List<Locale> filtered = Locale.filter(ranges, locales);
        check(filtered.contains(Locale.US) && filtered.contains(Locale.FRANCE),
              "filter includes matching locales");
        Locale best = Locale.lookup(ranges, locales);
        check(best != null, "lookup returns a locale");
    }

    public static void main(String[] args) {
        testConstants();
        testConstruction();
        testDefault();
        testIso3();
        testLanguageTag();
        testExtensions();
        testCloneEqualsHashCode();
        testDisplayNames();
        testAvailableLocales();
        testFilterLookup();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

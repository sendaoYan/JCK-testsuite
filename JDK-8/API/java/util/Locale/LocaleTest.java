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


    // ===================== 契约深化：forLanguageTag 容错/ISO3 异常/类别默认区/权重排序/filter 顺序 =====================
    static void testLocaleContracts() {
        check("und".equals(Locale.forLanguageTag("!!").toLanguageTag()),
              "forLanguageTag tolerates ill-formed -> und");
        check("und".equals(Locale.forLanguageTag("und").toLanguageTag()), "und round-trips");
        check("x-private".equals(Locale.forLanguageTag("x-private").toLanguageTag()),
              "private-use-only tag round-trips");
        check("ja-JP-u-ca-japanese-nu-jpan".equals(
              Locale.forLanguageTag("ja-JP-u-ca-japanese-nu-jpan").toLanguageTag()),
              "multiple unicode extensions round-trip");
        check("en-Latn-US".equals(
              new Locale.Builder().setLanguageTag("EN-latn-US").build().toLanguageTag()),
              "language tag normalized to canonical case");
        check("Latn".equals(Locale.forLanguageTag("sr-Latn-RS").getScript()), "getScript");
        check("1996".equals(Locale.forLanguageTag("de-DE-1996").getVariant()),
              "variant from tag subtag");
        check("foo".equals(Locale.forLanguageTag("de-DE-x-foo").getExtension('x')),
              "private extension value");
        try {
            new Locale("qq").getISO3Language();
            check(false, "unknown ISO3 language MissingResourceException");
        } catch (java.util.MissingResourceException mre) {
            check(true, "unknown ISO3 language MissingResourceException");
        }
        Locale oldAll = Locale.getDefault();
        Locale oldDisplay = Locale.getDefault(Locale.Category.DISPLAY);
        Locale oldFormat = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.DISPLAY, Locale.FRENCH);
            check(Locale.FRENCH.equals(Locale.getDefault(Locale.Category.DISPLAY)),
                  "setDefault(DISPLAY) takes effect");
            check(Locale.getDefault(Locale.Category.FORMAT).equals(oldFormat),
                  "FORMAT untouched by DISPLAY change");
            Locale.setDefault(Locale.Category.FORMAT, Locale.GERMAN);
            check(Locale.GERMAN.equals(Locale.getDefault(Locale.Category.FORMAT)),
                  "setDefault(FORMAT) takes effect");
            check(Locale.getDefault().equals(oldAll),
                  "overall default untouched by category setDefault");
        } finally {
            Locale.setDefault(Locale.Category.DISPLAY, oldDisplay);
            Locale.setDefault(Locale.Category.FORMAT, oldFormat);
        }
        List<Locale.LanguageRange> ranges =
              Locale.LanguageRange.parse("fr-CH;q=0.8, en;q=0.9, *;q=0.1");
        check(ranges.size() == 3, "parse yields three ranges");
        check("en".equals(ranges.get(0).getRange()) && ranges.get(0).getWeight() == 0.9,
              "parse orders by descending weight");
        check("fr-ch".equals(ranges.get(1).getRange()) && ranges.get(1).getWeight() == 0.8,
              "range normalized to lowercase");
        check("*".equals(ranges.get(2).getRange()) && ranges.get(2).getWeight() == 0.1,
              "wildcard range last");
        List<Locale.LanguageRange> pr = Locale.LanguageRange.parse("ja-JP, en-US, *");
        List<Locale> tags = new java.util.ArrayList<>();
        tags.add(Locale.forLanguageTag("en-US"));
        tags.add(Locale.forLanguageTag("ja-JP"));
        tags.add(Locale.forLanguageTag("de"));
        List<Locale> f = Locale.filter(pr, tags);
        check(f.size() == 3, "filter with wildcard matches all three");
        check("en-US".equals(f.get(0).toLanguageTag())
              && "ja-JP".equals(f.get(1).toLanguageTag())
              && "de".equals(f.get(2).toLanguageTag()),
              "JDK-8 filter preserves input-list order");
        check("ja-JP".equals(Locale.lookup(pr, tags).toLanguageTag()), "lookup best match");
        check(Locale.lookup(Locale.LanguageRange.parse("fr"), tags) == null, "lookup miss -> null");
        List<String> strs = new java.util.ArrayList<>();
        strs.add("en-us");
        strs.add("JA-jp");
        List<String> ft = Locale.filterTags(Locale.LanguageRange.parse("ja-JP, en"), strs);
        check(ft.size() == 2 && "ja-jp".equals(ft.get(0)) && "en-us".equals(ft.get(1)),
              "JDK-8 filterTags lowercases results");
        check("ja-jp".equals(Locale.lookupTag(pr, strs)), "JDK-8 lookupTag lowercases result");
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
        testLocaleContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

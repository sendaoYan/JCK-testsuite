/*
 * @test
 * @summary java.util.Locale conformance: white-box structure plus black-box behavior of
 *          language/country accessors, ENGLISH/US/ROOT/CHINA constants, constructors,
 *          forLanguageTag/toLanguageTag, ISO3 codes, display names, and available locales.
 * @run main LocaleTest
 */

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class LocaleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Locale");
        check(!c.isInterface(), "Locale is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Locale is final");
        check(Serializable.class.isAssignableFrom(c), "Locale implements Serializable");
        check(Cloneable.class.isAssignableFrom(c), "Locale implements Cloneable");
        c.getMethod("getLanguage");
        c.getMethod("getCountry");
        c.getMethod("getScript");
        c.getMethod("getVariant");
        c.getMethod("toLanguageTag");
        c.getMethod("forLanguageTag", String.class);
        c.getMethod("getISO3Language");
        c.getMethod("getISO3Country");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testConstants();
        testConstruction();
        testLanguageTag();
        testIso3();
        testDisplayNames();
        testAvailableLocales();
        testCloneEqualsHashCode();
        testExtensions();
        testFilterLookup();
    }

    static void testConstants() {
        Locale en = Locale.ENGLISH;
        check("en".equals(en.getLanguage()) && "".equals(en.getCountry()), "ENGLISH");
        Locale us = Locale.US;
        check("en".equals(us.getLanguage()) && "US".equals(us.getCountry()), "US");
        Locale root = Locale.ROOT;
        check("".equals(root.getLanguage()) && "".equals(root.getCountry()), "ROOT");
        Locale china = Locale.CHINA;
        check("zh".equals(china.getLanguage()) && "CN".equals(china.getCountry()), "CHINA");
    }

    static void testConstruction() {
        Locale fr = new Locale("fr", "FR");
        check("fr".equals(fr.getLanguage()) && "FR".equals(fr.getCountry()), "(language, country)");
        Locale built = new Locale.Builder().setLanguage("de").setRegion("DE").build();
        check("de".equals(built.getLanguage()) && "DE".equals(built.getCountry()), "Builder");
        check(Locale.US.equals(new Locale("en", "US")), "constructed equals constant");
        check(Locale.getDefault() != null, "getDefault non-null");
    }

    static void testLanguageTag() {
        check("en-US".equals(Locale.US.toLanguageTag()), "toLanguageTag en-US");
        check(Locale.US.equals(Locale.forLanguageTag("en-US")), "forLanguageTag en-US");
        check(Locale.ROOT.equals(Locale.forLanguageTag("")), "forLanguageTag empty == ROOT");
        Locale zh = Locale.forLanguageTag("zh-Hans-CN");
        check("zh".equals(zh.getLanguage()) && "Hans".equals(zh.getScript())
              && "CN".equals(zh.getCountry()), "forLanguageTag script/region");
    }

    static void testIso3() {
        check("eng".equals(Locale.US.getISO3Language()), "getISO3Language eng");
        check("USA".equals(Locale.US.getISO3Country()), "getISO3Country USA");
    }

    static void testDisplayNames() {
        check("English".equals(Locale.US.getDisplayLanguage(Locale.US)), "display language in en");
        check("United States".equals(Locale.US.getDisplayCountry(Locale.US)), "display country in en");
        check(Locale.US.getDisplayName(Locale.US).length() > 0, "display name non-empty");
    }

    static void testAvailableLocales() {
        Locale[] all = Locale.getAvailableLocales();
        check(all.length > 0, "getAvailableLocales non-empty");
        boolean hasUs = false;
        for (Locale l : all) {
            if (Locale.US.equals(l)) { hasUs = true; break; }
        }
        check(hasUs, "getAvailableLocales contains Locale.US");
        String[] langs = Locale.getISOLanguages();
        boolean hasEn = false;
        for (String s : langs) { if ("en".equals(s)) { hasEn = true; break; } }
        check(hasEn, "getISOLanguages contains en");
    }

    static void testCloneEqualsHashCode() {
        Locale us = new Locale("en", "US");
        Locale copy = (Locale) us.clone();
        check(us.equals(copy), "clone equals original");
        check(us.hashCode() == copy.hashCode(), "clone hashCode equal");
        check(us != copy, "clone is a distinct object");
        check(Locale.US.equals(new Locale("en", "US")), "equals same language/country");
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

    static void testFilterLookup() {
        java.util.List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse("en-*");
        java.util.List<Locale> candidates = new java.util.ArrayList<Locale>();
        candidates.add(Locale.US);
        candidates.add(Locale.CANADA);
        java.util.List<Locale> filtered = Locale.filter(ranges, candidates);
        check(filtered.size() == 2, "filter keeps both en candidates");
        check(filtered.contains(Locale.US) && filtered.contains(Locale.CANADA),
              "filter results contain US and CANADA");
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
        check("ja-JP".equals(f.get(0).toLanguageTag())
              && "en-US".equals(f.get(1).toLanguageTag())
              && "de".equals(f.get(2).toLanguageTag()),
              "JDK-9+ filter orders by priority list");
        check("ja-JP".equals(Locale.lookup(pr, tags).toLanguageTag()), "lookup best match");
        check(Locale.lookup(Locale.LanguageRange.parse("fr"), tags) == null, "lookup miss -> null");
        List<String> strs = new java.util.ArrayList<>();
        strs.add("en-us");
        strs.add("JA-jp");
        List<String> ft = Locale.filterTags(Locale.LanguageRange.parse("ja-JP, en"), strs);
        check(ft.size() == 2 && "JA-jp".equals(ft.get(0)) && "en-us".equals(ft.get(1)),
              "JDK-9+ filterTags preserves input case");
        check("JA-jp".equals(Locale.lookupTag(pr, strs)), "JDK-9+ lookupTag preserves input case");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLocaleContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

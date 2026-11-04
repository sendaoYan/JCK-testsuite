/*
 * @test
 * @summary java.util.Locale conformance: white-box structure plus black-box behavior of
 *          language/country accessors, ENGLISH/US/ROOT/CHINA constants, constructors,
 *          forLanguageTag/toLanguageTag, ISO3 codes, display names, and available locales.
 * @run main LocaleTest
 */

import java.io.Serializable;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

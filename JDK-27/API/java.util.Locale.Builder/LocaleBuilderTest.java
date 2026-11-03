/*
 * @test
 * @summary java.util.Locale.Builder conformance: white-box structure plus black-box behavior
 *          of language/region/script/variant/extensions, setLocale, setLanguageTag,
 *          unicode attributes/keywords, clear, and clearExtensions.
 * @run main LocaleBuilderTest
 */

import java.util.Locale;

public class LocaleBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Locale$Builder");
        check(!c.isInterface(), "Builder is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Builder is final");
        c.getMethod("setLanguage", String.class);
        c.getMethod("setRegion", String.class);
        c.getMethod("setScript", String.class);
        c.getMethod("setVariant", String.class);
        c.getMethod("setLocale", Locale.class);
        c.getMethod("setLanguageTag", String.class);
        c.getMethod("setUnicodeLocaleKeyword", String.class, String.class);
        c.getMethod("addUnicodeLocaleAttribute", String.class);
        c.getMethod("clear");
        c.getMethod("clearExtensions");
        c.getMethod("build");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testBuilder();
        testLanguageTag();
        testClearAndExtensions();
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

    static void testLanguageTag() {
        Locale l = new Locale.Builder().setLanguageTag("en-US").build();
        check(Locale.US.equals(l), "setLanguageTag en-US");
        Locale zh = new Locale.Builder().setLanguageTag("zh-Hans-CN").build();
        check("zh".equals(zh.getLanguage()) && "Hans".equals(zh.getScript())
              && "CN".equals(zh.getCountry()), "setLanguageTag with script/region");
    }

    static void testClearAndExtensions() {
        Locale l = new Locale.Builder()
                .setLanguage("en").setRegion("US").setExtension('u', "ca-japanese")
                .clearExtensions().build();
        check(!l.hasExtensions(), "clearExtensions removes extensions");
        check("en".equals(l.getLanguage()) && "US".equals(l.getCountry()),
              "clearExtensions keeps language/region");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

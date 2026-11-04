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


    // ===================== 契约深化：unicode 扩展键值/clearExtensions/非法输入矩阵/错误索引 =====================
    static void testBuilderContracts() {
        Locale.Builder b = new Locale.Builder().setLanguageTag("ja-JP-u-ca-japanese");
        check("ca-japanese".equals(b.build().getExtension('u')), "u extension value");
        check("japanese".equals(b.build().getUnicodeLocaleType("ca")), "unicode keyword type");
        b.setUnicodeLocaleKeyword("nu", "latn");
        check("latn".equals(b.build().getUnicodeLocaleType("nu")),
              "setUnicodeLocaleKeyword adds second keyword");
        b.clearExtensions();
        check("ja-JP".equals(b.build().toLanguageTag()), "clearExtensions drops u");
        check("und".equals(new Locale.Builder().clear().build().toLanguageTag()),
              "clear resets everything to und");
        // 非法输入矩阵（IllformedLocaleException + getErrorIndex）
        java.util.IllformedLocaleException e1 = grab(() -> new Locale.Builder().setRegion("USA"));
        check(e1.getErrorIndex() == 0 && e1.getMessage().startsWith("Ill-formed region"),
              "setRegion(USA) message + index 0");
        java.util.IllformedLocaleException e2 = grab(() -> new Locale.Builder().setScript("latin"));
        check(e2.getErrorIndex() == 0 && e2.getMessage().startsWith("Ill-formed script"),
              "setScript(latin) message + index 0");
        java.util.IllformedLocaleException e3 = grab(() -> new Locale.Builder().setLanguage("e#"));
        check(e3.getErrorIndex() == 0 && e3.getMessage().startsWith("Ill-formed language"),
              "setLanguage(e#) message + index 0");
        java.util.IllformedLocaleException e4 =
              grab(() -> new Locale.Builder().setVariant("TOOLONG-VARIANT-EXTRA-7-89"));
        check(e4.getErrorIndex() == 22, "overlong variant error index 22");
        java.util.IllformedLocaleException e5 =
              grab(() -> new Locale.Builder().setExtension('u', "ca-bogus-x"));
        check(e5.getErrorIndex() == 9, "bad unicode extension value index 9");
        java.util.IllformedLocaleException e6 = grab(() -> new Locale.Builder().setExtension('q', "x"));
        check(e6.getErrorIndex() == 0, "invalid singleton index 0");
        try {
            new Locale.Builder().setLanguageTag("!!");
            check(false, "setLanguageTag(!!) IllformedLocaleException");
        } catch (java.util.IllformedLocaleException ile) {
            check(ile.getErrorIndex() == 0, "setLanguageTag(!!) IllformedLocaleException");
        }
        check("en-Latn-US-x-foo".equals(
              new Locale.Builder().setLanguageTag("EN-latn-US-x-foo").build().toLanguageTag()),
              "setLanguageTag normalizes case and keeps private use");
        new Locale.Builder().setLanguage(null);
        check(true, "setLanguage(null) tolerated (clears)");
        new Locale.Builder().setExtension('x', "foo");
        check(true, "private-use singleton 'x' accepted");
        Locale.Builder reuse = new Locale.Builder().setLanguage("en").setRegion("US");
        Locale v1 = reuse.build();
        Locale v2 = reuse.build();
        check(v1 == v2 && v1.equals(v2),
              "build() reusable; Locale instances are interned per value");
    }

    static java.util.IllformedLocaleException grab(Runnable r) {
        try {
            r.run();
        } catch (java.util.IllformedLocaleException ile) {
            passed++;
            return ile;
        }
        throw new AssertionError("expected IllformedLocaleException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBuilderContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

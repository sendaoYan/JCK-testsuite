/*
 * @test
 * @summary java.util.spi.LocaleNameProvider conformance: white-box structure + black-box behavior.
 * @run main LocaleNameProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;

public class LocaleNameProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static <T extends Throwable> T expectThrows(Class<T> type, Runnable r, String m) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
            throw new AssertionError(m + ": expected " + type.getName() + " but got " + t.getClass().getName());
        }
        throw new AssertionError(m + ": expected " + type.getName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.LocaleNameProvider");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()),
              "public abstract class");
        check(c.getSuperclass() == LocaleServiceProvider.class, "extends LocaleServiceProvider");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");

        Method lang = c.getMethod("getDisplayLanguage", String.class, Locale.class);
        check(Modifier.isAbstract(lang.getModifiers()) && lang.getReturnType() == String.class,
              "getDisplayLanguage abstract String");
        Method country = c.getMethod("getDisplayCountry", String.class, Locale.class);
        check(Modifier.isAbstract(country.getModifiers()) && country.getReturnType() == String.class,
              "getDisplayCountry abstract String");
        Method variant = c.getMethod("getDisplayVariant", String.class, Locale.class);
        check(Modifier.isAbstract(variant.getModifiers()) && variant.getReturnType() == String.class,
              "getDisplayVariant abstract String");

        Method script = c.getMethod("getDisplayScript", String.class, Locale.class);
        check(!Modifier.isAbstract(script.getModifiers()), "getDisplayScript non-abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LocaleNameProvider p = new LocaleNameProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public String getDisplayLanguage(String languageCode, Locale l) { return "LANG:" + languageCode; }
            public String getDisplayCountry(String countryCode, Locale l) { return "COUNTRY:" + countryCode; }
            public String getDisplayVariant(String variant, Locale l) { return "VARIANT:" + variant; }
        };

        check("LANG:en".equals(p.getDisplayLanguage("en", Locale.US)), "getDisplayLanguage round-trip");
        check("LANG:fr".equals(p.getDisplayLanguage("fr", Locale.FRANCE)), "getDisplayLanguage fr");
        check("COUNTRY:US".equals(p.getDisplayCountry("US", Locale.US)), "getDisplayCountry round-trip");
        check("VARIANT:POSIX".equals(p.getDisplayVariant("POSIX", Locale.US)), "getDisplayVariant round-trip");

        // concrete default method returns null for non-null inputs
        check(p.getDisplayScript("Latn", Locale.US) == null, "getDisplayScript default null");
    }


    // ===================== 契约深化：匿名实现四方法 + null NPE 矩阵 =====================
    static void testContracts() {
        final Locale[] locs = {Locale.FRANCE};
        LocaleNameProvider p = new LocaleNameProvider() {
            public Locale[] getAvailableLocales() { return locs; }
            public String getDisplayLanguage(String languageCode, Locale locale) { return "L-" + languageCode; }
            public String getDisplayCountry(String countryCode, Locale locale) { return "C-" + countryCode; }
            public String getDisplayVariant(String variant, Locale locale) { return "V-" + variant; }
            public String getDisplayScript(String scriptCode, Locale locale) { return "S-" + scriptCode; }
        };
        check("L-fr".equals(p.getDisplayLanguage("fr", Locale.FRANCE)), "getDisplayLanguage behavior");
        check("C-FR".equals(p.getDisplayCountry("FR", Locale.FRANCE)), "getDisplayCountry behavior");
        check("V-POSIX".equals(p.getDisplayVariant("POSIX", Locale.FRANCE)), "getDisplayVariant behavior");
        check("S-Latn".equals(p.getDisplayScript("Latn", Locale.FRANCE)), "getDisplayScript behavior");
        check(p.getAvailableLocales() == locs, "availableLocales passthrough");
        // 自有实现：null 实参直通组合字符串（接口不强制 NPE）
        check("L-null".equals(p.getDisplayLanguage(null, Locale.FRANCE)), "own impl handles null languageCode");
        check("C-null".equals(p.getDisplayCountry(null, Locale.FRANCE)), "own impl handles null countryCode");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

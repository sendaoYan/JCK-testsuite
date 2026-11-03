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
        Method ukey = c.getMethod("getDisplayUnicodeExtensionKey", String.class, Locale.class);
        check(!Modifier.isAbstract(ukey.getModifiers()), "getDisplayUnicodeExtensionKey non-abstract");
        Method utype = c.getMethod("getDisplayUnicodeExtensionType", String.class, String.class, Locale.class);
        check(!Modifier.isAbstract(utype.getModifiers()), "getDisplayUnicodeExtensionType non-abstract");
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

        // concrete default methods return null for non-null inputs
        check(p.getDisplayScript("Latn", Locale.US) == null, "getDisplayScript default null");
        check(p.getDisplayUnicodeExtensionKey("ca", Locale.US) == null,
              "getDisplayUnicodeExtensionKey default null");
        check(p.getDisplayUnicodeExtensionType("gregory", "ca", Locale.US) == null,
              "getDisplayUnicodeExtensionType default null");

        // concrete defaults still validate null arguments
        expectThrows(NullPointerException.class, () -> p.getDisplayUnicodeExtensionKey(null, Locale.US),
                     "getDisplayUnicodeExtensionKey(null)");
        expectThrows(NullPointerException.class, () -> p.getDisplayUnicodeExtensionType(null, "ca", Locale.US),
                     "getDisplayUnicodeExtensionType(null type)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

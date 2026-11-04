/*
 * @test
 * @summary java.util.spi.LocaleServiceProvider conformance: white-box structure + black-box behavior.
 * @run main LocaleServiceProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class LocaleServiceProviderTest {
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
        Class<?> c = Class.forName("java.util.spi.LocaleServiceProvider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass is Object");

        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()),
              "constructor is protected");

        Method avail = c.getMethod("getAvailableLocales");
        check(Modifier.isPublic(avail.getModifiers()) && Modifier.isAbstract(avail.getModifiers()),
              "getAvailableLocales public abstract");
        check(avail.getReturnType() == Locale[].class, "getAvailableLocales returns Locale[]");

        Method sup = c.getMethod("isSupportedLocale", Locale.class);
        check(Modifier.isPublic(sup.getModifiers()) && !Modifier.isAbstract(sup.getModifiers()),
              "isSupportedLocale public non-abstract");
        check(sup.getReturnType() == boolean.class, "isSupportedLocale returns boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LocaleServiceProvider p = new LocaleServiceProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US, Locale.FRANCE};
            }
        };
        Locale[] locales = p.getAvailableLocales();
        check(locales.length == 2, "getAvailableLocales length == 2");
        check(Locale.US.equals(locales[0]) && Locale.FRANCE.equals(locales[1]),
              "getAvailableLocales contents preserved");

        check(p.isSupportedLocale(Locale.US), "isSupportedLocale(US)");
        check(p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale(FRANCE)");
        check(!p.isSupportedLocale(Locale.JAPAN), "isSupportedLocale(JAPAN) false");

        // extensions are stripped before comparison
        Locale usExt = Locale.forLanguageTag("en-US-u-ca-gregory");
        check(p.isSupportedLocale(usExt), "isSupportedLocale strips extensions");

        // a fresh array must be returned each call (no shared mutable state)
        p.getAvailableLocales()[0] = Locale.JAPAN;
        check(Locale.US.equals(p.getAvailableLocales()[0]),
              "getAvailableLocales returns fresh array");

        expectThrows(NullPointerException.class, () -> p.isSupportedLocale(null),
                     "isSupportedLocale(null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.text.spi.DateFormatSymbolsProvider conformance: white-box hierarchy + black-box anonymous-subclass factory behavior.
 * @run main DateFormatSymbolsProviderTest
 */

import java.text.DateFormatSymbols;
import java.text.spi.DateFormatSymbolsProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DateFormatSymbolsProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.spi.DateFormatSymbolsProvider");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"), "extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("getInstance", Locale.class) != null, "getInstance(Locale)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DateFormatSymbolsProvider p = new DateFormatSymbolsProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DateFormatSymbols getInstance(Locale l) {
                return DateFormatSymbols.getInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(!p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE false");
        check(p.getInstance(Locale.US) != null, "getInstance");
        check(p.getInstance(Locale.US) instanceof DateFormatSymbols, "factory returns DateFormatSymbols");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

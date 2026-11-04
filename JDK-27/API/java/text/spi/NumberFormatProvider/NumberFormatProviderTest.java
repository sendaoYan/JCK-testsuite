/*
 * @test
 * @summary java.text.spi.NumberFormatProvider conformance: white-box hierarchy + black-box factory methods incl. getCompactNumberInstance.
 * @run main NumberFormatProviderTest
 */

import java.text.NumberFormat;
import java.text.spi.NumberFormatProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class NumberFormatProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> t, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (t.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " but got " + x, x);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.spi.NumberFormatProvider");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"), "extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("getCurrencyInstance", Locale.class) != null, "getCurrencyInstance(Locale)");
        check(c.getMethod("getIntegerInstance", Locale.class) != null, "getIntegerInstance(Locale)");
        check(c.getMethod("getNumberInstance", Locale.class) != null, "getNumberInstance(Locale)");
        check(c.getMethod("getPercentInstance", Locale.class) != null, "getPercentInstance(Locale)");
        check(c.getMethod("getCompactNumberInstance", Locale.class, NumberFormat.Style.class) != null,
              "getCompactNumberInstance(Locale,Style)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        NumberFormatProvider p = new NumberFormatProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public NumberFormat getCurrencyInstance(Locale l) {
                return NumberFormat.getCurrencyInstance(Locale.US);
            }
            public NumberFormat getIntegerInstance(Locale l) {
                return NumberFormat.getIntegerInstance(Locale.US);
            }
            public NumberFormat getNumberInstance(Locale l) {
                return NumberFormat.getNumberInstance(Locale.US);
            }
            public NumberFormat getPercentInstance(Locale l) {
                return NumberFormat.getPercentInstance(Locale.US);
            }
            public NumberFormat getCompactNumberInstance(Locale l, NumberFormat.Style style) {
                return NumberFormat.getCompactNumberInstance(l, style);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(!p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE false");
        check(p.getCurrencyInstance(Locale.US) != null, "getCurrencyInstance");
        check(p.getIntegerInstance(Locale.US) != null, "getIntegerInstance");
        check(p.getNumberInstance(Locale.US) != null, "getNumberInstance");
        check(p.getPercentInstance(Locale.US) != null, "getPercentInstance");
        NumberFormat compact = p.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        check(compact != null, "overridden getCompactNumberInstance");
        check("1K".equals(compact.format(1000)), "compact instance formats 1000 -> 1K");

        // default implementation throws UnsupportedOperationException when not overridden
        NumberFormatProvider plain = new NumberFormatProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public NumberFormat getCurrencyInstance(Locale l) { return NumberFormat.getCurrencyInstance(Locale.US); }
            public NumberFormat getIntegerInstance(Locale l) { return NumberFormat.getIntegerInstance(Locale.US); }
            public NumberFormat getNumberInstance(Locale l) { return NumberFormat.getNumberInstance(Locale.US); }
            public NumberFormat getPercentInstance(Locale l) { return NumberFormat.getPercentInstance(Locale.US); }
        };
        expectThrows(() -> plain.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT),
                     UnsupportedOperationException.class, "default getCompactNumberInstance throws UOE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

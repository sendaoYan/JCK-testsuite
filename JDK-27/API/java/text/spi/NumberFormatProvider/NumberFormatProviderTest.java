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


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    static void assertSupportedLocaleMatrix(java.util.spi.LocaleServiceProvider p, String tag) {
        check(p.isSupportedLocale(Locale.US), tag + ": isSupportedLocale(US) true");
        check(!p.isSupportedLocale(Locale.ENGLISH),
              tag + ": isSupportedLocale(en) false (only en_US advertised)");
        check(p.isSupportedLocale(Locale.forLanguageTag("en-US-u-ca-buddhist")),
              tag + ": language-tag extensions are stripped before matching");
        check(!p.isSupportedLocale(Locale.FRANCE), tag + ": isSupportedLocale(fr_FR) false");
        check(!p.isSupportedLocale(new Locale("xx", "YY")), tag + ": unknown locale false");
        expectX(NullPointerException.class, () -> p.isSupportedLocale(null),
                tag + ": isSupportedLocale(null) -> NPE");
    }

    static void testSpiClassStructureContract(Class<?> c, String[] factoryMethods, Class<?>[][] params)
            throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), c.getName() + " abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"),
              c.getName() + " extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor (SPI, not instantiated directly)");
        for (int i = 0; i < factoryMethods.length; i++) {
            check(c.getMethod(factoryMethods[i], params[i]) != null,
                  "declares " + factoryMethods[i] + java.util.Arrays.toString(params[i]));
        }
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAvailableLocales") && names.contains("isSupportedLocale"),
              "inherits LocaleServiceProvider availability protocol");
    }

    static void testContractDeepening() throws Exception {
        testSpiClassStructureContract(java.text.spi.NumberFormatProvider.class,
                new String[] {"getNumberInstance", "getIntegerInstance", "getCurrencyInstance", "getPercentInstance"},
                new Class<?>[][] {{Locale.class}, {Locale.class}, {Locale.class}, {Locale.class}});

        java.text.spi.NumberFormatProvider p = new java.text.spi.NumberFormatProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public java.text.NumberFormat getNumberInstance(Locale l) { return java.text.NumberFormat.getNumberInstance(Locale.US); }
            public java.text.NumberFormat getIntegerInstance(Locale l) { return java.text.NumberFormat.getIntegerInstance(Locale.US); }
            public java.text.NumberFormat getCurrencyInstance(Locale l) { return java.text.NumberFormat.getCurrencyInstance(Locale.US); }
            public java.text.NumberFormat getPercentInstance(Locale l) { return java.text.NumberFormat.getPercentInstance(Locale.US); }
        };
        assertSupportedLocaleMatrix(p, "NFP");
        check("1,234.567".equals(p.getNumberInstance(Locale.CANADA).format(1234.567)),
              "number instance wired to en_US data: 1,234.567");
        check("1,235".equals(p.getIntegerInstance(Locale.CANADA).format(1234.567)),
              "integer instance rounds: 1,235");
        check(p.getCurrencyInstance(Locale.CANADA).format(1234.567)
              .equals(java.text.NumberFormat.getCurrencyInstance(Locale.US).format(1234.567)),
              "currency output equals framework en_US currency for same input");
        check(p.getPercentInstance(Locale.CANADA).format(0.4567)
              .equals(java.text.NumberFormat.getPercentInstance(Locale.US).format(0.4567)),
              "percent output equals framework en_US percent");
        java.text.ParsePosition pp = new java.text.ParsePosition(0);
        check(p.getNumberInstance(Locale.CANADA).parse("1,234.5", pp).doubleValue() == 1234.5
              && pp.getIndex() == 7, "returned instance parses en_US syntax");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

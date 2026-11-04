/*
 * @test
 * @summary java.text.spi.DecimalFormatSymbolsProvider conformance: white-box hierarchy + black-box anonymous-subclass factory behavior.
 * @run main DecimalFormatSymbolsProviderTest
 */

import java.text.DecimalFormatSymbols;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DecimalFormatSymbolsProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.spi.DecimalFormatSymbolsProvider");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"), "extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("getInstance", Locale.class) != null, "getInstance(Locale)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DecimalFormatSymbolsProvider p = new DecimalFormatSymbolsProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DecimalFormatSymbols getInstance(Locale l) {
                return DecimalFormatSymbols.getInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(!p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE false");
        check(p.getInstance(Locale.US) != null, "getInstance");
        check(p.getInstance(Locale.US) instanceof DecimalFormatSymbols, "factory returns DecimalFormatSymbols");
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
        testSpiClassStructureContract(java.text.spi.DecimalFormatSymbolsProvider.class,
                new String[] {"getInstance"}, new Class<?>[][] {{Locale.class}});

        java.text.spi.DecimalFormatSymbolsProvider p = new java.text.spi.DecimalFormatSymbolsProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public java.text.DecimalFormatSymbols getInstance(Locale l) { return java.text.DecimalFormatSymbols.getInstance(Locale.US); }
        };
        assertSupportedLocaleMatrix(p, "DFSYM");
        java.text.DecimalFormatSymbols us = p.getInstance(Locale.CANADA);
        check(us.getDecimalSeparator() == '.' && us.getGroupingSeparator() == ',',
              "en_US symbols: decimal '.', grouping ','");
        java.text.DecimalFormatSymbols de = java.text.DecimalFormatSymbols.getInstance(java.util.Locale.GERMANY);
        check(de.getDecimalSeparator() == ',' && de.getGroupingSeparator() == '.',
              "framework de symbols: decimal ',', grouping '.' (provider pattern parity)");
        java.text.DecimalFormat fmt = new java.text.DecimalFormat("#,##0.0", us);
        check("1,234.5".equals(fmt.format(1234.5)),
              "symbols returned by provider drive DecimalFormat output");
        java.text.DecimalFormatSymbols again = p.getInstance(Locale.CANADA);
        check(again != us, "fresh instance per call");
        check(again.equals(us), "instances equal (value semantics)");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.text.spi.DateFormatSymbolsProvider conformance: hierarchy and getInstance.
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

    static void testDateFormatSymbolsProvider() {
        DateFormatSymbolsProvider p = new DateFormatSymbolsProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DateFormatSymbols getInstance(Locale l) {
                return DateFormatSymbols.getInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getInstance(Locale.US) != null, "getInstance");
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
        testSpiClassStructureContract(java.text.spi.DateFormatSymbolsProvider.class,
                new String[] {"getInstance"}, new Class<?>[][] {{Locale.class}});

        java.text.spi.DateFormatSymbolsProvider p = new java.text.spi.DateFormatSymbolsProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public java.text.DateFormatSymbols getInstance(Locale l) { return new java.text.DateFormatSymbols(Locale.ENGLISH); }
        };
        assertSupportedLocaleMatrix(p, "DFSP");
        java.text.DateFormatSymbols s1 = p.getInstance(Locale.CANADA);
        check(java.util.Arrays.equals(s1.getMonths(), new java.text.DateFormatSymbols(Locale.ENGLISH).getMonths()),
              "symbols wired to ENGLISH month table");
        check(java.util.Arrays.equals(s1.getEras(), new java.text.DateFormatSymbols(Locale.ENGLISH).getEras()),
              "eras table equal");
        s1.setMonths(new String[]{"M0", "M1"});
        check(!java.util.Arrays.equals(p.getInstance(Locale.CANADA).getMonths(), s1.getMonths()),
              "each getInstance call returns an independent instance");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM", Locale.ENGLISH);
        sdf.setDateFormatSymbols(p.getInstance(Locale.ENGLISH));
        check("March".equals(sdf.format(new java.util.GregorianCalendar(2024, java.util.Calendar.MARCH, 7).getTime())),
              "symbols returned by provider drive SimpleDateFormat");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testDateFormatSymbolsProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

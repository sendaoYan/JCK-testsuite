/*
 * @test
 * @summary java.text.spi.DateFormatProvider conformance: white-box hierarchy + black-box anonymous-subclass factory behavior.
 * @run main DateFormatProviderTest
 */

import java.text.DateFormat;
import java.text.spi.DateFormatProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class DateFormatProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.spi.DateFormatProvider");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"), "extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("getTimeInstance", int.class, Locale.class) != null, "getTimeInstance(int,Locale)");
        check(c.getMethod("getDateInstance", int.class, Locale.class) != null, "getDateInstance(int,Locale)");
        check(c.getMethod("getDateTimeInstance", int.class, int.class, Locale.class) != null,
              "getDateTimeInstance(int,int,Locale)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DateFormatProvider p = new DateFormatProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public DateFormat getTimeInstance(int style, Locale l) {
                return DateFormat.getTimeInstance(style, Locale.US);
            }
            public DateFormat getDateInstance(int style, Locale l) {
                return DateFormat.getDateInstance(style, Locale.US);
            }
            public DateFormat getDateTimeInstance(int ds, int ts, Locale l) {
                return DateFormat.getDateTimeInstance(ds, ts, Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(!p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE false");
        check(p.getTimeInstance(DateFormat.SHORT, Locale.US) != null, "getTimeInstance");
        check(p.getDateInstance(DateFormat.SHORT, Locale.US) != null, "getDateInstance");
        check(p.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US) != null,
              "getDateTimeInstance");
        check(p.getDateInstance(DateFormat.SHORT, Locale.US) instanceof DateFormat, "factory returns DateFormat");
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
        testSpiClassStructureContract(java.text.spi.DateFormatProvider.class,
                new String[] {"getDateInstance", "getTimeInstance", "getDateTimeInstance"},
                new Class<?>[][] {{int.class, Locale.class}, {int.class, Locale.class},
                        {int.class, int.class, Locale.class}});

        java.text.spi.DateFormatProvider p = new java.text.spi.DateFormatProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public java.text.DateFormat getDateInstance(int s, Locale l) { return java.text.DateFormat.getDateInstance(s, Locale.ENGLISH); }
            public java.text.DateFormat getTimeInstance(int s, Locale l) { return java.text.DateFormat.getTimeInstance(s, Locale.ENGLISH); }
            public java.text.DateFormat getDateTimeInstance(int d, int t, Locale l) { return java.text.DateFormat.getDateTimeInstance(d, t, Locale.ENGLISH); }
        };
        assertSupportedLocaleMatrix(p, "DFP");
        java.util.Date fixed = new java.util.GregorianCalendar(2024, java.util.Calendar.MARCH, 7).getTime();
        for (int style : new int[] {java.text.DateFormat.FULL, java.text.DateFormat.LONG,
                java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT}) {
            check(p.getDateInstance(style, Locale.CANADA).format(fixed)
                  .equals(java.text.DateFormat.getDateInstance(style, Locale.ENGLISH).format(fixed)),
                  "date style " + style + " output equals framework ENGLISH");
            check(p.getTimeInstance(style, Locale.CANADA) != null, "time style " + style + " instance");
        }
        check(p.getDateTimeInstance(java.text.DateFormat.FULL, java.text.DateFormat.SHORT, Locale.CANADA)
              .format(fixed).contains(p.getDateInstance(java.text.DateFormat.FULL, Locale.CANADA).format(fixed)),
              "date-time composite contains the date part");
        java.text.ParsePosition pp = new java.text.ParsePosition(0);
        check(p.getTimeInstance(java.text.DateFormat.SHORT, Locale.CANADA)
              .parse(p.getTimeInstance(java.text.DateFormat.SHORT, Locale.CANADA).format(fixed), pp) != null,
              "returned time instance round-trips its own output");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.text.spi.CollatorProvider conformance: white-box hierarchy + black-box anonymous-subclass factory behavior.
 * @run main CollatorProviderTest
 */

import java.text.Collator;
import java.text.spi.CollatorProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class CollatorProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.spi.CollatorProvider");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"), "extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("getInstance", Locale.class) != null, "getInstance(Locale)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CollatorProvider p = new CollatorProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public Collator getInstance(Locale l) {
                return Collator.getInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(!p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE false");
        check(p.getInstance(Locale.US) != null, "getInstance");
        check(p.getInstance(Locale.US) instanceof Collator, "factory returns Collator");
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
        testSpiClassStructureContract(java.text.spi.CollatorProvider.class,
                new String[] {"getInstance"}, new Class<?>[][] {{Locale.class}});

        java.text.spi.CollatorProvider p = new java.text.spi.CollatorProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public java.text.Collator getInstance(Locale l) { return java.text.Collator.getInstance(Locale.ENGLISH); }
        };
        assertSupportedLocaleMatrix(p, "CFP");
        java.text.Collator c = p.getInstance(Locale.CANADA);
        check(c.compare("apple", "banana") == -1 && c.compare("apple", "apple") == 0,
              "returned collator orders strings");
        check(c.getCollationKey("apple").compareTo(c.getCollationKey("banana")) < 0,
              "collation keys consistent with compare");
        c.setStrength(java.text.Collator.PRIMARY);
        check(c.compare("Apple", "apple") == 0, "returned collator honors setStrength");
        check(p.getInstance(Locale.CANADA) instanceof java.text.RuleBasedCollator,
              "framework collators are RuleBasedCollator");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

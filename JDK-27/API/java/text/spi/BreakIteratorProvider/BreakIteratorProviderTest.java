/*
 * @test
 * @summary java.text.spi.BreakIteratorProvider conformance: white-box hierarchy + black-box anonymous-subclass factory behavior.
 * @run main BreakIteratorProviderTest
 */

import java.text.BreakIterator;
import java.text.spi.BreakIteratorProvider;
import java.util.Locale;
import java.util.spi.LocaleServiceProvider;

public class BreakIteratorProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.spi.BreakIteratorProvider");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.util.spi.LocaleServiceProvider"), "extends LocaleServiceProvider");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("getWordInstance", Locale.class) != null, "getWordInstance(Locale)");
        check(c.getMethod("getLineInstance", Locale.class) != null, "getLineInstance(Locale)");
        check(c.getMethod("getCharacterInstance", Locale.class) != null, "getCharacterInstance(Locale)");
        check(c.getMethod("getSentenceInstance", Locale.class) != null, "getSentenceInstance(Locale)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BreakIteratorProvider p = new BreakIteratorProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public BreakIterator getWordInstance(Locale l) {
                return BreakIterator.getWordInstance(Locale.US);
            }
            public BreakIterator getLineInstance(Locale l) {
                return BreakIterator.getLineInstance(Locale.US);
            }
            public BreakIterator getCharacterInstance(Locale l) {
                return BreakIterator.getCharacterInstance(Locale.US);
            }
            public BreakIterator getSentenceInstance(Locale l) {
                return BreakIterator.getSentenceInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale US");
        check(!p.isSupportedLocale(Locale.FRANCE), "isSupportedLocale FRANCE false");
        check(p.getWordInstance(Locale.US) != null, "getWordInstance");
        check(p.getLineInstance(Locale.US) != null, "getLineInstance");
        check(p.getCharacterInstance(Locale.US) != null, "getCharacterInstance");
        check(p.getSentenceInstance(Locale.US) != null, "getSentenceInstance");
        check(p.getWordInstance(Locale.US) instanceof BreakIterator, "factory returns BreakIterator");
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
        testSpiClassStructureContract(java.text.spi.BreakIteratorProvider.class,
                new String[] {"getWordInstance", "getLineInstance", "getCharacterInstance", "getSentenceInstance"},
                new Class<?>[][] {{Locale.class}, {Locale.class}, {Locale.class}, {Locale.class}});

        java.text.spi.BreakIteratorProvider p = new java.text.spi.BreakIteratorProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public java.text.BreakIterator getWordInstance(Locale l) { return java.text.BreakIterator.getWordInstance(Locale.US); }
            public java.text.BreakIterator getLineInstance(Locale l) { return java.text.BreakIterator.getLineInstance(Locale.US); }
            public java.text.BreakIterator getCharacterInstance(Locale l) { return java.text.BreakIterator.getCharacterInstance(Locale.US); }
            public java.text.BreakIterator getSentenceInstance(Locale l) { return java.text.BreakIterator.getSentenceInstance(Locale.US); }
        };
        assertSupportedLocaleMatrix(p, "BIP");
        java.text.BreakIterator wi = p.getWordInstance(Locale.CANADA);
        wi.setText("Hello, world. Foo.");
        check(wi.first() == 0 && wi.next() == 5, "word instance returned by provider is functional");
        check(p.getLineInstance(Locale.CANADA) != null && p.getCharacterInstance(Locale.CANADA) != null
              && p.getSentenceInstance(Locale.CANADA) != null,
              "factory methods accept any locale argument (no validation contract)");
        check(p.getAvailableLocales().length == 1 && p.getAvailableLocales()[0] == Locale.US,
              "getAvailableLocales returns the advertised array");
    }
    public static void main(String[] args) throws Exception {
        testContractDeepening();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

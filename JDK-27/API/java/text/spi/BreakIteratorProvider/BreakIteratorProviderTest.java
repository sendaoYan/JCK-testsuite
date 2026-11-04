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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.text.spi.BreakIteratorProvider conformance: hierarchy and instance methods.
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

    static void testBreakIteratorProvider() {
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
    }

    public static void main(String[] args) {
        testBreakIteratorProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

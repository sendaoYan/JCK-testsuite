/*
 * @test
 * @summary java.text.spi.CollatorProvider conformance: hierarchy and getInstance.
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

    static void testCollatorProvider() {
        CollatorProvider p = new CollatorProvider() {
            public Locale[] getAvailableLocales() {
                return new Locale[]{Locale.US};
            }
            public Collator getInstance(Locale l) {
                return Collator.getInstance(Locale.US);
            }
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getInstance(Locale.US) != null, "getInstance");
        check(p.isSupportedLocale(Locale.US), "isSupportedLocale");
    }

    public static void main(String[] args) {
        testCollatorProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

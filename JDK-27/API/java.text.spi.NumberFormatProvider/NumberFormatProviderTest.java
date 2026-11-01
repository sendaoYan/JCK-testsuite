/*
 * @test
 * @summary java.text.spi.NumberFormatProvider conformance: hierarchy and instance methods.
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

    static void testNumberFormatProvider() {
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
        };
        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check(p.getCurrencyInstance(Locale.US) != null, "getCurrencyInstance");
        check(p.getIntegerInstance(Locale.US) != null, "getIntegerInstance");
        check(p.getNumberInstance(Locale.US) != null, "getNumberInstance");
        check(p.getPercentInstance(Locale.US) != null, "getPercentInstance");
    }

    public static void main(String[] args) {
        testNumberFormatProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

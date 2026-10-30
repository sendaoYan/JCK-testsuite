/*
 * @test
 * @summary java.util.Currency API conformance: getCurrencyCode, getDefaultFractionDigits,
 *          getInstance caching/equality, and getInstance(Locale).
 * @run main CurrencyTest
 */

import java.util.Currency;
import java.util.Locale;

public class CurrencyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUsd() {
        Currency usd = Currency.getInstance("USD");
        check("USD".equals(usd.getCurrencyCode()), "getCurrencyCode");
        check(usd.getDefaultFractionDigits() == 2, "getDefaultFractionDigits");
        Currency usd2 = Currency.getInstance("USD");
        check(usd == usd2 || usd.equals(usd2), "getInstance returns equal instance");
    }

    static void testFromLocale() {
        Currency us = Currency.getInstance(Locale.US);
        check("USD".equals(us.getCurrencyCode()), "getInstance(Locale.US)");
    }

    public static void main(String[] args) {
        testUsd();
        testFromLocale();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.util.Currency conformance: white-box structure plus black-box behavior of
 *          getCurrencyCode, getDefaultFractionDigits, getNumericCode, getSymbol, getDisplayName,
 *          getInstance caching/equality, and getInstance(Locale).
 * @run main CurrencyTest
 */

import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;

public class CurrencyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Currency");
        check(!c.isInterface(), "Currency is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Currency is final");
        check(Serializable.class.isAssignableFrom(c), "Currency implements Serializable");
        c.getMethod("getInstance", String.class);
        c.getMethod("getInstance", Locale.class);
        c.getMethod("getCurrencyCode");
        c.getMethod("getDefaultFractionDigits");
        c.getMethod("getNumericCode");
        c.getMethod("getNumericCodeAsString");
        c.getMethod("getSymbol", Locale.class);
        c.getMethod("getDisplayName", Locale.class);
        c.getMethod("getAvailableCurrencies");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testUsd();
        testOtherCodes();
        testFromLocale();
        testAvailableCurrencies();
    }

    static void testUsd() {
        Currency usd = Currency.getInstance("USD");
        check("USD".equals(usd.getCurrencyCode()), "getCurrencyCode");
        check(usd.getDefaultFractionDigits() == 2, "getDefaultFractionDigits");
        check(usd.getNumericCode() == 840, "getNumericCode 840");
        check("840".equals(usd.getNumericCodeAsString()), "getNumericCodeAsString");
        check("$".equals(usd.getSymbol(Locale.US)), "getSymbol(Locale.US) == $");
        check("US Dollar".equals(usd.getDisplayName(Locale.US)), "getDisplayName(Locale.US) == US Dollar");

        Currency usd2 = Currency.getInstance("USD");
        check(usd == usd2 || usd.equals(usd2), "getInstance returns equal instance");
    }

    static void testOtherCodes() {
        Currency eur = Currency.getInstance("EUR");
        check("EUR".equals(eur.getCurrencyCode()) && eur.getDefaultFractionDigits() == 2,
              "EUR fraction digits 2");
        Currency jpy = Currency.getInstance("JPY");
        check("JPY".equals(jpy.getCurrencyCode()) && jpy.getDefaultFractionDigits() == 0,
              "JPY fraction digits 0");
    }

    static void testFromLocale() {
        Currency us = Currency.getInstance(Locale.US);
        check("USD".equals(us.getCurrencyCode()), "getInstance(Locale.US)");
    }

    static void testAvailableCurrencies() {
        Set<Currency> all = Currency.getAvailableCurrencies();
        check(!all.isEmpty(), "getAvailableCurrencies non-empty");
        check(all.contains(Currency.getInstance("USD")), "available currencies contain USD");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

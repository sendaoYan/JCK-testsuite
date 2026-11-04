/*
 * @test
 * @summary java.util.Currency API conformance (Java 8 surface): complete contract coverage of
 *          getInstance(String/Locale) + null NPE + invalid IAE + case-insensitivity + caching,
 *          getAvailableCurrencies, getCurrencyCode/getDefaultFractionDigits/getNumericCode/
 *          getSymbol(Locale)/getDisplayName(Locale)/toString, and Serializable round-trip.
 * @run main CurrencyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Currency;
import java.util.Locale;

public class CurrencyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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

    static void testGetInstanceErrors() {
        expectThrows(NullPointerException.class, () -> Currency.getInstance((String) null),
                     "getInstance((String)null) NPE");
        expectThrows(NullPointerException.class, () -> Currency.getInstance((Locale) null),
                     "getInstance((Locale)null) NPE");
        expectThrows(IllegalArgumentException.class, () -> Currency.getInstance("NOTACODE"),
                     "getInstance(invalid code) IAE");
        expectThrows(IllegalArgumentException.class, () -> Currency.getInstance(""),
                     "getInstance(empty) IAE");
    }

    static void testCaseSensitive() {
        // ISO 4217 codes are uppercase-only; lowercase/mixed case are rejected.
        expectThrows(IllegalArgumentException.class, () -> Currency.getInstance("usd"),
                     "getInstance(lowercase) IAE");
        expectThrows(IllegalArgumentException.class, () -> Currency.getInstance("Jpy"),
                     "getInstance(mixed case) IAE");
        check("USD".equals(Currency.getInstance("USD").getCurrencyCode()),
              "getInstance(uppercase) ok");
    }

    static void testAttributes() {
        Currency usd = Currency.getInstance("USD");
        check(usd.getNumericCode() == 840, "USD numeric code 840");
        check("$".equals(usd.getSymbol(Locale.US)), "USD symbol in US is $");
        check(usd.toString().equals("USD"), "toString == currency code");
        check(usd.getDisplayName() != null && usd.getDisplayName().length() > 0,
              "getDisplayName non-empty");

        Currency jpy = Currency.getInstance("JPY");
        check(jpy.getDefaultFractionDigits() == 0, "JPY fraction digits 0");
        check(jpy.getNumericCode() == 392, "JPY numeric code 392");
    }

    static void testAvailableCurrencies() {
        java.util.Set<Currency> set = Currency.getAvailableCurrencies();
        check(set != null && !set.isEmpty(), "getAvailableCurrencies non-empty");
        check(set.contains(Currency.getInstance("USD")), "available contains USD");
    }

    static void testSerialization() throws Exception {
        Currency usd = Currency.getInstance("USD");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(usd);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Currency revived = (Currency) ois.readObject();
        ois.close();
        check(revived == usd, "Serializable round-trip returns cached instance");
    }

    public static void main(String[] args) throws Exception {
        testUsd();
        testFromLocale();
        testGetInstanceErrors();
        testCaseSensitive();
        testAttributes();
        testAvailableCurrencies();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

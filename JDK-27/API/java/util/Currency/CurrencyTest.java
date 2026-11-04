/*
 * @test
 * @summary java.util.Currency conformance: white-box structure plus black-box behavior of
 *          getCurrencyCode, getDefaultFractionDigits, getNumericCode, getSymbol, getDisplayName,
 *          getInstance caching/equality, and getInstance(Locale).
 * @run main CurrencyTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
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
    static void testBlackBox() throws Exception {
        testUsd();
        testOtherCodes();
        testFromLocale();
        testAvailableCurrencies();
        testGetInstanceErrors();
        testSerialization();
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
        check(jpy.getNumericCode() == 392, "JPY numeric code 392");
        check("392".equals(jpy.getNumericCodeAsString()), "JPY numeric code as string 392");
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

    static void testGetInstanceErrors() {
        assertThrows(NullPointerException.class,
                () -> Currency.getInstance((String) null), "getInstance((String)null) NPE");
        assertThrows(NullPointerException.class,
                () -> Currency.getInstance((Locale) null), "getInstance((Locale)null) NPE");
        assertThrows(IllegalArgumentException.class,
                () -> Currency.getInstance("NOTACODE"), "getInstance(invalid code) IAE");
        assertThrows(IllegalArgumentException.class,
                () -> Currency.getInstance(""), "getInstance(empty) IAE");
        // ISO 4217 codes are uppercase-only; lowercase/mixed case are rejected.
        assertThrows(IllegalArgumentException.class,
                () -> Currency.getInstance("usd"), "getInstance(lowercase) IAE");
        assertThrows(IllegalArgumentException.class,
                () -> Currency.getInstance("Jpy"), "getInstance(mixed case) IAE");
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
        check("USD".equals(revived.getCurrencyCode()), "revived currency code");
    }


    // ===================== 契约深化：实例缓存/数值码/显示名/异常消息矩阵 =====================
    static void testCurrencyContracts() {
        Currency usd = Currency.getInstance("USD");
        check(usd == Currency.getInstance("USD"), "getInstance caches single instance");
        check(usd.equals(Currency.getInstance(Locale.US))
              && usd.hashCode() == Currency.getInstance("USD").hashCode(),
              "equals/hashCode consistent across lookups");
        Currency cny = Currency.getInstance(Locale.CHINA);
        check("CNY".equals(cny.getCurrencyCode()), "getInstance(Locale.CHINA) -> CNY");
        check("CNY".equals(Currency.getInstance("CNY").getCurrencyCode()), "getInstance(code CNY)");
        check(cny.getDefaultFractionDigits() == 2 && cny.getNumericCode() == 156, "CNY fd 2 numeric 156");
        check(Currency.getInstance("EUR").getDefaultFractionDigits() == 2, "EUR fd 2");
        check("US Dollar".equals(usd.getDisplayName(Locale.US)), "getDisplayName(US)");
        check("dollar des \u00c9tats-Unis".equals(usd.getDisplayName(Locale.FRENCH)),
              "getDisplayName(FRENCH) localized");
        check("$".equals(usd.getSymbol(Locale.US)), "USD symbol in US is $");
        check("CN\u00a5".equals(Currency.getInstance("CNY").getSymbol(Locale.US)),
              "CNY symbol in US is CN\u00a5 (CLDR data)");
        try { Currency.getInstance("BOGUS"); check(false, "BOGUS code IAE"); }
        catch (IllegalArgumentException iae) {
            check(true, "BOGUS code IAE");
            check(iae.getMessage() != null && iae.getMessage().contains("BOGUS")
                  && iae.getMessage().contains("3"), "descriptive IAE message names code and length");
        }
        try { Currency.getInstance(new Locale("xx", "XX")); check(false, "xx_XX locale IAE"); }
        catch (IllegalArgumentException iae) {
            check(true, "xx_XX locale IAE");
            check(iae.getMessage() != null && iae.getMessage().contains("xx_XX"),
                  "descriptive bad-country IAE message names locale");
        }
        // getNumericCodeAsString：JDK 24+ 新增，返回零填充三位字符串
        check("840".equals(usd.getNumericCodeAsString()), "getNumericCodeAsString zero-padded 840");
        check("156".equals(Currency.getInstance("CNY").getNumericCodeAsString()),
              "CNY getNumericCodeAsString 156");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCurrencyContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

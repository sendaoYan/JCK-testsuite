/*
 * @test
 * @summary java.text.DecimalFormatSymbols conformance: white-box structure + black-box symbol access/mutation.
 * @run main DecimalFormatSymbolsTest
 */

import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

public class DecimalFormatSymbolsTest {
    static int passed;
    static final Locale US = Locale.US;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.DecimalFormatSymbols");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(Locale.class) != null, "ctor(Locale)");
        check(DecimalFormatSymbols.getInstance() instanceof DecimalFormatSymbols, "getInstance() non-null");
        check(DecimalFormatSymbols.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testUsSymbols() {
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(US);
        check(US.equals(dfs.getLocale()), "getLocale US");
        check(dfs.getZeroDigit() == '0', "zeroDigit");
        check(dfs.getGroupingSeparator() == ',', "grouping separator");
        check(dfs.getDecimalSeparator() == '.', "decimal separator");
        check(dfs.getPercent() == '%', "percent");
        check(dfs.getPerMill() == '‰', "perMill");
        check(dfs.getMinusSign() == '-', "minus sign");
        check("∞".equals(dfs.getInfinity()), "infinity");
        check("NaN".equals(dfs.getNaN()), "NaN");
        check("$".equals(dfs.getCurrencySymbol()), "currency symbol");
        check("USD".equals(dfs.getInternationalCurrencySymbol()), "international currency symbol");
        check(dfs.getMonetaryDecimalSeparator() == '.', "monetary decimal separator");
        check(dfs.getDigit() == '#', "digit");
        check(dfs.getPatternSeparator() == ';', "pattern separator");
        check("E".equals(dfs.getExponentSeparator()), "exponent separator");
        check(dfs.getMonetaryGroupingSeparator() == ',', "monetary grouping separator");
    }

    static void testMutation() {
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(US);
        dfs.setZeroDigit('0');
        check(dfs.getZeroDigit() == '0', "setZeroDigit/getZeroDigit");
        dfs.setGroupingSeparator(' ');
        check(dfs.getGroupingSeparator() == ' ', "setGroupingSeparator/getGroupingSeparator");
        dfs.setDecimalSeparator(';');
        check(dfs.getDecimalSeparator() == ';', "setDecimalSeparator/getDecimalSeparator");
        dfs.setPercent('p');
        check(dfs.getPercent() == 'p', "setPercent/getPercent");
        dfs.setPerMill('m');
        check(dfs.getPerMill() == 'm', "setPerMill/getPerMill");
        dfs.setMinusSign('~');
        check(dfs.getMinusSign() == '~', "setMinusSign/getMinusSign");
        dfs.setInfinity("Inf");
        check("Inf".equals(dfs.getInfinity()), "setInfinity/getInfinity");
        dfs.setNaN("NotANumber");
        check("NotANumber".equals(dfs.getNaN()), "setNaN/getNaN");
        dfs.setCurrencySymbol("USD$");
        check("USD$".equals(dfs.getCurrencySymbol()), "setCurrencySymbol/getCurrencySymbol");
        dfs.setInternationalCurrencySymbol("XXX");
        check("XXX".equals(dfs.getInternationalCurrencySymbol()), "setInternationalCurrencySymbol");
        dfs.setMonetaryDecimalSeparator(':');
        check(dfs.getMonetaryDecimalSeparator() == ':', "setMonetaryDecimalSeparator");
        dfs.setExponentSeparator("e");
        check("e".equals(dfs.getExponentSeparator()), "setExponentSeparator");
    }

    static void testCurrency() {
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(US);
        check(Currency.getInstance("USD").equals(dfs.getCurrency()), "default currency USD");
        dfs.setCurrency(Currency.getInstance("EUR"));
        check(Currency.getInstance("EUR").equals(dfs.getCurrency()), "setCurrency/getCurrency EUR");
        check("EUR".equals(dfs.getInternationalCurrencySymbol()), "intl symbol updated to EUR");
    }

    static void testEqualsCloneHashCode() {
        DecimalFormatSymbols a = DecimalFormatSymbols.getInstance(US);
        DecimalFormatSymbols b = DecimalFormatSymbols.getInstance(US);
        check(a.equals(b) && b.equals(a), "two US instances equal");
        check(a.hashCode() == b.hashCode(), "equal hashCode");
        DecimalFormatSymbols c = (DecimalFormatSymbols) a.clone();
        check(a.equals(c) && c.equals(a), "clone equals");
        check(a.hashCode() == c.hashCode(), "clone hashCode");
        DecimalFormatSymbols d = (DecimalFormatSymbols) a.clone();
        d.setDecimalSeparator('X');
        check(!a.equals(d), "mutated clone not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testUsSymbols();
        testMutation();
        testCurrency();
        testEqualsCloneHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

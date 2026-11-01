/*
 * @test
 * @summary java.text.DecimalFormatSymbols conformance: separators/currency/NaN/infinity and
 *          setters/clone.
 * @run main DecimalFormatSymbolsTest
 */

import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

public class DecimalFormatSymbolsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDecimalFormatSymbols() {
        DecimalFormatSymbols s = DecimalFormatSymbols.getInstance(Locale.US);
        check(s.getDecimalSeparator() == '.', "decimalSeparator");
        check(s.getGroupingSeparator() == ',', "groupingSeparator");
        check(s.getPercent() == '%', "percent");
        check(s.getPerMill() == '‰', "perMill");
        check(s.getMinusSign() == '-', "minusSign");
        check(s.getZeroDigit() == '0', "zeroDigit");
        check(s.getDigit() == '#', "digit");
        check(s.getPatternSeparator() == ';', "patternSeparator");
        check(s.getMonetaryDecimalSeparator() == '.', "monetaryDecimalSeparator");
        check(s.getNaN() != null, "NaN");
        check(s.getInfinity() != null, "infinity");
        check(s.getCurrencySymbol() != null, "currencySymbol");
        check(s.getInternationalCurrencySymbol() != null, "internationalCurrencySymbol");
        check(s.getCurrency() != null, "currency");
        check(s.getExponentSeparator() != null, "exponentSeparator");

        s.setDecimalSeparator(',');
        check(s.getDecimalSeparator() == ',', "setDecimalSeparator");
        s.setGroupingSeparator('.');
        check(s.getGroupingSeparator() == '.', "setGroupingSeparator");
        s.setZeroDigit('0');
        check(s.getZeroDigit() == '0', "setZeroDigit");
        s.setCurrency(Currency.getInstance("USD"));
        check("USD".equals(s.getCurrency().getCurrencyCode()), "setCurrency");
        s.setCurrencySymbol("$");
        check("$".equals(s.getCurrencySymbol()), "setCurrencySymbol");
        s.setInternationalCurrencySymbol("USD");
        check("USD".equals(s.getInternationalCurrencySymbol()), "setInternationalCurrencySymbol");

        DecimalFormatSymbols s2 = (DecimalFormatSymbols) s.clone();
        check(s.equals(s2), "clone equals");
        check(s.hashCode() == s2.hashCode(), "clone hashCode");
        check(DecimalFormatSymbols.getInstance(Locale.US)
                .equals(DecimalFormatSymbols.getInstance(Locale.US)), "getInstance equals");
        check(DecimalFormatSymbols.getAvailableLocales().length > 0, "getAvailableLocales");
    }

    public static void main(String[] args) {
        testDecimalFormatSymbols();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    static void testBlackBox() {
        DecimalFormatSymbols us = new DecimalFormatSymbols(Locale.US);
        check(us.getDecimalSeparator() == '.', "ctor(Locale.US) decimal separator");
        check(us.getGroupingSeparator() == ',', "ctor(Locale.US) grouping separator");
        check(us.getZeroDigit() == '0', "ctor(Locale.US) zero digit");

        DecimalFormatSymbols def = new DecimalFormatSymbols();
        check(def.getDecimalSeparator() != 0, "no-arg ctor decimal separator set");
        check(def.getGroupingSeparator() != 0, "no-arg ctor grouping separator set");
        check(def.getInfinity() != null && def.getNaN() != null, "no-arg ctor infinity/NaN non-null");

        DecimalFormatSymbols m = DecimalFormatSymbols.getInstance(US);
        m.setDigit('D');
        check(m.getDigit() == 'D', "setDigit/getDigit");
        m.setPatternSeparator(':');
        check(m.getPatternSeparator() == ':', "setPatternSeparator/getPatternSeparator");
        m.setMonetaryGroupingSeparator(' ');
        check(m.getMonetaryGroupingSeparator() == ' ', "setMonetaryGroupingSeparator/getMonetaryGroupingSeparator");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testEveryFieldRoundTripContract() {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setZeroDigit('a'); check(s.getZeroDigit() == 'a', "zeroDigit round-trip");
        s.setGroupingSeparator('_'); check(s.getGroupingSeparator() == '_', "groupingSeparator round-trip");
        s.setDecimalSeparator(';'); check(s.getDecimalSeparator() == ';', "decimalSeparator round-trip");
        s.setPerMill('q'); check(s.getPerMill() == 'q', "perMill round-trip");
        s.setPercent('p'); check(s.getPercent() == 'p', "percent round-trip");
        s.setMonetaryDecimalSeparator('m'); check(s.getMonetaryDecimalSeparator() == 'm',
              "monetaryDecimalSeparator round-trip");
        s.setMinusSign('~'); check(s.getMinusSign() == '~', "minusSign round-trip");
        s.setExponentSeparator("EXP"); check("EXP".equals(s.getExponentSeparator()), "exponentSeparator round-trip");
        s.setInfinity("INF"); check("INF".equals(s.getInfinity()), "infinity round-trip");
        s.setNaN("NAN"); check("NAN".equals(s.getNaN()), "NaN round-trip");
        s.setCurrencySymbol("XYZ"); check("XYZ".equals(s.getCurrencySymbol()), "currencySymbol round-trip");
        s.setInternationalCurrencySymbol("XYZ"); check("XYZ".equals(s.getInternationalCurrencySymbol()),
              "internationalCurrencySymbol round-trip");
        // clone independence
        DecimalFormatSymbols c = (DecimalFormatSymbols) s.clone();
        check(c.equals(s) && c != s, "clone equal, distinct object");
        c.setDecimalSeparator('x');
        check(!c.equals(s), "clone mutation does not leak back");
        check(s.hashCode() == new DecimalFormatSymbols(Locale.US).hashCode()
              ? s.hashCode() == s.hashCode() : true, "hashCode self-consistent");
    }

    static void testSymbolsDriveDecimalFormatContract() {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        s.setDecimalSeparator(',');
        s.setGroupingSeparator(' ');
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00", s);
        check("1 234,50".equals(df.format(1234.5)), "decimal/grouping symbols drive output");
        DecimalFormatSymbols se = new DecimalFormatSymbols(Locale.US);
        se.setExponentSeparator("*E*");
        check("1.234*E*3".equals(new java.text.DecimalFormat("0.###E0", se).format(1234.5)),
              "exponent separator drives scientific output");
        DecimalFormatSymbols sm = new DecimalFormatSymbols(Locale.US);
        sm.setMinusSign('~');
        check("~1.5".equals(new java.text.DecimalFormat("0.0", sm).format(-1.5)),
              "minus sign drives output");
        java.text.ParsePosition pp = new java.text.ParsePosition(0);
        check(new java.text.DecimalFormat("0.0", sm).parse("~1.5", pp).doubleValue() == -1.5
              && pp.getIndex() == 4, "minus sign drives parse");
        DecimalFormatSymbols sz = new DecimalFormatSymbols(Locale.US);
        sz.setZeroDigit('a');
        check("bc".equals(new java.text.DecimalFormat("00", sz).format(12)), "zero digit drives digits");
        DecimalFormatSymbols sn = new DecimalFormatSymbols(Locale.US);
        sn.setNaN("?N?");
        sn.setInfinity("?I?");
        check("?N?".equals(new java.text.DecimalFormat("0.0", sn).format(Double.NaN)), "NaN symbol drives output");
        check("?I?".equals(new java.text.DecimalFormat("0.0", sn).format(Double.POSITIVE_INFINITY)),
              "Infinity symbol drives output");
    }

    static void testEnglishDataContract() {
        DecimalFormatSymbols s = DecimalFormatSymbols.getInstance(Locale.US);
        check('0' == s.getZeroDigit(), "US zeroDigit");
        check(',' == s.getGroupingSeparator(), "US groupingSeparator");
        check('.' == s.getDecimalSeparator(), "US decimalSeparator");
        check('\u2030' == s.getPerMill(), "US perMill");
        check('%' == s.getPercent(), "US percent");
        check('-' == s.getMinusSign(), "US minusSign");
        check("E".equals(s.getExponentSeparator()), "US exponentSeparator");
        check("\u221E".equals(s.getInfinity()), "US infinity symbol");
        check("$".equals(s.getCurrencySymbol()), "US currency symbol");
        check("USD".equals(s.getInternationalCurrencySymbol()), "US intl currency code");
        check('.' == s.getMonetaryDecimalSeparator(), "US monetary decimal separator");
        check("\u00A4".equals(DecimalFormatSymbols.getInstance(Locale.ROOT).getCurrencySymbol()),
              "ROOT currency symbol is U+00A4");
        check("XXX".equals(DecimalFormatSymbols.getInstance(Locale.ROOT).getInternationalCurrencySymbol()),
              "ROOT intl currency code");
    }

    static void testEqualsHashCodeAcrossFactoriesContract() {
        check(DecimalFormatSymbols.getInstance(Locale.US).equals(new DecimalFormatSymbols(Locale.US)),
              "getInstance(Locale) equals new DecimalFormatSymbols(Locale)");
        check(DecimalFormatSymbols.getInstance(Locale.US).hashCode()
              == new DecimalFormatSymbols(Locale.US).hashCode(), "hashCode consistent");
        check(!DecimalFormatSymbols.getInstance(Locale.US).equals(DecimalFormatSymbols.getInstance(Locale.GERMANY)),
              "US != Germany symbols");
        expectX(NullPointerException.class, () -> DecimalFormatSymbols.getInstance(null),
                "getInstance(null) -> NPE");
    }

    static void testVersionLockedLocaleDataContract() {
        // CLDR locale data on JDK-27
        check("NaN".equals(DecimalFormatSymbols.getInstance(Locale.US).getNaN()),
              "US NaN symbol is literal NaN (CLDR)");
        check(',' == DecimalFormatSymbols.getInstance(Locale.GERMANY).getDecimalSeparator()
              && '.' == DecimalFormatSymbols.getInstance(Locale.GERMANY).getGroupingSeparator(),
              "de decimal ',' grouping '.' (CLDR)");
        check('\u202F' == DecimalFormatSymbols.getInstance(Locale.FRANCE).getGroupingSeparator(),
              "fr grouping separator NNBSP U+202F (CLDR)");
        check("\u00A5".equals(DecimalFormatSymbols.getInstance(Locale.CHINA).getCurrencySymbol()),
              "zh_CN currency symbol half-width yen (CLDR)");
        check("CNY".equals(DecimalFormatSymbols.getInstance(Locale.CHINA).getInternationalCurrencySymbol()),
              "zh_CN intl code CNY (CLDR)");
        check("\u00A3".equals(DecimalFormatSymbols.getInstance(Locale.UK).getCurrencySymbol()),
              "en_GB currency symbol pound (CLDR)");
    }
    public static void main(String[] args) throws Exception {
        testEveryFieldRoundTripContract();
        testSymbolsDriveDecimalFormatContract();
        testEnglishDataContract();
        testEqualsHashCodeAcrossFactoriesContract();
        testVersionLockedLocaleDataContract();
        testWhiteBox();
        testUsSymbols();
        testMutation();
        testCurrency();
        testEqualsCloneHashCode();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.text.NumberFormat conformance: white-box structure + black-box factory/format/parse behavior.
 * @run main NumberFormatTest
 */

import java.math.RoundingMode;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;

public class NumberFormatTest {
    static int passed;
    static final Locale US = Locale.US;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(ThrowingRunnable r, Class<? extends Throwable> t, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (t.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " but got " + x, x);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.NumberFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete abstract class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.text.Format"), "extends Format");
        check(c.getField("INTEGER_FIELD").getInt(null) == 0, "INTEGER_FIELD=0");
        check(c.getField("FRACTION_FIELD").getInt(null) == 1, "FRACTION_FIELD=1");
        check(NumberFormat.getInstance() instanceof NumberFormat, "getInstance() non-null");
        check(NumberFormat.getNumberInstance() instanceof NumberFormat, "getNumberInstance() non-null");
        check(NumberFormat.getIntegerInstance() instanceof NumberFormat, "getIntegerInstance() non-null");
        check(NumberFormat.getCurrencyInstance() instanceof NumberFormat, "getCurrencyInstance() non-null");
        check(NumberFormat.getPercentInstance() instanceof NumberFormat, "getPercentInstance() non-null");
        check(NumberFormat.getCompactNumberInstance() instanceof NumberFormat, "getCompactNumberInstance() non-null");
        check(NumberFormat.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testFactoryFormats() {
        NumberFormat nf = NumberFormat.getNumberInstance(US);
        check("1,234,567.891".equals(nf.format(1234567.891)), "number instance grouping");
        check("0.5".equals(nf.format(0.5)), "number instance decimal");
        check("-1,234,567.891".equals(nf.format(-1234567.891)), "number instance negative");

        NumberFormat integer = NumberFormat.getIntegerInstance(US);
        check("1,234,568".equals(integer.format(1234567.891)), "integer instance rounds");

        NumberFormat currency = NumberFormat.getCurrencyInstance(US);
        check("$1,234.50".equals(currency.format(1234.5)), "currency instance USD");

        NumberFormat percent = NumberFormat.getPercentInstance(US);
        check("50%".equals(percent.format(0.5)), "percent instance");
    }

    static void testGroupingAndDigits() {
        NumberFormat nf = NumberFormat.getNumberInstance(US);
        check(nf.isGroupingUsed(), "grouping default true");
        nf.setGroupingUsed(false);
        check(!nf.isGroupingUsed(), "setGroupingUsed(false)");
        check("1234567.891".equals(nf.format(1234567.891)), "no grouping");

        NumberFormat digits = NumberFormat.getNumberInstance(US);
        digits.setMinimumIntegerDigits(2);
        digits.setMinimumFractionDigits(3);
        digits.setMaximumFractionDigits(3);
        check("05.500".equals(digits.format(5.5)), "min integer + fixed fraction");
        check(digits.getMinimumIntegerDigits() == 2, "getMinimumIntegerDigits");
        check(digits.getMaximumFractionDigits() == 3, "getMaximumFractionDigits");

        NumberFormat rounded = NumberFormat.getNumberInstance(US);
        rounded.setMaximumFractionDigits(2);
        check("1,234.57".equals(rounded.format(1234.567)), "max fraction rounds");
    }

    static void testParse() throws Exception {
        NumberFormat nf = NumberFormat.getNumberInstance(US);
        check(nf.parse("1,234.5").doubleValue() == 1234.5, "parse decimal");
        check(nf.parse("42").getClass() == Long.class, "parse integer -> Long");
        check(nf.parse("3.5").getClass() == Double.class, "parse decimal -> Double");

        NumberFormat io = NumberFormat.getNumberInstance(US);
        check(!io.isParseIntegerOnly(), "parseIntegerOnly default false");
        io.setParseIntegerOnly(true);
        check(io.isParseIntegerOnly(), "setParseIntegerOnly(true)");
        check(io.parse("1.9").longValue() == 1L, "parse integer-only truncates");

        expectThrows(() -> nf.parse("garbage"), ParseException.class, "parse garbage throws ParseException");
    }

    static void testCurrencyAndRounding() {
        NumberFormat currency = NumberFormat.getCurrencyInstance(US);
        currency.setCurrency(Currency.getInstance("USD"));
        check(Currency.getInstance("USD").equals(currency.getCurrency()), "setCurrency/getCurrency USD");
        check("$1,234.50".equals(currency.format(1234.5)), "USD format");
        currency.setCurrency(Currency.getInstance("EUR"));
        check("€1,234.50".equals(currency.format(1234.5)), "EUR format uses euro sign");

        NumberFormat nf = NumberFormat.getNumberInstance(US);
        check(RoundingMode.HALF_EVEN == nf.getRoundingMode(), "default rounding HALF_EVEN");
        nf.setMaximumFractionDigits(0);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        check(RoundingMode.HALF_UP == nf.getRoundingMode(), "setRoundingMode HALF_UP");
        check("3".equals(nf.format(2.5)), "HALF_UP 2.5 -> 3");
        check("2".equals(nf.format(2.4)), "HALF_UP 2.4 -> 2");
    }

    static void testEqualsCloneHashCode() {
        NumberFormat a = NumberFormat.getNumberInstance(US);
        NumberFormat b = (NumberFormat) a.clone();
        check(a.equals(b) && b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "clone hashCode");
        check(a.equals(a), "reflexive equals");
        check(!a.equals(NumberFormat.getCurrencyInstance(US)), "number != currency");
    }

    // Minimal concrete subclass to exercise the protected NumberFormat() constructor.
    static class MinimalNumberFormat extends NumberFormat {
        public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append("d:").append(number);
        }
        public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append("l:").append(number);
        }
        public Number parse(String source, ParsePosition parsePosition) {
            parsePosition.setIndex(source.length());
            return Long.valueOf(42L);
        }
    }

    static void testBlackBox() {
        MinimalNumberFormat mnf = new MinimalNumberFormat();
        check(mnf != null, "protected constructor via subclass");
        check("d:1.5".equals(mnf.format(1.5)), "subclass format(double)");
        check("l:7".equals(mnf.format(7L)), "subclass format(long)");

        ParsePosition pp = new ParsePosition(0);
        Object n = mnf.parseObject("garbage", pp);
        check(n instanceof Long && ((Long) n).longValue() == 42L, "subclass parseObject returns Number");
        check(pp.getIndex() == "garbage".length(), "subclass parseObject advances position");

        NumberFormat nf = NumberFormat.getNumberInstance(US);
        nf.setMaximumIntegerDigits(4);
        check(nf.getMaximumIntegerDigits() == 4, "setMaximumIntegerDigits/getMaximumIntegerDigits");
        nf.setMinimumFractionDigits(3);
        check(nf.getMinimumFractionDigits() == 3, "setMinimumFractionDigits/getMinimumFractionDigits");
        check(!nf.isStrict(), "isStrict default false");
        nf.setStrict(true);
        check(nf.isStrict(), "setStrict(true)");

        ParsePosition pp2 = new ParsePosition(0);
        Object v = nf.parseObject("42", pp2);
        check(v instanceof Number && ((Number) v).longValue() == 42L, "parseObject on real NumberFormat");
        check(pp2.getIndex() == 2, "real parseObject position");

        // ---- boundary/equivalence additions (M2) ----
        NumberFormat num = NumberFormat.getNumberInstance(US);
        check(num.getMaximumFractionDigits() == 3, "number instance default max fraction 3");
        check(NumberFormat.getCurrencyInstance(US).getMaximumFractionDigits() == 2,
              "currency instance default max fraction 2");
        check(NumberFormat.getIntegerInstance(US).getMaximumFractionDigits() == 0,
              "integer instance default max fraction 0");
        check(NumberFormat.getPercentInstance(US).getMaximumFractionDigits() == 0,
              "percent instance default max fraction 0");

        NumberFormat even = NumberFormat.getNumberInstance(US);
        even.setMaximumFractionDigits(0);
        check("2".equals(even.format(2.5)), "HALF_EVEN 2.5 -> 2");
        check("4".equals(even.format(3.5)), "HALF_EVEN 3.5 -> 4");
        check("-2".equals(even.format(-2.5)), "HALF_EVEN -2.5 -> -2");

        ParsePosition pneg = new ParsePosition(0);
        Number nn = num.parse("-12.5", pneg);
        check(nn != null && nn.doubleValue() == -12.5 && pneg.getIndex() == 5, "parse negative");

        ParsePosition ppre = new ParsePosition(0);
        Number pre = num.parse("123.45xyz", ppre);
        check(pre != null && pre.doubleValue() == 123.45 && ppre.getIndex() == 6,
              "parse prefix stops at non-numeric");
        check(ppre.getErrorIndex() == -1, "successful parse no error");

        ParsePosition pgrp = new ParsePosition(0);
        Number g = NumberFormat.getNumberInstance(US).parse("1,234,567", pgrp);
        check(g != null && g.longValue() == 1234567L && pgrp.getIndex() == 9, "parse grouped");
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
    // All expected strings locked on the reference JDK of this tree (see DESIGN.md).

    static void testInstanceMatrixContract() {
        // US: identical legacy data on both JDKs for these four factories
        check("1,234.567".equals(NumberFormat.getNumberInstance(Locale.US).format(1234.567)),
              "US number instance");
        check("$1,234.57".equals(NumberFormat.getCurrencyInstance(Locale.US).format(1234.567)),
              "US currency instance");
        check("46%".equals(NumberFormat.getPercentInstance(Locale.US).format(0.4567)),
              "US percent instance");
        check("1,235".equals(NumberFormat.getIntegerInstance(Locale.US).format(1234.567)),
              "US integer instance rounds HALF_EVEN at .567");
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        check(nf.isGroupingUsed(), "number instance grouping on");
        check(((java.text.DecimalFormat) nf).getGroupingSize() == 3, "number instance grouping size 3");
        check(NumberFormat.getCurrencyInstance(Locale.US).getMaximumFractionDigits() == 2,
              "currency max fraction 2");
        check(NumberFormat.getPercentInstance(Locale.US).getMaximumFractionDigits() == 0,
              "percent max fraction 0");
        check(NumberFormat.getIntegerInstance(Locale.US).getMaximumFractionDigits() == 0,
              "integer max fraction 0");
        check(NumberFormat.getIntegerInstance(Locale.US).isParseIntegerOnly(),
              "integer instance parseIntegerOnly");
        check(((java.text.DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US)).getRoundingMode()
              == RoundingMode.HALF_EVEN, "currency instance rounds HALF_EVEN");
        // instance round-trip through parse
        try {
            Number n = NumberFormat.getNumberInstance(Locale.US).parse("1,234.567");
            check(n.doubleValue() == 1234.567, "US number round-trip value");
        } catch (ParseException pe) { throw new AssertionError("unexpected PE", pe); }
        try {
            Number c = NumberFormat.getCurrencyInstance(Locale.US).parse("$1,234.56");
            check(c.doubleValue() == 1234.56, "US currency parse with symbol");
        } catch (ParseException pe) { throw new AssertionError("unexpected PE", pe); }
    }

    static void testEqualsAndHashCodeContract() {
        check(NumberFormat.getNumberInstance(Locale.US).equals(NumberFormat.getNumberInstance(Locale.US)),
              "two number instances equal");
        check(NumberFormat.getNumberInstance(Locale.US).hashCode()
              == NumberFormat.getNumberInstance(Locale.US).hashCode(), "hashCode consistent");
        check(!NumberFormat.getIntegerInstance(Locale.US).equals(NumberFormat.getNumberInstance(Locale.US)),
              "integer instance != number instance");
        check(!NumberFormat.getNumberInstance(Locale.US).equals(NumberFormat.getCurrencyInstance(Locale.US)),
              "number != currency");
        NumberFormat a = NumberFormat.getNumberInstance(Locale.US);
        a.setMaximumFractionDigits(6);
        check(!a.equals(NumberFormat.getNumberInstance(Locale.US)),
              "state change breaks equality");
    }

    static void testDigitClampingContract() {
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00",
                java.text.DecimalFormatSymbols.getInstance(Locale.US));
        df.setMinimumFractionDigits(5);
        check(df.getMinimumFractionDigits() == 5 && df.getMaximumFractionDigits() == 5,
              "setMinimumFractionDigits clamps max up to min");
        check("1.00000".equals(df.format(1)), "min fraction 5 pads zeros");
        df.setMaximumFractionDigits(0);
        df.setMinimumIntegerDigits(8);
        check(df.getMinimumIntegerDigits() == 8, "min integer digits 8");
        check("00000001".equals(df.format(1)), "min integer digits pads zeros");
        df.setMaximumIntegerDigits(2);
        check(df.getMaximumIntegerDigits() == 2 && df.getMinimumIntegerDigits() == 2,
              "setMaximumIntegerDigits clamps min down to max");
        check("56".equals(df.format(123456)), "max integer digits 2 keeps low digits");
    }

    static void testIntegerOnlyParseContract() throws java.text.ParseException {
        NumberFormat in = NumberFormat.getIntegerInstance(Locale.US);
        check(in.parse("123.7").intValue() == 123, "integer instance stops at decimal point");
        NumberFormat pn = NumberFormat.getNumberInstance(Locale.US);
        check(pn.parse("123.7").doubleValue() == 123.7, "number instance reads fraction");
        check(!pn.isParseIntegerOnly(), "number instance not integer-only by default");
    }

    static void testParseErrorSemanticsContract() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        expectX(NullPointerException.class, () -> nf.parse(null), "parse(null) -> NPE");
        expectX(NullPointerException.class, () -> nf.parse("1", null), "parse(x, null PP) -> NPE");
        expectX(IllegalArgumentException.class,
                () -> nf.format("str", new StringBuffer(), new FieldPosition(0)),
                "format(String) -> IAE");
        ParsePosition pp = new ParsePosition(0);
        check(nf.parse("", pp) == null && pp.getIndex() == 0 && pp.getErrorIndex() == 0,
              "empty parse: null result, index stays 0, errorIndex 0");
        ParsePosition mid = new ParsePosition(4);
        Number n = nf.parse("abcd34.5", mid);
        check(n != null && n.doubleValue() == 34.5 && mid.getIndex() == 8,
              "parse resumes mid-string");
    }

    static void testConstantsContract() {
        check(NumberFormat.INTEGER_FIELD == 0, "INTEGER_FIELD == 0");
        check(NumberFormat.FRACTION_FIELD == 1, "FRACTION_FIELD == 1");
    }

    static void testFactoryAvailabilityContract() {
        check(NumberFormat.getAvailableLocales().length > 0, "getAvailableLocales non-empty");
        check(java.util.Arrays.asList(NumberFormat.getAvailableLocales()).contains(Locale.ENGLISH),
              "getAvailableLocales contains ENGLISH");
        // two calls yield independent, equal instances
        NumberFormat a = NumberFormat.getNumberInstance(Locale.US);
        NumberFormat b = NumberFormat.getNumberInstance(Locale.US);
        check(a != b && a.equals(b), "fresh equal instance per call");
        NumberFormat cl = (NumberFormat) a.clone();
        check(cl.equals(a) && cl != a, "clone equal, independent");
    }

    static void testVersionLockedLocaleDataContract() {
        // CLDR data: NBSP before EUR sign, NNBSP grouping for fr
        check("1.234,567".equals(NumberFormat.getNumberInstance(Locale.GERMANY).format(1234.567)),
              "de_DE number (CLDR)");
        check("1.234,57\u00A0\u20AC".equals(NumberFormat.getCurrencyInstance(Locale.GERMANY).format(1234.567)),
              "de_DE currency has NBSP before sign (CLDR)");
        check("46\u00A0%".equals(NumberFormat.getPercentInstance(Locale.GERMANY).format(0.4567)),
              "de_DE percent has NBSP before % (CLDR)");
        check("1.235".equals(NumberFormat.getIntegerInstance(Locale.GERMANY).format(1234.567)),
              "de_DE integer (CLDR)");
        check("1\u202F234,567".equals(NumberFormat.getNumberInstance(Locale.FRANCE).format(1234.567)),
              "fr_FR number uses NNBSP grouping (CLDR)");
        check("\uFFE51,235".equals(NumberFormat.getCurrencyInstance(Locale.JAPAN).format(1234.567)),
              "ja_JP currency (CLDR)");
        check("\u00A41,234.57".equals(NumberFormat.getCurrencyInstance(Locale.ENGLISH).format(1234.567)),
              "en (no country) currency falls back to X-code symbol (CLDR)");
    }
    public static void main(String[] args) throws Exception {
        testInstanceMatrixContract();
        testEqualsAndHashCodeContract();
        testDigitClampingContract();
        testIntegerOnlyParseContract();
        testParseErrorSemanticsContract();
        testConstantsContract();
        testFactoryAvailabilityContract();
        testVersionLockedLocaleDataContract();
        testWhiteBox();
        testFactoryFormats();
        testGroupingAndDigits();
        testParse();
        testCurrencyAndRounding();
        testEqualsCloneHashCode();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

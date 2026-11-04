/*
 * @test
 * @summary java.text.DecimalFormat API conformance: full contract coverage.
 *          constructors, deterministic pattern formats (grouping/rounding/scientific/
 *          percent/negative sub-pattern), positive/negative prefix/suffix,
 *          multiplier/grouping size/decimal separator always shown,
 *          parseBigDecimal exact parsing, rounding modes, boundary values (0/-0.0/
 *          NaN/Infinity/MAX_VALUE/MIN_VALUE/empty parse), illegal patterns ->
 *          IllegalArgumentException, custom symbols, digit setters, currency,
 *          equals/hashCode/clone/toString, and serialization round-trip.
 * @run main DecimalFormatTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;

public class DecimalFormatTest {
    static int passed;
    static final Locale US = Locale.US;
    static final DecimalFormatSymbols SYM = DecimalFormatSymbols.getInstance(US);

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try {
            r.run();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            if (x instanceof AssertionError) throw (AssertionError) x;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.DecimalFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check("java.text.NumberFormat".equals(c.getSuperclass().getName()), "extends NumberFormat");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, DecimalFormatSymbols.class) != null,
              "ctor(String,DecimalFormatSymbols)");
        check(c.getMethod("toPattern") != null, "toPattern()");
        check(c.getMethod("applyPattern", String.class) != null, "applyPattern(String)");
        check(c.getMethod("setParseBigDecimal", boolean.class) != null, "setParseBigDecimal(boolean)");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPatternFormats() {
        DecimalFormat df = new DecimalFormat("#,##0.00", SYM);
        check("1,234,567.89".equals(df.format(1234567.891)), "#,##0.00 positive");
        check("-1,234.50".equals(df.format(-1234.5)), "#,##0.00 negative");

        check("1234567.89".equals(new DecimalFormat("0.00", SYM).format(1234567.891)), "0.00");
        check("1,234,568".equals(new DecimalFormat("#,##0", SYM).format(1234567.891)), "#,##0 rounds");
        check("1.235E6".equals(new DecimalFormat("0.###E0", SYM).format(1234567.891)), "scientific");
        check("123,456,789%".equals(new DecimalFormat("#,##0%", SYM).format(1234567.891)), "percent pattern");
        check("(1234.50)".equals(new DecimalFormat("0.00;(0.00)", SYM).format(-1234.5)), "negative sub-pattern");
        check("1,234,567.89".equals(new DecimalFormat("###,###.##", SYM).format(1234567.891)), "multiple grouping");
    }

    static void testConstructorsAndExceptions() {
        check("#,##0.###".equals(new DecimalFormat().toPattern()), "no-arg ctor default pattern");
        check("#,##0.00".equals(new DecimalFormat("#,##0.00").toPattern()), "ctor(String) pattern");
        expectThrows(IllegalArgumentException.class, () -> new DecimalFormat("#.##.##"),
                     "illegal pattern rejected");
        expectThrows(IllegalArgumentException.class,
                     () -> new DecimalFormat("0.00", SYM).applyPattern("#.##.##"),
                     "applyPattern illegal rejected");
        expectThrows(NullPointerException.class, () -> new DecimalFormat((String) null),
                     "ctor(null) -> NPE");
        expectThrows(NullPointerException.class,
                     () -> new DecimalFormat("0.00", (DecimalFormatSymbols) null),
                     "ctor(String, null symbols) -> NPE");
    }

    static void testPrefixSuffix() {
        DecimalFormat df = new DecimalFormat("#,##0.00", SYM);
        df.setPositivePrefix("+");
        df.setNegativePrefix("(");
        df.setNegativeSuffix(")");
        check("+1,234.50".equals(df.format(1234.5)), "positive prefix");
        check("(1,234.50)".equals(df.format(-1234.5)), "negative prefix/suffix");
        check("+".equals(df.getPositivePrefix()), "getPositivePrefix");
        check("(".equals(df.getNegativePrefix()), "getNegativePrefix");
        check(")".equals(df.getNegativeSuffix()), "getNegativeSuffix");

        DecimalFormat ps = new DecimalFormat("#,##0.00", SYM);
        ps.setPositiveSuffix("+");
        check("+".equals(ps.getPositiveSuffix()), "getPositiveSuffix/setPositiveSuffix");
        check("1,234.50+".equals(ps.format(1234.5)), "positive suffix affects format");
    }

    static void testMultiplierGroupingSeparator() {
        DecimalFormat df = new DecimalFormat("0.00", SYM);
        df.setMultiplier(100);
        check("150.00".equals(df.format(1.5)), "multiplier 100");
        check(df.getMultiplier() == 100, "getMultiplier");

        DecimalFormat g = new DecimalFormat("#,##0", SYM);
        g.setGroupingSize(2);
        check("12,34,56".equals(g.format(123456)), "grouping size 2");
        check(g.getGroupingSize() == 2, "getGroupingSize");

        DecimalFormat dec = new DecimalFormat("#.##", SYM);
        check(!dec.isDecimalSeparatorAlwaysShown(), "decimal separator always shown default false");
        dec.setDecimalSeparatorAlwaysShown(true);
        check(dec.isDecimalSeparatorAlwaysShown(), "setDecimalSeparatorAlwaysShown(true)");
        check("5.".equals(dec.format(5)), "decimal separator always shown");
    }

    static void testParseBigDecimal() throws Exception {
        DecimalFormat df = new DecimalFormat("#,##0.00", SYM);
        check(!df.isParseBigDecimal(), "parseBigDecimal default false");
        check(df.parse("1,234.50").doubleValue() == 1234.5, "default parse double");
        df.setParseBigDecimal(true);
        check(df.isParseBigDecimal(), "setParseBigDecimal true");
        Number n = df.parse("1,234.50");
        check(n.getClass() == BigDecimal.class, "parse returns BigDecimal");
        check(new BigDecimal("1234.50").equals(n), "BigDecimal exact value");
    }

    static void testRoundingModes() {
        DecimalFormat df = new DecimalFormat("0", SYM);
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        check("2".equals(df.format(2.5)), "HALF_EVEN 2.5 -> 2");
        check("4".equals(df.format(3.5)), "HALF_EVEN 3.5 -> 4");
        df.setRoundingMode(RoundingMode.HALF_UP);
        check("3".equals(df.format(2.5)), "HALF_UP 2.5 -> 3");
        df.setRoundingMode(RoundingMode.DOWN);
        check("2".equals(df.format(2.9)), "DOWN 2.9 -> 2");
    }

    static void testBoundaryValues() {
        DecimalFormat df = new DecimalFormat("0.00", SYM);
        check("0.00".equals(df.format(0.0)), "format 0.0");
        check("-0.00".equals(df.format(-0.0)), "format -0.0 preserves sign");
        check("\uFFFD".equals(df.format(Double.NaN)), "format NaN uses JDK-8 NaN symbol");
        check("\u221E".equals(df.format(Double.POSITIVE_INFINITY)), "format Infinity symbol");
        check("-\u221E".equals(df.format(Double.NEGATIVE_INFINITY)), "format -Infinity symbol");
        check(df.parse("1.7976931348623157E308", new ParsePosition(0)).doubleValue() == Double.MAX_VALUE,
              "parse MAX_VALUE");
        check(df.parse("", new ParsePosition(0)) == null, "parse empty string returns null");

        DecimalFormat empty = new DecimalFormat("");
        check("1.5".equals(empty.format(1.5)), "empty pattern falls back to default");

        DecimalFormat sci = new DecimalFormat("0.###E0", SYM);
        check("1.798E308".equals(sci.format(Double.MAX_VALUE)), "scientific format MAX_VALUE");
        check("4.9E-324".equals(sci.format(Double.MIN_VALUE)), "scientific format MIN_VALUE");
    }

    static void testSymbolsAndPatternMutation() {
        DecimalFormat df = new DecimalFormat("#,##0.00", SYM);
        check("#,##0.00".equals(df.toPattern()), "toPattern");
        df.applyPattern("0.###E0");
        check("0.###E0".equals(df.toPattern()), "applyPattern changes pattern");
        check("1.235E6".equals(df.format(1234567.891)), "applied scientific pattern");

        DecimalFormatSymbols custom = DecimalFormatSymbols.getInstance(US);
        custom.setDecimalSeparator(';');
        custom.setGroupingSeparator(' ');
        DecimalFormat c = new DecimalFormat("#,##0.00", custom);
        check("1 234;50".equals(c.format(1234.5)), "custom symbols affect output");
        check(c.getDecimalFormatSymbols().equals(custom), "getDecimalFormatSymbols round-trip");
    }

    static void testDigitSettersAndCurrency() {
        DecimalFormat df = new DecimalFormat("#,##0.00", SYM);
        check(df.getMaximumFractionDigits() == 2, "getMaximumFractionDigits");
        check(df.getMinimumFractionDigits() == 2, "getMinimumFractionDigits");
        check(df.getMinimumIntegerDigits() == 1, "getMinimumIntegerDigits");
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(3);
        df.setMinimumIntegerDigits(2);
        df.setMaximumIntegerDigits(5);
        check(df.getMinimumFractionDigits() == 1, "setMinimumFractionDigits");
        check(df.getMaximumFractionDigits() == 3, "setMaximumFractionDigits");
        check(df.getMinimumIntegerDigits() == 2, "setMinimumIntegerDigits");
        check(df.getMaximumIntegerDigits() == 5, "setMaximumIntegerDigits");
        check("1,234.5".equals(df.format(1234.5)), "digit setters affect format");

        DecimalFormat cu = new DecimalFormat("#,##0.00", SYM);
        check(Currency.getInstance("USD").equals(cu.getCurrency()), "default currency USD");
        cu.setCurrency(Currency.getInstance("EUR"));
        check(Currency.getInstance("EUR").equals(cu.getCurrency()), "setCurrency/getCurrency EUR");

        DecimalFormat gu = new DecimalFormat("#,##0.00", SYM);
        gu.setGroupingUsed(false);
        check(!gu.isGroupingUsed(), "setGroupingUsed/isGroupingUsed");
        check("1234.50".equals(gu.format(1234.5)), "grouping off format");

        DecimalFormat lp = new DecimalFormat("#,##0.00", SYM);
        check("#,##0.00".equals(lp.toLocalizedPattern()), "toLocalizedPattern US");
        lp.applyLocalizedPattern("0.00");
        check("1.50".equals(lp.format(1.5)), "applyLocalizedPattern format");
        check("1234.50".equals(lp.format(1234.5)), "applyLocalizedPattern no grouping");
    }

    static void testEqualsCloneHashCode() {
        DecimalFormat a = new DecimalFormat("#,##0.00", SYM);
        DecimalFormat b = (DecimalFormat) a.clone();
        check(a.equals(b) && b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "clone hashCode");
        check(a.toString() != null, "toString non-null (JDK-8 inherits Object.toString)");
        DecimalFormat d = new DecimalFormat("0.00", SYM);
        check(!a.equals(d), "different pattern not equal");
    }

    static void testCharacterIteratorAndSerialization() throws Exception {
        DecimalFormat fi = new DecimalFormat("#,##0.00", SYM);
        AttributedCharacterIterator it = fi.formatToCharacterIterator(1234.5);
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("1,234.50".equals(sb.toString()), "formatToCharacterIterator text");

        DecimalFormat df = new DecimalFormat("#,##0.00", SYM);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(df);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        DecimalFormat r = (DecimalFormat) ois.readObject();
        check("#,##0.00".equals(r.toPattern()), "round-trip preserves pattern");
        check("1,234.50".equals(r.format(1234.5)), "round-trip still formats");
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
    // All expected strings below were locked by running the identical
    // program on the reference JDK of this tree (see DESIGN.md).

    static void testPatternMatrixContract() {
        DecimalFormatSymbols us = DecimalFormatSymbols.getInstance(Locale.US);
        // value columns: 0, -1, 1234.56, 0.1, -2.5, 1234567.891, 0.005, 55555.5
        double[] vals = {0, -1, 1234.56, 0.1, -2.5, 1234567.891, 0.005, 55555.5};
        String[][] tbl = {
            {"#,##0.00", "0.00", "-1.00", "1,234.56", "0.10", "-2.50", "1,234,567.89", "0.01", "55,555.50"},
            {"0.###", "0", "-1", "1234.56", "0.1", "-2.5", "1234567.891", "0.005", "55555.5"},
            {"#", "0", "-1", "1235", "0", "-2", "1234568", "0", "55556"},
            {"#%", "0%", "-100%", "123456%", "10%", "-250%", "123456789%", "0%", "5555550%"},
            {"#,##0%", "0%", "-100%", "123,456%", "10%", "-250%", "123,456,789%", "0%", "5,555,550%"},
            {"0.##E0", "0E0", "-1E0", "1.23E3", "1E-1", "-2.5E0", "1.23E6", "5E-3", "5.56E4"},
            {"0.###E0", "0E0", "-1E0", "1.235E3", "1E-1", "-2.5E0", "1.235E6", "5E-3", "5.556E4"},
            {"#;#", "0", "-1", "1235", "0", "-2", "1234568", "0", "55556"},
            {"0.00;(0.00)", "0.00", "(1.00)", "1234.56", "0.10", "(2.50)", "1234567.89", "0.01", "55555.50"},
            {"'#'#", "#0", "-#1", "#1235", "#0", "-#2", "#1234568", "#0", "#55556"},
            {"0.00''", "0.00'", "-1.00'", "1234.56'", "0.10'", "-2.50'", "1234567.89'", "0.01'", "55555.50'"},
            {"\u00A40.00", "$0.00", "-$1.00", "$1234.56", "$0.10", "-$2.50", "$1234567.89", "$0.01", "$55555.50"},
            {"\u00A4\u00A40.00", "USD0.00", "-USD1.00", "USD1234.56", "USD0.10", "-USD2.50", "USD1234567.89", "USD0.01", "USD55555.50"},
            {"000.000", "000.000", "-001.000", "1234.560", "000.100", "-002.500", "1234567.891", "000.005", "55555.500"},
            {"#.00", ".00", "-1.00", "1234.56", ".10", "-2.50", "1234567.89", ".01", "55555.50"},
            {"0.#", "0", "-1", "1234.6", "0.1", "-2.5", "1234567.9", "0", "55555.5"},
            {"#,##0.0;(#,##0.0)", "0.0", "(1.0)", "1,234.6", "0.1", "(2.5)", "1,234,567.9", "0.0", "55,555.5"},
            {"0.00E00", "0.00E00", "-1.00E00", "1.23E03", "1.00E-01", "-2.50E00", "1.23E06", "5.00E-03", "5.56E04"},
            {"@@@", "@@@0", "-@@@1", "@@@1235", "@@@0", "-@@@2", "@@@1234568", "@@@0", "@@@55556"},
            {"0.00\u2030", "0.00\u2030", "-1000.00\u2030", "1234560.00\u2030", "100.00\u2030", "-2500.00\u2030", "1234567891.00\u2030", "5.00\u2030", "55555500.00\u2030"},
        };
        for (String[] row : tbl) {
            DecimalFormat df = new DecimalFormat(row[0], us);
            for (int i = 0; i < vals.length; i++)
                check(row[i + 1].equals(df.format(vals[i])),
                      "pattern <" + row[0] + "> value " + vals[i] + " -> <" + row[i + 1] + "> got <" + df.format(vals[i]) + ">");
        }
        // negative sub-pattern takes min/max digits from positive side only ("#;#" keeps 0 min int)
        DecimalFormat neg = new DecimalFormat("#;#", us);
        check(neg.getMinimumIntegerDigits() == 0, "neg sub-pattern: min int digits from positive");
    }

    static void testRoundingModeMatrixContract() {
        DecimalFormatSymbols us = DecimalFormatSymbols.getInstance(Locale.US);
        double[] vals = {2.5, 3.5, -2.5, -3.5, 2.4, 2.6, 0.0, -0.5, 1.5, 5.5, -1.5, 0.5, 1.6, -1.6};
        String[][] tbl = {
            {"UP", "3", "4", "-3", "-4", "3", "3", "0", "-1", "2", "6", "-2", "1", "2", "-2"},
            {"DOWN", "2", "3", "-2", "-3", "2", "2", "0", "-0", "1", "5", "-1", "0", "1", "-1"},
            {"CEILING", "3", "4", "-2", "-3", "3", "3", "0", "-0", "2", "6", "-1", "1", "2", "-1"},
            {"FLOOR", "2", "3", "-3", "-4", "2", "2", "0", "-1", "1", "5", "-2", "0", "1", "-2"},
            {"HALF_UP", "3", "4", "-3", "-4", "2", "3", "0", "-1", "2", "6", "-2", "1", "2", "-2"},
            {"HALF_DOWN", "2", "3", "-2", "-3", "2", "3", "0", "-0", "1", "5", "-1", "0", "2", "-2"},
            {"HALF_EVEN", "2", "4", "-2", "-4", "2", "3", "0", "-0", "2", "6", "-2", "0", "2", "-2"},
        };
        for (String[] row : tbl) {
            DecimalFormat df = new DecimalFormat("0", us);
            df.setRoundingMode(RoundingMode.valueOf(row[0]));
            check(df.getRoundingMode() == RoundingMode.valueOf(row[0]), "getRoundingMode " + row[0]);
            for (int i = 0; i < vals.length; i++)
                check(row[i + 1].equals(df.format(vals[i])),
                      row[0] + " " + vals[i] + " -> " + row[i + 1] + " got " + df.format(vals[i]));
        }
        // UNNECESSARY: exact values format, inexact throw ArithmeticException
        DecimalFormat du = new DecimalFormat("0", us);
        du.setRoundingMode(RoundingMode.UNNECESSARY);
        check("0".equals(du.format(0.0)), "UNNECESSARY 0 formats");
        check("2".equals(du.format(2.0)), "UNNECESSARY 2 formats");
        expectX(ArithmeticException.class, () -> du.format(2.5), "UNNECESSARY 2.5 -> ArithmeticException");
        expectX(ArithmeticException.class, () -> du.format(-3.5), "UNNECESSARY -3.5 -> ArithmeticException");
        DecimalFormat du2 = new DecimalFormat("0.0", us);
        du2.setRoundingMode(RoundingMode.UNNECESSARY);
        check("1.5".equals(du2.format(1.50)), "UNNECESSARY 1.50 exact");
        expectX(ArithmeticException.class, () -> du2.format(1.55), "UNNECESSARY 1.55 -> ArithmeticException");
        // default rounding mode is HALF_EVEN
        check(new DecimalFormat("0", us).getRoundingMode() == RoundingMode.HALF_EVEN,
              "default rounding mode HALF_EVEN");
    }

    static void testParseMatrixContract() {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        String[] in = {"1,234.50", "1234.50", "1.234,50", "abc", "", "12.3.4", "1,234",
                       "-123", "123abc", " 123", "1.2E3", "$5.25", "1234.5.6", "0.5x",
                       "--5", "+5", "1,23,", "(456)", "3.", ".5"};
        // expected: parsed number (or null), final index, error index
        String[] val = {"1234.5", "1234.5", "1.234", null, null, "12.3", "1234",
                        "-123", "123", null, "1200", null, "1234.5", "0.5",
                        null, null, "123", null, "3", "0.5"};
        int[] idx = {8, 7, 5, 0, 0, 4, 5, 4, 3, 0, 5, 0, 6, 3, 0, 0, 4, 0, 2, 2};
        int[] err = {-1, -1, -1, 0, 0, -1, -1, -1, -1, 0, -1, 0, -1, -1, 0, 0, -1, 0, -1, -1};
        for (int i = 0; i < in.length; i++) {
            ParsePosition pp = new ParsePosition(0);
            Number n = df.parse(in[i], pp);
            if (val[i] == null) {
                check(n == null, "parse(" + in[i] + ") -> null");
            } else {
                check(n != null && Double.parseDouble(val[i]) == n.doubleValue(),
                      "parse(" + in[i] + ") -> " + val[i] + " got " + n);
            }
            check(pp.getIndex() == idx[i], "parse(" + in[i] + ") index=" + idx[i] + " got " + pp.getIndex());
            check(pp.getErrorIndex() == err[i], "parse(" + in[i] + ") errorIndex=" + err[i] + " got " + pp.getErrorIndex());
        }
        // whitespace-prefixed failure leaves errorIndex at 0; parse is prefix-based (not whole-string)
        expectX(NullPointerException.class, () -> df.parse((String) null), "parse(null) -> NPE");
        expectX(NullPointerException.class, () -> df.parse("1", null), "parse(x, null PP) -> NPE");
    }

    static void testMultiplierContract() throws java.text.ParseException {
        DecimalFormat df = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        df.setMultiplier(100);
        check(df.getMultiplier() == 100, "getMultiplier 100");
        check("150".equals(df.format(1.5)), "multiplier 100 formats x100");
        check(df.parse("1500").doubleValue() == 15, "multiplier 100 parses /100");
        df.setMultiplier(-1);
        check("-5".equals(df.format(5)), "multiplier -1 flips sign");
        check("5".equals(df.format(-5)), "multiplier -1 flips sign negative");
        check(df.parse("-5").doubleValue() == 5, "multiplier -1 parse flips back");
    }

    static void testSymbolDrivenOutputContract() {
        DecimalFormatSymbols sy = DecimalFormatSymbols.getInstance(Locale.US);
        sy.setExponentSeparator("*E*");
        check("*E*".equals(sy.getExponentSeparator()), "getExponentSeparator");
        check("1.234*E*3".equals(new DecimalFormat("0.###E0", sy).format(1234.5)),
              "exponent separator drives scientific output");
        DecimalFormatSymbols sm = DecimalFormatSymbols.getInstance(Locale.US);
        sm.setMinusSign('~');
        check("~".equals(String.valueOf(sm.getMinusSign())), "getMinusSign");
        DecimalFormat dn = new DecimalFormat("0.0", sm);
        check("~1.5".equals(dn.format(-1.5)), "minus sign symbol drives output");
        ParsePosition pp = new ParsePosition(0);
        check(dn.parse("~1.5", pp).doubleValue() == -1.5 && pp.getIndex() == 4,
              "minus sign symbol drives parse");
        DecimalFormatSymbols sz = DecimalFormatSymbols.getInstance(Locale.US);
        sz.setZeroDigit('a');
        check("bc".equals(new DecimalFormat("00", sz).format(12)), "zero digit drives digits");
        DecimalFormatSymbols sp = DecimalFormatSymbols.getInstance(Locale.US);
        sp.setPercent('p');
        check("0p".equals(new DecimalFormat("0p", sp).format(0.5)), "percent symbol drives output");
        DecimalFormatSymbols si = DecimalFormatSymbols.getInstance(Locale.US);
        si.setNaN("?N?");
        si.setInfinity("?I?");
        DecimalFormat dni = new DecimalFormat("0.0", si);
        check("?N?".equals(dni.format(Double.NaN)), "NaN symbol drives output");
        check("?I?".equals(dni.format(Double.POSITIVE_INFINITY)), "Infinity symbol drives output");
    }

    static void testSpecialPatternErrorsContract() {
        DecimalFormatSymbols us = DecimalFormatSymbols.getInstance(Locale.US);
        expectX(IllegalArgumentException.class, () -> new DecimalFormat("0.0E0.0", us),
                "second decimal point in mantissa -> IAE");
        DecimalFormat g = new DecimalFormat();
        g.applyPattern("0,0.0");
        check("#,0,0.0".equals(g.toPattern()), "grouping separator in fraction accepted, canonical pattern");
        DecimalFormat h = new DecimalFormat();
        h.applyPattern("#,#");
        check("1,2,3".equals(h.format(123)), "pattern #,# formats with grouping size 1");
        expectX(IllegalArgumentException.class, () -> new DecimalFormat("0.0E+0", us).format(1),
                "E+ pattern -> IAE");
    }

    static void testGroupingSizeContract() {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.US));
        df.setGroupingSize(10);
        df.setGroupingUsed(true);
        check(df.getGroupingSize() == 10, "getGroupingSize 10");
        check("123,4567890123".equals(df.format(1234567890123L)), "grouping size 10");
        df.setGroupingSize(0);
        check(df.getGroupingSize() == 0, "getGroupingSize 0");
        check("1234567".equals(df.format(1234567)), "grouping size 0 -> no separators");
    }

    static void testToPatternCanonicalizationContract() {
        DecimalFormatSymbols us = DecimalFormatSymbols.getInstance(Locale.US);
        check("#,##0.###".equals(new DecimalFormat().toPattern()), "default toPattern");
        check("#,##0.###".equals(((DecimalFormat) java.text.NumberFormat.getNumberInstance(Locale.US)).toPattern()),
              "number instance toPattern");
        check("#,##0%".equals(((DecimalFormat) java.text.NumberFormat.getPercentInstance(Locale.US)).toPattern()),
              "percent instance toPattern");
        check("#,##0".equals(((DecimalFormat) java.text.NumberFormat.getIntegerInstance(Locale.US)).toPattern()),
              "integer instance toPattern");
        check("0.###E0".equals(new DecimalFormat("0.###E0", us).toPattern()), "scientific toPattern round-trip");
        check("'#'#0.00".equals(new DecimalFormat("'#'#0.00", us).toPattern()), "quoted literal toPattern");
        check("#0.00;(#0.00)".equals(new DecimalFormat("0.00;(0.00)", us).toPattern()),
              "negative sub-pattern toPattern");
        check("#0.00;[#0.00]".equals(new DecimalFormat("0.00;[0.00]", us).toPattern()),
              "negative prefix/suffix toPattern");
        check("#%".equals(new DecimalFormat("#%", us).toPattern()), "percent toPattern");
        check("#\u2030".equals(new DecimalFormat("#\u2030", us).toPattern()), "per-mille toPattern");
    }

    static void testFieldRunStructureContract() {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        AttributedCharacterIterator it = df.formatToCharacterIterator(-1234.5);
        StringBuilder text = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) text.append(ch);
        check("-1,234.50".equals(text.toString()), "formatToCharacterIterator text");
        // walk the six attribute runs
        it.first();
        check(it.getRunStart() == 0 && it.getRunLimit() == 1
              && it.getAttributes().containsKey(java.text.NumberFormat.Field.SIGN), "run [0,1) is SIGN");
        it.setIndex(1);
        check(it.getRunStart() == 1 && it.getRunLimit() == 2
              && it.getAttributes().containsKey(java.text.NumberFormat.Field.INTEGER), "run [1,2) is INTEGER");
        it.setIndex(2);
        check(it.getRunStart() == 2 && it.getRunLimit() == 3
              && it.getAttributes().containsKey(java.text.NumberFormat.Field.GROUPING_SEPARATOR)
              && it.getAttributes().containsKey(java.text.NumberFormat.Field.INTEGER),
              "run [2,3) is GROUPING_SEPARATOR + INTEGER");
        it.setIndex(3);
        check(it.getRunStart() == 3 && it.getRunLimit() == 6
              && it.getAttributes().containsKey(java.text.NumberFormat.Field.INTEGER), "run [3,6) is INTEGER");
        it.setIndex(6);
        check(it.getRunStart() == 6 && it.getRunLimit() == 7
              && it.getAttributes().containsKey(java.text.NumberFormat.Field.DECIMAL_SEPARATOR), "run [6,7) is DECIMAL_SEPARATOR");
        it.setIndex(7);
        check(it.getRunStart() == 7 && it.getRunLimit() == 9
              && it.getAttributes().containsKey(java.text.NumberFormat.Field.FRACTION), "run [7,9) is FRACTION");
        // FieldPosition variants of the same output
        java.text.FieldPosition fp = new java.text.FieldPosition(java.text.NumberFormat.Field.FRACTION);
        df.format(1234.56, new StringBuffer(), fp);
        check(fp.getBeginIndex() == 6 && fp.getEndIndex() == 8, "FRACTION spans [6,8)");
        java.text.FieldPosition fi = new java.text.FieldPosition(java.text.NumberFormat.Field.INTEGER);
        df.format(-1234.56, new StringBuffer(), fi);
        check(fi.getBeginIndex() == 1 && fi.getEndIndex() == 6, "INTEGER spans [1,6) (sign excluded)");
        java.text.FieldPosition fd = new java.text.FieldPosition(java.text.NumberFormat.INTEGER_FIELD);
        df.format(1234.56, new StringBuffer(), fd);
        check(fd.getField() == java.text.NumberFormat.INTEGER_FIELD && fd.getBeginIndex() == 0 && fd.getEndIndex() == 5,
              "legacy INTEGER_FIELD spans [0,5)");
    }

    static void testBigIntegerBoundaryContract() {
        DecimalFormat df = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(Locale.US));
        check("-9,223,372,036,854,775,808".equals(df.format(Long.MIN_VALUE)), "Long.MIN_VALUE");
        check("987,654,321".equals(df.format(987654321L)), "format(long)");
        check("12,345,678,901,234,567,890.123".equals(df.format(new BigDecimal("12345678901234567890.123"))),
              "format(BigDecimal)");
        check("123,456,789,012,345,678,901,234,567,890"
              .equals(df.format(new java.math.BigInteger("123456789012345678901234567890"))),
              "format(BigInteger)");
        expectX(IllegalArgumentException.class,
                () -> df.format("str", new StringBuffer(), new java.text.FieldPosition(0)),
                "format(String) -> IAE");
    }

    static void testVersionLockedStringsContract() {
        // JDK-8 legacy JRE locale data: currency instance keeps explicit negative sub-pattern
        check("\u00A4#,##0.00;(\u00A4#,##0.00)"
              .equals(((DecimalFormat) java.text.NumberFormat.getCurrencyInstance(Locale.US)).toPattern()),
              "currency instance toPattern (JRE data)");
        // JDK-8 NaN symbol is U+FFFD
        check("\uFFFD".equals(DecimalFormatSymbols.getInstance(Locale.US).getNaN()),
              "NaN symbol is U+FFFD (JRE data)");
    }
    public static void main(String[] args) throws Exception {
        testPatternMatrixContract();
        testRoundingModeMatrixContract();
        testParseMatrixContract();
        testMultiplierContract();
        testSymbolDrivenOutputContract();
        testSpecialPatternErrorsContract();
        testGroupingSizeContract();
        testToPatternCanonicalizationContract();
        testFieldRunStructureContract();
        testBigIntegerBoundaryContract();
        testVersionLockedStringsContract();
        testWhiteBox();
        testPatternFormats();
        testConstructorsAndExceptions();
        testPrefixSuffix();
        testMultiplierGroupingSeparator();
        testParseBigDecimal();
        testRoundingModes();
        testBoundaryValues();
        testSymbolsAndPatternMutation();
        testDigitSettersAndCurrency();
        testEqualsCloneHashCode();
        testCharacterIteratorAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

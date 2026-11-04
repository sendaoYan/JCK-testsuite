/*
 * @test
 * @summary java.text.DecimalFormat conformance: white-box structure + black-box pattern/format/parse behavior.
 * @run main DecimalFormatTest
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
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

    static void expectThrows(Runnable r, Class<? extends Throwable> t, String m) {
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
        Class<?> c = Class.forName("java.text.DecimalFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.text.NumberFormat"), "extends NumberFormat");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, DecimalFormatSymbols.class) != null, "ctor(String,DecimalFormatSymbols)");
        check(c.getMethod("toPattern") != null && c.getMethod("applyPattern", String.class) != null, "pattern methods");
        check(c.getMethod("setParseBigDecimal", boolean.class) != null, "setParseBigDecimal");
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
        check("NaN".equals(df.format(Double.NaN)), "format NaN");
        check("\u221e".equals(df.format(Double.POSITIVE_INFINITY)), "format Infinity symbol");
        check("-\u221e".equals(df.format(Double.NEGATIVE_INFINITY)), "format -Infinity symbol");
        check(df.parse("1.7976931348623157E308", new ParsePosition(0)).doubleValue() == Double.MAX_VALUE,
              "parse MAX_VALUE");
        check(df.parse("", new ParsePosition(0)) == null, "parse empty string returns null");
        // empty pattern is accepted and falls back to the default pattern
        DecimalFormat empty = new DecimalFormat("");
        check("1.5".equals(empty.format(1.5)), "empty pattern falls back to default");
        // illegal pattern with two decimal separators
        expectThrows(() -> new DecimalFormat("#.##.##"), IllegalArgumentException.class,
                     "illegal pattern rejected");
        expectThrows(() -> new DecimalFormat("0.00", SYM).applyPattern("#.##.##"),
                     IllegalArgumentException.class, "applyPattern illegal rejected");
        // MAX/MIN round-trip through scientific pattern is deterministic
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

    static void testEqualsCloneHashCode() {
        DecimalFormat a = new DecimalFormat("#,##0.00", SYM);
        DecimalFormat b = (DecimalFormat) a.clone();
        check(a.equals(b) && b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "clone hashCode");
        check(a.toString().contains("#,##0.00"), "toString contains pattern");
        DecimalFormat d = new DecimalFormat("0.00", SYM);
        check(!a.equals(d), "different pattern not equal");
    }

    static void testBlackBox() {
        // no-arg and String constructors
        check("#,##0.###".equals(new DecimalFormat().toPattern()), "no-arg ctor default pattern");
        check("#,##0.00".equals(new DecimalFormat("#,##0.00").toPattern()), "ctor(String) pattern");

        // integer/fraction digit getters and setters
        DecimalFormat df = new DecimalFormat("#,##0.00", SYM);
        check(df.getMaximumFractionDigits() == 2, "getMaximumFractionDigits");
        check(df.getMinimumFractionDigits() == 2, "getMinimumFractionDigits");
        check(df.getMinimumIntegerDigits() == 1, "getMinimumIntegerDigits");
        check(df.getMaximumIntegerDigits() > 1, "getMaximumIntegerDigits non-trivial");
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(3);
        df.setMinimumIntegerDigits(2);
        df.setMaximumIntegerDigits(5);
        check(df.getMinimumFractionDigits() == 1, "setMinimumFractionDigits");
        check(df.getMaximumFractionDigits() == 3, "setMaximumFractionDigits");
        check(df.getMinimumIntegerDigits() == 2, "setMinimumIntegerDigits");
        check(df.getMaximumIntegerDigits() == 5, "setMaximumIntegerDigits");
        check("1,234.5".equals(df.format(1234.5)), "digit setters affect format");

        // rounding mode getter/setter
        df.setRoundingMode(RoundingMode.HALF_UP);
        check(df.getRoundingMode() == RoundingMode.HALF_UP, "getRoundingMode/setRoundingMode");

        // positive suffix
        DecimalFormat ps = new DecimalFormat("#,##0.00", SYM);
        ps.setPositiveSuffix("+");
        check("+".equals(ps.getPositiveSuffix()), "getPositiveSuffix/setPositiveSuffix");
        check("1,234.50+".equals(ps.format(1234.5)), "positive suffix affects format");

        // grouping used
        DecimalFormat gu = new DecimalFormat("#,##0.00", SYM);
        gu.setGroupingUsed(false);
        check(!gu.isGroupingUsed(), "setGroupingUsed/isGroupingUsed");
        check("1234.50".equals(gu.format(1234.5)), "grouping off format");

        // currency
        DecimalFormat cu = new DecimalFormat("#,##0.00", SYM);
        check(Currency.getInstance("USD").equals(cu.getCurrency()), "default currency USD");
        cu.setCurrency(Currency.getInstance("EUR"));
        check(Currency.getInstance("EUR").equals(cu.getCurrency()), "setCurrency/getCurrency EUR");

        // setDecimalFormatSymbols
        DecimalFormatSymbols custom = DecimalFormatSymbols.getInstance(US);
        custom.setDecimalSeparator(';');
        DecimalFormat sd = new DecimalFormat("0.00", SYM);
        sd.setDecimalFormatSymbols(custom);
        check(custom.equals(sd.getDecimalFormatSymbols()), "setDecimalFormatSymbols/getDecimalFormatSymbols");
        check("1;50".equals(sd.format(1.5)), "custom symbols affect format");

        // strict mode
        DecimalFormat sr = new DecimalFormat("0.00", SYM);
        check(!sr.isStrict(), "isStrict default false");
        sr.setStrict(true);
        check(sr.isStrict(), "setStrict(true)");

        // localized pattern
        DecimalFormat lp = new DecimalFormat("#,##0.00", SYM);
        check("#,##0.00".equals(lp.toLocalizedPattern()), "toLocalizedPattern US");
        lp.applyLocalizedPattern("0.00");
        check("1.50".equals(lp.format(1.5)), "applyLocalizedPattern format");
        check("1234.50".equals(lp.format(1234.5)), "applyLocalizedPattern no grouping");

        // formatToCharacterIterator
        DecimalFormat fi = new DecimalFormat("#,##0.00", SYM);
        AttributedCharacterIterator it = fi.formatToCharacterIterator(1234.5);
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != AttributedCharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("1,234.50".equals(sb.toString()), "formatToCharacterIterator text");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPatternFormats();
        testPrefixSuffix();
        testMultiplierGroupingSeparator();
        testParseBigDecimal();
        testRoundingModes();
        testSymbolsAndPatternMutation();
        testEqualsCloneHashCode();
        testBoundaryValues();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

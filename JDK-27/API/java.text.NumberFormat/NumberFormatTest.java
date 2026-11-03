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
    }

    public static void main(String[] args) throws Exception {
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

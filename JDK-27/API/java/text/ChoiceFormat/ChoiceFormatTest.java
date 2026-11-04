/*
 * @test
 * @summary java.text.ChoiceFormat conformance: white-box structure + black-box range/format/parse behavior.
 * @run main ChoiceFormatTest
 */

import java.text.ChoiceFormat;
import java.util.Arrays;
import java.util.Locale;

public class ChoiceFormatTest {
    static int passed;

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
        Class<?> c = Class.forName("java.text.ChoiceFormat");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass().getName().equals("java.text.NumberFormat"), "extends NumberFormat");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(double[].class, String[].class) != null, "ctor(double[],String[])");
        check(c.getMethod("nextDouble", double.class) != null, "nextDouble(double)");
        check(c.getMethod("previousDouble", double.class) != null, "previousDouble(double)");
        check(c.getMethod("setChoices", double[].class, String[].class) != null, "setChoices");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPatternRange() {
        ChoiceFormat cf = new ChoiceFormat("0#none|1#one|2#few|5#many");
        check("none".equals(cf.format(-1.0)), "-1 -> none");
        check("none".equals(cf.format(0.0)), "0 -> none");
        check("none".equals(cf.format(0.5)), "0.5 -> none");
        check("one".equals(cf.format(1.0)), "1 -> one");
        check("few".equals(cf.format(2.0)), "2 -> few");
        check("few".equals(cf.format(4.9)), "4.9 -> few");
        check("many".equals(cf.format(5.0)), "5 -> many");
        check("many".equals(cf.format(100.0)), "100 -> many");
        check("one".equals(cf.format(1L)), "format(long)");
    }

    static void testParseRoundTrip() throws Exception {
        ChoiceFormat cf = new ChoiceFormat("0#none|1#one|2#few|5#many");
        check(cf.parse("none").doubleValue() == 0.0, "parse none -> 0.0");
        check(cf.parse("one").doubleValue() == 1.0, "parse one -> 1.0");
        check(cf.parse("few").doubleValue() == 2.0, "parse few -> 2.0");
        check(cf.parse("many").doubleValue() == 5.0, "parse many -> 5.0");
    }

    static void testNextPreviousDouble() {
        check(ChoiceFormat.nextDouble(1.0) == 1.0000000000000002, "nextDouble(1.0)");
        check(ChoiceFormat.previousDouble(1.0) == 0.9999999999999999, "previousDouble(1.0)");
        check(ChoiceFormat.nextDouble(1.0, true) == 1.0000000000000002, "nextDouble(1.0,true)");
        check(ChoiceFormat.nextDouble(1.0, false) == 0.9999999999999999, "nextDouble(1.0,false)");
    }

    static void testChoices() {
        ChoiceFormat cf = new ChoiceFormat(new double[]{1, 2, 3}, new String[]{"a", "b", "c"});
        check("a".equals(cf.format(1.0)), "limits format 1 -> a");
        check("b".equals(cf.format(2.0)), "limits format 2 -> b");
        check("c".equals(cf.format(3.0)), "limits format 3 -> c");
        check("a".equals(cf.format(1.5)), "limits format 1.5 -> a (lower bound inclusive)");
        check(Arrays.equals(cf.getLimits(), new double[]{1.0, 2.0, 3.0}), "getLimits");
        check(Arrays.equals(cf.getFormats(), new String[]{"a", "b", "c"}), "getFormats");

        cf.setChoices(new double[]{0, 10}, new String[]{"low", "high"});
        check(Arrays.equals(cf.getLimits(), new double[]{0.0, 10.0}), "setChoices/getLimits");
        check("high".equals(cf.format(10)), "setChoices format 10 -> high");
    }

    static void testStrictAndContract() {
        ChoiceFormat cf = new ChoiceFormat("0#none|1#one|2#many");
        expectThrows(() -> { boolean ignored = cf.isStrict(); },
                     UnsupportedOperationException.class, "isStrict throws UnsupportedOperationException");
        expectThrows(() -> cf.setStrict(true),
                     UnsupportedOperationException.class, "setStrict throws UnsupportedOperationException");

        ChoiceFormat a = new ChoiceFormat("0#none|1#one|2#many");
        ChoiceFormat b = (ChoiceFormat) a.clone();
        check(a.equals(b) && b.equals(a), "clone equals");
        check(a.hashCode() == b.hashCode(), "clone hashCode");
        check(a.equals(new ChoiceFormat("0#none|1#one|2#many")), "same choices equal");
        check(!a.equals(new ChoiceFormat("0#none|1#one")), "different choices not equal");
    }

    static void testBlackBox() throws Exception {
        ChoiceFormat cf = new ChoiceFormat("0#none|1#one|2#many");
        cf.applyPattern("0#no|1#yes");
        check("0.0#no|1.0#yes".equals(cf.toPattern()), "applyPattern/toPattern round-trip");
        check(cf.toString().contains("0.0#no|1.0#yes"), "toString contains pattern");
        check("no".equals(cf.format(-1.0)), "applyPattern format -1 -> no");
        check("no".equals(cf.format(0.0)), "applyPattern format 0 -> no");
        check("yes".equals(cf.format(1.0)), "applyPattern format 1 -> yes");
        check("yes".equals(cf.format(100.0)), "applyPattern format 100 -> yes");
        check("yes".equals(cf.format(1L)), "applyPattern format(long)");
        check(cf.parse("no").doubleValue() == 0.0, "applyPattern parse no -> 0.0");
        check(cf.parse("yes").doubleValue() == 1.0, "applyPattern parse yes -> 1.0");

        // ---- boundary/equivalence additions (M2) ----
        ChoiceFormat f = new ChoiceFormat("0#none|1#one|2#many");
        check("none".equals(f.format(Double.NEGATIVE_INFINITY)), "-infinity -> first choice");
        check("many".equals(f.format(Double.POSITIVE_INFINITY)), "+infinity -> last choice");
        check("none".equals(f.format(Double.NaN)), "NaN -> first choice");
        check(Double.isNaN(f.parse("unknown", new java.text.ParsePosition(0)).doubleValue()),
              "parse unknown -> NaN");

        check(ChoiceFormat.nextDouble(0.0) == Double.MIN_VALUE, "nextDouble(0) == MIN_VALUE");
        check(ChoiceFormat.previousDouble(0.0) == -Double.MIN_VALUE, "previousDouble(0) == -MIN_VALUE");
        check(ChoiceFormat.nextDouble(Double.MAX_VALUE) == Double.POSITIVE_INFINITY,
              "nextDouble(MAX) == +inf");
        check(ChoiceFormat.previousDouble(-Double.MAX_VALUE) == Double.NEGATIVE_INFINITY,
              "previousDouble(-MAX) == -inf");
        check(Double.isNaN(ChoiceFormat.nextDouble(Double.NaN)), "nextDouble(NaN) == NaN");
        check(Double.isNaN(ChoiceFormat.previousDouble(Double.NaN)), "previousDouble(NaN) == NaN");
        check(ChoiceFormat.nextDouble(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "nextDouble(+inf) == +inf");
        check(ChoiceFormat.previousDouble(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY,
              "previousDouble(-inf) == -inf");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPatternRange();
        testParseRoundTrip();
        testNextPreviousDouble();
        testChoices();
        testStrictAndContract();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

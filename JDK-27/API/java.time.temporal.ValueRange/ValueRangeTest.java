/*
 * @test
 * @summary java.time.temporal.ValueRange conformance: white-box structure + black-box behavior.
 * @run main ValueRangeTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;

public class ValueRangeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String m) {
        try { r.run(); } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(m + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(m + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.ValueRange");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getConstructors().length == 0, "no public constructor");
        boolean serializable = false;
        for (Class<?> i : c.getInterfaces()) if (i == Serializable.class) serializable = true;
        check(serializable, "implements Serializable");
        check(c.getMethod("of", long.class, long.class) != null, "of(long,long)");
        check(c.getMethod("of", long.class, long.class, long.class) != null, "of(long,long,long)");
        check(c.getMethod("of", long.class, long.class, long.class, long.class) != null, "of(long,long,long,long)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ValueRange fixed = ValueRange.of(1, 12);
        check(fixed.getMinimum() == 1, "fixed minimum");
        check(fixed.getLargestMinimum() == 1, "fixed largest minimum");
        check(fixed.getSmallestMaximum() == 12, "fixed smallest maximum");
        check(fixed.getMaximum() == 12, "fixed maximum");
        check(fixed.isFixed(), "fixed isFixed");
        check(fixed.isIntValue(), "fixed isIntValue");
        check(fixed.isValidValue(1) && fixed.isValidValue(12), "fixed valid bounds");
        check(!fixed.isValidValue(0) && !fixed.isValidValue(13), "fixed invalid outside");
        check(fixed.isValidIntValue(6), "fixed isValidIntValue");
        check(!fixed.isValidIntValue(0), "fixed invalid int");
        check(fixed.checkValidValue(5, ChronoField.MONTH_OF_YEAR) == 5, "fixed checkValidValue");
        check(fixed.checkValidIntValue(5, ChronoField.MONTH_OF_YEAR) == 5, "fixed checkValidIntValue");
        expectThrows(DateTimeException.class, () -> fixed.checkValidValue(13, ChronoField.MONTH_OF_YEAR), "fixed checkValidValue 13");
        expectThrows(DateTimeException.class, () -> fixed.checkValidIntValue(0, ChronoField.MONTH_OF_YEAR), "fixed checkValidIntValue 0");
        check(fixed.toString().equals("1 - 12"), "fixed toString");

        ValueRange var3 = ValueRange.of(1, 28, 31);
        check(var3.getMinimum() == 1, "var3 minimum");
        check(var3.getLargestMinimum() == 1, "var3 largest minimum");
        check(var3.getSmallestMaximum() == 28, "var3 smallest maximum");
        check(var3.getMaximum() == 31, "var3 maximum");
        check(!var3.isFixed(), "var3 not fixed");
        check(var3.isValidValue(29), "var3 29 valid");
        check(var3.isValidIntValue(31), "var3 31 valid int");
        check(var3.toString().equals("1 - 28/31"), "var3 toString");

        ValueRange var4 = ValueRange.of(1, 28, 30, 31);
        check(var4.getLargestMinimum() == 28, "var4 largest minimum");
        check(var4.getSmallestMaximum() == 30, "var4 smallest maximum");
        check(var4.getMinimum() == 1 && var4.getMaximum() == 31, "var4 min/max");
        check(var4.toString().equals("1/28 - 30/31"), "var4 toString");

        // equals / hashCode
        check(ValueRange.of(1, 12).equals(ValueRange.of(1, 12)), "equals same");
        check(!ValueRange.of(1, 12).equals(ValueRange.of(1, 28, 31)), "not equals different");
        check(ValueRange.of(1, 12).hashCode() == ValueRange.of(1, 12).hashCode(), "hashCode equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

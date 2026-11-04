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
        testDeepValueRange();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepValueRange() {
        ValueRange fixed = ValueRange.of(0, 59);
        check(fixed.isFixed() && fixed.getMinimum() == 0 && fixed.getMaximum() == 59
            && fixed.getSmallestMaximum() == 59 && fixed.getLargestMinimum() == 0, "fixed getters");
        xeq(fixed.toString(), "0 - 59", "fixed toString");
        check(fixed.isIntValue(), "isIntValue small");
        check(!ChronoField.NANO_OF_DAY.range().isIntValue(), "NanoOfDay not int value");
        ValueRange vr = ValueRange.of(1, 28, 31);
        check(!vr.isFixed(), "variable not fixed");
        check(vr.getMinimum() == 1 && vr.getLargestMinimum() == 1 && vr.getSmallestMaximum() == 28 && vr.getMaximum() == 31,
            "DOM-like getters");
        xeq(vr.toString(), "1 - 28/31", "variable toString");
        check(vr.isValidValue(28) && vr.isValidValue(31) && !vr.isValidValue(32) && !vr.isValidValue(0), "isValidValue");
        check(vr.isValidIntValue(31), "isValidIntValue");
        check(vr.checkValidValue(31, ChronoField.DAY_OF_MONTH) == 31L, "checkValidValue 31 ok");
        xact(() -> vr.checkValidValue(32, ChronoField.DAY_OF_MONTH), DateTimeException.class,
            "Invalid value for DayOfMonth (valid values 1 - 28/31): 32", "checkValidValue 32");
        xact(() -> vr.checkValidValue(0, ChronoField.DAY_OF_MONTH), DateTimeException.class,
            "Invalid value for DayOfMonth (valid values 1 - 28/31): 0", "checkValidValue 0");
        xact(() -> fixed.checkValidIntValue(60, ChronoField.SECOND_OF_MINUTE), DateTimeException.class,
            "Invalid value for SecondOfMinute (valid values 0 - 59): 60", "checkValidIntValue 60");
        check(fixed.checkValidIntValue(0, ChronoField.SECOND_OF_MINUTE) == 0, "checkValidIntValue 0 ok");
        xact(() -> ValueRange.of(5, 1), IllegalArgumentException.class,
            "Minimum value must be less than maximum value", "of(5,1)");
        xact(() -> ValueRange.of(1, 5, 3), IllegalArgumentException.class,
            "Smallest maximum value must be less than largest maximum value", "of(1,5,3)");
        xact(() -> ValueRange.of(1, 2, 3, 2), IllegalArgumentException.class,
            "Smallest maximum value must be less than largest maximum value", "of(1,2,3,2)");
        xact(() -> ValueRange.of(3, 1, 2), IllegalArgumentException.class,
            "Minimum value must be less than smallest maximum value", "of(3,1,2) JDK27");
        ValueRange wide = ValueRange.of(1, 5, 28, 31);
        check(wide.getMinimum() == 1 && wide.getLargestMinimum() == 5 && wide.getSmallestMaximum() == 28 && wide.getMaximum() == 31,
            "4-arg getters");
        xeq(wide.toString(), "1/5 - 28/31", "4-arg toString");
        check(ValueRange.of(0, 59).equals(ValueRange.of(0, 59)), "equals");
        check(ValueRange.of(0, 59).hashCode() == ValueRange.of(0, 59).hashCode(), "hashCode");
        check(!ValueRange.of(0, 59).equals(ValueRange.of(0, 58)), "not equals diff max");
        check(!ValueRange.of(1, 28, 31).equals(ValueRange.of(1, 28, 30)), "not equals diff smax");
        check(!ValueRange.of(0, Long.MAX_VALUE).isIntValue(), "long max not int value");
        check(ValueRange.of(1, 10).isValidValue(Long.MIN_VALUE) == false, "min long invalid");
        xact(() -> ChronoField.NANO_OF_SECOND.range().checkValidIntValue(1234567890, ChronoField.NANO_OF_SECOND), DateTimeException.class,
            "Invalid value for NanoOfSecond (valid values 0 - 999999999): 1234567890", "long field int check");
    }

}

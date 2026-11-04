/*
 * @test
 * @summary java.time.temporal.ValueRange API conformance: min/max and validity checks.
 * @run main ValueRangeTest
 */

import java.time.temporal.ValueRange;
import java.time.DateTimeException;
import java.time.temporal.ChronoField;

public class ValueRangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testValueRange() {
        ValueRange vr = ValueRange.of(1, 12);
        check(vr.getMinimum() == 1, "minimum");
        check(vr.getMaximum() == 12, "maximum");
        check(vr.isValidValue(1), "1 valid");
        check(vr.isValidValue(12), "12 valid");
        check(!vr.isValidValue(0), "0 invalid");
        check(!vr.isValidValue(13), "13 invalid");

        ValueRange vr2 = ValueRange.of(1, 28, 31);
        check(vr2.getLargestMinimum() == 1, "largest minimum");
        check(vr2.getSmallestMaximum() == 28, "smallest maximum");
        check(vr2.isValidValue(29), "29 valid");
        check(vr2.isValidIntValue(31), "31 valid int");
    }

    public static void main(String[] args) {
        testValueRange();
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
             "Minimum value must be less than maximum value", "of(3,1,2) JDK8");
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

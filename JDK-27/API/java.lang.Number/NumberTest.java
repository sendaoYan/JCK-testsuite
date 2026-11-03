/*
 * @test
 * @summary java.lang.Number conformance (white-box structural + black-box
 *          behavioral): abstract superclass, six primitive conversions, default
 *          byteValue()/shortValue() narrowing, and float/atomic/BigDecimal
 *          subclass conversions.
 *
 * Design case
 * -----------
 * 1. Contract: Number is the abstract superclass of all numeric wrapper types;
 *    it exposes intValue()/longValue()/floatValue()/doubleValue() (abstract) plus
 *    concrete byteValue()/shortValue() narrowing defaults.
 * 2. Usage: any Number subclass can be narrowed/widened through the six primitive
 *    accessors; doubleValue()/floatValue() approximate, int/long saturate NaN/Inf.
 * 3. Composition: obtain a Number (Integer/Long/Float/Double/AtomicInteger/...), then
 *    call intValue -> longValue -> doubleValue to observe truncation/widening.
 * 4. Invariants: Number is abstract with Object superclass; narrowing is deterministic
 *    (NaN->0, +Inf->MAX, -Inf->MIN for int/long).
 * 5. Boundary/exception: no methods throw; byteValue/shortValue discard high bits.
 * @run main NumberTest
 */

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NumberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        check(Modifier.isAbstract(Number.class.getModifiers()), "Number is abstract");
        check(Number.class.getSuperclass() == Object.class, "Number extends Object");

        Number[] instances = {
            Integer.valueOf(1), Long.valueOf(1L), Short.valueOf((short) 1),
            Byte.valueOf((byte) 1), Float.valueOf(1.0f), Double.valueOf(1.0),
            BigInteger.ONE, BigDecimal.ONE,
            new AtomicInteger(1), new AtomicLong(1L)
        };
        for (Number n : instances) {
            check(n instanceof Number, n.getClass().getName() + " is a Number");
        }
    }

    static void testIntegerConversions() {
        Number n = Integer.valueOf(300);
        check(n.intValue() == 300, "Integer.intValue");
        check(n.longValue() == 300L, "Integer.longValue");
        check(n.floatValue() == 300.0f, "Integer.floatValue");
        check(n.doubleValue() == 300.0, "Integer.doubleValue");
        check(n.byteValue() == (byte) 300, "Integer.byteValue truncates");
        check(n.shortValue() == 300, "Integer.shortValue");
    }

    static void testLongNarrowing() {
        Number n = Long.valueOf(0x1234567890abcdefL);
        check(n.intValue() == (int) 0x90abcdef, "Long.intValue truncates high half");
        check(n.byteValue() == (byte) 0xef, "Long.byteValue lowest byte");
        check(n.shortValue() == (short) 0xcdef, "Long.shortValue low two bytes");
        check(n.longValue() == 0x1234567890abcdefL, "Long.longValue identity");
    }

    static void testDoubleSpecialValues() {
        Number nan = Double.valueOf(Double.NaN);
        check(Double.isNaN(nan.doubleValue()), "NaN.doubleValue");
        check(Float.isNaN(nan.floatValue()), "NaN.floatValue");
        check(nan.intValue() == 0, "(int) NaN == 0");
        check(nan.longValue() == 0L, "(long) NaN == 0");
        check(nan.byteValue() == 0, "(byte) NaN == 0");
        check(nan.shortValue() == 0, "(short) NaN == 0");

        Number posInf = Double.valueOf(Double.POSITIVE_INFINITY);
        check(posInf.intValue() == Integer.MAX_VALUE, "(int) +Inf == Integer.MAX_VALUE");
        check(posInf.longValue() == Long.MAX_VALUE, "(long) +Inf == Long.MAX_VALUE");
        check(posInf.byteValue() == -1, "(byte) +Inf == -1");
        check(posInf.shortValue() == -1, "(short) +Inf == -1");

        Number negInf = Double.valueOf(Double.NEGATIVE_INFINITY);
        check(negInf.intValue() == Integer.MIN_VALUE, "(int) -Inf == Integer.MIN_VALUE");
        check(negInf.longValue() == Long.MIN_VALUE, "(long) -Inf == Long.MIN_VALUE");
    }

    static void testBigIntegerBigDecimal() {
        check(BigInteger.ONE.intValue() == 1, "BigInteger.ONE.intValue");
        check(BigInteger.ONE.longValue() == 1L, "BigInteger.ONE.longValue");
        check(BigDecimal.ONE.doubleValue() == 1.0, "BigDecimal.ONE.doubleValue");
        check(BigDecimal.ONE.floatValue() == 1.0f, "BigDecimal.ONE.floatValue");
    }

    // ---- White-box / structural ----
    static void testWhiteBox() {
        try {
            Class<?> c = Class.forName("java.lang.Number");
            check(!c.isInterface(), "Number is not an interface");
            check(Modifier.isAbstract(c.getModifiers()), "Number is abstract");
            check(c.getSuperclass() == Object.class, "Number extends Object");
            check(java.io.Serializable.class.isAssignableFrom(c), "Number implements Serializable");
            check(Modifier.isAbstract(c.getMethod("intValue").getModifiers()),
                  "intValue() is abstract");
            check(Modifier.isAbstract(c.getMethod("doubleValue").getModifiers()),
                  "doubleValue() is abstract");
            check(c.getMethod("intValue").getReturnType() == int.class,
                  "intValue() returns int");
            check(c.getMethod("doubleValue").getReturnType() == double.class,
                  "doubleValue() returns double");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testFloatSpecialValues() {
        Number nan = Float.valueOf(Float.NaN);
        check(Float.isNaN(nan.floatValue()), "Float NaN.floatValue");
        check(nan.intValue() == 0 && nan.longValue() == 0L, "(int/long) Float NaN == 0");
        check(nan.byteValue() == 0 && nan.shortValue() == 0, "(byte/short) Float NaN == 0");

        Number posInf = Float.valueOf(Float.POSITIVE_INFINITY);
        check(posInf.intValue() == Integer.MAX_VALUE, "(int) Float +Inf == MAX_VALUE");
        check(posInf.longValue() == Long.MAX_VALUE, "(long) Float +Inf == MAX_VALUE");

        Number negInf = Float.valueOf(Float.NEGATIVE_INFINITY);
        check(negInf.intValue() == Integer.MIN_VALUE, "(int) Float -Inf == MIN_VALUE");
        check(negInf.longValue() == Long.MIN_VALUE, "(long) Float -Inf == MIN_VALUE");
    }

    static void testAtomicAndFractional() {
        check(new AtomicInteger(0x12345678).intValue() == 0x12345678, "AtomicInteger.intValue");
        check(new AtomicInteger(0x12345678).longValue() == 0x12345678L, "AtomicInteger.longValue");
        check(new AtomicLong(0x1234567890abcdefL).intValue() == (int) 0x90abcdef,
              "AtomicLong.intValue truncates");
        // Fractional double narrows toward zero when truncated to int/long.
        check(Double.valueOf(3.99).intValue() == 3, "Double.intValue truncates toward zero");
        check(Double.valueOf(-3.99).intValue() == -3, "negative Double.intValue truncates");
    }

    // ---- Black-box / behavioral ----
    static void testBlackBox() {
        testHierarchy();
        testIntegerConversions();
        testLongNarrowing();
        testDoubleSpecialValues();
        testBigIntegerBigDecimal();
        testFloatSpecialValues();
        testAtomicAndFractional();
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

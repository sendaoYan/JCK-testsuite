/*
 * @test
 * @summary java.lang.Number API conformance: abstract superclass of the numeric
 *          wrappers, the six abstract primitive conversions, and the default
 *          byteValue()/shortValue() (JLS 5.1.3 narrowing, incl. NaN/Infinity
 *          saturation to int/long).
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

    public static void main(String[] args) {
        testHierarchy();
        testIntegerConversions();
        testLongNarrowing();
        testDoubleSpecialValues();
        testBigIntegerBigDecimal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    // ================= 契约深化（2026-11）追加 =================

    /** 四个整型包装的 42 经 Number 六访问器全表精确断言（Boolean 不是 Number，另测）。 */
    static void testWrapperConversionSuite() {
        Number[] fortyTwos = {
            Integer.valueOf(42), Long.valueOf(42L),
            Short.valueOf((short) 42), Byte.valueOf((byte) 42)
        };
        for (Number n : fortyTwos) {
            String cls = n.getClass().getSimpleName();
            check(n.byteValue() == 42, cls + ".byteValue == 42");
            check(n.shortValue() == 42, cls + ".shortValue == 42");
            check(n.intValue() == 42, cls + ".intValue == 42");
            check(n.longValue() == 42L, cls + ".longValue == 42");
            check(n.floatValue() == 42.0f, cls + ".floatValue == 42.0f");
            check(n.doubleValue() == 42.0, cls + ".doubleValue == 42.0");
        }
        // Boolean/Character 不属于 Number 家族
        Object boolObj = Boolean.TRUE;
        Object charObj = Character.valueOf('a');
        check(!(boolObj instanceof Number), "Boolean is not a Number");
        check(!(charObj instanceof Number), "Character is not a Number");
    }

    /** Double/Float 的截断（toward zero）与 -0.0 位级保真。 */
    static void testDoubleFloatPrecision() {
        // Double(3.9).intValue() == 3（向零截断）
        Double d = Double.valueOf(3.9);
        check(d.intValue() == 3, "Double(3.9).intValue == 3");
        check(d.longValue() == 3L, "Double(3.9).longValue == 3");
        check(d.byteValue() == 3, "Double(3.9).byteValue == 3");
        check(d.shortValue() == 3, "Double(3.9).shortValue == 3");
        check(d.floatValue() == 3.9f, "Double(3.9).floatValue == 3.9f");
        check(d.doubleValue() == 3.9, "Double(3.9).doubleValue");
        check(Double.valueOf(-3.9).intValue() == -3, "Double(-3.9).intValue == -3");
        // Double(257.75)：int 截断 + byte/short 窄化
        Number big = Double.valueOf(257.75);
        check(big.intValue() == 257, "Double(257.75).intValue == 257");
        check(big.shortValue() == 257, "Double(257.75).shortValue == 257");
        check(big.byteValue() == 1, "Double(257.75).byteValue == 1 (257 mod 256)");
        // Float(-0.0f).doubleValue() == -0.0（位级保真，经位模式断言避开 == 对 ±0 的混同）
        Number negZero = Float.valueOf(-0.0f);
        check(Double.doubleToLongBits(negZero.doubleValue()) == 0x8000000000000000L,
              "Float(-0.0f).doubleValue() == -0.0 (bits)");
        check(Float.floatToIntBits(negZero.floatValue()) == 0x80000000,
              "Float(-0.0f).floatValue() == -0.0f (bits)");
        check(negZero.intValue() == 0, "Float(-0.0f).intValue == 0");
        Number negZeroD = Double.valueOf(-0.0);
        check(Double.doubleToLongBits(negZeroD.doubleValue()) == 0x8000000000000000L,
              "Double(-0.0).doubleValue() == -0.0 (bits)");
        check(negZeroD.intValue() == 0, "Double(-0.0).intValue == 0");
        // Float(3.9f) 截断
        check(Float.valueOf(3.9f).intValue() == 3, "Float(3.9f).intValue == 3");
        check(Float.valueOf(-3.9f).longValue() == -3L, "Float(-3.9f).longValue == -3");
    }

    /** Long→double 的 2^53 精度损失与 Long→float 的 2^24 精度损失（值锁定）。 */
    static void testLongPrecisionLoss() {
        // Long.MAX_VALUE → double 丢失低 11 位（最近邻值锁定，双侧一致）
        check(Double.valueOf(Long.MAX_VALUE).doubleValue() == 9.223372036854776E18,
              "Long.MAX_VALUE -> 9.223372036854776E18");
        check(Double.valueOf(Long.MIN_VALUE).doubleValue() == -9.223372036854776E18,
              "Long.MIN_VALUE -> -9.223372036854776E18");
        check(Double.valueOf(Long.MAX_VALUE).doubleValue() != 9.223372036854775E18,
              "Long.MAX_VALUE rounded up (not 9.223372036854775E18)");
        // 2^63 恰为偶数尾数可表示的一半：显式位模式断言
        check(Double.doubleToLongBits((double) Long.MAX_VALUE) == 0x43E0000000000000L,
              "bits(Long.MAX_VALUE as double) == 0x43E0000000000000");
        // Long → float：2^24+1 不可表示，舍入到 2^24
        check(Long.valueOf(16777217L).floatValue() == 1.6777216E7f, "2^24+1 -> float 2^24");
        check(Long.valueOf(16777216L).floatValue() == 1.6777216E7f, "2^24 -> float exact");
        // long → double 在 2^53 内无损
        check(Long.valueOf(9007199254740993L).doubleValue() == 9.007199254740992E15,
              "2^53+1 -> double 2^53 (round-to-even)");
        // Integer.MAX_VALUE → float 也有精度损失（2^31 可精确，2^31-1 不可）
        check(Integer.valueOf(16777217).floatValue() == 1.6777216E7f, "Integer 2^24+1 -> float 2^24");
        // Long.MAX_VALUE → float
        check(Long.valueOf(Long.MAX_VALUE).floatValue() == 9.223372E18f, "Long.MAX_VALUE -> float");
    }

    /** JLS 5.1.3 饱和规则的极端样本。 */
    static void testSaturationExtremes() {
        check(Double.valueOf(1e300).longValue() == Long.MAX_VALUE, "1e300 -> long saturates MAX");
        check(Double.valueOf(-1e300).longValue() == Long.MIN_VALUE, "-1e300 -> long saturates MIN");
        check(Double.valueOf(1e300).intValue() == Integer.MAX_VALUE, "1e300 -> int saturates MAX");
        check(Double.valueOf(-1e300).intValue() == Integer.MIN_VALUE, "-1e300 -> int saturates MIN");
        check(Float.valueOf(1e30f).longValue() == Long.MAX_VALUE, "1e30f -> long saturates MAX");
        check(Float.valueOf(1e30f).intValue() == Integer.MAX_VALUE, "1e30f -> int saturates MAX");
        // Integer.MIN_VALUE 的 floatValue（-2^31 恰可精确表示）
        check(Integer.valueOf(Integer.MIN_VALUE).floatValue() == (float) Integer.MIN_VALUE,
              "Integer.MIN_VALUE floatValue exact (power of two)");
        check(Integer.valueOf(Integer.MIN_VALUE).doubleValue() == -2.147483648E9,
              "Integer.MIN_VALUE doubleValue exact");
    }

    /** 结构契约：4 抽象 + 2 具体窄化默认 + 公有方法名集合。 */
    static void testStructuralContract() {
        try {
            Class<?> c = Number.class;
            check(Modifier.isAbstract(c.getMethod("intValue").getModifiers()), "intValue abstract");
            check(Modifier.isAbstract(c.getMethod("longValue").getModifiers()), "longValue abstract");
            check(Modifier.isAbstract(c.getMethod("floatValue").getModifiers()), "floatValue abstract");
            check(Modifier.isAbstract(c.getMethod("doubleValue").getModifiers()), "doubleValue abstract");
            check(!Modifier.isAbstract(c.getMethod("byteValue").getModifiers()),
                  "byteValue concrete default (narrowing via intValue)");
            check(!Modifier.isAbstract(c.getMethod("shortValue").getModifiers()),
                  "shortValue concrete default (narrowing via intValue)");
            check(c.getMethod("byteValue").getReturnType() == byte.class, "byteValue returns byte");
            check(c.getMethod("shortValue").getReturnType() == short.class, "shortValue returns short");
            // Number 公有实例方法恰好是六个 xxxValue
            java.util.Set<String> names = new java.util.HashSet<String>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
                    names.add(m.getName());
                }
            }
            check(names.size() == 6, "exactly 6 public instance methods, got " + names);
            check(names.contains("intValue") && names.contains("longValue")
                  && names.contains("floatValue") && names.contains("doubleValue")
                  && names.contains("byteValue") && names.contains("shortValue"),
                  "method name set is the six xxxValue accessors");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /** BigInteger/BigDecimal 的 Number 语义（截断向零、超出按位窄化）。 */
    static void testBigDecimalBigIntegerExact() {
        // 2^64-1 的 BigInteger 经 longValue 按位截断为 -1
        check(new BigInteger("18446744073709551615").longValue() == -1L,
              "BigInteger(2^64-1).longValue == -1 (bit truncation)");
        // 2^32 的 intValue 为 0（低 32 位全 0）
        check(BigInteger.valueOf(2L).pow(32).intValue() == 0, "BigInteger(2^32).intValue == 0");
        check(BigInteger.valueOf(2L).pow(70).longValue() == 0L, "BigInteger(2^70).longValue == 0");
        check(new BigInteger("123456789abcdef", 16).longValue() == 0x123456789abcdefL,
              "BigInteger hex longValue exact");
        check(BigInteger.TEN.doubleValue() == 10.0, "BigInteger.TEN.doubleValue");
        check(BigInteger.TEN.floatValue() == 10.0f, "BigInteger.TEN.floatValue");
        // BigDecimal 截断向零（丢弃小数部分）
        check(new BigDecimal("2.5").intValue() == 2, "BigDecimal(2.5).intValue == 2");
        check(new BigDecimal("-2.5").intValue() == -2, "BigDecimal(-2.5).intValue == -2");
        check(new BigDecimal("2.5").longValue() == 2L, "BigDecimal(2.5).longValue == 2");
        check(new BigDecimal("123.456").doubleValue() == 123.456, "BigDecimal(123.456).doubleValue");
        check(BigDecimal.valueOf(0.1).doubleValue() == 0.1, "BigDecimal.valueOf(0.1).doubleValue");
        // AtomicInteger/AtomicLong 补充
        check(new AtomicInteger(-42).doubleValue() == -42.0, "AtomicInteger(-42).doubleValue");
        check(new AtomicLong(-42L).floatValue() == -42.0f, "AtomicLong(-42).floatValue");
        check(new AtomicLong(0xFFFFFFFFL).intValue() == -1, "AtomicLong(2^32-1).intValue == -1");
    }

    /** 跨类型 Number equals 恒 false（equals 不跨包装类型）。 */
    static void testCrossTypeEquals() {
        Number[] ones = {
            Integer.valueOf(1), Long.valueOf(1L), Short.valueOf((short) 1),
            Byte.valueOf((byte) 1), Float.valueOf(1.0f), Double.valueOf(1.0)
        };
        for (int i = 0; i < ones.length; i++) {
            for (int j = 0; j < ones.length; j++) {
                boolean expected = (i == j);
                check(ones[i].equals(ones[j]) == expected,
                      ones[i].getClass().getSimpleName() + ".equals("
                      + ones[j].getClass().getSimpleName() + ") == " + expected);
            }
        }
        // Float(1.0f) vs Double(1.0)：值相等但类型不同 → equals false
        check(!Float.valueOf(1.0f).equals(Double.valueOf(1.0)), "Float(1.0f) != Double(1.0)");
        // null 恒 false
        check(!Integer.valueOf(1).equals(null), "equals(null) == false");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testHierarchy();
        testIntegerConversions();
        testLongNarrowing();
        testDoubleSpecialValues();
        testBigIntegerBigDecimal();
        testFloatSpecialValues();
        testAtomicAndFractional();
        // 契约深化（2026-11）
        testWrapperConversionSuite();
        testDoubleFloatPrecision();
        testLongPrecisionLoss();
        testSaturationExtremes();
        testStructuralContract();
        testBigDecimalBigIntegerExact();
        testCrossTypeEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.math.BigDecimal API conformance: full contract coverage.
 *          constructors (String/double/BigInteger/int/long/char[] + MathContext) +
 *          exact exception matrix, null semantics, arithmetic with/without
 *          MathContext, divide overloads + non-terminating ArithmeticException,
 *          divideToIntegralValue/remainder/divideAndRemainder, pow, scale/
 *          precision/unscaledValue/ulp/movePointLeft/movePointRight/scaleByPowerOfTen/
 *          stripTrailingZeros, RoundingMode 8-mode matrix, abs/negate/plus/
 *          signum, compareTo-vs-equals scale semantics, min/max/hashCode,
 *          toString/toEngineeringString/toPlainString, toBigInteger/*Exact
 *          conversions, immutability, serialization round-trip + serialVersionUID.
 * @run main BigDecimalTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class BigDecimalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static BigDecimal roundTrip(BigDecimal v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (BigDecimal) ois.readObject();
    }

    static void checkUID(Class<?> c, long expected) {
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == expected,
              c.getSimpleName() + " serialVersionUID");
    }

    static void testConstructors() {
        check(new BigDecimal("0").equals(BigDecimal.ZERO), "String zero");
        check(new BigDecimal("10.00").scale() == 2, "String scale preserved");
        check(new BigDecimal("-1.5").toString().equals("-1.5"), "String negative");
        check(new BigDecimal("+1.5").toString().equals("1.5"), "String leading plus");
        check(new BigDecimal("1E+3").scale() == -3, "String exponent scale");
        check(new BigDecimal("0.5").equals(new BigDecimal(0.5)), "double exact 0.5");
        check(new BigDecimal(0.1).doubleValue() == 0.1, "double round-trip");
        check(new BigDecimal(BigInteger.TEN).equals(BigDecimal.TEN), "BigInteger");
        check(new BigDecimal(BigInteger.valueOf(123), 2).equals(new BigDecimal("1.23")),
                "BigInteger scale");
        check(new BigDecimal(123).equals(new BigDecimal("123")), "int");
        check(new BigDecimal(123L).equals(new BigDecimal("123")), "long");
        check(new BigDecimal(new char[]{'1','2','3'}).equals(new BigDecimal("123")), "char[]");
        check(new BigDecimal(new char[]{'-','1','.','5'}, 0, 4).equals(new BigDecimal("-1.5")),
                "char[] offset len");
        check(BigDecimal.valueOf(100L).equals(new BigDecimal("100")), "valueOf long");
        check(BigDecimal.valueOf(100L, 2).equals(new BigDecimal("1.00")), "valueOf long scale");
        check(BigDecimal.valueOf(0.1).equals(new BigDecimal("0.1")), "valueOf double");
        check(BigDecimal.valueOf(0.0).equals(new BigDecimal("0.0")), "valueOf double 0");
        check(new BigDecimal("123", MathContext.DECIMAL32).equals(new BigDecimal("123")),
                "String MathContext");
        check(new BigDecimal(123, MathContext.DECIMAL32).equals(new BigDecimal("123")),
                "int MathContext");
        // MathContext precision truncation on construction
        check(new BigDecimal("123456", new MathContext(3, RoundingMode.DOWN))
                .equals(new BigDecimal("1.23E+5")), "String MathContext rounding");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> new BigDecimal((String) null), "String null");
        checkThrows(NullPointerException.class, () -> new BigDecimal((BigInteger) null), "BigInteger null");
        checkThrows(NullPointerException.class, () -> new BigDecimal((char[]) null), "char[] null");
        checkThrows(NullPointerException.class, () -> new BigDecimal((char[]) null, 0, 1), "char[] off null");
        checkThrows(NullPointerException.class, () -> new BigDecimal("1", (MathContext) null), "String MC null");
        checkThrows(NullPointerException.class, () -> new BigDecimal(BigInteger.ONE, (MathContext) null), "BigInteger MC null");
        checkThrows(NullPointerException.class, () -> new BigDecimal(1, (MathContext) null), "int MC null");
        checkThrows(NullPointerException.class, () -> new BigDecimal(1L, (MathContext) null), "long MC null");
        checkThrows(NullPointerException.class, () -> new BigDecimal(1.0, (MathContext) null), "double MC null");
        BigDecimal one = BigDecimal.ONE;
        checkThrows(NullPointerException.class, () -> one.add(null), "add null");
        checkThrows(NullPointerException.class, () -> one.add(BigDecimal.ONE, (MathContext) null), "add MC null");
        checkThrows(NullPointerException.class, () -> one.subtract(null), "subtract null");
        checkThrows(NullPointerException.class, () -> one.multiply(null), "multiply null");
        checkThrows(NullPointerException.class, () -> one.divide(null), "divide null");
        checkThrows(NullPointerException.class, () -> one.divide(null, 2, RoundingMode.HALF_UP), "divide scale null divisor");
        checkThrows(NullPointerException.class, () -> one.divide(BigDecimal.ONE, (RoundingMode) null), "divide RoundingMode null");
        checkThrows(NullPointerException.class, () -> one.remainder(null), "remainder null");
        checkThrows(NullPointerException.class, () -> one.divideAndRemainder(null), "divideAndRemainder null");
        checkThrows(NullPointerException.class, () -> one.pow(2, (MathContext) null), "pow MC null");
        checkThrows(NullPointerException.class, () -> one.setScale(0, (RoundingMode) null), "setScale mode null");
        checkThrows(NullPointerException.class, () -> one.round((MathContext) null), "round null");
        checkThrows(NullPointerException.class, () -> one.abs((MathContext) null), "abs MC null");
        checkThrows(NullPointerException.class, () -> one.negate((MathContext) null), "negate MC null");
        checkThrows(NullPointerException.class, () -> one.plus((MathContext) null), "plus MC null");
        checkThrows(NullPointerException.class, () -> one.compareTo(null), "compareTo null");
        checkThrows(NullPointerException.class, () -> one.min(null), "min null");
        checkThrows(NullPointerException.class, () -> one.max(null), "max null");
    }

    static void testArithmetic() {
        BigDecimal a = new BigDecimal("10.00");
        BigDecimal b = new BigDecimal("3.0");
        check(a.add(b).equals(new BigDecimal("13.00")), "add");
        check(a.subtract(b).equals(new BigDecimal("7.00")), "subtract");
        check(a.multiply(b).equals(new BigDecimal("30.000")), "multiply");
        check(a.add(b, MathContext.DECIMAL32).equals(new BigDecimal("13.00")), "add MC");
        check(a.multiply(b, MathContext.DECIMAL32).equals(new BigDecimal("30.000")), "multiply MC");
        // immutability
        check(a.equals(new BigDecimal("10.00")) && b.equals(new BigDecimal("3.0")),
                "immutability after arithmetic");
        check(new BigDecimal("-1.5").plus().equals(new BigDecimal("-1.5")), "plus");
        check(new BigDecimal("1.5").plus().equals(new BigDecimal("1.5")), "plus positive");
    }

    static void testDivide() {
        BigDecimal a = new BigDecimal("10.00");
        BigDecimal b = new BigDecimal("4");
        check(a.divide(b, 2, RoundingMode.HALF_UP).equals(new BigDecimal("2.50")), "divide scale");
        check(a.divide(b, 2, BigDecimal.ROUND_HALF_UP).equals(new BigDecimal("2.50")),
                "divide scale int rounding");
        check(a.divide(b, RoundingMode.HALF_UP).equals(new BigDecimal("2.50")), "divide RoundingMode");
        check(a.divide(b).equals(new BigDecimal("2.50")), "divide exact");
        check(a.divide(b, MathContext.DECIMAL32).equals(new BigDecimal("2.50")), "divide MC");
        check(new BigDecimal("1").divide(new BigDecimal("8")).equals(new BigDecimal("0.125")),
                "divide terminating");
        check(new BigDecimal("1").divide(new BigDecimal("3"), MathContext.DECIMAL32)
                .equals(new BigDecimal("0.3333333")), "divide MC non-terminating");
        check(new BigDecimal("7").divideToIntegralValue(new BigDecimal("2"))
                .equals(new BigDecimal("3")), "divideToIntegralValue");
        check(new BigDecimal("-7").divideToIntegralValue(new BigDecimal("2"))
                .equals(new BigDecimal("-3")), "divideToIntegralValue negative");
        check(new BigDecimal("7").remainder(new BigDecimal("2")).equals(new BigDecimal("1")), "remainder");
        check(new BigDecimal("-7").remainder(new BigDecimal("2")).equals(new BigDecimal("-1")),
                "remainder negative");
        BigDecimal[] dr = new BigDecimal("7").divideAndRemainder(new BigDecimal("2"));
        check(dr[0].equals(new BigDecimal("3")) && dr[1].equals(new BigDecimal("1")),
                "divideAndRemainder");
    }

    static void testPow() {
        check(new BigDecimal("2.5").pow(3).equals(new BigDecimal("15.625")), "pow");
        check(new BigDecimal("5").pow(0).equals(BigDecimal.ONE), "pow 0");
        check(new BigDecimal("2").pow(10).equals(new BigDecimal("1024")), "pow int");
        check(new BigDecimal("2").pow(10, MathContext.DECIMAL32).equals(new BigDecimal("1024")),
                "pow MC");
    }

    static void testScalePrecision() {
        BigDecimal a = new BigDecimal("1.23");
        check(a.scale() == 2, "scale");
        check(a.precision() == 3, "precision");
        check(a.unscaledValue().equals(BigInteger.valueOf(123)), "unscaledValue");
        check(new BigDecimal("0.00100").precision() == 3, "precision ignores leading zeros");
        check(new BigDecimal("100").scale() == 0, "scale integer");
        check(new BigDecimal("1E+3").scale() == -3, "scale negative exponent");
        check(new BigDecimal("123").ulp().equals(BigDecimal.ONE), "ulp integer");
        check(new BigDecimal("1.5").ulp().equals(new BigDecimal("0.1")), "ulp");
        check(new BigDecimal("123").movePointLeft(2).equals(new BigDecimal("1.23")), "movePointLeft");
        check(new BigDecimal("1.23").movePointRight(2).equals(new BigDecimal("123")), "movePointRight");
        check(new BigDecimal("1.23").movePointLeft(0).equals(a), "movePointLeft 0");
        check(new BigDecimal("1.23").scaleByPowerOfTen(2).equals(new BigDecimal("123")),
                "scaleByPowerOfTen");
        check(new BigDecimal("1.23").scaleByPowerOfTen(-2).equals(new BigDecimal("0.0123")),
                "scaleByPowerOfTen negative");
        check(new BigDecimal("10.00").stripTrailingZeros().equals(new BigDecimal("1E+1")),
                "stripTrailingZeros");
        check(new BigDecimal("100").stripTrailingZeros().equals(new BigDecimal("1E+2")),
                "stripTrailingZeros integer");
        check(new BigDecimal("1.2300").stripTrailingZeros().equals(new BigDecimal("1.23")),
                "stripTrailingZeros fraction");
    }

    static void testRoundingMatrix() {
        // setScale(0, mode) on representative values
        checkRounding("1.5", new int[]{2, 1, 2, 1, 2, 1, 2});
        checkRounding("-1.5", new int[]{-2, -1, -1, -2, -2, -1, -2});
        checkRounding("2.5", new int[]{3, 2, 3, 2, 3, 2, 2});
        checkRounding("1.4", new int[]{2, 1, 2, 1, 1, 1, 1});
        checkRounding("1.6", new int[]{2, 1, 2, 1, 2, 2, 2});
        checkRounding("-1.4", new int[]{-2, -1, -1, -2, -1, -1, -1});
        // UNNECESSARY throws whenever rounding would change the value
        checkThrows(ArithmeticException.class,
                () -> new BigDecimal("1.5").setScale(0, RoundingMode.UNNECESSARY), "UNNECESSARY 1.5");
        // UNNECESSARY ok when exact
        check(new BigDecimal("1.0").setScale(0, RoundingMode.UNNECESSARY).equals(BigDecimal.ONE),
                "UNNECESSARY exact");
        check(new BigDecimal("123456").round(new MathContext(3, RoundingMode.HALF_UP))
                .equals(new BigDecimal("1.23E+5")), "round MathContext");
    }

    static void checkRounding(String value, int[] expected) {
        RoundingMode[] modes = { RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
                RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
                RoundingMode.HALF_EVEN };
        BigDecimal v = new BigDecimal(value);
        for (int i = 0; i < modes.length; i++) {
            BigDecimal r = v.setScale(0, modes[i]);
            check(r.equals(BigDecimal.valueOf(expected[i])),
                    "setScale(0," + modes[i] + ") of " + value + " == " + expected[i]
                            + " but was " + r);
        }
    }

    static void testAbsNegateSignum() {
        check(new BigDecimal("-1.5").abs().equals(new BigDecimal("1.5")), "abs");
        check(new BigDecimal("1.5").abs().equals(new BigDecimal("1.5")), "abs positive");
        check(new BigDecimal("-1.5").negate().equals(new BigDecimal("1.5")), "negate");
        check(new BigDecimal("1.5").signum() == 1, "signum positive");
        check(new BigDecimal("-1.5").signum() == -1, "signum negative");
        check(BigDecimal.ZERO.signum() == 0, "signum zero");
        BigDecimal v = new BigDecimal("-1.5");
        v.abs(); v.negate();
        check(v.equals(new BigDecimal("-1.5")), "immutability abs/negate");
    }

    static void testComparison() {
        BigDecimal a = new BigDecimal("2.0");
        BigDecimal b = new BigDecimal("2.00");
        BigDecimal c = new BigDecimal("3.0");
        check(a.compareTo(b) == 0, "compareTo ignores scale");
        check(!a.equals(b), "equals considers scale");
        check(a.compareTo(c) < 0 && c.compareTo(a) > 0, "compareTo order");
        check(a.equals(new BigDecimal("2.0")) && !a.equals(c), "equals");
        check(a.hashCode() == new BigDecimal("2.0").hashCode(), "hashCode equal");
        check(a.equals(null) == false, "equals null");
        check(a.equals("2.0") == false, "equals other type");
        check(a.min(c) == a && a.max(c) == c, "min/max");
    }

    static void testConversions() {
        check(new BigDecimal("1.5").toString().equals("1.5"), "toString");
        check(new BigDecimal("0.001").toPlainString().equals("0.001"), "toPlainString");
        check(new BigDecimal("1E+3").toPlainString().equals("1000"), "toPlainString exponent");
        check(new BigDecimal("1E+5").toEngineeringString().equals("100E+3"),
                "toEngineeringString multiple of 3");
        check(new BigDecimal("1000").toEngineeringString().equals("1000"),
                "toEngineeringString integer");
        check(new BigDecimal("3.7").toBigInteger().equals(BigInteger.valueOf(3)), "toBigInteger");
        check(new BigDecimal("3.0").toBigIntegerExact().equals(BigInteger.valueOf(3)),
                "toBigIntegerExact");
        check(new BigDecimal("123").longValue() == 123L, "longValue");
        check(new BigDecimal("123").longValueExact() == 123L, "longValueExact");
        check(new BigDecimal("123").intValue() == 123, "intValue");
        check(new BigDecimal("123").intValueExact() == 123, "intValueExact");
        check(new BigDecimal("123").shortValueExact() == (short) 123, "shortValueExact");
        check(new BigDecimal("123").byteValueExact() == (byte) 123, "byteValueExact");
        check(new BigDecimal("2.5").floatValue() == 2.5f, "floatValue");
        check(new BigDecimal("2.5").doubleValue() == 2.5d, "doubleValue");
    }

    static void testConversionsExactErrors() {
        checkThrows(ArithmeticException.class, () -> new BigDecimal("3.7").toBigIntegerExact(),
                "toBigIntegerExact fraction");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("1E+20").intValueExact(),
                "intValueExact overflow");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("1E+30").longValueExact(),
                "longValueExact overflow");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("1.5").byteValueExact(),
                "byteValueExact fraction");
    }

    static void testSerialization() throws Exception {
        checkUID(BigDecimal.class, 6108874887143696463L);
        BigDecimal[] vals = { BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal("-1.50"),
                new BigDecimal("123456789.12345678901234567890") };
        for (BigDecimal v : vals) {
            BigDecimal r = roundTrip(v);
            check(r.equals(v), "serialization round-trip " + v);
            check(r.scale() == v.scale(), "serialization preserves scale " + v);
            check(r != v, "serialization new instance " + v);
        }
    }

    static void testErrors() {
        checkThrows(NumberFormatException.class, () -> new BigDecimal("1.2.3"), "bad String");
        checkThrows(NumberFormatException.class, () -> new BigDecimal(""), "empty String");
        checkThrows(NumberFormatException.class, () -> new BigDecimal(Double.NaN), "double NaN");
        checkThrows(NumberFormatException.class, () -> new BigDecimal(Double.POSITIVE_INFINITY),
                "double Inf");
        checkThrows(NumberFormatException.class, () -> new BigDecimal(new char[]{'1','x'}, 0, 2),
                "bad char[]");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("1").divide(new BigDecimal("3")),
                "divide non-terminating");
        checkThrows(ArithmeticException.class, () -> BigDecimal.ONE.divide(BigDecimal.ZERO),
                "divide by zero");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("1.5").setScale(0, RoundingMode.UNNECESSARY),
                "setScale UNNECESSARY");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("1.5").setScale(0),
                "setScale(int) UNNECESSARY");
        checkThrows(IllegalArgumentException.class, () -> new BigDecimal("1").setScale(0, 99),
                "setScale invalid int rounding");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("10").pow(-1), "pow negative");
        checkThrows(ArithmeticException.class, () -> new BigDecimal("1").movePointRight(Integer.MAX_VALUE),
                "movePointRight overflow");
    }

    // ===== 契约深化（2026-11）：精确异常消息助手 =====

    static void checkMsg(Class<? extends Throwable> expected, String message, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            if (!expected.isInstance(t)) {
                throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            if (t.getMessage() == null || !t.getMessage().equals(message)) {
                throw new AssertionError(msg + ": expected message \"" + message
                        + "\" but was \"" + t.getMessage() + "\"");
            }
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // JDK-8 的 BigDecimal(String) 非法输入 NFE 无消息文本（JDK-19+ 起为详细文本）
    static void checkNullMsg(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            if (!expected.isInstance(t)) {
                throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            if (t.getMessage() != null) {
                throw new AssertionError(msg + ": expected null message but was \"" + t.getMessage() + "\"");
            }
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ===== 契约深化（2026-11）：double 构造精度坑（招牌教学契约） =====

    static void testDoubleConstructorPrecision() {
        // new BigDecimal(double) 取 double 的精确二进制展开；valueOf 用 Double.toString 规范十进制
        check(new BigDecimal(0.1).toString().equals(
              "0.1000000000000000055511151231257827021181583404541015625"),
              "new BigDecimal(0.1) 全等 55 位二进制展开");
        check(new BigDecimal(0.1).toString().length() == 57, "0.1 展开总长 57 字符");
        check(new BigDecimal(0.1).scale() == 55, "new BigDecimal(0.1) scale == 55");
        check(new BigDecimal(0.1).unscaledValue().equals(new BigInteger(
              "1000000000000000055511151231257827021181583404541015625")),
              "new BigDecimal(0.1) unscaledValue 精确");
        check(BigDecimal.valueOf(0.1).toString().equals("0.1"), "valueOf(0.1) == \"0.1\"");
        check(BigDecimal.valueOf(0.1).scale() == 1, "valueOf(0.1) scale 1");
        check(BigDecimal.valueOf(0.1).precision() == 1, "valueOf(0.1) precision 1");
        check(new BigDecimal(0.1).doubleValue() == 0.1, "new BigDecimal(0.1).doubleValue round-trip");
        check(new BigDecimal(0.1).add(new BigDecimal(0.2)).toString().equals(
              "0.3000000000000000166533453693773481063544750213623046875"),
              "double 构造的 0.1+0.2 精确和");
        check((0.1 + 0.2) != 0.3, "二进制 double 0.1+0.2 != 0.3 对照");
        check(new BigDecimal("0.1").add(new BigDecimal("0.2")).toString().equals("0.3"),
              "十进制 0.1+0.2 == 0.3");
        check(new BigDecimal("0.1").add(new BigDecimal("0.2")).equals(new BigDecimal("0.3")),
              "十进制 0.3 equals 契约");
        check(new BigDecimal(2.0).toString().equals("2"), "new BigDecimal(2.0) 最小 scale 0 → \"2\"");
        check(new BigDecimal(2.0).scale() == 0, "new BigDecimal(2.0) scale 0");
        check(new BigDecimal(2.0).equals(new BigDecimal("2")), "new BigDecimal(2.0) equals 2");
        check(BigDecimal.valueOf(1.0E-5).toString().equals("0.000010"), "valueOf(1.0E-5) == \"0.000010\"");
        check(BigDecimal.valueOf(1.0E-5).scale() == 6, "valueOf(1.0E-5) scale 6");
        check(new BigDecimal(1.0E-5).toString().equals(
              "0.000010000000000000000818030539140313095458623138256371021270751953125"),
              "new BigDecimal(1.0E-5) 精确展开");
        check(new BigDecimal(0.5).toString().equals("0.5"), "二进可精确表示的 0.5");
        check(new BigDecimal(0.5).scale() == 1, "0.5 scale 1");
        check(BigDecimal.valueOf(-0.0).signum() == 0, "valueOf(-0.0) signum 0");
        check(BigDecimal.valueOf(-0.0).toString().equals("0.0"), "valueOf(-0.0) 无负零字符串");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> new BigDecimal(Double.NaN),
              "new BigDecimal(NaN) 消息逐字");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> new BigDecimal(Double.POSITIVE_INFINITY),
              "new BigDecimal(+Inf) 消息逐字");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> new BigDecimal(Double.NEGATIVE_INFINITY),
              "new BigDecimal(-Inf) 消息逐字");
        // JDK-8：valueOf(NaN) 的 NFE 无消息文本（JDK-19+ 起为 "Infinite or NaN"）
        checkNullMsg(NumberFormatException.class, () -> BigDecimal.valueOf(Double.NaN),
              "valueOf(NaN) JDK-8 无消息");
    }

    // ===== 契约深化（2026-11）：String 语法与 scale/unscaled 精确 =====

    static void testStringGrammar() {
        BigDecimal e3 = new BigDecimal("1.23E+3");
        check(e3.toString().equals("1.23E+3"), "\"1.23E+3\" toString 保持");
        check(e3.scale() == -1, "\"1.23E+3\" scale -1");
        check(e3.unscaledValue().equals(BigInteger.valueOf(123)), "\"1.23E+3\" unscaled 123");
        check(e3.intValue() == 1230, "\"1.23E+3\" 值 1230");
        BigDecimal m3 = new BigDecimal("1.23e-3");
        check(m3.toString().equals("0.00123"), "\"1.23e-3\" 小写 e 普通形式");
        check(m3.scale() == 5, "\"1.23e-3\" scale 5");
        check(new BigDecimal("+1.23").scale() == 2, "\"+1.23\" 前导加号 scale 2");
        check(new BigDecimal("+1.23").unscaledValue().intValue() == 123, "\"+1.23\" unscaled 123");
        check(new BigDecimal("0").scale() == 0, "\"0\" scale 0");
        check(new BigDecimal("0").equals(BigDecimal.ZERO), "\"0\" equals ZERO");
        BigDecimal n0 = new BigDecimal("-0.0");
        check(n0.scale() == 1, "\"-0.0\" scale 1");
        check(n0.unscaledValue().signum() == 0, "\"-0.0\" unscaled 为 0");
        check(n0.signum() == 0, "\"-0.0\" signum 0（无负零）");
        check(!n0.equals(BigDecimal.ZERO), "\"-0.0\" 不 equals ZERO（scale 分叉）");
        check(n0.toString().equals("0.0"), "\"-0.0\" toString 归一为 \"0.0\"");
        check(new BigDecimal(new char[]{'1','.','2','3','E','+','3'}).equals(e3), "char[] 指数形式");
        check(new BigDecimal(new char[]{'x','1','.','2','3','E','+','3'}, 1, 7).equals(e3),
              "char[] offset/len 指数形式");
        check(BigDecimal.valueOf(0L, 5).toString().equals("0.00000"), "valueOf(0,5) 保留 scale");
        check(BigDecimal.valueOf(0L, 5).scale() == 5, "valueOf(0,5) scale 5");
        // JDK-8：以下非法串 NFE 均无消息文本（JDK-19+ 起带详细文本）
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal(""), "空串 NFE 无消息");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal("1.2.3"), "双小数点 NFE 无消息");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal("Infinity"), "Infinity 串 NFE 无消息");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal("1e"), "裸指数 NFE 无消息");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal("--1"), "双负号 NFE 无消息");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal("-"), "孤立负号 NFE 无消息");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal("1E+3x"), "指数尾随垃圾 NFE 无消息");
        checkNullMsg(NumberFormatException.class,
              () -> new BigDecimal(new char[]{'1','.','x'}, 0, 3), "char[] 坏字符 NFE 无消息");
    }

    // ===== 契约深化（2026-11）：equals vs compareTo 分叉 + hashCode 契约 =====

    static void testEqualsComparetoFork() {
        BigDecimal a = new BigDecimal("2.0");
        BigDecimal b = new BigDecimal("2.00");
        check(!a.equals(b), "2.0 vs 2.00 equals false（scale 参与 equals）");
        check(a.compareTo(b) == 0, "2.0 vs 2.00 compareTo == 0");
        check(b.compareTo(a) == 0, "compareTo 反对称（相等侧）");
        check(a.hashCode() == 621, "2.0 hashCode == 621（实测锁定）");
        check(b.hashCode() == 6202, "2.00 hashCode == 6202（实测锁定）");
        check(a.hashCode() != b.hashCode(), "等值不同 scale 的 hashCode 不同");
        check(new BigDecimal("3.0").hashCode() == 931, "3.0 hashCode == 931（实测锁定）");
        check(BigDecimal.ZERO.hashCode() == 0, "ZERO hashCode 0");
        check(new BigDecimal("0.0").hashCode() == 1, "\"0.0\" hashCode 1");
        check(new BigDecimal("-0.0").hashCode() == 1, "\"-0.0\" hashCode 1（与 0.0 同）");
        BigDecimal a2 = new BigDecimal("2.0");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals 相等 ⇒ hashCode 相等");
        check(a.equals(a), "equals 自反");
        check(new BigDecimal("1.0").compareTo(new BigDecimal("1.00")) == 0, "1.0 vs 1.00 compareTo 0");
        check(BigDecimal.ZERO.compareTo(new BigDecimal("0.00")) == 0, "ZERO vs 0.00 compareTo 0");
        check(!BigDecimal.ZERO.equals(new BigDecimal("0.00")), "ZERO vs 0.00 equals false");
        check(new BigDecimal("-0.0").compareTo(BigDecimal.ZERO) == 0, "\"-0.0\" vs ZERO compareTo 0");
        // compareTo 传递性与反对称抽检
        check(new BigDecimal("1").compareTo(new BigDecimal("2")) < 0, "1 < 2");
        check(new BigDecimal("2").compareTo(new BigDecimal("3")) < 0, "2 < 3");
        check(new BigDecimal("1").compareTo(new BigDecimal("3")) < 0, "1 < 3（传递性）");
        check(new BigDecimal("-1").compareTo(BigDecimal.ZERO) < 0, "-1 < 0");
        check(BigDecimal.ZERO.compareTo(new BigDecimal("1")) < 0, "0 < 1");
        BigDecimal x = new BigDecimal("7.13"), y = new BigDecimal("3.89");
        check(x.compareTo(y) == -y.compareTo(x), "sgn(a,b) == -sgn(b,a)");
        // min/max 在 compareTo 相等时返回 this（身份同一）
        check(a.min(b) == a, "min 相等时返回 this");
        check(a.max(b) == a, "max 相等时返回 this");
        check(a.max(new BigDecimal("3.0")).equals(new BigDecimal("3.0")), "max 3.0");
        check(a.min(new BigDecimal("1.5")).equals(new BigDecimal("1.5")), "min 1.5");
        BigDecimal h = new BigDecimal("12345.678");
        check(h.hashCode() == h.hashCode(), "hashCode 两次调用稳定");
        check(BigDecimal.ZERO.scale() == 0 && BigDecimal.ONE.scale() == 0 && BigDecimal.TEN.scale() == 0,
              "ZERO/ONE/TEN scale 0");
    }

    // ===== 契约深化（2026-11）：setScale 八模式核心表（±2.5 全表） =====

    static void testSetScaleEightModeTable() {
        checkModeTable("2.5", new String[]{"3", "2", "3", "2", "3", "2", "2"});
        checkModeTable("-2.5", new String[]{"-3", "-2", "-2", "-3", "-3", "-2", "-2"});
        checkModeTable("2.4", new String[]{"3", "2", "3", "2", "2", "2", "2"});
        checkModeTable("2.6", new String[]{"3", "2", "3", "2", "3", "3", "3"});
        checkModeTable("-2.6", new String[]{"-3", "-2", "-2", "-3", "-3", "-3", "-3"});
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("2.5").setScale(0, RoundingMode.UNNECESSARY),
              "2.5 UNNECESSARY 消息逐字");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("-2.5").setScale(0, RoundingMode.UNNECESSARY),
              "-2.5 UNNECESSARY 消息逐字");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("1.234").setScale(2), "setScale(int) 隐含 UNNECESSARY 消息");
        check(new BigDecimal("1.23").setScale(5).toString().equals("1.23000"), "setScale 升补零");
        check(new BigDecimal("1.2300").setScale(2, RoundingMode.UNNECESSARY).toString().equals("1.23"),
              "setScale 降位精确 UNNECESSARY OK");
        check(new BigDecimal("1230").setScale(-1).toString().equals("1.23E+3"), "setScale 负 scale");
        check(new BigDecimal("1.23").setScale(2) == new BigDecimal("1.23")
              || new BigDecimal("1.23").setScale(2).equals(new BigDecimal("1.23")),
              "setScale 目标 scale 等值");
        checkMsg(IllegalArgumentException.class, "Invalid rounding mode",
              () -> new BigDecimal("1").setScale(0, 99), "setScale(0,99) 消息逐字");
        checkMsg(IllegalArgumentException.class, "Invalid rounding mode",
              () -> new BigDecimal("1").divide(BigDecimal.ONE, 2, 99), "divide(scale,99) 消息逐字");
    }

    static void checkModeTable(String v, String[] expected) {
        RoundingMode[] modes = { RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
                RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
                RoundingMode.HALF_EVEN };
        for (int i = 0; i < modes.length; i++) {
            BigDecimal r = new BigDecimal(v).setScale(0, modes[i]);
            check(r.toString().equals(expected[i]),
                    "setScale(0," + modes[i] + ") of " + v + " == " + expected[i] + " but was " + r);
        }
    }

    // ===== 契约深化（2026-11）：divide 六+二模式矩阵（商的 preferred scale 行为） =====

    static void testDivideModeMatrix() {
        // divide(divisor, RoundingMode)：商的 scale 取被除数 scale
        String[] ten3 = {"4", "3", "4", "3", "3", "3", "3"};
        String[] negTen3 = {"-4", "-3", "-3", "-4", "-3", "-3", "-3"};
        String[] one3 = {"1", "0", "1", "0", "0", "0", "0"};
        checkDivideMode("10", "3", ten3);
        checkDivideMode("-10", "3", negTen3);
        checkDivideMode("1", "3", one3);
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("10").divide(new BigDecimal("3"), RoundingMode.UNNECESSARY),
              "divide UNNECESSARY 非整除消息逐字");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("1").divide(new BigDecimal("3"), RoundingMode.UNNECESSARY),
              "1/3 UNNECESSARY 消息逐字");
        // 精确整除 + UNNECESSARY OK
        check(new BigDecimal("10").divide(new BigDecimal("2"), RoundingMode.UNNECESSARY)
                .toString().equals("5"), "10/2 UNNECESSARY 精确");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("1").divide(new BigDecimal("8"), RoundingMode.UNNECESSARY),
              "1/8 商 scale 0 不精确 → UNNECESSARY 抛");
        // divide(bd, scale, mode)：目标 scale
        check(new BigDecimal("10").divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP)
                .toString().equals("3.33"), "10/3 scale2 HALF_UP");
        check(new BigDecimal("10").divide(new BigDecimal("3"), 4, RoundingMode.HALF_EVEN)
                .toString().equals("3.3333"), "10/3 scale4 HALF_EVEN");
        check(new BigDecimal("2").divide(new BigDecimal("3"), 3, RoundingMode.HALF_UP)
                .toString().equals("0.667"), "2/3 scale3 HALF_UP");
        check(new BigDecimal("-10").divide(new BigDecimal("3"), 2, RoundingMode.FLOOR)
                .toString().equals("-3.34"), "-10/3 scale2 FLOOR");
        check(new BigDecimal("10").divide(new BigDecimal("3"), 1, BigDecimal.ROUND_HALF_UP)
                .toString().equals("3.3"), "10/3 scale1 旧整型常量 HALF_UP");
        check(new BigDecimal("2.5").divide(new BigDecimal("1"), RoundingMode.HALF_UP)
                .toString().equals("2.5"), "2.5/1 商保持被除数 scale");
        // 精确除尽
        check(new BigDecimal("1").divide(new BigDecimal("8")).toString().equals("0.125"), "1/8 精确");
        check(new BigDecimal("10").divide(new BigDecimal("4")).toString().equals("2.5"), "10/4 精确");
        checkMsg(ArithmeticException.class,
              "Non-terminating decimal expansion; no exact representable decimal result.",
              () -> new BigDecimal("1").divide(new BigDecimal("3")), "非终止展开消息逐字");
        checkMsg(ArithmeticException.class,
              "Non-terminating decimal expansion; no exact representable decimal result.",
              () -> new BigDecimal("1").divide(new BigDecimal("3"), MathContext.UNLIMITED),
              "UNLIMITED 非终止展开消息逐字");
        checkMsg(ArithmeticException.class, "Division by zero",
              () -> new BigDecimal("1").divide(BigDecimal.ZERO), "divide by zero 消息逐字");
        // divideAndRemainder 商余精确矩阵
        BigDecimal[] dr1 = new BigDecimal("-7").divideAndRemainder(new BigDecimal("2"));
        check(dr1[0].toString().equals("-3") && dr1[1].toString().equals("-1"),
              "-7/2 divideAndRemainder [-3,-1]");
        BigDecimal[] dr2 = new BigDecimal("7.5").divideAndRemainder(new BigDecimal("2"));
        check(dr2[0].toString().equals("3.0") && dr2[1].toString().equals("1.5"),
              "7.5/2 divideAndRemainder [3.0,1.5]");
        check(new BigDecimal("4").remainder(new BigDecimal("3")).toString().equals("1"),
              "4 rem 3 == 1");
        check(new BigDecimal("-4").remainder(new BigDecimal("3")).toString().equals("-1"),
              "-4 rem 3 == -1（符号随被除数）");
        check(new BigDecimal("1.5").remainder(new BigDecimal("2")).toString().equals("1.5"),
              "1.5 rem 2 == 1.5");
        check(new BigDecimal("-7.5").remainder(new BigDecimal("2")).toString().equals("-1.5"),
              "-7.5 rem 2 == -1.5");
        check(new BigDecimal("2").divideToIntegralValue(new BigDecimal("3")).toString().equals("0"),
              "2/3 整商 0");
        check(new BigDecimal("7.5").divideToIntegralValue(new BigDecimal("2")).toString().equals("3.0"),
              "7.5/2 整商 3.0");
    }

    static void checkDivideMode(String dividend, String divisor, String[] expected) {
        RoundingMode[] modes = { RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
                RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
                RoundingMode.HALF_EVEN };
        for (int i = 0; i < modes.length; i++) {
            BigDecimal r = new BigDecimal(dividend).divide(new BigDecimal(divisor), modes[i]);
            check(r.toString().equals(expected[i]),
                    "divide " + dividend + "/" + divisor + " " + modes[i]
                            + " == " + expected[i] + " but was " + r);
        }
    }

    // ===== 契约深化（2026-11）：scale/strip/ulp/movePoint 家族 =====

    static void testScaleStripUlpFamily() {
        BigDecimal z = new BigDecimal("0.00").stripTrailingZeros();
        check(z.toString().equals("0"), "strip(0.00) == \"0\"");
        check(z.scale() == 0, "strip(0.00) scale 0（8u 已修复，两侧一致）");
        check(z.unscaledValue().signum() == 0, "strip(0.00) unscaled 0");
        check(BigDecimal.ZERO.stripTrailingZeros().scale() == 0, "strip(ZERO) scale 0");
        BigDecimal h = new BigDecimal("100").stripTrailingZeros();
        check(h.toString().equals("1E+2"), "strip(100) == \"1E+2\"");
        check(h.scale() == -2, "strip(100) scale -2");
        check(h.unscaledValue().intValue() == 1, "strip(100) unscaled 1");
        check(h.equals(new BigDecimal("1E+2")), "strip(100) equals 1E+2");
        check(new BigDecimal("1.2300").stripTrailingZeros().scale() == 2, "strip(1.2300) scale 2");
        check(new BigDecimal("1.2300").stripTrailingZeros().unscaledValue().intValue() == 123,
              "strip(1.2300) unscaled 123");
        check(new BigDecimal("600.00").stripTrailingZeros().toString().equals("6E+2"),
              "strip(600.00) == \"6E+2\"");
        BigDecimal m = new BigDecimal("1.23");
        check(m.movePointRight(1).toString().equals("12.3"), "1.23 movePointRight(1)");
        check(m.movePointRight(1).scale() == 1, "movePointRight(1) scale 1");
        check(m.movePointLeft(1).toString().equals("0.123"), "1.23 movePointLeft(1)");
        check(m.movePointLeft(1).scale() == 3, "movePointLeft(1) scale 3");
        check(m.movePointRight(-1).toString().equals("0.123"), "movePointRight(-1) 反向");
        check(m.movePointLeft(-1).toString().equals("12.3"), "movePointLeft(-1) 反向");
        check(m.movePointRight(0).equals(m), "movePointRight(0) 恒等");
        check(m.scaleByPowerOfTen(2).toString().equals("123"), "scaleByPowerOfTen(2)");
        check(m.scaleByPowerOfTen(2).scale() == 0, "scaleByPowerOfTen(2) scale 0");
        check(m.scaleByPowerOfTen(-2).toString().equals("0.0123"), "scaleByPowerOfTen(-2)");
        check(m.ulp().toString().equals("0.01"), "ulp(1.23) == 0.01");
        check(new BigDecimal("123").ulp().equals(BigDecimal.ONE), "ulp(123) == 1");
        check(new BigDecimal("1E+3").ulp().toString().equals("1E+3"), "ulp(1E+3) == 1E+3");
        check(new BigDecimal("0.00").ulp().toString().equals("0.01"), "ulp(0.00) == 0.01");
        check(new BigDecimal("-1.0").ulp().toString().equals("0.1"), "ulp(-1.0) == 0.1");
        check(BigDecimal.ZERO.ulp().equals(BigDecimal.ONE), "ulp(ZERO) == 1");
        check(new BigDecimal("0.00").precision() == 1, "precision(0.00) == 1");
        check(new BigDecimal("1E+2").precision() == 1, "precision(1E+2) == 1");
        check(new BigDecimal("0.00100").precision() == 3, "precision(0.00100) == 3");
        check(new BigDecimal("2.00").add(new BigDecimal("3.0")).scale() == 2, "add scale = max");
        check(new BigDecimal("2.00").multiply(new BigDecimal("3.0")).scale() == 3, "multiply scale 相加");
        check(new BigDecimal("1.5").subtract(new BigDecimal("1.5")).toString().equals("0.0"),
              "1.5-1.5 == 0.0 保持 scale");
        check(new BigDecimal("1.0").subtract(new BigDecimal("0.9")).toString().equals("0.1"),
              "1.0-0.9 == 0.1（十进制精确）");
    }

    // ===== 契约深化（2026-11）：toString/toPlainString/toEngineeringString 分叉 =====

    static void testStringForms() {
        BigDecimal e2 = new BigDecimal("1E+2");
        check(e2.toString().equals("1E+2"), "1E+2 toString 科学计数形态");
        check(e2.toPlainString().equals("100"), "1E+2 toPlainString 展开");
        check(e2.toEngineeringString().equals("100"), "1E+2 toEngineeringString 指数归 0");
        check(new BigDecimal("1E+3").toEngineeringString().equals("1E+3"), "1E+3 工程计数保持（3 的倍数）");
        BigDecimal tiny = new BigDecimal("0.0000001");
        check(tiny.toString().equals("1E-7"), "0.0000001 toString == 1E-7");
        check(tiny.toPlainString().equals("0.0000001"), "0.0000001 toPlainString");
        check(tiny.toEngineeringString().equals("100E-9"), "0.0000001 工程计数 100E-9");
        check(new BigDecimal("1E-10").toString().equals("1E-10"), "1E-10 toString");
        check(new BigDecimal("1E-5").toEngineeringString().equals("0.00001"), "1E-5 工程计数 0.00001");
        check(new BigDecimal("0.000000").toString().equals("0.000000"), "scale 6 零普通形态");
        check(new BigDecimal("12340000").toString().equals("12340000"), "大整数普通形态");
        check(new BigDecimal("12345.6789").toEngineeringString().equals("12345.6789"),
              "12345.6789 工程计数保持");
        check(new BigDecimal("1.23E+3").toString().equals("1.23E+3"), "1.23E+3 toString 保持");
        check(new BigDecimal("-0.0").toString().equals("0.0"), "-0.0 无负零字符串");
        check(new BigDecimal("0").movePointRight(2).toString().equals("0"), "0 movePointRight(2) == 0");
    }

    // ===== 契约深化（2026-11）：MathContext 联动（round/常量/UNLIMITED） =====

    static void testMathContextRounding() {
        check(MathContext.DECIMAL32.getPrecision() == 7, "DECIMAL32 precision 7");
        check(MathContext.DECIMAL32.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL32 HALF_EVEN");
        check(MathContext.DECIMAL64.getPrecision() == 16, "DECIMAL64 precision 16");
        check(MathContext.DECIMAL128.getPrecision() == 34, "DECIMAL128 precision 34");
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED precision 0");
        check(MathContext.UNLIMITED.getRoundingMode() == RoundingMode.HALF_UP, "UNLIMITED HALF_UP");
        check(new BigDecimal("123456789").round(MathContext.DECIMAL32).toString().equals("1.234568E+8"),
              "round DECIMAL32 实测锁定");
        check(new BigDecimal("123456789012345678").round(MathContext.DECIMAL64).toString()
                .equals("1.234567890123457E+17"), "round DECIMAL64 实测锁定");
        check(new BigDecimal("12345678901234567890123456789012345")
                .round(MathContext.DECIMAL128).toString()
                .equals("1.234567890123456789012345678901234E+34"), "round DECIMAL128 实测锁定");
        check(new BigDecimal("123456").round(MathContext.UNLIMITED).toString().equals("123456"),
              "round UNLIMITED 恒等");
        check(new BigDecimal("1.5").round(MathContext.UNLIMITED).toString().equals("1.5"),
              "round UNLIMITED 小数恒等");
        check(new BigDecimal("123456789").add(BigDecimal.ONE, MathContext.DECIMAL32).toString()
                .equals("1.234568E+8"), "add DECIMAL32 舍入");
        check(new BigDecimal("-123456789").negate(MathContext.DECIMAL32).toString().equals("1.234568E+8"),
              "negate MC 舍入");
        check(new BigDecimal("2").pow(10, MathContext.DECIMAL32).toString().equals("1024"),
              "pow MC 精确");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("123456").round(new MathContext(2, RoundingMode.UNNECESSARY)),
              "round UNNECESSARY 非精确消息逐字");
        check(new BigDecimal("120000").round(new MathContext(2, RoundingMode.UNNECESSARY))
                .toString().equals("1.2E+5"), "round UNNECESSARY 精确 OK");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("123000").round(new MathContext(2, RoundingMode.UNNECESSARY)),
              "round UNNECESSARY 需舍位时抛");
    }

    // ===== 契约深化（2026-11）：精确转换消息与截断/回绕语义 =====

    static void testExactConversionMessages() {
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("3.7").intValueExact(), "intValueExact 小数消息逐字");
        checkMsg(ArithmeticException.class, "Rounding necessary",
              () -> new BigDecimal("12.5").shortValueExact(), "shortValueExact 小数消息");
        checkMsg(ArithmeticException.class, "Overflow",
              () -> new BigDecimal("1E+20").intValueExact(), "intValueExact 溢出消息逐字");
        checkMsg(ArithmeticException.class, "Overflow",
              () -> new BigDecimal("1E+30").longValueExact(), "longValueExact 溢出消息逐字");
        checkMsg(ArithmeticException.class, "Overflow",
              () -> new BigDecimal("128").byteValueExact(), "byteValueExact 溢出消息");
        checkMsg(ArithmeticException.class, "Overflow",
              () -> new BigDecimal("32768").shortValueExact(), "shortValueExact 溢出消息");
        checkMsg(ArithmeticException.class, "Invalid operation",
              () -> new BigDecimal("10").pow(-1), "pow 负指数消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger would overflow supported range",
              () -> new BigDecimal("1").movePointRight(Integer.MAX_VALUE),
              "movePointRight 溢出消息逐字");
        check(new BigDecimal("123.9").intValue() == 123, "intValue 截断");
        check(new BigDecimal("123.9").longValue() == 123L, "longValue 截断");
        check(new BigDecimal("1E+10").intValue() == 1410065408, "intValue 回绕 2^31 域");
        check(new BigDecimal("1E+400").longValue() == 0L, "longValue 回绕（10^400 低 64 位为 0）");
        check(new BigDecimal("2.5").toBigInteger().intValue() == 2, "toBigInteger 向零截断");
        check(new BigDecimal("-2.5").toBigInteger().intValue() == -2, "toBigInteger 负数向零截断");
        check(new BigDecimal("1E+400").doubleValue() == Double.POSITIVE_INFINITY, "doubleValue 溢出 Infinity");
        check(new BigDecimal("1E+400").floatValue() == Float.POSITIVE_INFINITY, "floatValue 溢出 Infinity");
        check(new BigDecimal("-1E+400").doubleValue() == Double.NEGATIVE_INFINITY, "doubleValue 负溢出");
        check(new BigDecimal("2.5").floatValue() == 2.5f, "floatValue 精确");
    }

    // ===== 契约深化（2026-11）：序列化与不可变性补充 =====

    static void testSerializationExtra() throws Exception {
        BigDecimal[] vals = { new BigDecimal("1E-7"), new BigDecimal("1E+400"),
                new BigDecimal("0.000"), new BigDecimal("-0.0"), new BigDecimal("1230") };
        for (BigDecimal v : vals) {
            BigDecimal r = roundTrip(v);
            check(r.equals(v), "序列化 round-trip 值 " + v);
            check(r.scale() == v.scale(), "序列化保留 scale " + v);
            check(r.unscaledValue().equals(v.unscaledValue()), "序列化保留 unscaled " + v);
            check(r.precision() == v.precision(), "序列化保留 precision " + v);
            check(r.hashCode() == v.hashCode(), "序列化保留 hashCode " + v);
        }
        check(new BigDecimal("2").pow(10).toString().equals("1024"), "2^10 == 1024 字符串");
        check(new BigDecimal("5").pow(1).toString().equals("5"), "pow(1) 自身");
        check(new BigDecimal("7").pow(0).equals(BigDecimal.ONE), "pow(0) == 1");
        BigDecimal neg = new BigDecimal("-1.5");
        check(neg.abs().equals(new BigDecimal("1.5")), "abs");
        check(neg.negate().equals(new BigDecimal("1.5")), "negate");
        check(new BigDecimal("-0.0").abs().toString().equals("0.0"), "abs(-0.0) == 0.0");
        check(new BigDecimal("-0.0").negate().toString().equals("0.0"), "negate(-0.0) == 0.0");
        check(new BigDecimal("-0.0").plus().toString().equals("0.0"), "plus(-0.0) == 0.0");
        check(new BigDecimal("0.0").negate().signum() == 0, "negate(0.0) 无负零");
        // 不可变性补充
        BigDecimal v = new BigDecimal("1.2300");
        v.setScale(3, RoundingMode.HALF_UP);
        v.stripTrailingZeros();
        v.movePointLeft(1);
        v.movePointRight(1);
        v.scaleByPowerOfTen(2);
        v.round(MathContext.DECIMAL32);
        v.ulp();
        check(v.equals(new BigDecimal("1.2300")), "setScale/strip/movePoint/round 均不改自身");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testNullSemantics();
        testArithmetic();
        testDivide();
        testPow();
        testScalePrecision();
        testRoundingMatrix();
        testAbsNegateSignum();
        testComparison();
        testConversions();
        testConversionsExactErrors();
        testSerialization();
        testErrors();
        testDoubleConstructorPrecision();
        testStringGrammar();
        testEqualsComparetoFork();
        testSetScaleEightModeTable();
        testDivideModeMatrix();
        testScaleStripUlpFamily();
        testStringForms();
        testMathContextRounding();
        testExactConversionMessages();
        testSerializationExtra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

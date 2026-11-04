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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

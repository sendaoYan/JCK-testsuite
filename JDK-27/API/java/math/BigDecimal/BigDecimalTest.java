/*
 * @test
 * @summary java.math.BigDecimal conformance: white-box structure + black-box behavior
 *          (construction, exact decimal arithmetic, scale/precision/unscaledValue,
 *           rounding modes via MathContext/setScale/divide, movePoint/scaleByPowerOfTen,
 *           stripTrailingZeros, compareTo vs equals, conversions, ArithmeticException).
 * @run main BigDecimalTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public class BigDecimalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " but got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.math.BigDecimal");

        check(!c.isInterface() && !c.isEnum(), "BigDecimal is a concrete class");
        check(Modifier.isPublic(c.getModifiers()), "BigDecimal is public");
        check(!Modifier.isAbstract(c.getModifiers()), "BigDecimal is not abstract");
        check(c.getSuperclass() == Number.class, "BigDecimal extends Number");
        check(Arrays.asList(c.getInterfaces()).contains(Comparable.class),
              "BigDecimal implements Comparable");

        for (String f : new String[]{"ZERO", "ONE", "TWO", "TEN"}) {
            Field field = c.getField(f);
            check(Modifier.isPublic(field.getModifiers())
                  && Modifier.isStatic(field.getModifiers())
                  && Modifier.isFinal(field.getModifiers())
                  && field.getType() == BigDecimal.class,
                  "static final BigDecimal " + f);
        }
        check(((BigDecimal) c.getField("ZERO").get(null)).signum() == 0, "ZERO has value 0");

        int[] roundValues = {0, 1, 2, 3, 4, 5, 6, 7};
        String[] roundNames = {"ROUND_UP", "ROUND_DOWN", "ROUND_CEILING", "ROUND_FLOOR",
                               "ROUND_HALF_UP", "ROUND_HALF_DOWN", "ROUND_HALF_EVEN",
                               "ROUND_UNNECESSARY"};
        for (int i = 0; i < roundNames.length; i++) {
            Field f = c.getField(roundNames[i]);
            check(f.getType() == int.class && f.getInt(null) == roundValues[i],
                  "ROUND_ constant " + roundNames[i]);
        }

        String[][] methods = {
            {"valueOf", "long"},
            {"valueOf", "long", "int"},
            {"valueOf", "double"},
            {"add", "java.math.BigDecimal"},
            {"subtract", "java.math.BigDecimal"},
            {"multiply", "java.math.BigDecimal"},
            {"divide", "java.math.BigDecimal"},
            {"divide", "java.math.BigDecimal", "java.math.MathContext"},
            {"divide", "java.math.BigDecimal", "int", "java.math.RoundingMode"},
            {"divideToIntegralValue", "java.math.BigDecimal"},
            {"remainder", "java.math.BigDecimal"},
            {"divideAndRemainder", "java.math.BigDecimal"},
            {"sqrt", "java.math.MathContext"},
            {"rootn", "int", "java.math.MathContext"},
            {"pow", "int"},
            {"pow", "int", "java.math.MathContext"},
            {"abs"},
            {"negate"},
            {"plus"},
            {"signum"},
            {"scale"},
            {"precision"},
            {"unscaledValue"},
            {"round", "java.math.MathContext"},
            {"setScale", "int", "java.math.RoundingMode"},
            {"setScale", "int"},
            {"movePointLeft", "int"},
            {"movePointRight", "int"},
            {"scaleByPowerOfTen", "int"},
            {"stripTrailingZeros"},
            {"compareTo", "java.math.BigDecimal"},
            {"equals", "java.lang.Object"},
            {"hashCode"},
            {"toString"},
            {"toEngineeringString"},
            {"toPlainString"},
            {"toBigInteger"},
            {"toBigIntegerExact"},
            {"ulp"},
        };
        for (String[] m : methods) {
            Class<?>[] params = new Class<?>[m.length - 1];
            for (int i = 1; i < m.length; i++) params[i - 1] = bdForName(m[i]);
            Method method = c.getMethod(m[0], params);
            check(Modifier.isPublic(method.getModifiers()), "public method " + m[0]);
        }
    }

    static Class<?> bdForName(String s) throws Exception {
        switch (s) {
            case "long": return long.class;
            case "int": return int.class;
            case "double": return double.class;
            default: return Class.forName(s);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        construction();
        arithmetic();
        scalePrecision();
        rounding();
        comparisonEquals();
        conversions();
        boundariesAndExceptions();
        roundingModeEnumeration();
        boundaryConversions();
        testSerialization();
        testNullSemantics();
    }

    static void construction() {
        check(new BigDecimal("10.00").toString().equals("10.00"), "String ctor preserves scale");
        check(new BigDecimal("-12.34").toString().equals("-12.34"), "String ctor negative");
        check(new BigDecimal("1E+10").toString().equals("1E+10"), "String ctor exponent form");
        check(new BigDecimal("1E+10").scale() == -10, "exponent form negative scale");
        check(new BigDecimal("1E+10").precision() == 1, "exponent form precision 1");
        check(new BigDecimal("0").signum() == 0, "String ctor zero");
        // valueOf(double) uses canonical decimal; new BigDecimal(double) uses exact binary
        check(BigDecimal.valueOf(0.1).toString().equals("0.1"), "valueOf(double) canonical");
        check(new BigDecimal(0.1).toPlainString().equals(
              "0.1000000000000000055511151231257827021181583404541015625"),
              "new BigDecimal(double) exact binary expansion");
        check(BigDecimal.valueOf(2.5).toString().equals("2.5"), "valueOf(double) 2.5");
        // valueOf(long[, scale])
        check(BigDecimal.valueOf(123L).toString().equals("123"), "valueOf(long)");
        check(BigDecimal.valueOf(123L, 2).toString().equals("1.23"), "valueOf(long, scale)");
        check(BigDecimal.valueOf(0L, 2).toString().equals("0.00"), "valueOf(0, scale) keeps scale");
        // BigInteger constructors
        check(new BigDecimal(BigInteger.valueOf(123), 2).toString().equals("1.23"),
              "BigDecimal(BigInteger, scale)");
        check(new BigDecimal(BigInteger.valueOf(123)).toString().equals("123"), "BigDecimal(BigInteger)");
        // int / long constructors
        check(new BigDecimal(42).toString().equals("42"), "BigDecimal(int)");
        check(new BigDecimal(42L).toString().equals("42"), "BigDecimal(long)");
        // constants
        check(BigDecimal.ONE.toString().equals("1"), "ONE constant");
        check(BigDecimal.TEN.toString().equals("10"), "TEN constant");
        check(BigDecimal.ZERO.toString().equals("0"), "ZERO constant");
    }

    static void arithmetic() {
        BigDecimal a = new BigDecimal("10.00");
        BigDecimal b = new BigDecimal("3.0");
        check(a.add(b).equals(new BigDecimal("13.00")), "add (scale = max)");
        check(new BigDecimal("1.5").add(new BigDecimal("2.0")).toString().equals("3.5"), "add 1.5+2.0");
        check(new BigDecimal("0.1").add(new BigDecimal("0.2")).toString().equals("0.3"), "add 0.1+0.2 exact");
        check(a.subtract(b).equals(new BigDecimal("7.00")), "subtract");
        check(new BigDecimal("2.0").subtract(new BigDecimal("1.5")).toString().equals("0.5"), "subtract");
        check(a.multiply(b).equals(new BigDecimal("30.000")), "multiply (scales add)");
        check(new BigDecimal("1.5").multiply(new BigDecimal("2.0")).toString().equals("3.00"), "multiply");

        // division variants
        check(a.divide(b, 2, RoundingMode.HALF_UP).equals(new BigDecimal("3.33")), "divide(scale, mode)");
        check(new BigDecimal("10").divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP)
              .equals(new BigDecimal("3.33")), "divide 10/3 HALF_UP");
        check(BigDecimal.ONE.divide(BigDecimal.TWO).equals(new BigDecimal("0.5")), "divide exact terminating");
        check(BigDecimal.ONE.divide(new BigDecimal("3"), new MathContext(5, RoundingMode.HALF_UP))
              .equals(new BigDecimal("0.33333")), "divide with MathContext");

        // divideToIntegralValue / remainder / divideAndRemainder
        check(new BigDecimal("10").divideToIntegralValue(new BigDecimal("3")).equals(new BigDecimal("3")),
              "divideToIntegralValue 10/3");
        check(new BigDecimal("-10").divideToIntegralValue(new BigDecimal("3")).equals(new BigDecimal("-3")),
              "divideToIntegralValue -10/3");
        check(new BigDecimal("10.0").remainder(new BigDecimal("3")).equals(new BigDecimal("1.0")),
              "remainder 10.0/3");
        BigDecimal[] dr = new BigDecimal("10").divideAndRemainder(new BigDecimal("3"));
        check(dr.length == 2 && dr[0].equals(new BigDecimal("3")) && dr[1].equals(new BigDecimal("1")),
              "divideAndRemainder 10/3");

        // sqrt / rootn / pow
        check(new BigDecimal("25").sqrt(new MathContext(4)).equals(new BigDecimal("5")), "sqrt exact");
        check(new BigDecimal("2").sqrt(new MathContext(4)).equals(new BigDecimal("1.414")), "sqrt 2 mc4");
        check(new BigDecimal("27").rootn(3, new MathContext(10)).equals(new BigDecimal("3")), "rootn 27,3");
        check(new BigDecimal("-27").rootn(3, new MathContext(10)).equals(new BigDecimal("-3")),
              "rootn odd root of negative -27,3");
        check(new BigDecimal("1.5").pow(3).equals(new BigDecimal("3.375")), "pow 1.5^3");
        check(new BigDecimal("1.5").pow(3, new MathContext(4)).equals(new BigDecimal("3.375")), "pow with mc");
        check(new BigDecimal("5").pow(-2, new MathContext(4)).equals(new BigDecimal("0.04")), "pow negative with mc");

        // abs / negate / plus / signum
        check(new BigDecimal("-1.5").abs().equals(new BigDecimal("1.5")), "abs");
        check(new BigDecimal("1.5").negate().equals(new BigDecimal("-1.5")), "negate");
        check(new BigDecimal("1.5").plus().equals(new BigDecimal("1.5")), "plus");
        check(a.signum() == 1 && BigDecimal.ZERO.signum() == 0 && new BigDecimal("-1").signum() == -1,
              "signum");
    }

    static void scalePrecision() {
        check(new BigDecimal("12.34").scale() == 2, "scale 12.34");
        check(new BigDecimal("12.34").precision() == 4, "precision 12.34");
        check(new BigDecimal("12.34").unscaledValue().equals(BigInteger.valueOf(1234)), "unscaledValue");
        check(new BigDecimal("0.00").scale() == 2, "scale 0.00");
        check(new BigDecimal("0.00").precision() == 1, "precision 0.00 == 1");
        check(new BigDecimal("0.00").unscaledValue().signum() == 0, "unscaledValue zero");

        // setScale
        check(new BigDecimal("1.5").setScale(2).equals(new BigDecimal("1.50")), "setScale pad zeros");
        check(new BigDecimal("1.50").setScale(1).equals(new BigDecimal("1.5")), "setScale reduce");
        check(new BigDecimal("1.55").setScale(1, RoundingMode.HALF_UP).equals(new BigDecimal("1.6")),
              "setScale HALF_UP");

        // movePointLeft / movePointRight / scaleByPowerOfTen
        check(new BigDecimal("2.5").movePointLeft(1).equals(new BigDecimal("0.25")), "movePointLeft");
        check(new BigDecimal("2.5").movePointRight(1).equals(new BigDecimal("25")), "movePointRight");
        check(new BigDecimal("2.5").scaleByPowerOfTen(-2).equals(new BigDecimal("0.025")), "scaleByPowerOfTen");

        // stripTrailingZeros
        check(new BigDecimal("10.00").stripTrailingZeros().equals(new BigDecimal("1E+1")),
              "stripTrailingZeros 10.00");
        check(new BigDecimal("600.0").stripTrailingZeros().equals(new BigDecimal("6E+2")),
              "stripTrailingZeros 600.0");
        check(new BigDecimal("0.00").stripTrailingZeros().toString().equals("0"),
              "stripTrailingZeros 0.00 == 0");
        check(BigDecimal.ZERO.stripTrailingZeros().toString().equals("0"), "stripTrailingZeros ZERO");

        // ulp
        check(BigDecimal.ONE.ulp().equals(BigDecimal.ONE), "ulp of 1 == 1");
        check(new BigDecimal("0.00").ulp().equals(new BigDecimal("0.01")), "ulp of 0.00");
        check(new BigDecimal("2.50").ulp().equals(new BigDecimal("0.01")), "ulp of 2.50");
        check(new BigDecimal("1234").ulp().equals(BigDecimal.ONE), "ulp of integer 1234");
    }

    static void rounding() {
        // RoundingMode semantics via BigDecimal.round(MathContext)
        MathContext mc = new MathContext(1);
        check(new BigDecimal("5.5").round(new MathContext(1, RoundingMode.HALF_UP)).equals(new BigDecimal("6")),
              "HALF_UP 5.5 -> 6");
        check(new BigDecimal("2.5").round(new MathContext(1, RoundingMode.HALF_UP)).equals(new BigDecimal("3")),
              "HALF_UP 2.5 -> 3");
        check(new BigDecimal("2.5").round(new MathContext(1, RoundingMode.HALF_EVEN)).equals(new BigDecimal("2")),
              "HALF_EVEN 2.5 -> 2");
        check(new BigDecimal("-2.5").round(new MathContext(1, RoundingMode.HALF_EVEN)).equals(new BigDecimal("-2")),
              "HALF_EVEN -2.5 -> -2");
        check(new BigDecimal("1.1").round(new MathContext(1, RoundingMode.CEILING)).equals(new BigDecimal("2")),
              "CEILING 1.1 -> 2");
        check(new BigDecimal("-1.1").round(new MathContext(1, RoundingMode.FLOOR)).equals(new BigDecimal("-2")),
              "FLOOR -1.1 -> -2");
        check(new BigDecimal("1.6").round(new MathContext(1, RoundingMode.DOWN)).equals(new BigDecimal("1")),
              "DOWN 1.6 -> 1");
        check(new BigDecimal("-1.6").round(new MathContext(1, RoundingMode.UP)).equals(new BigDecimal("-2")),
              "UP -1.6 -> -2");
        check(new BigDecimal("1.0").round(new MathContext(1, RoundingMode.UNNECESSARY)).equals(new BigDecimal("1")),
              "UNNECESSARY exact value");
    }

    static void comparisonEquals() {
        BigDecimal v1 = new BigDecimal("2.0");
        BigDecimal v2 = new BigDecimal("2.00");
        check(v1.compareTo(v2) == 0, "compareTo ignores scale");
        check(!v1.equals(v2), "equals considers scale (2.0 != 2.00)");
        check(v1.equals(new BigDecimal("2.0")), "equals same value and scale");
        check(v1.hashCode() == new BigDecimal("2.0").hashCode(), "equal objects equal hashCode");
        check(new BigDecimal("2.0").min(new BigDecimal("2.00")).equals(new BigDecimal("2.0")),
              "min returns this when equal");
        check(new BigDecimal("2.0").max(new BigDecimal("2.00")).equals(new BigDecimal("2.0")),
              "max returns this when equal");
        check(new BigDecimal("1.5").min(new BigDecimal("2.0")).equals(new BigDecimal("1.5")), "min");
        check(new BigDecimal("1.5").max(new BigDecimal("2.0")).equals(new BigDecimal("2.0")), "max");
        check(new BigDecimal("10.00").compareTo(new BigDecimal("3.0")) > 0, "compareTo positive");
        check(new BigDecimal("3.0").compareTo(new BigDecimal("10.00")) < 0, "compareTo negative");
    }

    static void conversions() {
        check(new BigDecimal("12.34").toBigInteger().equals(BigInteger.valueOf(12)), "toBigInteger truncates");
        check(new BigDecimal("12.00").toBigIntegerExact().equals(BigInteger.valueOf(12)), "toBigIntegerExact");
        check(new BigDecimal("2.5").longValue() == 2L, "longValue truncates");
        check(new BigDecimal("2.5").intValue() == 2, "intValue truncates");
        check(new BigDecimal("2.5").doubleValue() == 2.5, "doubleValue");
        check(new BigDecimal("2.5").floatValue() == 2.5f, "floatValue");
        check(new BigDecimal("12.34").toPlainString().equals("12.34"), "toPlainString");
        check(new BigDecimal("1E+10").toPlainString().equals("10000000000"), "toPlainString expands exponent");
        check(new BigDecimal("1.234E+5").toEngineeringString().equals("123.4E+3"),
              "toEngineeringString groups exponent by 3");
        check(new BigDecimal("-1.234E-5").toEngineeringString().equals("-0.00001234"),
              "toEngineeringString small value");
        check(new BigDecimal("-12.34").toEngineeringString().equals("-12.34"), "toEngineeringString plain");
        check(new BigDecimal("12340000").toString().equals("12340000"), "toString integer");
    }

    static void boundariesAndExceptions() {
        // exact division of non-terminating value throws ArithmeticException
        expectArithmetic(() -> BigDecimal.ONE.divide(new BigDecimal("3")), "non-terminating exact divide");
        // divide by zero
        expectArithmetic(() -> BigDecimal.ONE.divide(BigDecimal.ZERO), "divide by zero");
        // sqrt / rootn of negative (even root of negative throws; odd root is allowed)
        expectArithmetic(() -> new BigDecimal("-4").sqrt(new MathContext(4)), "sqrt negative");
        expectArithmetic(() -> new BigDecimal("-4").rootn(2, new MathContext(4)), "rootn even root of negative");
        expectArithmetic(() -> new BigDecimal("-27").rootn(0, new MathContext(4)), "rootn n == 0");
        // pow(int) with negative exponent (requires MathContext)
        expectArithmetic(() -> new BigDecimal("5").pow(-2), "pow(int) negative exponent");
        // UNNECESSARY rounding when inexact
        expectArithmetic(() -> new BigDecimal("1.1").round(new MathContext(1, RoundingMode.UNNECESSARY)),
              "round UNNECESSARY inexact");
        // exact integer conversions when fractional part present
        expectArithmetic(() -> new BigDecimal("12.34").toBigIntegerExact(), "toBigIntegerExact fractional");
        expectArithmetic(() -> new BigDecimal("1.9").intValueExact(), "intValueExact fractional");
        expectArithmetic(() -> new BigDecimal("1.9").longValueExact(), "longValueExact fractional");
        expectArithmetic(() -> new BigDecimal("1.9").shortValueExact(), "shortValueExact fractional");
        expectArithmetic(() -> new BigDecimal("1.9").byteValueExact(), "byteValueExact fractional");
        // invalid String constructor
        expectNumberFormat(() -> new BigDecimal("1.2.3"), "invalid String ctor");
        // exact arithmetic for very large values
        BigDecimal big = new BigDecimal("123456789012345678901234567890.123456789");
        check(big.add(BigDecimal.ZERO).equals(big), "large add identity");
        check(big.multiply(BigDecimal.ONE).equals(big), "large multiply identity");
        check(big.subtract(big).signum() == 0, "large subtract itself == 0");
    }

    static void expectArithmetic(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected ArithmeticException: " + msg);
        } catch (ArithmeticException expected) {
            passed++;
        }
    }

    static void expectNumberFormat(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected NumberFormatException: " + msg);
        } catch (NumberFormatException expected) {
            passed++;
        }
    }

    static void roundingModeEnumeration() {
        // all 8 RoundingMode values applied to the tie 2.5 and -2.5 at precision 1
        String[] pos = {"3", "2", "3", "2", "3", "2", "2", null};
        String[] neg = {"-3", "-2", "-2", "-3", "-3", "-2", "-2", null};
        RoundingMode[] modes = RoundingMode.values();
        check(modes.length == 8, "8 rounding modes");
        for (int i = 0; i < 8; i++) {
            RoundingMode m = modes[i];
            MathContext mc = new MathContext(1, m);
            if (m == RoundingMode.UNNECESSARY) {
                expectArithmetic(() -> new BigDecimal("2.5").round(mc), "UNNECESSARY tie throws");
                expectArithmetic(() -> new BigDecimal("-2.5").round(mc), "UNNECESSARY tie throws (neg)");
            } else {
                check(new BigDecimal("2.5").round(mc).toString().equals(pos[i]),
                      "round 2.5 mode " + m);
                check(new BigDecimal("-2.5").round(mc).toString().equals(neg[i]),
                      "round -2.5 mode " + m);
            }
        }
        check(new BigDecimal("2.5").setScale(0, RoundingMode.HALF_DOWN).equals(new BigDecimal("2")),
              "HALF_DOWN 2.5 -> 2");
        check(new BigDecimal("2.6").setScale(0, RoundingMode.HALF_DOWN).equals(new BigDecimal("3")),
              "HALF_DOWN 2.6 -> 3");
        check(new BigDecimal("-2.5").setScale(0, RoundingMode.HALF_DOWN).equals(new BigDecimal("-2")),
              "HALF_DOWN -2.5 -> -2");
        expectArithmetic(() -> new BigDecimal("1.55").setScale(1, RoundingMode.UNNECESSARY),
              "setScale UNNECESSARY inexact");
        check(new BigDecimal("1.50").setScale(1, RoundingMode.UNNECESSARY).equals(new BigDecimal("1.5")),
              "setScale UNNECESSARY exact");
    }

    static void boundaryConversions() {
        expectNumberFormat(() -> BigDecimal.valueOf(Double.NaN), "valueOf(NaN)");
        expectNumberFormat(() -> BigDecimal.valueOf(Double.POSITIVE_INFINITY), "valueOf(+Inf)");
        expectNumberFormat(() -> new BigDecimal(Double.NaN), "new BigDecimal(double NaN)");
        expectNumberFormat(() -> new BigDecimal(Double.NEGATIVE_INFINITY), "new BigDecimal(double -Inf)");
        check(BigDecimal.valueOf(-0.0).signum() == 0 && BigDecimal.valueOf(-0.0).toString().equals("0.0"),
              "valueOf(-0.0) normalized to 0.0");
        check(BigDecimal.valueOf(Long.MAX_VALUE).longValue() == Long.MAX_VALUE, "valueOf(Long.MAX)");
        check(BigDecimal.valueOf(Long.MIN_VALUE).longValue() == Long.MIN_VALUE, "valueOf(Long.MIN)");
        check(new BigDecimal("4294967296").intValue() == 0, "intValue wraps 2^32 -> 0");
        check(new BigDecimal("18446744073709551616").longValue() == 0, "longValue wraps 2^64 -> 0");
        check(new BigDecimal("2147483647").intValueExact() == Integer.MAX_VALUE, "intValueExact MAX");
        check(new BigDecimal("-2147483648").intValueExact() == Integer.MIN_VALUE, "intValueExact MIN");
        check(new BigDecimal("9223372036854775807").longValueExact() == Long.MAX_VALUE, "longValueExact MAX");
        check(new BigDecimal("-9223372036854775808").longValueExact() == Long.MIN_VALUE, "longValueExact MIN");
        check(new BigDecimal("127").byteValueExact() == 127, "byteValueExact 127");
        expectArithmetic(() -> new BigDecimal("128").byteValueExact(), "byteValueExact 128 overflow");
        expectArithmetic(() -> new BigDecimal("32768").shortValueExact(), "shortValueExact 32768 overflow");
        check(new BigDecimal("5").pow(0).equals(BigDecimal.ONE), "pow(0) == 1");
        check(new BigDecimal("5").pow(1).equals(new BigDecimal("5")), "pow(1) == self");
        check(BigDecimal.ZERO.ulp().equals(BigDecimal.ONE), "ZERO ulp == 1");
        check(new BigDecimal("-1.0").ulp().equals(new BigDecimal("0.1")), "ulp of -1.0 == 0.1");
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

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> new BigDecimal((String) null), "String null");
        expectThrows(NullPointerException.class, () -> new BigDecimal((BigInteger) null), "BigInteger null");
        expectThrows(NullPointerException.class, () -> new BigDecimal((char[]) null), "char[] null");
        expectThrows(NullPointerException.class, () -> new BigDecimal((char[]) null, 0, 1), "char[] off null");
        expectThrows(NullPointerException.class, () -> new BigDecimal("1", (MathContext) null), "String MC null");
        expectThrows(NullPointerException.class, () -> new BigDecimal(BigInteger.ONE, (MathContext) null), "BigInteger MC null");
        expectThrows(NullPointerException.class, () -> new BigDecimal(1, (MathContext) null), "int MC null");
        expectThrows(NullPointerException.class, () -> new BigDecimal(1L, (MathContext) null), "long MC null");
        expectThrows(NullPointerException.class, () -> new BigDecimal(1.0, (MathContext) null), "double MC null");
        BigDecimal one = BigDecimal.ONE;
        expectThrows(NullPointerException.class, () -> one.add(null), "add null");
        expectThrows(NullPointerException.class, () -> one.add(BigDecimal.ONE, (MathContext) null), "add MC null");
        expectThrows(NullPointerException.class, () -> one.subtract(null), "subtract null");
        expectThrows(NullPointerException.class, () -> one.multiply(null), "multiply null");
        expectThrows(NullPointerException.class, () -> one.divide(null), "divide null");
        expectThrows(NullPointerException.class, () -> one.divide(null, 2, RoundingMode.HALF_UP), "divide scale null divisor");
        expectThrows(NullPointerException.class, () -> one.divide(BigDecimal.ONE, (RoundingMode) null), "divide RoundingMode null");
        expectThrows(NullPointerException.class, () -> one.remainder(null), "remainder null");
        expectThrows(NullPointerException.class, () -> one.divideAndRemainder(null), "divideAndRemainder null");
        expectThrows(NullPointerException.class, () -> one.pow(2, (MathContext) null), "pow MC null");
        expectThrows(NullPointerException.class, () -> one.setScale(0, (RoundingMode) null), "setScale mode null");
        expectThrows(NullPointerException.class, () -> one.round((MathContext) null), "round null");
        expectThrows(NullPointerException.class, () -> one.abs((MathContext) null), "abs MC null");
        expectThrows(NullPointerException.class, () -> one.negate((MathContext) null), "negate MC null");
        expectThrows(NullPointerException.class, () -> one.plus((MathContext) null), "plus MC null");
        expectThrows(NullPointerException.class, () -> one.compareTo(null), "compareTo null");
        expectThrows(NullPointerException.class, () -> one.min(null), "min null");
        expectThrows(NullPointerException.class, () -> one.max(null), "max null");
    }

    // ===== 契约深化（2026-11）：精确异常消息助手 =====

    static void checkMsg(Class<? extends Throwable> expected, String message, Runnable r, String msg) {
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

    // 个别非法串（如 ""、"1e"）在 JDK-27 仍无消息文本
    static void checkNullMsg(Class<? extends Throwable> expected, Runnable r, String msg) {
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
        check(BigDecimal.TWO.toString().equals("2"), "TWO 常量（JDK-19+）");
        check(BigDecimal.TWO.scale() == 0, "TWO scale 0");
        check(BigDecimal.TWO.equals(new BigDecimal("2")), "TWO equals 2");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> new BigDecimal(Double.NaN),
              "new BigDecimal(NaN) 消息逐字");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> new BigDecimal(Double.POSITIVE_INFINITY),
              "new BigDecimal(+Inf) 消息逐字");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> new BigDecimal(Double.NEGATIVE_INFINITY),
              "new BigDecimal(-Inf) 消息逐字");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> BigDecimal.valueOf(Double.NaN),
              "valueOf(NaN) 消息逐字（JDK-19+ 有文本）");
        checkMsg(NumberFormatException.class, "Infinite or NaN", () -> BigDecimal.valueOf(Double.POSITIVE_INFINITY),
              "valueOf(+Inf) 消息逐字");
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
        // JDK-27：非法串 NFE 带详细诊断文本（JDK-8 为 null——版本差异）
        checkMsg(NumberFormatException.class, "Character array contains more than one decimal point.",
              () -> new BigDecimal("1.2.3"), "双小数点消息逐字");
        checkMsg(NumberFormatException.class,
              "Character I is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.",
              () -> new BigDecimal("Infinity"), "Infinity 串消息逐字");
        checkMsg(NumberFormatException.class,
              "Character - is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.",
              () -> new BigDecimal("--1"), "双负号消息逐字");
        checkMsg(NumberFormatException.class, "No digits found.",
              () -> new BigDecimal("-"), "孤立负号消息逐字");
        checkMsg(NumberFormatException.class, "Not a digit.",
              () -> new BigDecimal("1E+3x"), "指数尾随垃圾消息逐字");
        checkMsg(NumberFormatException.class,
              "Character x is neither a decimal digit number, decimal point, nor \"e\" notation exponential mark.",
              () -> new BigDecimal(new char[]{'1','.','x'}, 0, 3), "char[] 坏字符消息逐字");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal(""), "空串 NFE 无消息（两侧一致）");
        checkNullMsg(NumberFormatException.class, () -> new BigDecimal("1e"), "裸指数 NFE 无消息（两侧一致）");
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
        check(BigDecimal.ZERO.scale() == 0 && BigDecimal.ONE.scale() == 0
              && BigDecimal.TWO.scale() == 0 && BigDecimal.TEN.scale() == 0,
              "ZERO/ONE/TWO/TEN scale 0");
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
        check(new BigDecimal("1.23").setScale(2).equals(new BigDecimal("1.23")),
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
        check(z.scale() == 0, "strip(0.00) scale 0（与 JDK-8 一致）");
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
        // sqrt（JDK-9+）补充：非完全平方 floor 与负数消息
        check(new BigDecimal("2").sqrt(new MathContext(10)).toString().equals("1.414213562"),
              "sqrt(2) mc10 实测锁定");
        checkMsg(ArithmeticException.class, "Negative radicand with even root degree",
              () -> new BigDecimal("-4").sqrt(new MathContext(4)), "sqrt 负数消息逐字");
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
        testWhiteBox();
        testBlackBox();
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

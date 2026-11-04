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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

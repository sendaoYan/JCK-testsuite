/*
 * @test
 * @summary java.lang.StrictMath conformance (white-box structural + black-box
 *          behavioral): E/PI/TAU, abs/min/max/clamp, ceil/floor/round/rint,
 *          sqrt/cbrt/hypot, pow/exp/log/log10, trig + hyperbolic + inverse,
 *          toDegrees/toRadians, floorDiv/floorMod/ceilDiv/ceilMod, copySign/
 *          nextUp/nextDown/ulp/signum, exact arithmetic (incl. divideExact/
 *          absExact/multiplyFull/multiplyHigh/powExact), fma, and NaN/Infinity.
 * @run main StrictMathTest
 *
 * Design case
 * -----------
 * 1. Contract: StrictMath is a final utility class whose public functions are
 *    bit-for-bit portable (JDK 9+ uses the same intrinsics/fdlibm backend as Math).
 * 2. Usage: call StrictMath directly on primitives; exact methods throw
 *    ArithmeticException on overflow; clamp bounds a value; fma fuses multiply-add.
 * 3. Composition: floorDiv(x,y)*y+floorMod(x,y)==x; ceilDiv*divisor+ceilMod==x;
 *    toDegrees(toRadians(x))==x; nextUp/nextDown/ulp walk adjacent values.
 * 4. Invariants: exact methods never wrap; absExact(MIN) and divideExact(MIN,-1)
 *    throw; clamp(min>max or NaN bound) throws IllegalArgumentException.
 * 5. Boundary/exception: NaN/Infinity/-0.0 propagate deterministically (sqrt(-1)
 *    is NaN, min/max with NaN is NaN, signum(-0.0) preserves sign, round(NaN)==0).
 */

public class StrictMathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectArithmeticException(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected ArithmeticException");
        } catch (ArithmeticException expected) {
            passed++;
        }
    }

    static void expectIllegalArgumentException(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void checkClose(double a, double b, String msg) {
        if (Double.isNaN(a) && Double.isNaN(b)) {
            passed++;
            return;
        }
        double scale = Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
        if (Math.abs(a - b) <= 1e-14 * scale) {
            passed++;
        } else {
            throw new AssertionError(msg + ": |" + a + " - " + b + "| too large");
        }
    }

    // ---- White-box / structural ----
    static void testWhiteBox() {
        try {
            Class<?> c = Class.forName("java.lang.StrictMath");
            check(!c.isInterface(), "StrictMath is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "StrictMath is final");
            check(c.getSuperclass() == Object.class, "StrictMath extends Object directly");
            check(java.lang.reflect.Modifier.isStatic(c.getField("E").getModifiers())
                  && java.lang.reflect.Modifier.isFinal(c.getField("E").getModifiers()),
                  "E is public static final");
            check(java.lang.reflect.Modifier.isStatic(c.getField("TAU").getModifiers()),
                  "TAU is public static");
            check(java.lang.reflect.Modifier.isStatic(
                  c.getMethod("sin", double.class).getModifiers()), "sin(double) is static");
            check(c.getMethod("sqrt", double.class).getReturnType() == double.class,
                  "sqrt(double) returns double");
            check(c.getMethod("clamp", double.class, double.class, double.class)
                  .getReturnType() == double.class, "clamp(double,double,double) returns double");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testConstants() {
        check(StrictMath.E == Math.E && StrictMath.PI == Math.PI, "E/PI == Math.E/PI");
        check(StrictMath.TAU == 2.0 * StrictMath.PI, "TAU == 2*PI");
        check(StrictMath.toDegrees(StrictMath.TAU) == 360.0, "toDegrees(TAU) == 360");
    }

    static void testAbsMinMax() {
        check(StrictMath.abs(-5) == 5 && StrictMath.abs(5) == 5, "abs(int)");
        check(StrictMath.abs(-5L) == 5L && StrictMath.abs(-5.5) == 5.5, "abs(long/double)");
        check(StrictMath.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE, "abs(MIN) stays negative");
        check(1.0 / StrictMath.abs(-0.0) == Double.POSITIVE_INFINITY, "abs(-0.0) == +0.0");
        check(StrictMath.min(3, 5) == 3 && StrictMath.max(3, 5) == 5, "min/max int");
        check(StrictMath.min(3L, 5L) == 3L && StrictMath.max(3L, 5L) == 5L, "min/max long");
        check(Double.isNaN(StrictMath.min(Double.NaN, 1.0)), "min with NaN is NaN");
        check(Double.isNaN(StrictMath.max(1.0, Double.NaN)), "max with NaN is NaN");
        check(1.0 / StrictMath.min(-0.0, 0.0) == Double.NEGATIVE_INFINITY,
              "min(-0.0,0.0) == -0.0");
        check(1.0 / StrictMath.max(-0.0, 0.0) == Double.POSITIVE_INFINITY,
              "max(-0.0,0.0) == +0.0");
    }

    static void testRounding() {
        check(StrictMath.floor(2.5) == 2.0 && StrictMath.floor(-2.5) == -3.0, "floor");
        check(StrictMath.ceil(2.5) == 3.0 && StrictMath.ceil(-2.5) == -2.0, "ceil");
        check(StrictMath.rint(2.5) == 2.0 && StrictMath.rint(3.5) == 4.0, "rint half-to-even");
        check(StrictMath.round(2.4) == 2L && StrictMath.round(2.5) == 3L, "round(double)");
        check(StrictMath.round(-2.5) == -2L, "round(-2.5) toward +inf");
        check(StrictMath.round(2.5f) == 3, "round(float) returns int");
        check(StrictMath.round(Double.NaN) == 0L, "round(NaN) == 0");
        check(StrictMath.round(Double.POSITIVE_INFINITY) == Long.MAX_VALUE,
              "round(+Inf) == Long.MAX_VALUE");
        check(StrictMath.round(Double.NEGATIVE_INFINITY) == Long.MIN_VALUE,
              "round(-Inf) == Long.MIN_VALUE");
    }

    static void testPowersRoots() {
        check(StrictMath.sqrt(9.0) == 3.0 && StrictMath.sqrt(0.0) == 0.0, "sqrt");
        check(Double.isNaN(StrictMath.sqrt(-1.0)), "sqrt(-1) is NaN");
        check(StrictMath.cbrt(27.0) == 3.0 && StrictMath.cbrt(-8.0) == -2.0, "cbrt");
        check(StrictMath.hypot(3.0, 4.0) == 5.0, "hypot(3,4)");
        check(StrictMath.pow(2.0, 10.0) == 1024.0, "pow(2,10)");
        check(StrictMath.pow(0.0, 0.0) == 1.0, "pow(0,0) == 1");
        check(StrictMath.pow(-2.0, 3.0) == -8.0, "pow(-2,3)");
    }

    static void testExpLog() {
        check(StrictMath.exp(0.0) == 1.0, "exp(0)");
        checkClose(StrictMath.exp(1.0), StrictMath.E, "exp(1) ~ E");
        check(StrictMath.expm1(0.0) == 0.0, "expm1(0)");
        check(StrictMath.log(1.0) == 0.0, "log(1)");
        checkClose(StrictMath.log(StrictMath.E), 1.0, "log(E) ~ 1");
        checkClose(StrictMath.log10(1000.0), 3.0, "log10(1000) ~ 3");
        check(StrictMath.log1p(0.0) == 0.0, "log1p(0)");
    }

    static void testTrig() {
        check(StrictMath.sin(0.0) == 0.0, "sin(0)");
        check(StrictMath.cos(0.0) == 1.0, "cos(0)");
        check(StrictMath.tan(0.0) == 0.0, "tan(0)");
        check(StrictMath.asin(0.0) == 0.0 && StrictMath.acos(1.0) == 0.0, "asin/acos");
        check(StrictMath.atan(0.0) == 0.0, "atan(0)");
        checkClose(StrictMath.atan2(1.0, 1.0), StrictMath.PI / 4, "atan2(1,1) ~ PI/4");
        check(StrictMath.sinh(0.0) == 0.0 && StrictMath.cosh(0.0) == 1.0
              && StrictMath.tanh(0.0) == 0.0, "sinh/cosh/tanh");
        check(StrictMath.asinh(0.0) == 0.0 && StrictMath.acosh(1.0) == 0.0
              && StrictMath.atanh(0.0) == 0.0, "asinh/acosh/atanh");
        check(StrictMath.toDegrees(StrictMath.PI) == 180.0, "toDegrees(PI) == 180");
        check(StrictMath.toRadians(180.0) == StrictMath.PI, "toRadians(180) == PI");
        checkClose(StrictMath.toDegrees(StrictMath.toRadians(45.0)), 45.0,
                   "toDegrees(toRadians(45)) ~ 45");
    }

    static void testFloorDivMod() {
        check(StrictMath.floorDiv(7, 3) == 2 && StrictMath.floorMod(7, 3) == 1,
              "floorDiv/Mod positive");
        check(StrictMath.floorDiv(-7, 3) == -3 && StrictMath.floorMod(-7, 3) == 2,
              "floorDiv/Mod negative dividend");
        check(StrictMath.floorDiv(7, -3) == -3 && StrictMath.floorMod(7, -3) == -2,
              "floorDiv/Mod negative divisor");
        check(StrictMath.floorDiv(-7L, 3L) == -3L && StrictMath.floorMod(-7L, 3L) == 2L,
              "floorDiv/Mod long");
        check(StrictMath.floorDiv(7, 3) * 3 + StrictMath.floorMod(7, 3) == 7, "floor identity");
        expectArithmeticException(() -> StrictMath.floorDiv(1, 0), "floorDiv by zero");
        expectArithmeticException(() -> StrictMath.floorMod(1, 0), "floorMod by zero");
    }

    static void testCeilDivMod() {
        check(StrictMath.ceilDiv(7, 3) == 3 && StrictMath.ceilMod(7, 3) == -2, "ceilDiv/Mod");
        check(StrictMath.ceilDiv(-7, 3) == -2 && StrictMath.ceilMod(-7, 3) == -1,
              "ceilDiv/Mod negative");
        check(StrictMath.ceilDiv(7, 3) * 3 + StrictMath.ceilMod(7, 3) == 7, "ceil identity");
        expectArithmeticException(() -> StrictMath.ceilDiv(1, 0), "ceilDiv by zero");
    }

    static void testExactArithmetic() {
        check(StrictMath.addExact(1, 2) == 3 && StrictMath.subtractExact(1, 2) == -1,
              "add/subtractExact");
        check(StrictMath.multiplyExact(3, 4) == 12 && StrictMath.incrementExact(4) == 5
              && StrictMath.decrementExact(4) == 3, "multiply/increment/decrementExact");
        check(StrictMath.negateExact(-5) == 5 && StrictMath.toIntExact(5L) == 5,
              "negateExact/toIntExact");
        expectArithmeticException(() -> StrictMath.addExact(Integer.MAX_VALUE, 1),
                                  "addExact overflow");
        expectArithmeticException(() -> StrictMath.multiplyExact(Integer.MAX_VALUE, 2),
                                  "multiplyExact overflow");
        expectArithmeticException(() -> StrictMath.subtractExact(Integer.MIN_VALUE, 1),
                                  "subtractExact overflow");
        expectArithmeticException(() -> StrictMath.incrementExact(Integer.MAX_VALUE),
                                  "incrementExact overflow");
        expectArithmeticException(() -> StrictMath.decrementExact(Integer.MIN_VALUE),
                                  "decrementExact overflow");
        expectArithmeticException(() -> StrictMath.negateExact(Integer.MIN_VALUE),
                                  "negateExact(MIN) overflow");
        expectArithmeticException(() -> StrictMath.toIntExact(Integer.MAX_VALUE + 1L),
                                  "toIntExact overflow");
        expectArithmeticException(() -> StrictMath.addExact(Long.MAX_VALUE, 1L),
                                  "addExact long overflow");
    }

    static void testDivideAbsExact() {
        check(StrictMath.divideExact(10, 3) == 3, "divideExact(10,3)");
        check(StrictMath.divideExact(-10, 3) == -3, "divideExact(-10,3) truncates");
        expectArithmeticException(() -> StrictMath.divideExact(1, 0), "divideExact by zero");
        expectArithmeticException(() -> StrictMath.divideExact(Integer.MIN_VALUE, -1),
                                  "divideExact(MIN,-1) overflow");
        check(StrictMath.absExact(5) == 5 && StrictMath.absExact(-5) == 5, "absExact");
        expectArithmeticException(() -> StrictMath.absExact(Integer.MIN_VALUE),
                                  "absExact(int MIN)");
        expectArithmeticException(() -> StrictMath.absExact(Long.MIN_VALUE),
                                  "absExact(long MIN)");
    }

    static void testMultiplyFullHigh() {
        check(StrictMath.multiplyFull(65536, 65536) == 4294967296L, "multiplyFull(2^16,2^16)");
        check(StrictMath.multiplyFull(-1, -1) == 1L, "multiplyFull(-1,-1)");
        check(StrictMath.multiplyHigh(0x100000000L, 0x100000000L) == 1L,
              "multiplyHigh(2^32,2^32) == 1");
        check(StrictMath.multiplyHigh(-1L, -1L) == 0L, "multiplyHigh(-1,-1) == 0");
        check(StrictMath.unsignedMultiplyHigh(-1L, -1L) == -2L,
              "unsignedMultiplyHigh(-1,-1) == -2");
    }

    static void testPowExact() {
        check(StrictMath.powExact(2, 10) == 1024, "powExact(2,10)");
        check(StrictMath.powExact(0, 0) == 1, "powExact(0,0) == 1");
        check(StrictMath.powExact(-2, 31) == Integer.MIN_VALUE, "powExact(-2,31) == MIN");
        expectArithmeticException(() -> StrictMath.powExact(2, 31), "powExact(2,31) overflow");
        expectArithmeticException(() -> StrictMath.powExact(2, -1),
                                  "powExact negative exponent");
    }

    static void testClamp() {
        check(StrictMath.clamp(5, 0, 10) == 5, "clamp(5,0,10)");
        check(StrictMath.clamp(-5, 0, 10) == 0 && StrictMath.clamp(15, 0, 10) == 10,
              "clamp clamps to bounds");
        check(StrictMath.clamp(5.5, 0.0, 10.0) == 5.5, "clamp(double)");
        expectIllegalArgumentException(() -> StrictMath.clamp(1, 2, 1),
                                       "clamp(min > max) throws");
        expectIllegalArgumentException(() -> StrictMath.clamp(1.0, Double.NaN, 2.0),
                                       "clamp(NaN min) throws");
    }

    static void testFmaSignumCopySign() {
        check(StrictMath.fma(2.0, 3.0, 4.0) == 10.0, "fma(2,3,4) == 10");
        check(Double.isNaN(StrictMath.fma(Double.NaN, 1.0, 1.0)), "fma with NaN is NaN");
        check(StrictMath.signum(5.0) == 1.0 && StrictMath.signum(-5.0) == -1.0
              && StrictMath.signum(0.0) == 0.0, "signum");
        check(1.0 / StrictMath.signum(-0.0) == Double.NEGATIVE_INFINITY,
              "signum(-0.0) preserves sign");
        check(Double.isNaN(StrictMath.signum(Double.NaN)), "signum(NaN) is NaN");
        check(StrictMath.copySign(1.0, -2.0) == -1.0 && StrictMath.copySign(-1.0, 2.0) == 1.0,
              "copySign");
        check(StrictMath.copySign(1.0f, -2.0f) == -1.0f, "copySign(float)");
    }

    static void testUlpNext() {
        check(StrictMath.ulp(1.0) == 0x1.0p-52, "ulp(1.0) == 2^-52");
        check(StrictMath.ulp(1.0f) == 0x1.0p-23f, "ulp(1.0f) == 2^-23");
        check(StrictMath.ulp(0.0) == Double.MIN_VALUE, "ulp(0.0) == MIN_VALUE");
        check(StrictMath.nextUp(1.0) > 1.0 && StrictMath.nextDown(1.0) < 1.0,
              "nextUp/nextDown");
        check(StrictMath.nextUp(0.0) == Double.MIN_VALUE, "nextUp(0.0) == MIN_VALUE");
        check(StrictMath.nextDown(0.0) == -Double.MIN_VALUE, "nextDown(0.0) == -MIN_VALUE");
        check(StrictMath.nextUp(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "nextUp(+Inf) == +Inf");
        check(StrictMath.nextAfter(1.0, 2.0) > 1.0 && StrictMath.nextAfter(1.0, 0.0) < 1.0,
              "nextAfter");
        check(StrictMath.nextAfter(Double.POSITIVE_INFINITY, 0.0) == Double.MAX_VALUE,
              "nextAfter(+Inf,0.0) == MAX_VALUE");
        check(StrictMath.getExponent(8.0) == 3 && StrictMath.scalb(1.0, 3) == 8.0,
              "getExponent/scalb");
    }

    // ---- Black-box / behavioral ----
    static void testBlackBox() {
        testConstants();
        testAbsMinMax();
        testRounding();
        testPowersRoots();
        testExpLog();
        testTrig();
        testFloorDivMod();
        testCeilDivMod();
        testExactArithmetic();
        testDivideAbsExact();
        testMultiplyFullHigh();
        testPowExact();
        testClamp();
        testFmaSignumCopySign();
        testUlpNext();
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.Math API conformance (Java 8 surface): constants, abs/min/max,
 *          exact arithmetic (addExact/subtractExact/multiplyExact/toIntExact with
 *          ArithmeticException on overflow), floorDiv/floorMod, rounding (floor/ceil/
 *          rint/round), elementary functions (sqrt/cbrt/pow/exp/log/log10/log1p/expm1/
 *          hypot), trig + hyperbolic + inverse trig, degrees/radians, signum/copySign/
 *          ulp/nextAfter/nextUp/nextDown/scalb/getExponent/IEEEremainder, and random.
 * @run main MathTest
 */

public class MathTest {
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

    static void testConstants() {
        check(Math.E == java.lang.Math.E && Math.PI == java.lang.Math.PI, "E and PI accessible");
        check(Math.PI > 3.14 && Math.PI < 3.15, "PI ~ 3.14159");
        check(Math.E > 2.71 && Math.E < 2.72, "E ~ 2.71828");
    }

    static void testAbs() {
        check(Math.abs(-5) == 5, "abs(-5)");
        check(Math.abs(5) == 5, "abs(5)");
        check(Math.abs(0) == 0, "abs(0)");
        check(Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE, "abs(Integer.MIN_VALUE) stays negative");
        check(Math.abs(Long.MIN_VALUE) == Long.MIN_VALUE, "abs(Long.MIN_VALUE) stays negative");
        check(Math.abs(-5.5) == 5.5, "abs(-5.5)");
        check(Math.abs(-5.5f) == 5.5f, "abs(-5.5f)");
        check(Math.abs(0.0) == 0.0 && 1.0 / Math.abs(-0.0) == Double.POSITIVE_INFINITY,
              "abs(-0.0) == +0.0");
        check(Double.isNaN(Math.abs(Double.NaN)), "abs(NaN) is NaN");
        check(Math.abs(Double.NEGATIVE_INFINITY) == Double.POSITIVE_INFINITY, "abs(-Inf) == Inf");
    }

    static void testMinMax() {
        check(Math.min(3, 5) == 3, "min(3,5)");
        check(Math.max(3, 5) == 5, "max(3,5)");
        check(Math.min(3L, 5L) == 3L && Math.max(3L, 5L) == 5L, "min/max long");
        check(Math.min(3.0f, 5.0f) == 3.0f && Math.max(3.0f, 5.0f) == 5.0f, "min/max float");
        check(Double.isNaN(Math.min(Double.NaN, 1.0)), "min with NaN is NaN");
        check(Double.isNaN(Math.max(Double.NaN, 1.0)), "max with NaN is NaN");
        check(Math.min(-0.0, 0.0) == -0.0 && 1.0 / Math.min(-0.0, 0.0) == Double.NEGATIVE_INFINITY,
              "min(-0.0,0.0) == -0.0");
        check(Math.max(-0.0, 0.0) == 0.0 && 1.0 / Math.max(-0.0, 0.0) == Double.POSITIVE_INFINITY,
              "max(-0.0,0.0) == 0.0");
    }

    static void testExactArithmetic() {
        check(Math.addExact(1, 2) == 3, "addExact(1,2)");
        check(Math.subtractExact(1, 2) == -1, "subtractExact(1,2)");
        check(Math.multiplyExact(3, 4) == 12, "multiplyExact(3,4)");
        check(Math.addExact(1L, 2L) == 3L && Math.multiplyExact(3L, 4L) == 12L, "exact long");
        check(Math.incrementExact(4) == 5 && Math.decrementExact(4) == 3, "increment/decrementExact");
        check(Math.negateExact(-5) == 5 && Math.negateExact(5) == -5, "negateExact");

        expectArithmeticException(() -> Math.addExact(Integer.MAX_VALUE, 1), "addExact overflow");
        expectArithmeticException(() -> Math.subtractExact(Integer.MIN_VALUE, 1), "subtractExact overflow");
        expectArithmeticException(() -> Math.multiplyExact(Integer.MAX_VALUE, 2), "multiplyExact overflow");
        expectArithmeticException(() -> Math.addExact(Long.MAX_VALUE, 1L), "addExact long overflow");
        expectArithmeticException(() -> Math.negateExact(Integer.MIN_VALUE), "negateExact(MIN) overflow");
        expectArithmeticException(() -> Math.incrementExact(Integer.MAX_VALUE), "incrementExact overflow");
        expectArithmeticException(() -> Math.decrementExact(Integer.MIN_VALUE), "decrementExact overflow");

        check(Math.toIntExact(5L) == 5, "toIntExact(5)");
        expectArithmeticException(() -> Math.toIntExact(Integer.MAX_VALUE + 1L), "toIntExact overflow");
    }

    static void testFloorDivMod() {
        check(Math.floorDiv(7, 3) == 2 && Math.floorMod(7, 3) == 1, "floorDiv/Mod positive");
        check(Math.floorDiv(-7, 3) == -3 && Math.floorMod(-7, 3) == 2, "floorDiv/Mod negative dividend");
        check(Math.floorDiv(7, -3) == -3 && Math.floorMod(7, -3) == -2, "floorDiv/Mod negative divisor");
        check(Math.floorDiv(-7L, 3L) == -3L && Math.floorMod(-7L, 3L) == 2L, "floorDiv/Mod long");
        expectArithmeticException(() -> Math.floorDiv(1, 0), "floorDiv by zero");
        expectArithmeticException(() -> Math.floorMod(1, 0), "floorMod by zero");
    }

    static void testRounding() {
        check(Math.floor(2.5) == 2.0, "floor(2.5)");
        check(Math.floor(-2.5) == -3.0, "floor(-2.5)");
        check(Math.ceil(2.5) == 3.0, "ceil(2.5)");
        check(Math.ceil(-2.5) == -2.0, "ceil(-2.5)");
        check(Math.rint(2.5) == 2.0, "rint(2.5) rounds to even");
        check(Math.rint(3.5) == 4.0, "rint(3.5) rounds to even");
        check(Math.rint(-2.5) == -2.0, "rint(-2.5)");
        check(Math.round(2.4) == 2L && Math.round(2.5) == 3L, "round(double) half-up");
        check(Math.round(-2.5) == -2L, "round(-2.5) toward +inf");
        check(Math.round(2.5f) == 3, "round(float) returns int");
        check(Math.round(Double.NaN) == 0L, "round(NaN) == 0");
        check(Math.round(Double.POSITIVE_INFINITY) == Long.MAX_VALUE, "round(+Inf) == Long.MAX_VALUE");
        check(Math.round(Double.NEGATIVE_INFINITY) == Long.MIN_VALUE, "round(-Inf) == Long.MIN_VALUE");
    }

    static void testPowersAndRoots() {
        check(Math.sqrt(9.0) == 3.0, "sqrt(9)");
        check(Math.sqrt(0.0) == 0.0, "sqrt(0)");
        check(Double.isNaN(Math.sqrt(-1.0)), "sqrt(-1) is NaN");
        check(Math.sqrt(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "sqrt(Inf)");
        check(Math.cbrt(27.0) == 3.0, "cbrt(27)");
        check(Math.cbrt(-8.0) == -2.0, "cbrt(-8)");
        check(Math.pow(2.0, 3.0) == 8.0, "pow(2,3)");
        check(Math.pow(2.0, 0.0) == 1.0, "pow(2,0)");
        check(Math.pow(0.0, 0.0) == 1.0, "pow(0,0) == 1");
        check(Math.pow(-2.0, 3.0) == -8.0, "pow(-2,3)");
        check(Math.pow(4.0, 0.5) == 2.0, "pow(4,0.5)");
    }

    static void testExpLog() {
        check(Math.exp(0.0) == 1.0, "exp(0)");
        check(Math.exp(1.0) == Math.E, "exp(1) == E");
        check(Math.expm1(0.0) == 0.0, "expm1(0)");
        check(Math.log(1.0) == 0.0, "log(1)");
        check(Math.log(Math.E) == 1.0, "log(E) == 1");
        check(Math.log10(1000.0) == 3.0, "log10(1000)");
        check(Math.log1p(0.0) == 0.0, "log1p(0)");
        check(Math.hypot(3.0, 4.0) == 5.0, "hypot(3,4)");
    }

    static void testTrig() {
        check(Math.sin(0.0) == 0.0, "sin(0)");
        check(Math.cos(0.0) == 1.0, "cos(0)");
        check(Math.tan(0.0) == 0.0, "tan(0)");
        check(Math.asin(0.0) == 0.0, "asin(0)");
        check(Math.acos(1.0) == 0.0, "acos(1)");
        check(Math.atan(0.0) == 0.0, "atan(0)");
        check(Math.atan2(1.0, 1.0) == Math.PI / 4, "atan2(1,1) == PI/4");
        check(Math.sinh(0.0) == 0.0, "sinh(0)");
        check(Math.cosh(0.0) == 1.0, "cosh(0)");
        check(Math.tanh(0.0) == 0.0, "tanh(0)");
        check(Math.toDegrees(Math.PI) == 180.0, "toDegrees(PI) == 180");
        check(Math.toRadians(180.0) == Math.PI, "toRadians(180) == PI");
    }

    static void testSignumCopySign() {
        check(Math.signum(5.0) == 1.0 && Math.signum(-5.0) == -1.0 && Math.signum(0.0) == 0.0,
              "signum(double)");
        check(1.0 / Math.signum(-0.0) == Double.NEGATIVE_INFINITY, "signum(-0.0) preserves sign");
        check(Double.isNaN(Math.signum(Double.NaN)), "signum(NaN) is NaN");
        check(Math.signum(5.0f) == 1.0f && Math.signum(-5.0f) == -1.0f, "signum(float)");

        check(Math.copySign(1.0, -2.0) == -1.0, "copySign(1,-2)");
        check(Math.copySign(-1.0, 2.0) == 1.0, "copySign(-1,2)");
        check(Math.copySign(1.0f, -2.0f) == -1.0f, "copySign(float)");
        check(Math.copySign(1.0, 0.0) == 1.0, "copySign(1,+0)");
    }

    static void testUlpNext() {
        check(Math.ulp(1.0) == 0x1.0p-52, "ulp(1.0) == 2^-52");
        check(Math.ulp(1.0f) == 0x1.0p-23f, "ulp(1.0f) == 2^-23");
        check(Math.ulp(0.0) == Double.MIN_VALUE, "ulp(0.0) == MIN_VALUE");
        check(Math.ulp(0.0f) == Float.MIN_VALUE, "ulp(0.0f) == MIN_VALUE");

        check(Math.nextUp(1.0) > 1.0 && Math.nextDown(1.0) < 1.0, "nextUp/nextDown(1.0)");
        check(Math.nextUp(0.0) == Double.MIN_VALUE, "nextUp(0.0) == MIN_VALUE");
        check(Math.nextDown(0.0) == -Double.MIN_VALUE, "nextDown(0.0) == -MIN_VALUE");
        check(Math.nextUp(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "nextUp(Inf) == Inf");
        check(Math.nextAfter(1.0, 2.0) > 1.0 && Math.nextAfter(1.0, 0.0) < 1.0, "nextAfter(1.0)");
        check(Math.nextAfter(1.0, 1.0) == 1.0, "nextAfter(1,1) == 1");
        check(Math.nextAfter(Double.POSITIVE_INFINITY, 0.0) == Double.MAX_VALUE,
              "nextAfter(Inf,0) == MAX_VALUE");
    }

    static void testScalbExponent() {
        check(Math.scalb(1.0, 3) == 8.0, "scalb(1,3)");
        check(Math.scalb(1.0, -1) == 0.5, "scalb(1,-1)");
        check(Math.scalb(1.5, 2) == 6.0, "scalb(1.5,2)");
        check(Math.scalb(1.0f, 2) == 4.0f, "scalb(float)");
        check(Math.getExponent(8.0) == 3, "getExponent(8) == 3");
        check(Math.getExponent(1.0) == 0, "getExponent(1) == 0");
        check(Math.getExponent(0.5) == -1, "getExponent(0.5) == -1");
        check(Math.getExponent(Double.MAX_VALUE) == 1023, "getExponent(MAX) == 1023");
        check(Math.getExponent(4.0f) == 2, "getExponent(4f) == 2");
    }

    static void testIEEEremainder() {
        check(Math.IEEEremainder(5.0, 3.0) == -1.0, "IEEEremainder(5,3) == -1");
        check(Math.IEEEremainder(6.0, 3.0) == 0.0, "IEEEremainder(6,3) == 0");
        check(Math.IEEEremainder(7.0, 3.0) == 1.0, "IEEEremainder(7,3) == 1");
    }

    static void testRandom() {
        for (int i = 0; i < 100; i++) {
            double r = Math.random();
            check(r >= 0.0 && r < 1.0, "random() in [0,1)");
        }
    }

    public static void main(String[] args) {
        testConstants();
        testAbs();
        testMinMax();
        testExactArithmetic();
        testFloorDivMod();
        testRounding();
        testPowersAndRoots();
        testExpLog();
        testTrig();
        testSignumCopySign();
        testUlpNext();
        testScalbExponent();
        testIEEEremainder();
        testRandom();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

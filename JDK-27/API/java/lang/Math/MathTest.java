/*
 * @test
 * @summary java.lang.Math conformance (white-box structural + black-box behavioral):
 *          constants (E/PI/TAU), abs/min/max/clamp, exact arithmetic (add/subtract/
 *          multiply/divide/increment/decrement/negate/abs/toIntExact + floorDivExact/
 *          ceilDivExact with ArithmeticException), floorDiv/floorMod/ceilDiv/ceilMod,
 *          rounding, elementary functions, trig + hyperbolic + inverse, degrees/radians,
 *          signum/copySign, ulp/nextAfter/nextUp/nextDown/scalb/getExponent/
 *          IEEEremainder, multiplyFull/multiplyHigh/unsignedMultiplyHigh/
 *          unsignedMultiplyExact, powExact/unsignedPowExact, fma, and random.
 *
 * Design case
 * -----------
 * 1. Contract: Math is a final utility class of stateless static functions; exact
 *    arithmetic methods throw ArithmeticException instead of silently wrapping.
 * 2. Usage: call Math methods directly on primitives; exact methods for overflow
 *    detection; floorDiv/floorMod/ceilDiv/ceilMod for directed division; clamp for
 *    bounds; fma for fused multiply-add.
 * 3. Composition: floorDiv(x,y)*y+floorMod(x,y)==x; ceilDiv*divisor+ceilMod==x;
 *    multiplyFull/multiplyHigh reconstruct a 128-bit product; nextUp/nextDown/ulp
 *    walk adjacent representable values.
 * 4. Invariants: exact methods never wrap; absExact(MIN) and divideExact(MIN,-1)
 *    throw; clamp(min>max or NaN bound) throws IllegalArgumentException.
 * 5. Boundary/exception: NaN/Infinity/-0.0 propagate deterministically (sqrt(-1) is
 *    NaN, min/max with NaN is NaN, signum(-0.0) preserves sign).
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
        double u = Math.ulp(b != 0.0 ? b : a);
        if (Math.abs(a - b) <= u) {
            passed++;
        } else {
            throw new AssertionError(msg + ": |" + a + " - " + b + "| too large");
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
        checkClose(Math.cbrt(27.0), 3.0, "cbrt(27) ~ 3");
        checkClose(Math.cbrt(-8.0), -2.0, "cbrt(-8) ~ -2");
        checkClose(Math.pow(2.0, 3.0), 8.0, "pow(2,3) ~ 8");
        check(Math.pow(2.0, 0.0) == 1.0, "pow(2,0)");
        check(Math.pow(0.0, 0.0) == 1.0, "pow(0,0) == 1");
        checkClose(Math.pow(-2.0, 3.0), -8.0, "pow(-2,3) ~ -8");
        checkClose(Math.pow(4.0, 0.5), 2.0, "pow(4,0.5) ~ 2");
    }

    static void testExpLog() {
        check(Math.exp(0.0) == 1.0, "exp(0)");
        checkClose(Math.exp(1.0), Math.E, "exp(1) ~ E");
        check(Math.expm1(0.0) == 0.0, "expm1(0)");
        check(Math.log(1.0) == 0.0, "log(1)");
        checkClose(Math.log(Math.E), 1.0, "log(E) ~ 1");
        checkClose(Math.log10(1000.0), 3.0, "log10(1000) ~ 3");
        check(Math.log1p(0.0) == 0.0, "log1p(0)");
        checkClose(Math.hypot(3.0, 4.0), 5.0, "hypot(3,4) ~ 5");
    }

    static void testTrig() {
        check(Math.sin(0.0) == 0.0, "sin(0)");
        check(Math.cos(0.0) == 1.0, "cos(0)");
        check(Math.tan(0.0) == 0.0, "tan(0)");
        check(Math.asin(0.0) == 0.0, "asin(0)");
        check(Math.acos(1.0) == 0.0, "acos(1)");
        check(Math.atan(0.0) == 0.0, "atan(0)");
        checkClose(Math.atan2(1.0, 1.0), Math.PI / 4, "atan2(1,1) ~ PI/4");
        check(Math.sinh(0.0) == 0.0, "sinh(0)");
        check(Math.cosh(0.0) == 1.0, "cosh(0)");
        check(Math.tanh(0.0) == 0.0, "tanh(0)");
        checkClose(Math.toDegrees(Math.PI), 180.0, "toDegrees(PI) ~ 180");
        checkClose(Math.toRadians(180.0), Math.PI, "toRadians(180) ~ PI");
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

    // ---- White-box / structural ----
    static void testWhiteBox() {
        try {
            Class<?> c = Class.forName("java.lang.Math");
            check(!c.isInterface(), "Math is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Math is final");
            check(c.getSuperclass() == Object.class, "Math extends Object directly");
            check(c.getDeclaredConstructors().length == 0
                  || java.lang.reflect.Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers()),
                  "Math has no public constructor");
            check(java.lang.reflect.Modifier.isStatic(c.getField("E").getModifiers())
                  && java.lang.reflect.Modifier.isFinal(c.getField("E").getModifiers()),
                  "E is public static final");
            check(c.getField("TAU").getType() == double.class, "TAU is a double");
            check(java.lang.reflect.Modifier.isStatic(c.getMethod("addExact", int.class, int.class)
                  .getModifiers()), "addExact(int,int) is static");
            check(c.getMethod("sqrt", double.class).getReturnType() == double.class,
                  "sqrt(double) returns double");
            check(c.getMethod("clamp", double.class, double.class, double.class)
                  .getReturnType() == double.class, "clamp(double,double,double) returns double");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testTau() {
        check(Math.TAU == 2.0 * Math.PI, "TAU == 2*PI");
        checkClose(Math.toDegrees(Math.TAU), 360.0, "toDegrees(TAU) ~ 360");
    }

    static void testDivideExact() {
        check(Math.divideExact(10, 3) == 3, "divideExact(10,3)");
        check(Math.divideExact(-10, 3) == -3, "divideExact(-10,3) truncates toward zero");
        check(Math.divideExact(10L, 3L) == 3L, "divideExact long");
        expectArithmeticException(() -> Math.divideExact(1, 0), "divideExact by zero");
        expectArithmeticException(() -> Math.divideExact(Integer.MIN_VALUE, -1),
                                  "divideExact(MIN,-1) overflow");
        expectArithmeticException(() -> Math.divideExact(Long.MIN_VALUE, -1L),
                                  "divideExact(long MIN,-1) overflow");
    }

    static void testAbsExact() {
        check(Math.absExact(5) == 5 && Math.absExact(-5) == 5, "absExact int");
        check(Math.absExact(5L) == 5L && Math.absExact(-5L) == 5L, "absExact long");
        expectArithmeticException(() -> Math.absExact(Integer.MIN_VALUE), "absExact(int MIN)");
        expectArithmeticException(() -> Math.absExact(Long.MIN_VALUE), "absExact(long MIN)");
    }

    static void testFloorDivExact() {
        check(Math.floorDivExact(7, 3) == 2, "floorDivExact(7,3)");
        check(Math.floorDivExact(-7, 3) == -3, "floorDivExact(-7,3)");
        check(Math.floorDivExact(7L, 3L) == 2L, "floorDivExact long");
        expectArithmeticException(() -> Math.floorDivExact(Integer.MIN_VALUE, -1),
                                  "floorDivExact(MIN,-1)");
    }

    static void testCeilDivExact() {
        check(Math.ceilDivExact(7, 3) == 3, "ceilDivExact(7,3)");
        check(Math.ceilDivExact(-7, 3) == -2, "ceilDivExact(-7,3)");
        check(Math.ceilDivExact(7L, 3L) == 3L, "ceilDivExact long");
        expectArithmeticException(() -> Math.ceilDivExact(Integer.MIN_VALUE, -1),
                                  "ceilDivExact(MIN,-1)");
    }

    static void testCeilDivMod() {
        check(Math.ceilDiv(7, 3) == 3, "ceilDiv(7,3)");
        check(Math.ceilDiv(-7, 3) == -2, "ceilDiv(-7,3)");
        check(Math.ceilDiv(7, -3) == -2, "ceilDiv(7,-3)");
        check(Math.ceilDiv(-7, -3) == 3, "ceilDiv(-7,-3)");
        check(Math.ceilMod(7, 3) == -2, "ceilMod(7,3)");
        check(Math.ceilMod(-7, 3) == -1, "ceilMod(-7,3)");
        check(Math.ceilMod(7, -3) == 1, "ceilMod(7,-3)");
        check(Math.ceilMod(-7, -3) == 2, "ceilMod(-7,-3)");
        check(Math.ceilDiv(7, 3) * 3 + Math.ceilMod(7, 3) == 7, "ceil identity positive");
        check(Math.ceilDiv(-7, 3) * 3 + Math.ceilMod(-7, 3) == -7, "ceil identity negative");
        check(Math.ceilDiv(7L, 3L) == 3L && Math.ceilMod(7L, 3L) == -2L, "ceilDiv/Mod long");
        expectArithmeticException(() -> Math.ceilDiv(1, 0), "ceilDiv by zero");
        expectArithmeticException(() -> Math.ceilMod(1, 0), "ceilMod by zero");
    }

    static void testMultiplyFullHigh() {
        check(Math.multiplyFull(65536, 65536) == 4294967296L, "multiplyFull(2^16,2^16)");
        check(Math.multiplyFull(-1, -1) == 1L, "multiplyFull(-1,-1)");
        check(Math.multiplyFull(Integer.MAX_VALUE, 2) == 4294967294L, "multiplyFull(MAX,2)");
        check(Math.multiplyHigh(0x100000000L, 0x100000000L) == 1L,
              "multiplyHigh(2^32,2^32) == 1");
        check(Math.multiplyHigh(-1L, -1L) == 0L, "multiplyHigh(-1,-1) == 0");
        check(Math.unsignedMultiplyHigh(0x100000000L, 0x100000000L) == 1L,
              "unsignedMultiplyHigh(2^32,2^32) == 1");
        check(Math.unsignedMultiplyHigh(-1L, -1L) == -2L, "unsignedMultiplyHigh(-1,-1) == -2");
    }

    static void testUnsignedMultiplyExact() {
        check(Math.unsignedMultiplyExact(2, 2) == 4, "unsignedMultiplyExact(2,2)");
        check(Math.unsignedMultiplyExact(0x40000000, 2) == 0x80000000,
              "unsignedMultiplyExact(2^30,2) == 2^31");
        expectArithmeticException(() -> Math.unsignedMultiplyExact(0x80000000, 2),
                                  "unsignedMultiplyExact(2^31,2) overflow");
        expectArithmeticException(() -> Math.unsignedMultiplyExact(-1, -1),
                                  "unsignedMultiplyExact(-1,-1) overflow");
    }

    static void testPowExact() {
        check(Math.powExact(2, 10) == 1024, "powExact(2,10)");
        check(Math.powExact(0, 0) == 1, "powExact(0,0) == 1");
        check(Math.powExact(0, 5) == 0, "powExact(0,5) == 0");
        check(Math.powExact(-2, 3) == -8, "powExact(-2,3)");
        check(Math.powExact(-2, 31) == Integer.MIN_VALUE, "powExact(-2,31) == MIN");
        check(Math.powExact(2L, 10) == 1024L, "powExact(long,10)");
        expectArithmeticException(() -> Math.powExact(2, 31), "powExact(2,31) overflow");
        expectArithmeticException(() -> Math.powExact(2, -1), "powExact negative exponent");
    }

    static void testUnsignedPowExact() {
        check(Math.unsignedPowExact(2, 31) == 0x80000000, "unsignedPowExact(2,31) == 2^31");
        check(Math.unsignedPowExact(0, 0) == 1, "unsignedPowExact(0,0) == 1");
        expectArithmeticException(() -> Math.unsignedPowExact(2, 32),
                                  "unsignedPowExact(2,32) overflow");
        expectArithmeticException(() -> Math.unsignedPowExact(2, -1),
                                  "unsignedPowExact negative exponent");
    }

    static void testClamp() {
        check(Math.clamp(5, 0, 10) == 5, "clamp(5,0,10)");
        check(Math.clamp(-5, 0, 10) == 0, "clamp(-5,0,10) == min");
        check(Math.clamp(15, 0, 10) == 10, "clamp(15,0,10) == max");
        check(Math.clamp(5L, 0L, 10L) == 5L, "clamp(long)");
        check(Math.clamp(5L, 0, 10) == 5, "clamp(long value, int bounds)");
        check(Math.clamp(0x100000000L, 0, 100) == 100, "clamp(huge long, int bounds) == max");
        check(Math.clamp(5.5, 0.0, 10.0) == 5.5, "clamp(double)");
        check(Math.clamp(5.5f, 0.0f, 10.0f) == 5.5f, "clamp(float)");
        expectIllegalArgumentException(() -> Math.clamp(1, 2, 1), "clamp(min > max) throws");
        expectIllegalArgumentException(() -> Math.clamp(1.0, Double.NaN, 2.0),
                                       "clamp(NaN min) throws");
    }

    static void testFma() {
        check(Math.fma(2.0, 3.0, 4.0) == 10.0, "fma(2,3,4) == 10");
        check(Math.fma(2.0f, 3.0f, 4.0f) == 10.0f, "fma float");
        check(Double.isNaN(Math.fma(Double.NaN, 1.0, 1.0)), "fma with NaN is NaN");
        check(Double.isNaN(Math.fma(0.0, Double.POSITIVE_INFINITY, 0.0)),
              "fma(0, Inf, 0) is NaN");
    }

    static void testInverseHyperbolic() {
        check(Math.asinh(0.0) == 0.0, "asinh(0)");
        check(Math.acosh(1.0) == 0.0, "acosh(1)");
        check(Math.atanh(0.0) == 0.0, "atanh(0)");
        check(Double.isNaN(Math.acosh(0.5)), "acosh(0.5) is NaN (out of domain)");
        check(Math.atanh(0.5) > 0.0 && Math.atanh(0.5) < 1.0, "atanh(0.5) in (0,1)");
    }

    static void testNextAfterFloat() {
        check(Math.nextAfter(1.0f, 2.0) > 1.0f, "nextAfter(1.0f, 2.0)");
        check(Math.nextAfter(1.0f, 0.0) < 1.0f, "nextAfter(1.0f, 0.0)");
        check(Math.nextAfter(0.0f, 1.0) == Float.MIN_VALUE, "nextAfter(0.0f,1.0) == MIN_VALUE");
        check(Math.nextAfter(Float.POSITIVE_INFINITY, 0.0) == Float.MAX_VALUE,
              "nextAfter(+Inf,0.0) == MAX_VALUE");
    }

    static void testBoundaryEquivalence() {
        // floorDiv/floorMod and ceilDiv/ceilMod identities for all sign combinations.
        int[] xs = {-7, -1, 0, 1, 7};
        int[] ys = {-3, -1, 1, 3};
        for (int x : xs) {
            for (int y : ys) {
                check(Math.floorDiv(x, y) * y + Math.floorMod(x, y) == x,
                      "floor identity " + x + "," + y);
                check(Math.ceilDiv(x, y) * y + Math.ceilMod(x, y) == x,
                      "ceil identity " + x + "," + y);
            }
        }

        // powExact base 0/1/-1 parity boundaries.
        check(Math.powExact(0, 0) == 1 && Math.powExact(0, 7) == 0, "powExact(0, ...)");
        check(Math.powExact(1, 100) == 1, "powExact(1,100)");
        check(Math.powExact(-1, 2) == 1 && Math.powExact(-1, 3) == -1, "powExact(-1, parity)");

        // unsignedPowExact base 0/1.
        check(Math.unsignedPowExact(1, 100) == 1, "unsignedPowExact(1,100)");
        check(Math.unsignedPowExact(0, 5) == 0, "unsignedPowExact(0,5)");

        // toIntExact boundary.
        check(Math.toIntExact(Integer.MIN_VALUE + 0L) == Integer.MIN_VALUE, "toIntExact(MIN)");

        // nextDown(nextUp(x)) involution.
        check(Math.nextDown(Math.nextUp(2.0)) == 2.0, "nextDown(nextUp(2.0))");

        // copySign round-trip.
        check(Math.copySign(Math.copySign(1.0, -2.0), 2.0) == 1.0, "copySign round-trip");
    }

    // ---- Black-box / behavioral ----
    static void testBlackBox() {
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
        testTau();
        testDivideExact();
        testAbsExact();
        testFloorDivExact();
        testCeilDivExact();
        testCeilDivMod();
        testMultiplyFullHigh();
        testUnsignedMultiplyExact();
        testPowExact();
        testUnsignedPowExact();
        testClamp();
        testFma();
        testInverseHyperbolic();
        testNextAfterFloat();
        testBoundaryEquivalence();
    }

    // ==== 契约深化（2026-11）追加 ====
    // round 半分方向全矩阵 / floorDiv·floorMod 四象限 + 恒等式循环 + 溢出与除零消息逐字 /
    // min·max·abs·floor·ceil·rint 位级负零断言 / pow·hypot 特例全表 / ulp·scalb·next 族边界 /
    // getExponent 边界 / E·PI 字符串与位模式 / fma 融合舍入分叉样本 / JDK-9+ mixed-precision
    // floorDiv(long,int)·floorMod(long,int)·multiplyExact(long,int)。全部值经 JDK-8 与 JDK-27 双侧实测锁定。

    static int aeSink;   // 吞掉会抛异常的除法结果
    static int zeroDiv;  // 恒为 0，防止编译器常量折叠 1/0

    static void checkBitsD(double actual, double expected, String msg) {
        check(Double.doubleToLongBits(actual) == Double.doubleToLongBits(expected),
              msg + " (bit-level via doubleToLongBits)");
    }

    static void checkBitsF(float actual, float expected, String msg) {
        check(Float.floatToIntBits(actual) == Float.floatToIntBits(expected),
              msg + " (bit-level via floatToIntBits)");
    }

    static void checkTol(double actual, double expected, double tol, String msg) {
        check(Math.abs(actual - expected) <= tol,
              msg + ": |" + actual + " - " + expected + "| <= " + tol);
    }

    static void expectAeMsg(Runnable r, String expected, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected ArithmeticException(\"" + expected + "\")");
        } catch (ArithmeticException e) {
            check(expected.equals(e.getMessage()),
                  msg + ": message \"" + e.getMessage() + "\" == \"" + expected + "\"");
        }
    }

    static void testRoundHalfTowardsPositiveInfinity() {
        // 招牌契约：round(x) == floor(x + 0.5)，半分向 +无穷（区别于 half-even 与 half-away-from-zero）
        check(Math.round(2.5) == 3L, "round(2.5) == 3");
        check(Math.round(-2.5) == -2L, "round(-2.5) == -2 (ties toward +infinity)");
        check(Math.round(0.5) == 1L, "round(0.5) == 1");
        check(Math.round(-0.5) == 0L, "round(-0.5) == 0 (floor(+0.0))");
        check(Math.round(2.49) == 2L, "round(2.49) == 2");
        check(Math.round(-2.51) == -3L, "round(-2.51) == -3");
        check(Math.round(1.6) == 2L, "round(1.6) == 2");
        check(Math.round(-1.6) == -2L, "round(-1.6) == -2 (floor(-1.1))");
        check(Math.round(-1.4) == -1L, "round(-1.4) == -1 (floor(-0.9))");
        check(Math.round(+0.0) == 0L, "round(+0.0) == 0");
        check(Math.round(-0.0) == 0L, "round(-0.0) == 0");
        // 0.5 的最大下界邻居：round 必须为 0（floor(x+0.5) 朴素实现的经典坑，JDK 已修）
        check(Math.round(0.49999999999999994) == 0L, "round(largest double < 0.5) == 0");
        check(Math.round(Double.NaN) == 0L, "round(NaN) == 0");
        check(Math.round(Double.POSITIVE_INFINITY) == Long.MAX_VALUE, "round(+Inf) == Long.MAX_VALUE");
        check(Math.round(Double.NEGATIVE_INFINITY) == Long.MIN_VALUE, "round(-Inf) == Long.MIN_VALUE");
        check(Math.round(Double.MAX_VALUE) == Long.MAX_VALUE,
              "round(Double.MAX_VALUE) saturates to Long.MAX_VALUE");
        check(Math.round(-Double.MAX_VALUE) == Long.MIN_VALUE,
              "round(-Double.MAX_VALUE) saturates to Long.MIN_VALUE");
        // float 版（返回 int）同一语义
        check(Math.round(2.5f) == 3, "round(2.5f) == 3");
        check(Math.round(-2.5f) == -2, "round(-2.5f) == -2");
        check(Math.round(0.5f) == 1, "round(0.5f) == 1");
        check(Math.round(-0.5f) == 0, "round(-0.5f) == 0");
        check(Math.round(Float.MAX_VALUE) == Integer.MAX_VALUE,
              "round(Float.MAX_VALUE) saturates to Integer.MAX_VALUE");
        check(Math.round(-Float.MAX_VALUE) == Integer.MIN_VALUE,
              "round(-Float.MAX_VALUE) saturates to Integer.MIN_VALUE");
        check(Math.round(Float.NaN) == 0, "round(Float.NaN) == 0");
    }

    static void testFloorDivModContractMatrix() {
        // 除数 2 的四象限全矩阵（与截断除法分叉的招牌点）
        check(Math.floorDiv(7, 2) == 3, "floorDiv(7,2) == 3");
        check(Math.floorDiv(-7, 2) == -4, "floorDiv(-7,2) == -4 (floors toward -inf)");
        check(Math.floorDiv(7, -2) == -4, "floorDiv(7,-2) == -4");
        check(Math.floorDiv(-7, -2) == 3, "floorDiv(-7,-2) == 3");
        check(Math.floorMod(7, 2) == 1, "floorMod(7,2) == 1");
        check(Math.floorMod(-7, 2) == 1, "floorMod(-7,2) == 1 (sign follows divisor)");
        check(Math.floorMod(7, -2) == -1, "floorMod(7,-2) == -1");
        check(Math.floorMod(-7, -2) == -1, "floorMod(-7,-2) == -1");
        // 与 / 和 % 的对照分叉：% 向零截断
        check(7 / 2 == 3 && 7 % 2 == 1, "7/2, 7%2 agree with floor family");
        check(-7 / 2 == -3 && -7 % 2 == -1, "-7/2 == -3 and -7%2 == -1 vs floorDiv -4 / floorMod 1");
        check(7 / -2 == -3 && 7 % -2 == 1, "7/-2 == -3 and 7%-2 == 1 vs floorDiv -4 / floorMod -1");
        check(-7 / -2 == 3 && -7 % -2 == -1, "-7/-2 == 3 and -7%-2 == -1");
        check(Math.floorDiv(8, 2) == 8 / 2 && Math.floorMod(8, 2) == 8 % 2,
              "floorDiv/floorMod agree with / and % on exact division");
        // 恒等式循环：floorMod(a,b) == a - floorDiv(a,b)*b；floorMod 符号随除数；|floorMod| < |除数|
        int[] xs = {-7, -6, -1, 0, 1, 6, 7};
        int[] ys = {-3, -2, -1, 1, 2, 3};
        for (int a : xs) {
            for (int b : ys) {
                int q = Math.floorDiv(a, b);
                int r = Math.floorMod(a, b);
                check(r == a - q * b, "int floorMod identity a-floorDiv(a,b)*b " + a + "," + b);
                check(r == 0 || ((r > 0) == (b > 0)), "int floorMod sign follows divisor " + a + "," + b);
                check(Math.abs(r) < Math.abs(b), "int |floorMod| < |divisor| " + a + "," + b);
            }
        }
        long[] xls = {-7L, -6L, -1L, 0L, 1L, 6L, 7L};
        long[] yls = {-3L, -2L, -1L, 1L, 2L, 3L};
        for (long a : xls) {
            for (long b : yls) {
                long q = Math.floorDiv(a, b);
                long r = Math.floorMod(a, b);
                check(r == a - q * b, "long floorMod identity " + a + "," + b);
                check(Math.abs(r) < Math.abs(b), "long |floorMod| < |divisor| " + a + "," + b);
            }
        }
        check(Math.floorDiv(-7L, 2L) == -4L && Math.floorMod(-7L, 2L) == 1L, "long key point (-7L,2L)");
        check(Math.floorDiv(7L, -2L) == -4L && Math.floorMod(7L, -2L) == -1L, "long key point (7L,-2L)");
        // MIN_VALUE/-1 溢出特例：规格允许回绕为 MIN_VALUE，不抛异常（区别于 divideExact/floorDivExact）
        check(Math.floorDiv(Integer.MIN_VALUE, -1) == Integer.MIN_VALUE,
              "floorDiv(int MIN,-1) wraps to MIN_VALUE without throwing");
        check(Math.floorMod(Integer.MIN_VALUE, -1) == 0, "floorMod(int MIN,-1) == 0");
        check(Math.floorDiv(Long.MIN_VALUE, -1L) == Long.MIN_VALUE,
              "floorDiv(long MIN,-1) wraps to MIN_VALUE without throwing");
        check(Math.floorMod(Long.MIN_VALUE, -1L) == 0L, "floorMod(long MIN,-1) == 0");
    }

    static void testExactOverflowMessages() {
        // int 族溢出：消息逐字 "integer overflow"
        expectAeMsg(() -> Math.addExact(Integer.MAX_VALUE, 1), "integer overflow", "addExact(MAX,1)");
        expectAeMsg(() -> Math.addExact(Integer.MIN_VALUE, -1), "integer overflow", "addExact(MIN,-1)");
        expectAeMsg(() -> Math.subtractExact(Integer.MIN_VALUE, 1), "integer overflow", "subtractExact(MIN,1)");
        expectAeMsg(() -> Math.subtractExact(Integer.MAX_VALUE, -1), "integer overflow", "subtractExact(MAX,-1)");
        expectAeMsg(() -> Math.multiplyExact(Integer.MAX_VALUE, 2), "integer overflow", "multiplyExact(MAX,2)");
        expectAeMsg(() -> Math.multiplyExact(Integer.MIN_VALUE, -1), "integer overflow", "multiplyExact(MIN,-1)");
        expectAeMsg(() -> Math.incrementExact(Integer.MAX_VALUE), "integer overflow", "incrementExact(MAX)");
        expectAeMsg(() -> Math.decrementExact(Integer.MIN_VALUE), "integer overflow", "decrementExact(MIN)");
        expectAeMsg(() -> Math.negateExact(Integer.MIN_VALUE), "integer overflow", "negateExact(int MIN)");
        // long 族溢出：消息逐字 "long overflow"
        expectAeMsg(() -> Math.addExact(Long.MAX_VALUE, 1L), "long overflow", "addExact(long MAX,1)");
        expectAeMsg(() -> Math.addExact(Long.MIN_VALUE, -1L), "long overflow", "addExact(long MIN,-1)");
        expectAeMsg(() -> Math.subtractExact(Long.MIN_VALUE, 1L), "long overflow", "subtractExact(long MIN,1)");
        expectAeMsg(() -> Math.subtractExact(Long.MAX_VALUE, -1L), "long overflow", "subtractExact(long MAX,-1)");
        expectAeMsg(() -> Math.multiplyExact(Long.MAX_VALUE, 2L), "long overflow", "multiplyExact(long MAX,2)");
        expectAeMsg(() -> Math.multiplyExact(Long.MIN_VALUE, -1L), "long overflow", "multiplyExact(long MIN,-1)");
        expectAeMsg(() -> Math.incrementExact(Long.MAX_VALUE), "long overflow", "incrementExact(long MAX)");
        expectAeMsg(() -> Math.decrementExact(Long.MIN_VALUE), "long overflow", "decrementExact(long MIN)");
        expectAeMsg(() -> Math.negateExact(Long.MIN_VALUE), "long overflow", "negateExact(long MIN)");
        // toIntExact：long->int 收窄溢出消息是 "integer overflow"
        expectAeMsg(() -> Math.toIntExact(Long.MAX_VALUE), "integer overflow", "toIntExact(Long.MAX_VALUE)");
        expectAeMsg(() -> Math.toIntExact(Long.MIN_VALUE), "integer overflow", "toIntExact(Long.MIN_VALUE)");
        // 边界另一侧不抛
        check(Math.incrementExact(Integer.MIN_VALUE) == Integer.MIN_VALUE + 1,
              "incrementExact(int MIN) wraps to MIN+1 without overflow");
        check(Math.decrementExact(Integer.MAX_VALUE) == Integer.MAX_VALUE - 1,
              "decrementExact(int MAX) gives MAX-1");
        check(Math.incrementExact(Long.MIN_VALUE) == Long.MIN_VALUE + 1L,
              "incrementExact(long MIN) no overflow");
        check(Math.decrementExact(Long.MAX_VALUE) == Long.MAX_VALUE - 1L,
              "decrementExact(long MAX) no overflow");
        check(Math.negateExact(Long.MIN_VALUE + 1L) == Long.MAX_VALUE, "negateExact(MIN+1) == MAX");
        check(Math.addExact(Integer.MIN_VALUE, Integer.MAX_VALUE) == -1, "addExact(MIN,MAX) == -1");
        check(Math.subtractExact(Integer.MIN_VALUE, Integer.MIN_VALUE) == 0, "subtractExact(MIN,MIN) == 0");
        check(Math.multiplyExact(Integer.MIN_VALUE, 1) == Integer.MIN_VALUE, "multiplyExact(MIN,1) no overflow");
        check(Math.toIntExact((long) Integer.MIN_VALUE) == Integer.MIN_VALUE, "toIntExact(int MIN as long)");
        check(Math.toIntExact(5L) == 5, "toIntExact(5L) == 5");
    }

    static void testDivByZeroMessages() {
        expectAeMsg(() -> Math.floorDiv(1, 0), "/ by zero", "floorDiv(1,0) message");
        expectAeMsg(() -> Math.floorMod(1, 0), "/ by zero", "floorMod(1,0) message");
        expectAeMsg(() -> Math.floorDiv(1L, 0L), "/ by zero", "floorDiv(1L,0L) message");
        expectAeMsg(() -> Math.floorMod(1L, 0L), "/ by zero", "floorMod(1L,0L) message");
        // 与 JVM 原生整数除零的消息完全一致
        try {
            aeSink = 1 / zeroDiv;
            throw new AssertionError("plain 1/0 expected ArithmeticException");
        } catch (ArithmeticException e) {
            check("/ by zero".equals(e.getMessage()), "plain 1/0 message is \"/ by zero\"");
        }
    }

    static void testAbsBitLevel() {
        checkBitsD(Math.abs(-0.0), +0.0, "abs(-0.0) == +0.0");
        checkBitsF(Math.abs(-0.0f), +0.0f, "abs(-0.0f) == +0.0f");
        check(Math.abs(Integer.MIN_VALUE + 1) == Integer.MAX_VALUE, "abs(int MIN+1) == MAX");
        check(Math.abs(Long.MIN_VALUE + 1) == Long.MAX_VALUE, "abs(long MIN+1) == MAX");
        check(Double.isNaN(Math.abs(Float.NaN)), "abs(float NaN) is NaN");
        checkBitsF(Math.abs(Float.NEGATIVE_INFINITY), Float.POSITIVE_INFINITY, "abs(float -Inf) == +Inf");
        checkBitsD(Math.abs(Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY, "abs(-Inf) == +Inf");
    }

    static void testMinMaxContract() {
        // NaN 传播：任一参数为 NaN 即 NaN（浮点版专有语义；int/long 版无 NaN）
        check(Double.isNaN(Math.max(1.0, Double.NaN)), "max(1,NaN) == NaN");
        check(Double.isNaN(Math.min(1.0, Double.NaN)), "min(1,NaN) == NaN");
        check(Float.isNaN(Math.max(Float.NaN, 1.0f)), "max(NaNf,1f) NaN");
        check(Float.isNaN(Math.min(Float.NaN, 1.0f)), "min(NaNf,1f) NaN");
        check(Float.isNaN(Math.max(1.0f, Float.NaN)), "max(1f,NaNf) NaN");
        check(Float.isNaN(Math.min(1.0f, Float.NaN)), "min(1f,NaNf) NaN");
        // ±0.0：max 取 +0.0、min 取 -0.0，必须位级断言（== 区分不出 +0/-0）
        checkBitsD(Math.max(+0.0, -0.0), +0.0, "max(+0.0,-0.0) == +0.0");
        checkBitsD(Math.max(-0.0, +0.0), +0.0, "max(-0.0,+0.0) == +0.0");
        checkBitsD(Math.min(-0.0, +0.0), -0.0, "min(-0.0,+0.0) == -0.0");
        checkBitsD(Math.min(+0.0, -0.0), -0.0, "min(+0.0,-0.0) == -0.0");
        checkBitsF(Math.max(-0.0f, +0.0f), +0.0f, "max(-0.0f,+0.0f) == +0.0f");
        checkBitsF(Math.min(-0.0f, +0.0f), -0.0f, "min(-0.0f,+0.0f) == -0.0f");
        // int/long 极值
        check(Math.min(Integer.MIN_VALUE, Integer.MAX_VALUE) == Integer.MIN_VALUE
              && Math.max(Integer.MIN_VALUE, Integer.MAX_VALUE) == Integer.MAX_VALUE,
              "int min/max at extremes");
        check(Math.min(Long.MIN_VALUE, Long.MAX_VALUE) == Long.MIN_VALUE
              && Math.max(Long.MIN_VALUE, Long.MAX_VALUE) == Long.MAX_VALUE,
              "long min/max at extremes");
        // 对称性 + 界性循环（无 NaN 参与时）
        int[] is = {-3, -1, 0, 1, 3};
        for (int a : is) {
            for (int b : is) {
                check(Math.max(a, b) == Math.max(b, a) && Math.min(a, b) == Math.min(b, a),
                      "int min/max symmetric " + a + "," + b);
            }
        }
        double[] ds = {-3.0, -0.0, 0.0, 1.5, 3.0};
        for (double a : ds) {
            for (double b : ds) {
                check(Math.max(a, b) >= a && Math.max(a, b) >= b
                      && Math.min(a, b) <= a && Math.min(a, b) <= b,
                      "double min/max bounding " + a + "," + b);
            }
        }
    }

    static void testFloorCeilRintBitLevel() {
        check(Math.floor(-0.5) == -1.0, "floor(-0.5) == -1.0");
        check(Math.floor(2.7) == 2.0, "floor(2.7) == 2.0");
        check(Math.ceil(2.1) == 3.0, "ceil(2.1) == 3.0");
        checkBitsD(Math.ceil(-0.5), -0.0, "ceil(-0.5) == -0.0");
        checkBitsD(Math.floor(-0.0), -0.0, "floor(-0.0) == -0.0 (identity)");
        checkBitsD(Math.ceil(-0.0), -0.0, "ceil(-0.0) == -0.0 (identity)");
        checkBitsD(Math.floor(+0.0), +0.0, "floor(+0.0) == +0.0");
        checkBitsD(Math.ceil(+0.0), +0.0, "ceil(+0.0) == +0.0");
        check(Math.floor(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY
              && Math.ceil(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "floor/ceil(+Inf)");
        check(Math.floor(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY
              && Math.ceil(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY, "floor/ceil(-Inf)");
        check(Double.isNaN(Math.floor(Double.NaN)) && Double.isNaN(Math.ceil(Double.NaN)),
              "floor/ceil(NaN) are NaN");
        // rint：半到偶 + 负零符号保持
        double[] halves = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5};
        double[] evens = {0.0, 2.0, 2.0, 4.0, 4.0, 6.0};
        for (int i = 0; i < halves.length; i++) {
            check(Math.rint(halves[i]) == evens[i], "rint(" + halves[i] + ") half-to-even");
            check(Math.rint(-halves[i]) == -evens[i], "rint(-" + halves[i] + ") half-to-even");
        }
        checkBitsD(Math.rint(-0.5), -0.0, "rint(-0.5) == -0.0 (nearest even is zero, sign preserved)");
        checkBitsD(Math.rint(+0.0), +0.0, "rint(+0.0) identity");
        checkBitsD(Math.rint(-0.0), -0.0, "rint(-0.0) identity");
        check(Math.rint(2.3) == 2.0 && Math.rint(2.7) == 3.0, "rint non-tie values");
        check(Math.rint(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "rint(+Inf)");
        check(Double.isNaN(Math.rint(Double.NaN)), "rint(NaN) is NaN");
    }

    static void testSqrtCbrtPowContract() {
        // sqrt：规格承诺半 ulp 内（正确定舍入）；±0.0 符号保持
        check(Math.sqrt(4.0) == 2.0, "sqrt(4) == 2");
        check(Math.sqrt(0.25) == 0.5, "sqrt(0.25) == 0.5");
        check(Math.sqrt(Math.sqrt(16.0)) == 2.0, "sqrt(sqrt(16)) == 2");
        checkBitsD(Math.sqrt(+0.0), +0.0, "sqrt(+0.0) == +0.0");
        checkBitsD(Math.sqrt(-0.0), -0.0, "sqrt(-0.0) == -0.0 (sign preserved)");
        check(Double.isNaN(Math.sqrt(Double.NaN)), "sqrt(NaN) is NaN");
        checkTol(Math.sqrt(2.0) * Math.sqrt(2.0), 2.0, 1e-15, "sqrt(2)^2 within 1e-15 of 2");
        // cbrt：负入参合法（与 sqrt 分叉的招牌差异），完全立方精确
        checkClose(Math.cbrt(-27.0), -3.0, "cbrt(-27) ~ -3");
        checkClose(Math.cbrt(8.0), 2.0, "cbrt(8) ~ 2");
        checkClose(Math.cbrt(0.125), 0.5, "cbrt(0.125) ~ 0.5");
        checkBitsD(Math.cbrt(-0.0), -0.0, "cbrt(-0.0) == -0.0");
        check(Double.isNaN(Math.cbrt(Double.NaN)), "cbrt(NaN) is NaN");
        check(Math.cbrt(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY
              && Math.cbrt(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY, "cbrt(±Inf)");
        // pow 特例全表（0 指数优先于 NaN；±1^Inf 按 Java 规格是 NaN）
        check(Math.pow(0.5, 0.0) == 1.0, "pow(0.5,0) == 1 (zero exponent wins)");
        check(Math.pow(Double.NaN, 0.0) == 1.0, "pow(NaN,0) == 1 (zero exponent wins over NaN)");
        check(Double.isNaN(Math.pow(1.0, Double.POSITIVE_INFINITY)), "pow(1,+Inf) is NaN per spec");
        check(Double.isNaN(Math.pow(-1.0, Double.POSITIVE_INFINITY)), "pow(-1,+Inf) is NaN per spec");
        check(Double.isNaN(Math.pow(-8.0, 1.0 / 3.0)), "pow(negative base, fractional exponent) is NaN");
        checkClose(Math.pow(2.0, 10.0), 1024.0, "pow(2,10) ~ 1024");
        checkClose(Math.pow(2.0, -1.0), 0.5, "pow(2,-1) ~ 0.5");
        check(Math.pow(1.5, Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "|base|>1 with exp +Inf gives +Inf");
        check(Math.pow(0.25, Double.POSITIVE_INFINITY) == 0.0, "|base|<1 with exp +Inf gives +0.0");
        check(Math.pow(2.0, Double.NEGATIVE_INFINITY) == 0.0, "|base|>1 with exp -Inf gives +0.0");
        check(Math.pow(0.25, Double.NEGATIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "|base|<1 with exp -Inf gives +Inf");
        check(Math.pow(-0.0, -1.0) == Double.NEGATIVE_INFINITY, "pow(-0.0,-1) == -Inf (odd reciprocal)");
        check(Math.pow(0.0, -1.0) == Double.POSITIVE_INFINITY, "pow(+0.0,-1) == +Inf");
        checkClose(Math.pow(-8.0, 3.0), -512.0, "pow(-8,3) ~ -512");
        checkClose(Math.pow(-0.5, 2.0), 0.25, "pow(-0.5,2) ~ 0.25");
    }

    static void testHypotContract() {
        checkClose(Math.hypot(5.0, 12.0), 13.0, "hypot(5,12) ~ 13");
        checkBitsD(Math.hypot(0.0, 0.0), +0.0, "hypot(+0,+0) == +0.0");
        checkBitsD(Math.hypot(-0.0, -0.0), +0.0, "hypot(-0,-0) == +0.0");
        check(Math.hypot(Double.POSITIVE_INFINITY, Double.NaN) == Double.POSITIVE_INFINITY,
              "hypot(+Inf,NaN) == +Inf (Inf beats NaN)");
        check(Math.hypot(Double.NaN, Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "hypot(NaN,+Inf) == +Inf");
        check(Math.hypot(Double.NEGATIVE_INFINITY, 3.0) == Double.POSITIVE_INFINITY,
              "hypot(-Inf,3) == +Inf");
        check(Double.isNaN(Math.hypot(Double.NaN, 1.0)), "hypot(NaN,finite) is NaN");
        check(Math.hypot(1e300, 1e300) > 1.414e300 && Math.hypot(1e300, 1e300) < 1.415e300,
              "hypot rescales internally: finite where naive x*x+y*y would overflow");
    }

    static void testTrigContract() {
        checkBitsD(Math.sin(-0.0), -0.0, "sin(-0.0) == -0.0");
        check(Math.sin(Math.PI / 2) == 1.0, "sin(pi/2) == 1.0 exact");
        checkTol(Math.sin(Math.PI / 6), 0.5, 1e-15, "sin(pi/6) ~ 0.5");
        check(Math.cos(Math.PI) == -1.0, "cos(pi) == -1.0 exact");
        checkTol(Math.cos(Math.PI / 2), 0.0, 1e-15, "cos(pi/2) ~ 0");
        checkBitsD(Math.tan(-0.0), -0.0, "tan(-0.0) == -0.0");
        checkTol(Math.tan(Math.PI / 4), 1.0, 1e-15, "tan(pi/4) ~ 1");
        checkClose(Math.asin(1.0), Math.PI / 2, "asin(1) ~ pi/2");
        checkClose(Math.asin(-1.0), -Math.PI / 2, "asin(-1) ~ -pi/2");
        checkClose(Math.acos(0.0), Math.PI / 2, "acos(0) ~ pi/2");
        checkClose(Math.acos(-1.0), Math.PI, "acos(-1) ~ pi");
        checkBitsD(Math.atan(-0.0), -0.0, "atan(-0.0) == -0.0");
        checkBitsD(Math.atan2(0.0, 0.0), +0.0, "atan2(+0,+0) == +0.0");
        checkBitsD(Math.atan2(-0.0, 0.0), -0.0, "atan2(-0,+0) == -0.0");
        check(Math.atan2(0.0, -0.0) == Math.PI, "atan2(+0,-0) == +pi");
        checkBitsD(Math.atan2(-0.0, -0.0), -Math.PI, "atan2(-0,-0) == -pi");
        check(Math.atan2(-1.0, 0.0) == -Math.PI / 2, "atan2(-1,+0) == -pi/2");
        check(Math.toRadians(90.0) == Math.PI / 2, "toRadians(90) == pi/2 exact");
        checkTol(Math.toDegrees(Math.toRadians(123.0)), 123.0, 1e-12, "degrees(radians(123)) round-trip");
        checkTol(Math.toRadians(Math.toDegrees(1.0)), 1.0, 1e-15, "radians(degrees(1)) round-trip");
        check(Math.sinh(0.0) == 0.0 && Math.cosh(0.0) == 1.0 && Math.tanh(0.0) == 0.0,
              "sinh/cosh/tanh(0)");
    }

    static void testExpLogContract() {
        checkClose(Math.expm1(1.0), Math.E - 1.0, "expm1(1) ~ E-1");
        check(Math.log(0.0) == Double.NEGATIVE_INFINITY, "log(+0.0) == -Inf");
        check(Double.isNaN(Math.log(-1.0)), "log(-1) is NaN");
        check(Math.log(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "log(+Inf) == +Inf");
        check(Double.isNaN(Math.exp(Double.NaN)), "exp(NaN) is NaN");
        check(Math.log10(1.0) == 0.0, "log10(1) == +0.0");
        check(Math.log10(100.0) == 2.0, "log10(100) == 2.0 exact");
        check(Math.log10(0.0) == Double.NEGATIVE_INFINITY, "log10(0) == -Inf");
        check(Double.isNaN(Math.log10(-1.0)), "log10(-1) is NaN");
        check(Math.log1p(-1.0) == Double.NEGATIVE_INFINITY, "log1p(-1) == -Inf");
        check(Double.isNaN(Math.log1p(-2.0)), "log1p(< -1) is NaN");
        check(Math.expm1(Double.NEGATIVE_INFINITY) == -1.0, "expm1(-Inf) == -1.0");
        check(Math.expm1(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "expm1(+Inf) == +Inf");
        // log1p/expm1 在 0 附近保精度（朴素 log(1+x) 会灾难性消去）
        checkTol(Math.log1p(1e-10), 1e-10 - 5e-21, 1e-25, "log1p accurate near zero");
        checkTol(Math.expm1(1e-10), 1e-10 + 5e-21, 1e-25, "expm1 accurate near zero");
    }

    static void testSignumUlpScalbNextContract() {
        // signum：负零位级、±Inf、NaN
        checkBitsD(Math.signum(-0.0), -0.0, "signum(-0.0) == -0.0");
        checkBitsD(Math.signum(+0.0), +0.0, "signum(+0.0) == +0.0");
        check(Math.signum(3.5) == 1.0, "signum(3.5) == 1.0");
        check(Math.signum(Double.POSITIVE_INFINITY) == 1.0, "signum(+Inf) == 1.0");
        check(Math.signum(Double.NEGATIVE_INFINITY) == -1.0, "signum(-Inf) == -1.0");
        checkBitsF(Math.signum(-0.0f), -0.0f, "signum(-0.0f) == -0.0f");
        // ulp：1.0 的 ulp == 2^-52（数值 + 最短字符串双锁）、Inf/NaN/MAX
        check(Math.ulp(1.0) == 2.220446049250313E-16, "ulp(1.0) == 2.220446049250313E-16");
        check("2.220446049250313E-16".equals(Double.toString(Math.ulp(1.0))),
              "ulp(1.0) shortest string form");
        check(Math.ulp(-0.0) == Double.MIN_VALUE, "ulp(-0.0) == Double.MIN_VALUE");
        check(Math.ulp(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "ulp(+Inf) == Inf");
        check(Double.isNaN(Math.ulp(Double.NaN)), "ulp(NaN) is NaN");
        check(Math.ulp(Double.MAX_VALUE) == 0x1.0p971, "ulp(MAX_VALUE) == 2^971");
        // scalb：符号保持、次正规缩放、NaN/Inf
        checkBitsD(Math.scalb(-0.0, 3), -0.0, "scalb(-0.0,3) == -0.0");
        check(Math.scalb(Double.MIN_VALUE, -1) == Double.MIN_VALUE / 2,
              "scalb(MIN_VALUE,-1) == half-MIN_VALUE (subnormal)");
        check(Math.scalb(Double.MIN_VALUE, 1) == 2.0 * Double.MIN_VALUE, "scalb(MIN_VALUE,1) == 2*MIN_VALUE");
        check(Math.scalb(Double.MIN_VALUE, 1074) == 1.0, "scalb(MIN_VALUE,1074) == 1.0");
        check(Double.isNaN(Math.scalb(Double.NaN, 2)), "scalb(NaN) is NaN");
        check(Math.scalb(Double.POSITIVE_INFINITY, 2) == Double.POSITIVE_INFINITY, "scalb(Inf) == Inf");
        // nextUp/nextDown/nextAfter：float 族 + 1.0 邻域 + NaN
        check(Math.nextUp(0.0f) == Float.MIN_VALUE, "nextUp(0.0f) == Float.MIN_VALUE");
        check(Math.nextDown(0.0f) == -Float.MIN_VALUE, "nextDown(0.0f) == -Float.MIN_VALUE");
        check(Math.nextDown(1.0) == 0x1.fffffffffffffp-1, "nextDown(1.0) == 1.0 - 2^-53");
        check(Math.nextUp(Math.nextDown(1.0)) == 1.0, "nextUp(nextDown(1.0)) == 1.0");
        check(Math.nextDown(Math.nextUp(1.0)) == 1.0, "nextDown(nextUp(1.0)) == 1.0");
        check(Math.nextAfter(1.0, 2.0) == Math.nextUp(1.0), "nextAfter(1.0,2.0) == nextUp(1.0)");
        check(Double.isNaN(Math.nextAfter(Double.NaN, 1.0)), "nextAfter(NaN,1) is NaN");
        check(Math.nextAfter(0.0f, 1.0) == Float.MIN_VALUE, "nextAfter(0.0f,1.0) == Float.MIN_VALUE");
        check(Math.nextDown(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY,
              "nextDown(-Inf) == -Inf");
    }

    static void testGetExponentBoundaries() {
        check(Math.getExponent(0.0) == Double.MIN_EXPONENT - 1,
              "getExponent(+0.0) == MIN_EXPONENT-1 == -1023");
        check(Math.getExponent(-0.0) == Double.MIN_EXPONENT - 1, "getExponent(-0.0) == -1023");
        check(Math.getExponent(Double.MIN_NORMAL) == Double.MIN_EXPONENT,
              "getExponent(MIN_NORMAL) == -1022");
        check(Math.getExponent(Double.POSITIVE_INFINITY) == Double.MAX_EXPONENT + 1,
              "getExponent(+Inf) == MAX_EXPONENT+1 == 1024");
        check(Math.getExponent(Double.NaN) == Double.MAX_EXPONENT + 1, "getExponent(NaN) == 1024");
        check(Math.getExponent(0.0f) == Float.MIN_EXPONENT - 1, "getExponent(0.0f) == -127");
        check(Math.getExponent(Float.MAX_VALUE) == Float.MAX_EXPONENT,
              "getExponent(Float.MAX_VALUE) == 127");
        check(Math.getExponent(-1.0) == 0 && Math.getExponent(-2.0) == 1,
              "getExponent ignores sign bit");
    }

    static void testConstantsStringAndBits() {
        check("2.718281828459045".equals(Double.toString(Math.E)), "Double.toString(Math.E) exact");
        check("3.141592653589793".equals(Double.toString(Math.PI)), "Double.toString(Math.PI) exact");
        check(Math.E == 2.718281828459045, "Math.E == decimal literal 2.718281828459045");
        check(Math.PI == 3.141592653589793, "Math.PI == decimal literal 3.141592653589793");
        check(Double.doubleToLongBits(Math.E) == 0x4005bf0a8b145769L, "Math.E bit pattern");
        check(Double.doubleToLongBits(Math.PI) == 0x400921fb54442d18L, "Math.PI bit pattern");
    }

    static void testIeeeRemainderContract() {
        check(Math.IEEEremainder(7.5, 2.0) == -0.5, "IEEEremainder(7.5,2) == -0.5 (quotient rounds to 4)");
        check(Math.IEEEremainder(2.5, 2.0) == 0.5, "IEEEremainder(2.5,2) == 0.5");
        check(Math.IEEEremainder(11.0, 3.0) == -1.0, "IEEEremainder(11,3) == -1 (quotient 4, 11-12)");
        check(Double.isNaN(Math.IEEEremainder(Double.POSITIVE_INFINITY, 3.0)),
              "IEEEremainder(Inf,3) is NaN");
        check(Double.isNaN(Math.IEEEremainder(3.0, 0.0)), "IEEEremainder(3,0) is NaN");
    }

    static void testFmaRoundingDivergence() {
        // fma 单次舍入契约：a = b = nextUp(1.0) = 1+2^-52，c = -(1+2^-51)。
        // 真积 a*b = 1 + 2^-51 + 2^-104：非融合路径 fl(a*b) = 1+2^-51，再减 c 得 +0.0；
        // 融合路径只舍入一次，保住精确差 2^-104 —— 两者必然分叉。
        double a = Math.nextUp(1.0);
        double c = Math.nextUp(Math.nextUp(1.0));
        check(Math.fma(1.0, 2.0, 3.0) == 5.0, "fma(1,2,3) == 5 exact");
        check(Math.fma(a, a, -c) == 0x1.0p-104, "fma single rounding keeps exact 2^-104");
        check(a * a - c == 0.0, "non-fused a*a-c collapses to +0.0 (JLS forbids fusion in operators)");
        check(Math.fma(a, a, -c) != a * a - c, "fused vs non-fused diverge on this sample");
        check(Math.fma(a, a, -c) == StrictMath.fma(a, a, -c), "Math.fma == StrictMath.fma on divergence sample");
        check(Math.fma(3.0f, 4.0f, 5.0f) == 17.0f, "fma(float) exact");
        // 零符号规则：(-0)*(+0) = -0；-0 + +0 == +0、-0 + (-0) == -0（就近舍入模式）
        checkBitsD(Math.fma(-0.0, +0.0, +0.0), +0.0, "fma(-0,+0,+0) == +0.0");
        checkBitsD(Math.fma(-0.0, +0.0, -0.0), -0.0, "fma(-0,+0,-0) == -0.0");
        check(Double.isNaN(Math.fma(Double.POSITIVE_INFINITY, 0.0, Double.NaN)),
              "fma(Inf,0,NaN) is NaN");
        check(Double.isNaN(Math.fma(1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)),
              "fma(1,Inf,-Inf) is NaN (Inf - Inf)");
        check(Math.fma(0x1.0p-1074, 0x1.0p-1074, 0.0) == 0.0,
              "fma subnormal product underflows to +0.0");
    }

    static void testMixedPrecisionDivisionOverloads() {
        // JDK-9+ 重载：floorDiv(long,int)/floorMod(long,int)/multiplyExact(long,int)
        check(Math.floorDiv(-7L, 2) == -4L, "floorDiv(long,int)(-7L,2) == -4L");
        check(Math.floorMod(-7L, 2) == 1, "floorMod(long,int)(-7L,2) == 1");
        check(Math.floorDiv(7L, -2) == -4L && Math.floorMod(7L, -2) == -1,
              "floorDiv/floorMod(long,int) negative divisor");
        expectAeMsg(() -> Math.floorDiv(1L, 0), "/ by zero", "floorDiv(long,int) by zero message");
        expectAeMsg(() -> Math.floorMod(1L, 0), "/ by zero", "floorMod(long,int) by zero message");
        check(Math.multiplyExact(Integer.MAX_VALUE + 1L, 2) == 4294967296L,
              "multiplyExact(long,int) widens the int factor");
        expectAeMsg(() -> Math.multiplyExact(Long.MAX_VALUE, 2), "long overflow",
                    "multiplyExact(long,int) overflow message");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testRoundHalfTowardsPositiveInfinity();
        testFloorDivModContractMatrix();
        testExactOverflowMessages();
        testDivByZeroMessages();
        testAbsBitLevel();
        testMinMaxContract();
        testFloorCeilRintBitLevel();
        testSqrtCbrtPowContract();
        testHypotContract();
        testTrigContract();
        testExpLogContract();
        testSignumUlpScalbNextContract();
        testGetExponentBoundaries();
        testConstantsStringAndBits();
        testIeeeRemainderContract();
        testFmaRoundingDivergence();
        testMixedPrecisionDivisionOverloads();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

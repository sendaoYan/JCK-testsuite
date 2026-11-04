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
        double scale = Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
        if (Math.abs(a - b) <= 1e-14 * scale) {
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
        check(Math.pow(2.0, 3.0) == 8.0, "pow(2,3)");
        check(Math.pow(2.0, 0.0) == 1.0, "pow(2,0)");
        check(Math.pow(0.0, 0.0) == 1.0, "pow(0,0) == 1");
        check(Math.pow(-2.0, 3.0) == -8.0, "pow(-2,3)");
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
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

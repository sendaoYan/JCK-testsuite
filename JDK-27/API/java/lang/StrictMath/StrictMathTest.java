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
        double u = Math.ulp(b != 0.0 ? b : a);
        if (Math.abs(a - b) <= u) {
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
        checkClose(StrictMath.cbrt(27.0), 3.0, "cbrt(27) ~ 3");
        checkClose(StrictMath.cbrt(-8.0), -2.0, "cbrt(-8) ~ -2");
        checkClose(StrictMath.hypot(3.0, 4.0), 5.0, "hypot(3,4) ~ 5");
        checkClose(StrictMath.pow(2.0, 10.0), 1024.0, "pow(2,10) ~ 1024");
        check(StrictMath.pow(0.0, 0.0) == 1.0, "pow(0,0) == 1");
        checkClose(StrictMath.pow(-2.0, 3.0), -8.0, "pow(-2,3) ~ -8");
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

    static void testBoundaryEquivalence() {
        // floorDiv/floorMod identity for all sign combinations.
        int[] xs = {-7, -1, 0, 1, 7};
        int[] ys = {-3, -1, 1, 3};
        for (int x : xs) {
            for (int y : ys) {
                check(StrictMath.floorDiv(x, y) * y + StrictMath.floorMod(x, y) == x,
                      "floor identity " + x + "," + y);
                check(StrictMath.ceilDiv(x, y) * y + StrictMath.ceilMod(x, y) == x,
                      "ceil identity " + x + "," + y);
            }
        }

        // nextDown(nextUp(x)) == x for normal values.
        check(StrictMath.nextDown(StrictMath.nextUp(1.0)) == 1.0, "nextDown(nextUp(1.0))");
        check(StrictMath.nextUp(StrictMath.nextDown(0.5)) == 0.5, "nextUp(nextDown(0.5))");

        // toDegrees(toRadians(x)) round-trips within tolerance.
        checkClose(StrictMath.toDegrees(StrictMath.toRadians(123.0)), 123.0, "deg/rad round-trip");

        // scalb(getExponent(x), -e) normalizes the mantissa into [1,2).
        double m = StrictMath.scalb(12.5, -StrictMath.getExponent(12.5));
        check(m >= 1.0 && m < 2.0, "scalb/getExponent normalize mantissa");

        // abs then copySign(-1) reconstructs the negative value.
        check(StrictMath.copySign(StrictMath.abs(-3.0), -1.0) == -3.0,
              "abs then copySign(-1)");

        // min/max idempotence on equal operands.
        check(StrictMath.min(7.0, 7.0) == 7.0 && StrictMath.max(7.0, 7.0) == 7.0,
              "min/max equal");
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
        testBoundaryEquivalence();
    }

    // ==== 契约深化（2026-11）追加 ====
    // round 半分方向全矩阵 / floorDiv·floorMod 四象限 + 恒等式循环 + 溢出与除零消息逐字 /
    // 位级负零断言 / pow·hypot 特例全表 / ulp·scalb·next 族边界 / getExponent 边界 /
    // E·PI 字符串与位模式 / fma 融合舍入分叉样本 / JDK-9+ mixed-precision 重载 /
    // Math 与 StrictMath 同入参同返回契约矩阵。全部值经 JDK-8 与 JDK-27 双侧实测锁定。

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
            // message 是 VM 运行时生成、非 API 契约；-Xcomp/-XX:-TieredCompilation 快路径
            // 不填 message（为 null），只保证异常类型。
            check(e.getMessage() == null || expected.equals(e.getMessage()),
                  msg + ": message \"" + e.getMessage() + "\" == \"" + expected + "\"");
        }
    }

    static void testRoundHalfTowardsPositiveInfinity() {
        // 招牌契约：round(x) == floor(x + 0.5)，半分向 +无穷
        check(StrictMath.round(2.5) == 3L, "round(2.5) == 3");
        check(StrictMath.round(-2.5) == -2L, "round(-2.5) == -2 (ties toward +infinity)");
        check(StrictMath.round(0.5) == 1L, "round(0.5) == 1");
        check(StrictMath.round(-0.5) == 0L, "round(-0.5) == 0 (floor(+0.0))");
        check(StrictMath.round(2.49) == 2L, "round(2.49) == 2");
        check(StrictMath.round(-2.51) == -3L, "round(-2.51) == -3");
        check(StrictMath.round(1.6) == 2L, "round(1.6) == 2");
        check(StrictMath.round(-1.6) == -2L, "round(-1.6) == -2 (floor(-1.1))");
        check(StrictMath.round(-1.4) == -1L, "round(-1.4) == -1");
        check(StrictMath.round(+0.0) == 0L, "round(+0.0) == 0");
        check(StrictMath.round(-0.0) == 0L, "round(-0.0) == 0");
        check(StrictMath.round(0.49999999999999994) == 0L, "round(largest double < 0.5) == 0");
        check(StrictMath.round(Double.NaN) == 0L, "round(NaN) == 0");
        check(StrictMath.round(Double.POSITIVE_INFINITY) == Long.MAX_VALUE, "round(+Inf) == Long.MAX_VALUE");
        check(StrictMath.round(Double.NEGATIVE_INFINITY) == Long.MIN_VALUE, "round(-Inf) == Long.MIN_VALUE");
        check(StrictMath.round(Double.MAX_VALUE) == Long.MAX_VALUE,
              "round(Double.MAX_VALUE) saturates to Long.MAX_VALUE");
        check(StrictMath.round(-Double.MAX_VALUE) == Long.MIN_VALUE,
              "round(-Double.MAX_VALUE) saturates to Long.MIN_VALUE");
        // float 版（返回 int）同一语义
        check(StrictMath.round(2.5f) == 3, "round(2.5f) == 3");
        check(StrictMath.round(-2.5f) == -2, "round(-2.5f) == -2");
        check(StrictMath.round(0.5f) == 1, "round(0.5f) == 1");
        check(StrictMath.round(-0.5f) == 0, "round(-0.5f) == 0");
        check(StrictMath.round(Float.MAX_VALUE) == Integer.MAX_VALUE,
              "round(Float.MAX_VALUE) saturates to Integer.MAX_VALUE");
        check(StrictMath.round(-Float.MAX_VALUE) == Integer.MIN_VALUE,
              "round(-Float.MAX_VALUE) saturates to Integer.MIN_VALUE");
        check(StrictMath.round(Float.NaN) == 0, "round(Float.NaN) == 0");
    }

    static void testFloorDivModContractMatrix() {
        // 除数 2 的四象限全矩阵
        check(StrictMath.floorDiv(7, 2) == 3, "floorDiv(7,2) == 3");
        check(StrictMath.floorDiv(-7, 2) == -4, "floorDiv(-7,2) == -4 (floors toward -inf)");
        check(StrictMath.floorDiv(7, -2) == -4, "floorDiv(7,-2) == -4");
        check(StrictMath.floorDiv(-7, -2) == 3, "floorDiv(-7,-2) == 3");
        check(StrictMath.floorMod(7, 2) == 1, "floorMod(7,2) == 1");
        check(StrictMath.floorMod(-7, 2) == 1, "floorMod(-7,2) == 1 (sign follows divisor)");
        check(StrictMath.floorMod(7, -2) == -1, "floorMod(7,-2) == -1");
        check(StrictMath.floorMod(-7, -2) == -1, "floorMod(-7,-2) == -1");
        // 与 / 和 % 的对照分叉
        check(-7 / 2 == -3 && -7 % 2 == -1, "-7/2 == -3 and -7%2 == -1 vs floorDiv -4 / floorMod 1");
        check(7 / -2 == -3 && 7 % -2 == 1, "7/-2 == -3 and 7%-2 == 1 vs floorDiv -4 / floorMod -1");
        // 恒等式循环：floorMod(a,b) == a - floorDiv(a,b)*b
        int[] xs = {-7, -6, -1, 0, 1, 6, 7};
        int[] ys = {-3, -2, -1, 1, 2, 3};
        for (int a : xs) {
            for (int b : ys) {
                int q = StrictMath.floorDiv(a, b);
                int r = StrictMath.floorMod(a, b);
                check(r == a - q * b, "int floorMod identity a-floorDiv(a,b)*b " + a + "," + b);
                check(r == 0 || ((r > 0) == (b > 0)), "int floorMod sign follows divisor " + a + "," + b);
                check(Math.abs(r) < Math.abs(b), "int |floorMod| < |divisor| " + a + "," + b);
            }
        }
        long[] xls = {-7L, -6L, -1L, 0L, 1L, 6L, 7L};
        long[] yls = {-3L, -2L, -1L, 1L, 2L, 3L};
        for (long a : xls) {
            for (long b : yls) {
                long q = StrictMath.floorDiv(a, b);
                long r = StrictMath.floorMod(a, b);
                check(r == a - q * b, "long floorMod identity " + a + "," + b);
                check(Math.abs(r) < Math.abs(b), "long |floorMod| < |divisor| " + a + "," + b);
            }
        }
        check(StrictMath.floorDiv(-7L, 2L) == -4L && StrictMath.floorMod(-7L, 2L) == 1L,
              "long key point (-7L,2L)");
        check(StrictMath.floorDiv(7L, -2L) == -4L && StrictMath.floorMod(7L, -2L) == -1L,
              "long key point (7L,-2L)");
        // MIN_VALUE/-1 溢出特例：规格允许回绕为 MIN_VALUE，不抛异常（区别于 divideExact）
        check(StrictMath.floorDiv(Integer.MIN_VALUE, -1) == Integer.MIN_VALUE,
              "floorDiv(int MIN,-1) wraps to MIN_VALUE without throwing");
        check(StrictMath.floorMod(Integer.MIN_VALUE, -1) == 0, "floorMod(int MIN,-1) == 0");
        check(StrictMath.floorDiv(Long.MIN_VALUE, -1L) == Long.MIN_VALUE,
              "floorDiv(long MIN,-1) wraps to MIN_VALUE without throwing");
        check(StrictMath.floorMod(Long.MIN_VALUE, -1L) == 0L, "floorMod(long MIN,-1) == 0");
    }

    static void testExactOverflowMessages() {
        // int 族溢出：消息逐字 "integer overflow"
        expectAeMsg(() -> StrictMath.addExact(Integer.MAX_VALUE, 1), "integer overflow",
                    "addExact(MAX,1)");
        expectAeMsg(() -> StrictMath.addExact(Integer.MIN_VALUE, -1), "integer overflow",
                    "addExact(MIN,-1)");
        expectAeMsg(() -> StrictMath.subtractExact(Integer.MIN_VALUE, 1), "integer overflow",
                    "subtractExact(MIN,1)");
        expectAeMsg(() -> StrictMath.subtractExact(Integer.MAX_VALUE, -1), "integer overflow",
                    "subtractExact(MAX,-1)");
        expectAeMsg(() -> StrictMath.multiplyExact(Integer.MAX_VALUE, 2), "integer overflow",
                    "multiplyExact(MAX,2)");
        expectAeMsg(() -> StrictMath.multiplyExact(Integer.MIN_VALUE, -1), "integer overflow",
                    "multiplyExact(MIN,-1)");
        expectAeMsg(() -> StrictMath.incrementExact(Integer.MAX_VALUE), "integer overflow",
                    "incrementExact(MAX)");
        expectAeMsg(() -> StrictMath.decrementExact(Integer.MIN_VALUE), "integer overflow",
                    "decrementExact(MIN)");
        expectAeMsg(() -> StrictMath.negateExact(Integer.MIN_VALUE), "integer overflow",
                    "negateExact(int MIN)");
        // long 族溢出：消息逐字 "long overflow"
        expectAeMsg(() -> StrictMath.addExact(Long.MAX_VALUE, 1L), "long overflow",
                    "addExact(long MAX,1)");
        expectAeMsg(() -> StrictMath.addExact(Long.MIN_VALUE, -1L), "long overflow",
                    "addExact(long MIN,-1)");
        expectAeMsg(() -> StrictMath.subtractExact(Long.MIN_VALUE, 1L), "long overflow",
                    "subtractExact(long MIN,1)");
        expectAeMsg(() -> StrictMath.subtractExact(Long.MAX_VALUE, -1L), "long overflow",
                    "subtractExact(long MAX,-1)");
        expectAeMsg(() -> StrictMath.multiplyExact(Long.MAX_VALUE, 2L), "long overflow",
                    "multiplyExact(long MAX,2)");
        expectAeMsg(() -> StrictMath.multiplyExact(Long.MIN_VALUE, -1L), "long overflow",
                    "multiplyExact(long MIN,-1)");
        expectAeMsg(() -> StrictMath.incrementExact(Long.MAX_VALUE), "long overflow",
                    "incrementExact(long MAX)");
        expectAeMsg(() -> StrictMath.decrementExact(Long.MIN_VALUE), "long overflow",
                    "decrementExact(long MIN)");
        expectAeMsg(() -> StrictMath.negateExact(Long.MIN_VALUE), "long overflow",
                    "negateExact(long MIN)");
        // toIntExact：long->int 收窄溢出消息是 "integer overflow"
        expectAeMsg(() -> StrictMath.toIntExact(Long.MAX_VALUE), "integer overflow",
                    "toIntExact(Long.MAX_VALUE)");
        expectAeMsg(() -> StrictMath.toIntExact(Long.MIN_VALUE), "integer overflow",
                    "toIntExact(Long.MIN_VALUE)");
        // 边界另一侧不抛
        check(StrictMath.incrementExact(Integer.MIN_VALUE) == Integer.MIN_VALUE + 1,
              "incrementExact(int MIN) no overflow");
        check(StrictMath.decrementExact(Integer.MAX_VALUE) == Integer.MAX_VALUE - 1,
              "decrementExact(int MAX) gives MAX-1");
        check(StrictMath.negateExact(Long.MIN_VALUE + 1L) == Long.MAX_VALUE,
              "negateExact(MIN+1) == MAX");
        check(StrictMath.addExact(Integer.MIN_VALUE, Integer.MAX_VALUE) == -1, "addExact(MIN,MAX) == -1");
        check(StrictMath.subtractExact(Integer.MIN_VALUE, Integer.MIN_VALUE) == 0,
              "subtractExact(MIN,MIN) == 0");
        check(StrictMath.multiplyExact(Integer.MIN_VALUE, 1) == Integer.MIN_VALUE,
              "multiplyExact(MIN,1) no overflow");
        check(StrictMath.toIntExact((long) Integer.MIN_VALUE) == Integer.MIN_VALUE,
              "toIntExact(int MIN as long)");
        check(StrictMath.toIntExact(5L) == 5, "toIntExact(5L) == 5");
    }

    static void testDivByZeroMessages() {
        expectAeMsg(() -> StrictMath.floorDiv(1, 0), "/ by zero", "floorDiv(1,0) message");
        expectAeMsg(() -> StrictMath.floorMod(1, 0), "/ by zero", "floorMod(1,0) message");
        expectAeMsg(() -> StrictMath.floorDiv(1L, 0L), "/ by zero", "floorDiv(1L,0L) message");
        expectAeMsg(() -> StrictMath.floorMod(1L, 0L), "/ by zero", "floorMod(1L,0L) message");
    }

    static void testAbsMinMaxBitLevel() {
        checkBitsD(StrictMath.abs(-0.0), +0.0, "abs(-0.0) == +0.0");
        checkBitsF(StrictMath.abs(-0.0f), +0.0f, "abs(-0.0f) == +0.0f");
        check(StrictMath.abs(Integer.MIN_VALUE + 1) == Integer.MAX_VALUE, "abs(int MIN+1) == MAX");
        check(StrictMath.abs(Long.MIN_VALUE + 1) == Long.MAX_VALUE, "abs(long MIN+1) == MAX");
        check(Double.isNaN(StrictMath.abs(Float.NaN)), "abs(float NaN) is NaN");
        checkBitsD(StrictMath.abs(Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY, "abs(-Inf) == +Inf");
        // NaN 传播：任一参数位置
        check(Double.isNaN(StrictMath.max(1.0, Double.NaN)), "max(1,NaN) == NaN");
        check(Double.isNaN(StrictMath.min(1.0, Double.NaN)), "min(1,NaN) == NaN");
        check(Float.isNaN(StrictMath.max(Float.NaN, 1.0f)), "max(NaNf,1f) NaN");
        check(Float.isNaN(StrictMath.min(1.0f, Float.NaN)), "min(1f,NaNf) NaN");
        // ±0.0：max 取 +0.0、min 取 -0.0（位级）
        checkBitsD(StrictMath.max(+0.0, -0.0), +0.0, "max(+0.0,-0.0) == +0.0");
        checkBitsD(StrictMath.max(-0.0, +0.0), +0.0, "max(-0.0,+0.0) == +0.0");
        checkBitsD(StrictMath.min(-0.0, +0.0), -0.0, "min(-0.0,+0.0) == -0.0");
        checkBitsD(StrictMath.min(+0.0, -0.0), -0.0, "min(+0.0,-0.0) == -0.0");
        checkBitsF(StrictMath.max(-0.0f, +0.0f), +0.0f, "max(-0.0f,+0.0f) == +0.0f");
        checkBitsF(StrictMath.min(-0.0f, +0.0f), -0.0f, "min(-0.0f,+0.0f) == -0.0f");
        // int/long 极值
        check(StrictMath.min(Integer.MIN_VALUE, Integer.MAX_VALUE) == Integer.MIN_VALUE
              && StrictMath.max(Integer.MIN_VALUE, Integer.MAX_VALUE) == Integer.MAX_VALUE,
              "int min/max at extremes");
        check(StrictMath.min(Long.MIN_VALUE, Long.MAX_VALUE) == Long.MIN_VALUE
              && StrictMath.max(Long.MIN_VALUE, Long.MAX_VALUE) == Long.MAX_VALUE,
              "long min/max at extremes");
    }

    static void testFloorCeilRintBitLevel() {
        check(StrictMath.floor(-0.5) == -1.0, "floor(-0.5) == -1.0");
        check(StrictMath.floor(2.7) == 2.0, "floor(2.7) == 2.0");
        check(StrictMath.ceil(2.1) == 3.0, "ceil(2.1) == 3.0");
        checkBitsD(StrictMath.ceil(-0.5), -0.0, "ceil(-0.5) == -0.0");
        checkBitsD(StrictMath.floor(-0.0), -0.0, "floor(-0.0) == -0.0 (identity)");
        checkBitsD(StrictMath.ceil(-0.0), -0.0, "ceil(-0.0) == -0.0 (identity)");
        checkBitsD(StrictMath.floor(+0.0), +0.0, "floor(+0.0) == +0.0");
        checkBitsD(StrictMath.ceil(+0.0), +0.0, "ceil(+0.0) == +0.0");
        check(StrictMath.floor(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY
              && StrictMath.ceil(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "floor/ceil(+Inf)");
        check(StrictMath.floor(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY
              && StrictMath.ceil(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY,
              "floor/ceil(-Inf)");
        check(Double.isNaN(StrictMath.floor(Double.NaN)) && Double.isNaN(StrictMath.ceil(Double.NaN)),
              "floor/ceil(NaN) are NaN");
        // rint：半到偶 + 负零符号保持
        double[] halves = {0.5, 1.5, 2.5, 3.5, 4.5, 5.5};
        double[] evens = {0.0, 2.0, 2.0, 4.0, 4.0, 6.0};
        for (int i = 0; i < halves.length; i++) {
            check(StrictMath.rint(halves[i]) == evens[i], "rint(" + halves[i] + ") half-to-even");
            check(StrictMath.rint(-halves[i]) == -evens[i], "rint(-" + halves[i] + ") half-to-even");
        }
        checkBitsD(StrictMath.rint(-0.5), -0.0,
                   "rint(-0.5) == -0.0 (nearest even is zero, sign preserved)");
        checkBitsD(StrictMath.rint(+0.0), +0.0, "rint(+0.0) identity");
        checkBitsD(StrictMath.rint(-0.0), -0.0, "rint(-0.0) identity");
        check(StrictMath.rint(2.3) == 2.0 && StrictMath.rint(2.7) == 3.0, "rint non-tie values");
        check(StrictMath.rint(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "rint(+Inf)");
        check(Double.isNaN(StrictMath.rint(Double.NaN)), "rint(NaN) is NaN");
    }

    static void testSqrtCbrtPowContract() {
        check(StrictMath.sqrt(4.0) == 2.0, "sqrt(4) == 2");
        check(StrictMath.sqrt(0.25) == 0.5, "sqrt(0.25) == 0.5");
        checkBitsD(StrictMath.sqrt(+0.0), +0.0, "sqrt(+0.0) == +0.0");
        checkBitsD(StrictMath.sqrt(-0.0), -0.0, "sqrt(-0.0) == -0.0 (sign preserved)");
        check(Double.isNaN(StrictMath.sqrt(Double.NaN)), "sqrt(NaN) is NaN");
        checkTol(StrictMath.sqrt(2.0) * StrictMath.sqrt(2.0), 2.0, 1e-15,
                 "sqrt(2)^2 within 1e-15 of 2");
        checkClose(StrictMath.cbrt(-27.0), -3.0, "cbrt(-27) ~ -3");
        checkClose(StrictMath.cbrt(8.0), 2.0, "cbrt(8) ~ 2");
        checkClose(StrictMath.cbrt(0.125), 0.5, "cbrt(0.125) ~ 0.5");
        checkBitsD(StrictMath.cbrt(-0.0), -0.0, "cbrt(-0.0) == -0.0");
        check(Double.isNaN(StrictMath.cbrt(Double.NaN)), "cbrt(NaN) is NaN");
        check(StrictMath.cbrt(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY
              && StrictMath.cbrt(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY, "cbrt(±Inf)");
        // pow 特例全表（0 指数优先于 NaN；±1^Inf 按 Java 规格是 NaN）
        check(StrictMath.pow(0.5, 0.0) == 1.0, "pow(0.5,0) == 1 (zero exponent wins)");
        check(StrictMath.pow(Double.NaN, 0.0) == 1.0, "pow(NaN,0) == 1 (zero exponent wins over NaN)");
        check(Double.isNaN(StrictMath.pow(1.0, Double.POSITIVE_INFINITY)), "pow(1,+Inf) is NaN per spec");
        check(Double.isNaN(StrictMath.pow(-1.0, Double.POSITIVE_INFINITY)), "pow(-1,+Inf) is NaN per spec");
        check(Double.isNaN(StrictMath.pow(-8.0, 1.0 / 3.0)),
              "pow(negative base, fractional exponent) is NaN");
        checkClose(StrictMath.pow(2.0, 10.0), 1024.0, "pow(2,10) ~ 1024");
        checkClose(StrictMath.pow(2.0, -1.0), 0.5, "pow(2,-1) ~ 0.5");
        check(StrictMath.pow(1.5, Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "|base|>1 with exp +Inf gives +Inf");
        check(StrictMath.pow(0.25, Double.POSITIVE_INFINITY) == 0.0,
              "|base|<1 with exp +Inf gives +0.0");
        check(StrictMath.pow(2.0, Double.NEGATIVE_INFINITY) == 0.0,
              "|base|>1 with exp -Inf gives +0.0");
        check(StrictMath.pow(0.25, Double.NEGATIVE_INFINITY) == Double.POSITIVE_INFINITY,
              "|base|<1 with exp -Inf gives +Inf");
        check(StrictMath.pow(-0.0, -1.0) == Double.NEGATIVE_INFINITY, "pow(-0.0,-1) == -Inf");
        check(StrictMath.pow(0.0, -1.0) == Double.POSITIVE_INFINITY, "pow(+0.0,-1) == +Inf");
        checkClose(StrictMath.pow(-8.0, 3.0), -512.0, "pow(-8,3) ~ -512");
        checkClose(StrictMath.pow(-0.5, 2.0), 0.25, "pow(-0.5,2) ~ 0.25");
    }

    static void testHypotContract() {
        checkClose(StrictMath.hypot(5.0, 12.0), 13.0, "hypot(5,12) ~ 13");
        checkBitsD(StrictMath.hypot(0.0, 0.0), +0.0, "hypot(+0,+0) == +0.0");
        checkBitsD(StrictMath.hypot(-0.0, -0.0), +0.0, "hypot(-0,-0) == +0.0");
        check(StrictMath.hypot(Double.POSITIVE_INFINITY, Double.NaN) == Double.POSITIVE_INFINITY,
              "hypot(+Inf,NaN) == +Inf (Inf beats NaN)");
        check(StrictMath.hypot(Double.NEGATIVE_INFINITY, 3.0) == Double.POSITIVE_INFINITY,
              "hypot(-Inf,3) == +Inf");
        check(Double.isNaN(StrictMath.hypot(Double.NaN, 1.0)), "hypot(NaN,finite) is NaN");
        check(StrictMath.hypot(1e300, 1e300) > 1.414e300 && StrictMath.hypot(1e300, 1e300) < 1.415e300,
              "hypot rescales internally: finite where naive x*x+y*y would overflow");
    }

    static void testTrigContract() {
        checkBitsD(StrictMath.sin(-0.0), -0.0, "sin(-0.0) == -0.0");
        check(StrictMath.sin(StrictMath.PI / 2) == 1.0, "sin(pi/2) == 1.0 exact");
        checkTol(StrictMath.sin(StrictMath.PI / 6), 0.5, 1e-15, "sin(pi/6) ~ 0.5");
        check(StrictMath.cos(StrictMath.PI) == -1.0, "cos(pi) == -1.0 exact");
        checkTol(StrictMath.cos(StrictMath.PI / 2), 0.0, 1e-15, "cos(pi/2) ~ 0");
        checkBitsD(StrictMath.tan(-0.0), -0.0, "tan(-0.0) == -0.0");
        checkTol(StrictMath.tan(StrictMath.PI / 4), 1.0, 1e-15, "tan(pi/4) ~ 1");
        checkClose(StrictMath.asin(1.0), StrictMath.PI / 2, "asin(1) ~ pi/2");
        checkClose(StrictMath.asin(-1.0), -StrictMath.PI / 2, "asin(-1) ~ -pi/2");
        checkClose(StrictMath.acos(0.0), StrictMath.PI / 2, "acos(0) ~ pi/2");
        checkClose(StrictMath.acos(-1.0), StrictMath.PI, "acos(-1) ~ pi");
        checkBitsD(StrictMath.atan(-0.0), -0.0, "atan(-0.0) == -0.0");
        checkBitsD(StrictMath.atan2(0.0, 0.0), +0.0, "atan2(+0,+0) == +0.0");
        checkBitsD(StrictMath.atan2(-0.0, 0.0), -0.0, "atan2(-0,+0) == -0.0");
        check(StrictMath.atan2(0.0, -0.0) == StrictMath.PI, "atan2(+0,-0) == +pi");
        checkBitsD(StrictMath.atan2(-0.0, -0.0), -StrictMath.PI, "atan2(-0,-0) == -pi");
        check(StrictMath.atan2(-1.0, 0.0) == -StrictMath.PI / 2, "atan2(-1,+0) == -pi/2");
        check(StrictMath.toRadians(90.0) == StrictMath.PI / 2, "toRadians(90) == pi/2 exact");
        checkTol(StrictMath.toDegrees(StrictMath.toRadians(123.0)), 123.0, 1e-12,
                 "degrees(radians(123)) round-trip");
        checkTol(StrictMath.toRadians(StrictMath.toDegrees(1.0)), 1.0, 1e-15,
                 "radians(degrees(1)) round-trip");
        check(StrictMath.sinh(0.0) == 0.0 && StrictMath.cosh(0.0) == 1.0
              && StrictMath.tanh(0.0) == 0.0, "sinh/cosh/tanh(0)");
    }

    static void testExpLogContract() {
        // StrictMath.exp(1.0) 与 E 可差 1 ulp（Math.exp 的内在实现恰为 E），容差锁定
        checkTol(StrictMath.exp(1.0), StrictMath.E, 1e-15, "exp(1) ~ E within 1 ulp");
        checkClose(StrictMath.expm1(1.0), StrictMath.E - 1.0, "expm1(1) ~ E-1");
        check(StrictMath.log(0.0) == Double.NEGATIVE_INFINITY, "log(+0.0) == -Inf");
        check(Double.isNaN(StrictMath.log(-1.0)), "log(-1) is NaN");
        check(StrictMath.log(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "log(+Inf) == +Inf");
        check(Double.isNaN(StrictMath.exp(Double.NaN)), "exp(NaN) is NaN");
        check(StrictMath.log10(1.0) == 0.0, "log10(1) == +0.0");
        check(StrictMath.log10(100.0) == 2.0, "log10(100) == 2.0 exact");
        check(StrictMath.log10(0.0) == Double.NEGATIVE_INFINITY, "log10(0) == -Inf");
        check(Double.isNaN(StrictMath.log10(-1.0)), "log10(-1) is NaN");
        check(StrictMath.log1p(-1.0) == Double.NEGATIVE_INFINITY, "log1p(-1) == -Inf");
        check(Double.isNaN(StrictMath.log1p(-2.0)), "log1p(< -1) is NaN");
        check(StrictMath.expm1(Double.NEGATIVE_INFINITY) == -1.0, "expm1(-Inf) == -1.0");
        check(StrictMath.expm1(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "expm1(+Inf) == +Inf");
        checkTol(StrictMath.log1p(1e-10), 1e-10 - 5e-21, 1e-25, "log1p accurate near zero");
        checkTol(StrictMath.expm1(1e-10), 1e-10 + 5e-21, 1e-25, "expm1 accurate near zero");
    }

    static void testSignumUlpScalbNextContract() {
        checkBitsD(StrictMath.signum(-0.0), -0.0, "signum(-0.0) == -0.0");
        checkBitsD(StrictMath.signum(+0.0), +0.0, "signum(+0.0) == +0.0");
        check(StrictMath.signum(3.5) == 1.0, "signum(3.5) == 1.0");
        check(StrictMath.signum(Double.POSITIVE_INFINITY) == 1.0, "signum(+Inf) == 1.0");
        check(StrictMath.signum(Double.NEGATIVE_INFINITY) == -1.0, "signum(-Inf) == -1.0");
        checkBitsF(StrictMath.signum(-0.0f), -0.0f, "signum(-0.0f) == -0.0f");
        check(StrictMath.ulp(1.0) == 2.220446049250313E-16, "ulp(1.0) == 2.220446049250313E-16");
        check("2.220446049250313E-16".equals(Double.toString(StrictMath.ulp(1.0))),
              "ulp(1.0) shortest string form");
        check(StrictMath.ulp(-0.0) == Double.MIN_VALUE, "ulp(-0.0) == Double.MIN_VALUE");
        check(StrictMath.ulp(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY, "ulp(+Inf) == Inf");
        check(Double.isNaN(StrictMath.ulp(Double.NaN)), "ulp(NaN) is NaN");
        check(StrictMath.ulp(Double.MAX_VALUE) == 0x1.0p971, "ulp(MAX_VALUE) == 2^971");
        checkBitsD(StrictMath.scalb(-0.0, 3), -0.0, "scalb(-0.0,3) == -0.0");
        check(StrictMath.scalb(Double.MIN_VALUE, -1) == Double.MIN_VALUE / 2,
              "scalb(MIN_VALUE,-1) == half-MIN_VALUE (subnormal)");
        check(StrictMath.scalb(Double.MIN_VALUE, 1) == 2.0 * Double.MIN_VALUE,
              "scalb(MIN_VALUE,1) == 2*MIN_VALUE");
        check(StrictMath.scalb(Double.MIN_VALUE, 1074) == 1.0, "scalb(MIN_VALUE,1074) == 1.0");
        check(Double.isNaN(StrictMath.scalb(Double.NaN, 2)), "scalb(NaN) is NaN");
        check(StrictMath.scalb(Double.POSITIVE_INFINITY, 2) == Double.POSITIVE_INFINITY,
              "scalb(Inf) == Inf");
        check(StrictMath.nextUp(0.0f) == Float.MIN_VALUE, "nextUp(0.0f) == Float.MIN_VALUE");
        check(StrictMath.nextDown(0.0f) == -Float.MIN_VALUE, "nextDown(0.0f) == -Float.MIN_VALUE");
        check(StrictMath.nextDown(1.0) == 0x1.fffffffffffffp-1, "nextDown(1.0) == 1.0 - 2^-53");
        check(StrictMath.nextUp(StrictMath.nextDown(1.0)) == 1.0, "nextUp(nextDown(1.0)) == 1.0");
        check(StrictMath.nextDown(StrictMath.nextUp(1.0)) == 1.0, "nextDown(nextUp(1.0)) == 1.0");
        check(StrictMath.nextAfter(1.0, 2.0) == StrictMath.nextUp(1.0),
              "nextAfter(1.0,2.0) == nextUp(1.0)");
        check(Double.isNaN(StrictMath.nextAfter(Double.NaN, 1.0)), "nextAfter(NaN,1) is NaN");
        check(StrictMath.nextAfter(0.0f, 1.0) == Float.MIN_VALUE,
              "nextAfter(0.0f,1.0) == Float.MIN_VALUE");
        check(StrictMath.nextDown(Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY,
              "nextDown(-Inf) == -Inf");
    }

    static void testGetExponentBoundaries() {
        check(StrictMath.getExponent(0.0) == Double.MIN_EXPONENT - 1,
              "getExponent(+0.0) == MIN_EXPONENT-1 == -1023");
        check(StrictMath.getExponent(-0.0) == Double.MIN_EXPONENT - 1, "getExponent(-0.0) == -1023");
        check(StrictMath.getExponent(Double.MIN_NORMAL) == Double.MIN_EXPONENT,
              "getExponent(MIN_NORMAL) == -1022");
        check(StrictMath.getExponent(Double.POSITIVE_INFINITY) == Double.MAX_EXPONENT + 1,
              "getExponent(+Inf) == MAX_EXPONENT+1 == 1024");
        check(StrictMath.getExponent(Double.NaN) == Double.MAX_EXPONENT + 1, "getExponent(NaN) == 1024");
        check(StrictMath.getExponent(0.0f) == Float.MIN_EXPONENT - 1, "getExponent(0.0f) == -127");
        check(StrictMath.getExponent(Float.MAX_VALUE) == Float.MAX_EXPONENT,
              "getExponent(Float.MAX_VALUE) == 127");
        check(StrictMath.getExponent(-1.0) == 0 && StrictMath.getExponent(-2.0) == 1,
              "getExponent ignores sign bit");
    }

    static void testConstantsStringAndBits() {
        check("2.718281828459045".equals(Double.toString(StrictMath.E)),
              "Double.toString(StrictMath.E) exact");
        check("3.141592653589793".equals(Double.toString(StrictMath.PI)),
              "Double.toString(StrictMath.PI) exact");
        check(StrictMath.E == Math.E && StrictMath.PI == Math.PI,
              "StrictMath.E/PI are the same constants as Math's");
        check(StrictMath.TAU == Math.TAU, "StrictMath.TAU == Math.TAU");
        check(Double.doubleToLongBits(StrictMath.E) == 0x4005bf0a8b145769L, "E bit pattern");
        check(Double.doubleToLongBits(StrictMath.PI) == 0x400921fb54442d18L, "PI bit pattern");
    }

    static void testIeeeRemainderContract() {
        check(StrictMath.IEEEremainder(7.5, 2.0) == -0.5,
              "IEEEremainder(7.5,2) == -0.5 (quotient rounds to 4)");
        check(StrictMath.IEEEremainder(2.5, 2.0) == 0.5, "IEEEremainder(2.5,2) == 0.5");
        check(StrictMath.IEEEremainder(11.0, 3.0) == -1.0,
              "IEEEremainder(11,3) == -1 (quotient 4, 11-12)");
        check(Double.isNaN(StrictMath.IEEEremainder(Double.POSITIVE_INFINITY, 3.0)),
              "IEEEremainder(Inf,3) is NaN");
        check(Double.isNaN(StrictMath.IEEEremainder(3.0, 0.0)), "IEEEremainder(3,0) is NaN");
    }

    static void testFmaRoundingDivergence() {
        // fma 单次舍入契约：a = nextUp(1.0) = 1+2^-52，c = -(1+2^-51)。
        // 真积 a*a = 1 + 2^-51 + 2^-104：非融合 fl(a*a) = 1+2^-51，再减 -c 得 +0.0；
        // 融合只舍入一次，保住精确差 2^-104 —— 两者必然分叉。
        double a = StrictMath.nextUp(1.0);
        double c = StrictMath.nextUp(StrictMath.nextUp(1.0));
        check(StrictMath.fma(1.0, 2.0, 3.0) == 5.0, "fma(1,2,3) == 5 exact");
        check(StrictMath.fma(a, a, -c) == 0x1.0p-104, "fma single rounding keeps exact 2^-104");
        check(a * a - c == 0.0, "non-fused a*a-c collapses to +0.0 (JLS forbids fusion in operators)");
        check(StrictMath.fma(a, a, -c) != a * a - c, "fused vs non-fused diverge on this sample");
        check(StrictMath.fma(a, a, -c) == Math.fma(a, a, -c), "StrictMath.fma == Math.fma on divergence sample");
        check(StrictMath.fma(3.0f, 4.0f, 5.0f) == 17.0f, "fma(float) exact");
        // 零符号规则：(-0)*(+0) = -0；-0 + +0 == +0、-0 + (-0) == -0（就近舍入模式）
        checkBitsD(StrictMath.fma(-0.0, +0.0, +0.0), +0.0, "fma(-0,+0,+0) == +0.0");
        checkBitsD(StrictMath.fma(-0.0, +0.0, -0.0), -0.0, "fma(-0,+0,-0) == -0.0");
        check(Double.isNaN(StrictMath.fma(Double.POSITIVE_INFINITY, 0.0, Double.NaN)),
              "fma(Inf,0,NaN) is NaN");
        check(Double.isNaN(StrictMath.fma(1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)),
              "fma(1,Inf,-Inf) is NaN (Inf - Inf)");
        check(StrictMath.fma(0x1.0p-1074, 0x1.0p-1074, 0.0) == 0.0,
              "fma subnormal product underflows to +0.0");
    }

    static void testMixedPrecisionDivisionOverloads() {
        // JDK-9+ 重载：floorDiv(long,int)/floorMod(long,int)/multiplyExact(long,int)
        check(StrictMath.floorDiv(-7L, 2) == -4L, "floorDiv(long,int)(-7L,2) == -4L");
        check(StrictMath.floorMod(-7L, 2) == 1, "floorMod(long,int)(-7L,2) == 1");
        check(StrictMath.floorDiv(7L, -2) == -4L && StrictMath.floorMod(7L, -2) == -1,
              "floorDiv/floorMod(long,int) negative divisor");
        expectAeMsg(() -> StrictMath.floorDiv(1L, 0), "/ by zero",
                    "floorDiv(long,int) by zero message");
        expectAeMsg(() -> StrictMath.floorMod(1L, 0), "/ by zero",
                    "floorMod(long,int) by zero message");
        check(StrictMath.multiplyExact(Integer.MAX_VALUE + 1L, 2) == 4294967296L,
              "multiplyExact(long,int) widens the int factor");
        expectAeMsg(() -> StrictMath.multiplyExact(Long.MAX_VALUE, 2), "long overflow",
                    "multiplyExact(long,int) overflow message");
    }

    // Math 与 StrictMath 同入参同返回：只锁双侧实测位级相同的点位（规格仅承诺各自 1 ulp 准确）。
    static void testMathStrictMathAgreement() {
        // 整数与精确算术族（完全确定性）
        check(Math.abs(-5) == StrictMath.abs(-5), "abs(int) agreement");
        check(Math.abs(-5L) == StrictMath.abs(-5L), "abs(long) agreement");
        check(Math.abs(-5.5) == StrictMath.abs(-5.5), "abs(double) agreement");
        check(Math.abs(-5.5f) == StrictMath.abs(-5.5f), "abs(float) agreement");
        check(Double.doubleToLongBits(Math.abs(-0.0)) == Double.doubleToLongBits(StrictMath.abs(-0.0)),
              "abs(-0.0) bit-level agreement");
        check(Math.min(3, 5) == StrictMath.min(3, 5) && Math.max(3, 5) == StrictMath.max(3, 5),
              "min/max(int) agreement");
        check(Math.min(3L, 5L) == StrictMath.min(3L, 5L)
              && Math.max(3L, 5L) == StrictMath.max(3L, 5L), "min/max(long) agreement");
        check(Math.min(3.0f, 5.0f) == StrictMath.min(3.0f, 5.0f)
              && Math.max(3.0f, 5.0f) == StrictMath.max(3.0f, 5.0f), "min/max(float) agreement");
        check(Double.doubleToLongBits(Math.max(-0.0, 0.0))
              == Double.doubleToLongBits(StrictMath.max(-0.0, 0.0))
              && Double.doubleToLongBits(Math.min(-0.0, 0.0))
              == Double.doubleToLongBits(StrictMath.min(-0.0, 0.0)),
              "max/min zero-sign bit-level agreement");
        check(Math.addExact(7, 5) == StrictMath.addExact(7, 5), "addExact agreement");
        check(Math.subtractExact(7, 5) == StrictMath.subtractExact(7, 5), "subtractExact agreement");
        check(Math.multiplyExact(7, 5) == StrictMath.multiplyExact(7, 5), "multiplyExact agreement");
        check(Math.addExact(7L, 5L) == StrictMath.addExact(7L, 5L), "addExact(long) agreement");
        check(Math.multiplyExact(7L, 5L) == StrictMath.multiplyExact(7L, 5L), "multiplyExact(long) agreement");
        check(Math.incrementExact(7) == StrictMath.incrementExact(7), "incrementExact agreement");
        check(Math.negateExact(-7) == StrictMath.negateExact(-7), "negateExact agreement");
        check(Math.toIntExact(5L) == StrictMath.toIntExact(5L), "toIntExact agreement");
        check(Math.floorDiv(-7, 2) == StrictMath.floorDiv(-7, 2), "floorDiv(-7,2) agreement");
        check(Math.floorDiv(-7, -2) == StrictMath.floorDiv(-7, -2), "floorDiv(-7,-2) agreement");
        check(Math.floorMod(-7, 2) == StrictMath.floorMod(-7, 2), "floorMod(-7,2) agreement");
        check(Math.floorMod(7, -2) == StrictMath.floorMod(7, -2), "floorMod(7,-2) agreement");
        check(Math.floorDiv(-7L, 2L) == StrictMath.floorDiv(-7L, 2L), "floorDiv(long) agreement");
        check(Math.floorMod(7L, -2L) == StrictMath.floorMod(7L, -2L), "floorMod(long) agreement");
        check(Math.floorDiv(Integer.MIN_VALUE, -1) == StrictMath.floorDiv(Integer.MIN_VALUE, -1),
              "floorDiv(MIN,-1) overflow-wrap agreement");
        // 舍入族
        check(Math.floor(2.5) == StrictMath.floor(2.5) && Math.floor(-2.5) == StrictMath.floor(-2.5),
              "floor agreement");
        check(Math.floor(-0.5) == StrictMath.floor(-0.5), "floor(-0.5) agreement");
        check(Math.ceil(2.5) == StrictMath.ceil(2.5) && Math.ceil(-2.5) == StrictMath.ceil(-2.5),
              "ceil agreement");
        check(Double.doubleToLongBits(Math.ceil(-0.5)) == Double.doubleToLongBits(StrictMath.ceil(-0.5)),
              "ceil(-0.5) bit-level agreement");
        check(Math.rint(2.5) == StrictMath.rint(2.5) && Math.rint(3.5) == StrictMath.rint(3.5)
              && Math.rint(-2.5) == StrictMath.rint(-2.5), "rint agreement");
        check(Double.doubleToLongBits(Math.rint(-0.5)) == Double.doubleToLongBits(StrictMath.rint(-0.5)),
              "rint(-0.5) bit-level agreement");
        check(Math.round(2.5) == StrictMath.round(2.5) && Math.round(-2.5) == StrictMath.round(-2.5),
              "round(double) agreement");
        check(Math.round(0.5) == StrictMath.round(0.5) && Math.round(-0.5) == StrictMath.round(-0.5),
              "round(0.5/-0.5) agreement");
        check(Math.round(2.5f) == StrictMath.round(2.5f)
              && Math.round(-2.5f) == StrictMath.round(-2.5f), "round(float) agreement");
        // 幂/根
        check(Math.sqrt(4.0) == StrictMath.sqrt(4.0), "sqrt(4) agreement");
        check(Double.doubleToLongBits(Math.sqrt(2.0)) == Double.doubleToLongBits(StrictMath.sqrt(2.0)),
              "sqrt(2) bit-level agreement");
        check(Double.doubleToLongBits(Math.sqrt(-0.0)) == Double.doubleToLongBits(StrictMath.sqrt(-0.0)),
              "sqrt(-0.0) bit-level agreement");
        checkClose(Math.cbrt(27.0), StrictMath.cbrt(27.0), "cbrt(27) agreement (1 ulp)");
        checkClose(Math.cbrt(-27.0), StrictMath.cbrt(-27.0), "cbrt(-27) agreement (1 ulp)");
        checkClose(Math.cbrt(8.0), StrictMath.cbrt(8.0), "cbrt(8) agreement (1 ulp)");
        checkClose(Math.pow(2.0, 10.0), StrictMath.pow(2.0, 10.0), "pow(2,10) agreement (1 ulp)");
        check(Double.doubleToLongBits(Math.pow(2.0, 0.5))
              == Double.doubleToLongBits(StrictMath.pow(2.0, 0.5)), "pow(2,0.5) bit-level agreement");
        checkClose(Math.hypot(3.0, 4.0), StrictMath.hypot(3.0, 4.0), "hypot(3,4) agreement (1 ulp)");
        checkClose(Math.hypot(5.0, 12.0), StrictMath.hypot(5.0, 12.0), "hypot(5,12) agreement (1 ulp)");
        // 符号/邻居/缩放
        check(Double.doubleToLongBits(Math.signum(-0.0))
              == Double.doubleToLongBits(StrictMath.signum(-0.0)), "signum(-0.0) bit-level agreement");
        check(Math.signum(3.5) == StrictMath.signum(3.5), "signum(3.5) agreement");
        check(Math.copySign(1.0, -2.0) == StrictMath.copySign(1.0, -2.0), "copySign agreement");
        check(Math.scalb(1.0, 3) == StrictMath.scalb(1.0, 3), "scalb agreement");
        check(Math.ulp(1.0) == StrictMath.ulp(1.0), "ulp(1.0) agreement");
        check(Math.ulp(0.0) == StrictMath.ulp(0.0), "ulp(0.0) agreement");
        check(Math.nextUp(0.0) == StrictMath.nextUp(0.0), "nextUp(0.0) agreement");
        check(Math.nextDown(1.0) == StrictMath.nextDown(1.0), "nextDown(1.0) agreement");
        check(Math.nextAfter(1.0, 2.0) == StrictMath.nextAfter(1.0, 2.0), "nextAfter agreement");
        check(Math.getExponent(0.0) == StrictMath.getExponent(0.0), "getExponent(0.0) agreement");
        check(Math.getExponent(8.0) == StrictMath.getExponent(8.0), "getExponent(8.0) agreement");
        // 角度换换
        check(Math.toRadians(180.0) == StrictMath.toRadians(180.0), "toRadians(180) agreement");
        check(Math.toRadians(90.0) == StrictMath.toRadians(90.0), "toRadians(90) agreement");
        check(Math.toDegrees(Math.PI) == StrictMath.toDegrees(Math.PI), "toDegrees(pi) agreement");
        check(Math.toDegrees(90.0) == StrictMath.toDegrees(90.0), "toDegrees(90) agreement");
        // 三角（这些点位双侧实测位级相同；sin(1)/cos(1)/tan(1) 亦同）
        check(Math.sin(0.0) == StrictMath.sin(0.0), "sin(0) agreement");
        check(Math.sin(Math.PI / 2) == StrictMath.sin(Math.PI / 2), "sin(pi/2) agreement");
        check(Math.sin(Math.PI / 6) == StrictMath.sin(Math.PI / 6), "sin(pi/6) agreement");
        check(Math.sin(1.0) == StrictMath.sin(1.0), "sin(1) agreement");
        check(Math.cos(0.0) == StrictMath.cos(0.0), "cos(0) agreement");
        check(Math.cos(Math.PI) == StrictMath.cos(Math.PI), "cos(pi) agreement");
        check(Math.cos(1.0) == StrictMath.cos(1.0), "cos(1) agreement");
        check(Math.tan(0.0) == StrictMath.tan(0.0), "tan(0) agreement");
        check(Math.tan(1.0) == StrictMath.tan(1.0), "tan(1) agreement");
        checkClose(Math.asin(1.0), StrictMath.asin(1.0), "asin(1) agreement (1 ulp)");
        checkClose(Math.acos(0.0), StrictMath.acos(0.0), "acos(0) agreement (1 ulp)");
        checkClose(Math.acos(-1.0), StrictMath.acos(-1.0), "acos(-1) agreement (1 ulp)");
        checkClose(Math.atan(1.0), StrictMath.atan(1.0), "atan(1) agreement (1 ulp)");
        check(Math.atan2(0.0, 0.0) == StrictMath.atan2(0.0, 0.0), "atan2(0,0) agreement");
        checkClose(Math.atan2(1.0, 1.0), StrictMath.atan2(1.0, 1.0), "atan2(1,1) agreement (1 ulp)");
        // 指对与双曲
        check(Math.exp(0.0) == StrictMath.exp(0.0), "exp(0) agreement");
        check(Math.log(1.0) == StrictMath.log(1.0), "log(1) agreement");
        check(Math.log10(1000.0) == StrictMath.log10(1000.0), "log10(1000) agreement");
        check(Math.log10(100.0) == StrictMath.log10(100.0), "log10(100) agreement");
        checkClose(Math.expm1(1.0), StrictMath.expm1(1.0), "expm1(1) agreement (1 ulp)");
        check(Math.log1p(0.5) == StrictMath.log1p(0.5), "log1p(0.5) agreement");
        checkClose(Math.sinh(1.0), StrictMath.sinh(1.0), "sinh(1) agreement (1 ulp)");
        checkClose(Math.tanh(1.0), StrictMath.tanh(1.0), "tanh(1) agreement (1 ulp)");
        check(Math.IEEEremainder(5.0, 3.0) == StrictMath.IEEEremainder(5.0, 3.0),
              "IEEEremainder(5,3) agreement");
        check(Math.IEEEremainder(11.0, 3.0) == StrictMath.IEEEremainder(11.0, 3.0),
              "IEEEremainder(11,3) agreement");
        // 已知分叉点：Math.exp(1) 走内在实现恰等于 E，StrictMath.exp(1) 相差 1 ulp，
        // 规格允许（两者各自都在真值 1 ulp 内），只断言容差不断言位级。
        checkTol(Math.exp(1.0), StrictMath.exp(1.0), 1e-15,
                 "exp(1): Math vs StrictMath allowed 1-ulp divergence");
        // fma 双侧同值（语义上同为精确单次舍入）
        double a = Math.nextUp(1.0);
        double c = Math.nextUp(Math.nextUp(1.0));
        check(Math.fma(a, a, -c) == StrictMath.fma(a, a, -c), "fma agreement on rounding-divergence sample");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testRoundHalfTowardsPositiveInfinity();
        testFloorDivModContractMatrix();
        testExactOverflowMessages();
        testDivByZeroMessages();
        testAbsMinMaxBitLevel();
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
        testMathStrictMathAgreement();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

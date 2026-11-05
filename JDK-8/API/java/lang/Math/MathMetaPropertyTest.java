/*
 * @test
 * @summary Math/StrictMath randomized meta-properties: seeded random inputs
 *          assert Math-vs-StrictMath agreement within 2 ulp for the 1-ulp
 *          transcendental family, bit-identical results for the correctly
 *          rounded family, and bit-exact spec special-value tables.
 * @run main MathMetaPropertyTest
 */

import java.util.Random;

public class MathMetaPropertyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final long SEED = 20261105L;
    static final int N = 20000;
    static final Random R = new Random(SEED);

    // ---- generators ----
    static double uni(double lo, double hi) {
        return lo + (hi - lo) * R.nextDouble();
    }
    static double bitsFinite() {
        for (;;) {
            double d = Double.longBitsToDouble(R.nextLong());
            if (Double.isFinite(d)) return d;
        }
    }

    // ulp-distance between a and b measured at the larger magnitude
    static double ulpDiff(double a, double b) {
        if (a == b) return 0;                       // covers -0.0 == 0.0, but see bits()
        if (Double.isNaN(a) && Double.isNaN(b)) return 0;
        if (Double.isInfinite(a) || Double.isInfinite(b))
            return Double.isInfinite(a) && Double.isInfinite(b) && a == b ? 0 : Double.MAX_VALUE;
        double m = Math.max(Math.abs(a), Math.abs(b));
        double u = Math.ulp(m);
        return Math.abs(a - b) / u;                 // in ulps of the larger value
    }
    static boolean bits(double a, double b) {
        return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b);
    }

    interface F1 { double f(double x); }
    interface F2 { double f(double x, double y); }

    // generic one-argument agreement sampler
    static void agree1(String name, F1 fm, F1 fs, double[] xs, double tolUlp) {
        double worst = 0; int worstAt = -1; double worstX = 0;
        for (int i = 0; i < xs.length; i++) {
            double a = fm.f(xs[i]), b = fs.f(xs[i]);
            if (Double.isNaN(a) && Double.isNaN(b)) continue;
            double d = ulpDiff(a, b);
            if (d > worst) { worst = d; worstAt = i; worstX = xs[i]; }
        }
        check(worst <= tolUlp,
              name + " Math-vs-StrictMath drift " + worst + " ulp at x=" + worstX
              + " (sample " + worstAt + "), tolerance " + tolUlp);
        passed += xs.length; // one agreement per sample
    }

    static void agree2(String name, F2 fm, F2 fs, double[] xs, double[] ys, double tolUlp) {
        double worst = 0; String where = "";
        for (int i = 0; i < xs.length; i++) {
            double a = fm.f(xs[i], ys[i]), b = fs.f(xs[i], ys[i]);
            if (Double.isNaN(a) && Double.isNaN(b)) continue;
            double d = ulpDiff(a, b);
            if (d > worst) { worst = d; where = "(" + xs[i] + "," + ys[i] + ")"; }
        }
        check(worst <= tolUlp,
              name + " Math-vs-StrictMath drift " + worst + " ulp at " + where
              + ", tolerance " + tolUlp);
        passed += xs.length;
    }

    static double[] gen(int count, java.util.function.DoubleSupplier g) {
        double[] a = new double[count];
        for (int i = 0; i < a.length; i++) a[i] = g.getAsDouble();
        return a;
    }
    // Java 8 lacks DoubleSupplier? it exists in 8. keep.

    public static void main(String[] args) {
        R.setSeed(SEED);

        // ---------- 1) 1-ulp family: agreement <= 2 ulp ----------
        agree1("sin", x -> Math.sin(x), x -> StrictMath.sin(x),
               gen(N, () -> uni(-1000, 1000)), 2.0);
        agree1("cos", x -> Math.cos(x), x -> StrictMath.cos(x),
               gen(N, () -> uni(-1000, 1000)), 2.0);
        agree1("tan", x -> Math.tan(x), x -> StrictMath.tan(x),
               gen(N, () -> uni(-300, 300)), 2.0);
        agree1("asin", x -> Math.asin(x), x -> StrictMath.asin(x),
               gen(N, () -> uni(-1.0, 1.0)), 2.0);
        agree1("acos", x -> Math.acos(x), x -> StrictMath.acos(x),
               gen(N, () -> uni(-1.0, 1.0)), 2.0);
        agree1("atan", x -> Math.atan(x), x -> StrictMath.atan(x),
               gen(N, () -> uni(-1e12, 1e12)), 2.0);
        agree1("exp", x -> Math.exp(x), x -> StrictMath.exp(x),
               gen(N, () -> uni(-745, 709)), 2.0);
        agree1("expm1", x -> Math.expm1(x), x -> StrictMath.expm1(x),
               gen(N, () -> uni(-709, 709)), 2.0);
        agree1("log", x -> Math.log(x), x -> StrictMath.log(x),
               gen(N, () -> Math.abs(uni(1e-300, 1e300))), 2.0);
        agree1("log10", x -> Math.log10(x), x -> StrictMath.log10(x),
               gen(N, () -> Math.abs(uni(1e-300, 1e300))), 2.0);
        agree1("log1p", x -> Math.log1p(x), x -> StrictMath.log1p(x),
               gen(N, () -> uni(-1 + 1e-12, 1e12)), 2.0);
        agree1("cbrt", x -> Math.cbrt(x), x -> StrictMath.cbrt(x),
               gen(N, MathMetaPropertyTest::bitsFinite), 2.0);
        agree1("sinh", x -> Math.sinh(x), x -> StrictMath.sinh(x),
               gen(N, () -> uni(-710, 710)), 2.0);
        agree1("cosh", x -> Math.cosh(x), x -> StrictMath.cosh(x),
               gen(N, () -> uni(-710, 710)), 2.0);
        agree1("tanh", x -> Math.tanh(x), x -> StrictMath.tanh(x),
               gen(N, () -> uni(-710, 710)), 2.0);

        agree2("atan2", (x, y) -> Math.atan2(x, y), (x, y) -> StrictMath.atan2(x, y),
               gen(N, () -> uni(-1e9, 1e9)), gen(N, () -> uni(-1e9, 1e9)), 2.0);
        agree2("pow", (x, y) -> Math.pow(x, y), (x, y) -> StrictMath.pow(x, y),
               gen(N, () -> uni(-9, 9)), gen(N, () -> uni(-12, 12)), 2.0);
        agree2("hypot", (x, y) -> Math.hypot(x, y), (x, y) -> StrictMath.hypot(x, y),
               gen(N, () -> uni(-1e300, 1e300)), gen(N, () -> uni(-1e300, 1e300)), 2.0);

        // ---------- 2) correctly rounded family: bit-identical ----------
        agree1("sqrt", x -> Math.sqrt(x), x -> StrictMath.sqrt(x),
               gen(N, MathMetaPropertyTest::bitsFinite), 0.0);
        // ulpDiff==0 only proved ==; demand raw-bit identity through sampling
        {
            double worstX = 0; boolean ok = true;
            for (int i = 0; i < N; i++) {
                double x = bitsFinite();
                if (x < 0) x = -x;
                if (!bits(Math.sqrt(x), StrictMath.sqrt(x))) { ok = false; worstX = x; break; }
            }
            check(ok, "sqrt Math vs StrictMath must be bit-identical, x=" + worstX);
            passed += N;
        }

        // ---------- 3) spec special-value tables (bit-exact) ----------
        double nan = Double.NaN, inf = Double.POSITIVE_INFINITY;
        check(bits(Math.exp(nan), nan) && bits(Math.exp(inf), inf)
              && bits(Math.exp(-inf), 0.0), "exp specials");
        check(bits(Math.log(0.0), -inf) && bits(Math.log(-0.0), -inf)
              && Double.isNaN(Math.log(-1)) && bits(Math.log(inf), inf)
              && Double.isNaN(Math.log(nan)), "log specials");
        check(bits(Math.log10(0.0), -inf) && bits(Math.log10(inf), inf)
              && Double.isNaN(Math.log10(-1)), "log10 specials");
        check(bits(Math.log1p(-1.0), -inf) && Double.isNaN(Math.log1p(-2.0))
              && bits(Math.log1p(inf), inf), "log1p specials");
        check(bits(Math.sqrt(-0.0), -0.0) && bits(Math.sqrt(0.0), 0.0)
              && Double.isNaN(Math.sqrt(-1)) && bits(Math.sqrt(inf), inf)
              && Double.isNaN(Math.sqrt(nan)), "sqrt specials");
        check(bits(Math.cbrt(0.0), 0.0) && bits(Math.cbrt(-0.0), -0.0)
              && bits(Math.cbrt(inf), inf) && bits(Math.cbrt(-inf), -inf)
              && bits(Math.cbrt(-8.0), -Math.cbrt(8.0)), "cbrt specials");
        // sin/cos/tan at zero are signed-zero exact; infinities are NaN
        check(bits(Math.sin(0.0), 0.0) && bits(Math.sin(-0.0), -0.0)
              && Double.isNaN(Math.sin(inf)), "sin specials");
        check(bits(Math.cos(0.0), 1.0) && bits(Math.cos(-0.0), 1.0)
              && Double.isNaN(Math.cos(inf)), "cos specials");
        check(bits(Math.tan(0.0), 0.0) && bits(Math.tan(-0.0), -0.0)
              && Double.isNaN(Math.tan(inf)), "tan specials");
        check(bits(Math.sinh(0.0), 0.0) && bits(Math.sinh(-0.0), -0.0)
              && bits(Math.sinh(inf), inf) && bits(Math.sinh(-inf), -inf), "sinh specials");
        check(bits(Math.cosh(0.0), 1.0) && bits(Math.cosh(-0.0), 1.0)
              && bits(Math.cosh(inf), inf) && bits(Math.cosh(-inf), inf), "cosh specials");
        check(bits(Math.tanh(0.0), 0.0) && bits(Math.tanh(-0.0), -0.0)
              && bits(Math.tanh(inf), 1.0) && bits(Math.tanh(-inf), -1.0), "tanh specials");
        // asin/acos domain: outside [-1,1] NaN (boundary handled within 1.5 ulp by spec)
        check(Double.isNaN(Math.asin(1.0000001)) && Double.isNaN(Math.acos(-1.0000001))
              && Double.isNaN(Math.asin(inf)) && Double.isNaN(Math.acos(-inf)), "asin/acos domain");
        // pow spec table rows (JLS/Math docs)
        double[] anyX = { 0.0, -0.0, 1.0, -1.0, 2.0, -2.0, inf, -inf, nan,
                          Double.MIN_VALUE, -Double.MIN_VALUE, Double.MAX_VALUE };
        for (double x : anyX) {
            check(bits(Math.pow(x, 0.0), 1.0) && bits(Math.pow(x, -0.0), 1.0),
                  "pow(" + x + ", +-0) == 1");
        }
        for (double y : anyX) {
            if (Double.isFinite(y)) {
                check(bits(Math.pow(1.0, y), 1.0), "pow(1, " + y + ") == 1");
            }
        }
        // spec rows: any NaN argument yields NaN (except the x^(+-0) rows above)
        check(Double.isNaN(Math.pow(1.0, nan)) && Double.isNaN(Math.pow(-1.0, inf))
              && Double.isNaN(Math.pow(1.0, inf)) && Double.isNaN(Math.pow(-1.0, -Double.NEGATIVE_INFINITY)),
              "pow NaN-argument rows: pow(1,NaN)/pow(+-1,+-Inf) are NaN per Java spec");
        check(bits(Math.pow(0.0, 1.0), 0.0) && bits(Math.pow(0.0, 3.7), 0.0)
              && bits(Math.pow(0.0, -1.0), inf) && bits(Math.pow(-0.0, -3.0), -inf)
              && bits(Math.pow(-0.0, -2.0), inf), "pow zero-base rows");
        check(bits(Math.pow(inf, 2.0), inf) && bits(Math.pow(inf, -2.0), 0.0)
              && bits(Math.pow(-inf, 3.0), -inf) && bits(Math.pow(-inf, 2.0), inf)
              && bits(Math.pow(-inf, -3.0), -0.0), "pow infinity rows");
        check(Double.isNaN(Math.pow(-2.0, 0.5)) && Double.isNaN(Math.pow(nan, 2.0)),
              "pow NaN rows");
        // atan2 full quadrant table (exact rows)
        double p = 1.0, z = 0.0;
        check(bits(Math.atan2(z, z), z) && bits(Math.atan2(z, -z), Math.PI)
              && bits(Math.atan2(-z, -z), -Math.PI), "atan2 zero quadrants");
        check(bits(Math.atan2(p, inf), z) && bits(Math.atan2(p, -inf), Math.PI)
              && bits(Math.atan2(inf, p), Math.PI / 2)
              && bits(Math.atan2(-inf, p), -Math.PI / 2), "atan2 infinity rows");
        check(Double.isNaN(Math.atan2(nan, z)) && Double.isNaN(Math.atan2(z, nan)),
              "atan2 NaN rows");

        // ---------- 4) domain properties on random inputs ----------
        for (int i = 0; i < 2000; i++) {
            double x = uni(-100, 100);
            check(Math.sin(x) >= -1 && Math.sin(x) <= 1, "sin range");
            check(Math.cos(x) >= -1 && Math.cos(x) <= 1, "cos range");
            double h = Math.hypot(x, 3 * x);
            check(h >= 0 && h < Double.POSITIVE_INFINITY, "hypot finite non-negative");
            double e = Math.exp(x);
            check(e > 0, "exp positive");
            double t = Math.tanh(x);
            check(t >= -1 && t <= 1, "tanh range");
            if (x > -5 && x < 5) check(t > -1 && t < 1, "tanh strict range for |x|<5");
        }

        // determinism: same seed, same stream shape (regression guard)
        Random r2 = new Random(SEED);
        check(r2.nextLong() == new Random(SEED).nextLong(), "seeded Random reproducible");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

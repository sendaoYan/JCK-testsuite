/*
 * @test
 * @summary java.lang.Double API conformance: constants (incl. bit patterns for
 *          MIN_NORMAL/MIN_VALUE), parseDouble (whitespace trim, f/d suffix, hex,
 *          NaN/Infinity, NFE vs NPE on null), NaN/-0.0 semantics, bit conversion
 *          (doubleToLongBits/longBitsToDouble), compare/equals, and hashCode.
 * @run main DoubleTest
 */

public class DoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNFE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NumberFormatException");
        } catch (NumberFormatException expected) {
            passed++;
        }
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testConstants() {
        check(Double.SIZE == 64, "SIZE == 64");
        check(Double.BYTES == 8, "BYTES == 8");
        check(Double.TYPE == double.class, "TYPE == double.class");
        check(Double.MAX_VALUE == 0x1.fffffffffffffP+1023, "MAX_VALUE bits");
        check(Double.MIN_VALUE == 0x0.0000000000001P-1022, "MIN_VALUE bits");
        // MIN_NORMAL is 2^-1022, the smallest normal positive double.
        check(Double.doubleToLongBits(Double.MIN_NORMAL) == 0x0010000000000000L,
              "MIN_NORMAL bits");
        check(Double.POSITIVE_INFINITY == Double.longBitsToDouble(0x7ff0000000000000L),
              "POSITIVE_INFINITY == 0x7ff0000000000000");
        check(Double.NEGATIVE_INFINITY == Double.longBitsToDouble(0xfff0000000000000L),
              "NEGATIVE_INFINITY == 0xfff0000000000000");
        check(Double.isNaN(Double.NaN), "NaN is NaN");
    }

    static void testParse() {
        check(Double.parseDouble("1.5") == 1.5, "parseDouble(1.5)");
        check(Double.parseDouble("-1.5") == -1.5, "parseDouble(-1.5)");
        check(Double.parseDouble("1.5d") == 1.5, "parseDouble(1.5d)");
        check(Double.parseDouble("1.5f") == 1.5, "parseDouble(1.5f) suffix ignored");
        check(Double.parseDouble(" 1.5 ") == 1.5, "parseDouble trims whitespace");
        check(Double.parseDouble(".5") == 0.5, "parseDouble(.5)");
        check(Double.parseDouble("5.") == 5.0, "parseDouble(5.)");
        check(Double.parseDouble("1e3") == 1000.0, "parseDouble(1e3)");
        check(Double.parseDouble("0x1.8p1") == 3.0, "parseDouble(hex 0x1.8p1)");
        check(Double.isNaN(Double.parseDouble("NaN")), "parseDouble(NaN) is NaN");
        check(Double.parseDouble("Infinity") == Double.POSITIVE_INFINITY, "parseDouble(Inf)");
        check(Double.parseDouble("-Infinity") == Double.NEGATIVE_INFINITY, "parseDouble(-Inf)");

        expectNFE(() -> Double.parseDouble(""), "parseDouble(empty)");
        expectNFE(() -> Double.parseDouble("abc"), "parseDouble(abc)");
        expectNFE(() -> Double.parseDouble("1.0.0"), "parseDouble(1.0.0)");
        expectNFE(() -> Double.parseDouble("0x"), "parseDouble(0x bare)");
        expectNFE(() -> Double.parseDouble("d"), "parseDouble(d alone)");
        expectNPE(() -> Double.parseDouble(null), "parseDouble(null)");
    }

    static void testNaNInfinity() {
        check(Double.NaN != Double.NaN, "NaN != NaN");
        check(Double.isNaN(Double.NaN), "isNaN(NaN)");
        check(!Double.isNaN(1.0), "!isNaN(1.0)");
        check(Double.isInfinite(Double.POSITIVE_INFINITY), "isInfinite(+Inf)");
        check(Double.isInfinite(Double.NEGATIVE_INFINITY), "isInfinite(-Inf)");
        check(!Double.isInfinite(1.0), "!isInfinite(1.0)");
        check(Double.isFinite(1.0), "isFinite(1.0)");
        check(!Double.isFinite(Double.NaN), "!isFinite(NaN)");
        check(!Double.isFinite(Double.POSITIVE_INFINITY), "!isFinite(Inf)");
    }

    static void testSignedZero() {
        check(-0.0 == 0.0, "-0.0 == 0.0");
        check(Double.compare(-0.0, 0.0) < 0, "compare(-0.0, 0.0) < 0");
        check(Double.compare(0.0, -0.0) > 0, "compare(0.0, -0.0) > 0");
        check(!Double.valueOf(-0.0).equals(Double.valueOf(0.0)),
              "-0.0 not equals 0.0 (distinct bit patterns)");
        check(Double.doubleToLongBits(-0.0) == 0x8000000000000000L, "doubleToLongBits(-0.0)");
        check(Double.doubleToLongBits(0.0) == 0x0000000000000000L, "doubleToLongBits(0.0)");
    }

    static void testBitConversion() {
        check(Double.doubleToLongBits(Double.NaN) == 0x7ff8000000000000L,
              "doubleToLongBits(NaN) canonical");
        check(Double.longBitsToDouble(0x7ff0000000000000L) == Double.POSITIVE_INFINITY,
              "longBitsToDouble(0x7ff0000000000000) == Inf");
        check(Double.longBitsToDouble(0xfff0000000000000L) == Double.NEGATIVE_INFINITY,
              "longBitsToDouble(0xfff0000000000000) == -Inf");
        check(Double.doubleToLongBits(1.5) == 0x3ff8000000000000L, "doubleToLongBits(1.5)");
        check(Double.longBitsToDouble(0x3ff8000000000000L) == 1.5, "longBitsToDouble(1.5)");
        check(Double.doubleToRawLongBits(Double.longBitsToDouble(0x7ff8000000000001L))
              == 0x7ff8000000000001L, "doubleToRawLongBits preserves NaN payload");
    }

    static void testCompareEquals() {
        check(Double.compare(1.0, 2.0) < 0, "compare(1,2) < 0");
        check(Double.compare(2.0, 1.0) > 0, "compare(2,1) > 0");
        check(Double.compare(1.0, 1.0) == 0, "compare(1,1) == 0");
        check(Double.compare(Double.NaN, 1.0) > 0, "compare(NaN, x) > 0");
        check(Double.compare(1.0, Double.NaN) < 0, "compare(x, NaN) < 0");
        check(Double.compare(Double.NaN, Double.NaN) == 0, "compare(NaN, NaN) == 0");
        check(Double.valueOf(1.5).compareTo(Double.valueOf(2.0)) < 0, "compareTo < 0");
        check(Double.valueOf(Double.NaN).compareTo(Double.valueOf(1.0)) > 0, "NaN.compareTo > all");

        check(Double.valueOf(1.5).equals(Double.valueOf(1.5)), "equals same");
        check(!Double.valueOf(1.5).equals(Double.valueOf(2.0)), "!equals different");
        check(Double.valueOf(Double.NaN).equals(Double.valueOf(Double.NaN)), "NaN equals NaN");
        check(!Double.valueOf(1.5).equals(Float.valueOf(1.5f)), "!equals Float");
    }

    static void testHashCodeToString() {
        long bits = Double.doubleToLongBits(1.5);
        check(Double.valueOf(1.5).hashCode() == (int) (bits ^ (bits >>> 32)),
              "hashCode == XOR of halves");
        check(Double.toString(1.5).equals("1.5"), "toString(1.5)");
        check(Double.toString(Double.NaN).equals("NaN"), "toString(NaN)");
        check(Double.toString(Double.POSITIVE_INFINITY).equals("Infinity"), "toString(Inf)");
        check(Double.toString(Double.NEGATIVE_INFINITY).equals("-Infinity"), "toString(-Inf)");
        check(Double.parseDouble(Double.toString(1.5)) == 1.5, "toString round-trips");
        check(Double.valueOf("1.5") == 1.5, "valueOf(String)");
    }

    public static void main(String[] args) {
        testConstants();
        testParse();
        testNaNInfinity();
        testSignedZero();
        testBitConversion();
        testCompareEquals();
        testHashCodeToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.Float API conformance: constants (incl. bit patterns for
 *          MIN_NORMAL/MIN_VALUE), parseFloat (whitespace trim, f/d suffix, hex,
 *          NaN/Infinity, NFE vs NPE on null), NaN/-0.0 semantics, bit conversion
 *          (floatToIntBits/intBitsToFloat), compare/equals, and hashCode.
 * @run main FloatTest
 */

public class FloatTest {
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
        check(Float.SIZE == 32, "SIZE == 32");
        check(Float.BYTES == 4, "BYTES == 4");
        check(Float.TYPE == float.class, "TYPE == float.class");
        check(Float.MAX_VALUE == 0x1.fffffeP+127f, "MAX_VALUE bits");
        check(Float.MIN_VALUE == 0x0.000002P-126f, "MIN_VALUE bits");
        // MIN_NORMAL is 2^-126, the smallest normal positive float.
        check(Float.floatToIntBits(Float.MIN_NORMAL) == 0x00800000, "MIN_NORMAL bits");
        check(Float.POSITIVE_INFINITY == Float.intBitsToFloat(0x7f800000),
              "POSITIVE_INFINITY == 0x7f800000");
        check(Float.NEGATIVE_INFINITY == Float.intBitsToFloat(0xff800000),
              "NEGATIVE_INFINITY == 0xff800000");
        check(Float.isNaN(Float.NaN), "NaN is NaN");
    }

    static void testParse() {
        check(Float.parseFloat("1.5") == 1.5f, "parseFloat(1.5)");
        check(Float.parseFloat("-1.5") == -1.5f, "parseFloat(-1.5)");
        check(Float.parseFloat("1.5f") == 1.5f, "parseFloat(1.5f)");
        check(Float.parseFloat("1.5d") == 1.5f, "parseFloat(1.5d) suffix ignored");
        check(Float.parseFloat(" 1.5 ") == 1.5f, "parseFloat trims whitespace");
        check(Float.parseFloat(".5") == 0.5f, "parseFloat(.5)");
        check(Float.parseFloat("5.") == 5.0f, "parseFloat(5.)");
        check(Float.parseFloat("1e10") == 1e10f, "parseFloat(1e10)");
        check(Float.parseFloat("0x1.8p1") == 3.0f, "parseFloat(hex 0x1.8p1)");
        check(Float.parseFloat("NaN") != Float.parseFloat("NaN"), "parseFloat(NaN) is NaN");
        check(Float.isNaN(Float.parseFloat("NaN")), "parseFloat(NaN) is NaN");
        check(Float.parseFloat("Infinity") == Float.POSITIVE_INFINITY, "parseFloat(Infinity)");
        check(Float.parseFloat("-Infinity") == Float.NEGATIVE_INFINITY, "parseFloat(-Infinity)");

        expectNFE(() -> Float.parseFloat(""), "parseFloat(empty)");
        expectNFE(() -> Float.parseFloat("abc"), "parseFloat(abc)");
        expectNFE(() -> Float.parseFloat("1.0.0"), "parseFloat(1.0.0)");
        expectNFE(() -> Float.parseFloat("0x"), "parseFloat(0x bare)");
        expectNFE(() -> Float.parseFloat("f"), "parseFloat(f alone)");
        expectNPE(() -> Float.parseFloat(null), "parseFloat(null)");
    }

    static void testNaNInfinity() {
        check(Float.NaN != Float.NaN, "NaN != NaN");
        check(Float.isNaN(Float.NaN), "isNaN(NaN)");
        check(!Float.isNaN(1.0f), "!isNaN(1.0)");
        check(Float.isInfinite(Float.POSITIVE_INFINITY), "isInfinite(+Inf)");
        check(Float.isInfinite(Float.NEGATIVE_INFINITY), "isInfinite(-Inf)");
        check(!Float.isInfinite(1.0f), "!isInfinite(1.0)");
        check(Float.isFinite(1.0f), "isFinite(1.0)");
        check(!Float.isFinite(Float.NaN), "!isFinite(NaN)");
        check(!Float.isFinite(Float.POSITIVE_INFINITY), "!isFinite(Inf)");
    }

    static void testSignedZero() {
        check(-0.0f == 0.0f, "-0.0 == 0.0");
        check(Float.compare(-0.0f, 0.0f) < 0, "compare(-0.0, 0.0) < 0");
        check(Float.compare(0.0f, -0.0f) > 0, "compare(0.0, -0.0) > 0");
        check(!Float.valueOf(-0.0f).equals(Float.valueOf(0.0f)),
              "-0.0 not equals 0.0 (distinct bit patterns)");
        check(Float.floatToIntBits(-0.0f) == 0x80000000, "floatToIntBits(-0.0)");
        check(Float.floatToIntBits(0.0f) == 0x00000000, "floatToIntBits(0.0)");
    }

    static void testBitConversion() {
        check(Float.floatToIntBits(Float.NaN) == 0x7fc00000, "floatToIntBits(NaN) canonical");
        check(Float.intBitsToFloat(0x7f800000) == Float.POSITIVE_INFINITY,
              "intBitsToFloat(0x7f800000) == Inf");
        check(Float.intBitsToFloat(0xff800000) == Float.NEGATIVE_INFINITY,
              "intBitsToFloat(0xff800000) == -Inf");
        check(Float.floatToIntBits(1.5f) == 0x3fc00000, "floatToIntBits(1.5)");
        check(Float.intBitsToFloat(0x3fc00000) == 1.5f, "intBitsToFloat(0x3fc00000)");
        // floatToRawIntBits preserves the NaN payload, unlike floatToIntBits.
        check(Float.floatToRawIntBits(Float.intBitsToFloat(0x7fc00001)) == 0x7fc00001,
              "floatToRawIntBits preserves NaN payload");
    }

    static void testCompareEquals() {
        check(Float.compare(1.0f, 2.0f) < 0, "compare(1,2) < 0");
        check(Float.compare(2.0f, 1.0f) > 0, "compare(2,1) > 0");
        check(Float.compare(1.0f, 1.0f) == 0, "compare(1,1) == 0");
        check(Float.compare(Float.NaN, 1.0f) > 0, "compare(NaN, x) > 0");
        check(Float.compare(1.0f, Float.NaN) < 0, "compare(x, NaN) < 0");
        check(Float.compare(Float.NaN, Float.NaN) == 0, "compare(NaN, NaN) == 0");
        check(Float.valueOf(1.5f).compareTo(Float.valueOf(2.0f)) < 0, "compareTo < 0");
        check(Float.valueOf(Float.NaN).compareTo(Float.valueOf(1.0f)) > 0, "NaN.compareTo > all");

        check(Float.valueOf(1.5f).equals(Float.valueOf(1.5f)), "equals same");
        check(!Float.valueOf(1.5f).equals(Float.valueOf(2.0f)), "!equals different");
        check(Float.valueOf(Float.NaN).equals(Float.valueOf(Float.NaN)), "NaN equals NaN");
        check(!Float.valueOf(1.5f).equals(Double.valueOf(1.5)), "!equals Double");
    }

    static void testHashCodeToString() {
        check(Float.valueOf(1.5f).hashCode() == Float.floatToIntBits(1.5f),
              "hashCode == floatToIntBits");
        check(Float.toString(1.5f).equals("1.5"), "toString(1.5)");
        check(Float.toString(Float.NaN).equals("NaN"), "toString(NaN)");
        check(Float.toString(Float.POSITIVE_INFINITY).equals("Infinity"), "toString(Inf)");
        check(Float.toString(Float.NEGATIVE_INFINITY).equals("-Infinity"), "toString(-Inf)");
        // round-trip: toString then parseFloat recovers the original value
        check(Float.parseFloat(Float.toString(1.5f)) == 1.5f, "toString round-trips");
        check(Float.valueOf("1.5") == 1.5f, "valueOf(String)");
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

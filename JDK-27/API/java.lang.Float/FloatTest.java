/*
 * @test
 * @summary java.lang.Float conformance (white-box structural + black-box
 *          behavioral): constants, parseFloat, NaN/-0.0 semantics, bit conversion,
 *          float16ToFloat, sum/max/min, boxing, compare/equals, hashCode.
 *
 * Design case
 * -----------
 * 1. Contract: Float is an immutable wrapper over a 32-bit IEEE-754 float;
 *    parseFloat accepts decimal/hex/NaN/Infinity with optional whitespace and a
 *    trailing f/d suffix, throws NFE on malformed input and NPE on null.
 * 2. Usage: parseFloat/valueOf -> floatValue -> floatToIntBits/intBitsToFloat
 *    round-trip -> compare/equals -> toString round-trip.
 * 3. Composition: intBitsToFloat(floatToIntBits(x)) == x for non-NaN; NaN and
 *    signed zero distinguished by bit pattern; compare orders -0.0 < +0.0, NaN max.
 * 4. Invariants: immutability; equals by bit pattern (NaN==NaN, -0.0!=+0.0);
 *    hashCode == floatToIntBits(value).
 * 5. Boundary/exception: SIZE==32/BYTES==4; MIN_VALUE subnormal; NaN/Infinity
 *    propagate through isNaN/isInfinite/isFinite; float16ToFloat decodes binary16.
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

    // ---- White-box / structural ----
    static void testWhiteBox() {
        try {
            Class<?> c = Class.forName("java.lang.Float");
            check(!c.isInterface(), "Float is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Float is final");
            check(c.getSuperclass() == Number.class, "Float extends Number");
            check(Comparable.class.isAssignableFrom(c), "Float implements Comparable");
            check(c.getField("MAX_VALUE").getType() == float.class
                  && java.lang.reflect.Modifier.isStatic(c.getField("MAX_VALUE").getModifiers()),
                  "MAX_VALUE is public static final float");
            check(c.getMethod("parseFloat", String.class).getReturnType() == float.class,
                  "parseFloat(String) returns float");
            check(c.getMethod("valueOf", float.class).getReturnType() == Float.class,
                  "valueOf(float) returns Float");
            check(c.getMethod("floatToIntBits", float.class).getReturnType() == int.class,
                  "floatToIntBits(float) returns int");
            check(c.getMethod("float16ToFloat", short.class).getReturnType() == float.class,
                  "float16ToFloat(short) returns float");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testSumMaxMin() {
        check(Float.sum(1.5f, 2.5f) == 4.0f, "sum(1.5,2.5)");
        check(Float.max(1.5f, 2.5f) == 2.5f && Float.min(1.5f, 2.5f) == 1.5f,
              "max/min floats");
        check(Float.isNaN(Float.sum(Float.NaN, 1.0f)), "sum with NaN is NaN");
        check(Float.isNaN(Float.max(Float.NaN, 1.0f)), "max(NaN, x) is NaN");
        check(Float.compare(Float.max(-0.0f, 0.0f), 0.0f) == 0, "max(-0.0,0.0) == +0.0");
    }

    static void testBoundaryParseAndFormat() {
        check(Float.parseFloat("3.4028235E38") == Float.MAX_VALUE, "parseFloat(MAX)");
        check(Float.parseFloat("1.4E-45") == Float.MIN_VALUE, "parseFloat(MIN_VALUE)");
        check(Float.parseFloat("3.5E38") == Float.POSITIVE_INFINITY, "parseFloat overflow -> +Inf");
        check(Float.parseFloat("-3.5E38") == Float.NEGATIVE_INFINITY, "parseFloat overflow -> -Inf");
        check(Float.parseFloat("1e-50") == 0.0f, "parseFloat underflow -> 0.0");
        check(Float.parseFloat("0x1.0p-126") == Float.MIN_NORMAL, "parseFloat hex MIN_NORMAL");
        check(Float.parseFloat("0x1.0p128") == Float.POSITIVE_INFINITY, "parseFloat hex overflow");
        check(Float.valueOf("+1.5") == 1.5f, "valueOf(+1.5) leading plus");
        check(Float.valueOf(Float.MAX_VALUE).floatValue() == Float.MAX_VALUE, "valueOf(MAX)");
        check(Float.toString(Float.MAX_VALUE).equals("3.4028235E38"), "toString(MAX)");
        check(Float.toString(Float.MIN_VALUE).equals("1.4E-45"), "toString(MIN_VALUE)");
        check(Float.compare(Float.MAX_VALUE, Float.POSITIVE_INFINITY) < 0, "compare(MAX, Inf) < 0");
        check(Float.compare(Float.NEGATIVE_INFINITY, Float.MIN_VALUE) < 0, "compare(-Inf, MIN) < 0");
        check(Float.compare(Float.MIN_VALUE, 0.0f) > 0, "compare(MIN_VALUE, 0.0) > 0");
        check(Float.sum(Float.MAX_VALUE, Float.MAX_VALUE) == Float.POSITIVE_INFINITY,
              "sum(MAX, MAX) overflows to +Inf");
        check(Float.floatToIntBits(Float.MAX_VALUE) == 0x7f7fffff, "bits(MAX)");
        check(Float.floatToIntBits(Float.MIN_VALUE) == 0x1, "bits(MIN_VALUE)");
        check(Float.floatToIntBits(-0.0f) == 0x80000000 && Float.compare(-0.0f, 0.0f) < 0,
              "valueOf(-0.0) preserves sign");
    }

    static void testFloat16() {
        check(Float.float16ToFloat((short) 0x3C00) == 1.0f, "float16ToFloat(0x3C00)==1.0");
        check(Float.float16ToFloat((short) 0x0000) == 0.0f, "float16ToFloat(0x0000)==0.0");
        check(Float.float16ToFloat((short) 0x3800) == 0.5f, "float16ToFloat(0x3800)==0.5");
        check(Float.float16ToFloat((short) 0xBC00) == -1.0f, "float16ToFloat(0xBC00)==-1.0");
        check(Float.float16ToFloat((short) 0x7C00) == Float.POSITIVE_INFINITY,
              "float16ToFloat(0x7C00)==+Inf");
        check(Float.float16ToFloat((short) 0xFC00) == Float.NEGATIVE_INFINITY,
              "float16ToFloat(0xFC00)==-Inf");
        check(Float.isNaN(Float.float16ToFloat((short) 0x7E00)), "float16ToFloat(0x7E00) is NaN");
    }

    static void testBoxing() {
        Float boxed = 1.5f;
        check(boxed.floatValue() == 1.5f, "unboxing recovers float");
        Float sum = boxed + 0.5f;                  // unbox, add, rebox
        check(sum == 2.0f, "boxed arithmetic re-boxes");
        check(sum.compareTo(boxed) > 0, "compareTo after arithmetic");
        check(Float.isNaN(Float.NaN + 0.0f), "autobox arithmetic propagates NaN");
    }

    // ---- Black-box / behavioral ----
    static void testBlackBox() {
        testConstants();
        testParse();
        testNaNInfinity();
        testSignedZero();
        testBitConversion();
        testCompareEquals();
        testHashCodeToString();
        testSumMaxMin();
        testBoundaryParseAndFormat();
        testFloat16();
        testBoxing();
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

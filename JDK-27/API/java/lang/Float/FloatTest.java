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

    // ---- Contract deepening (2026-11): parse matrix, bit-level round-trip,
    // compare semantics, sum/min/max zero+NaN details, hashCode, toString
    // version gates (JDK-19+ Ryu shortest representation), numeric conversions.
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testParseMatrix() {
        check(Float.parseFloat("1e3") == 1000.0f, "parseFloat(1e3)");
        check(Float.parseFloat("1.0f") == 1.0f, "parseFloat(1.0f) trailing f");
        check(Float.parseFloat("2.5d") == 2.5f, "parseFloat(2.5d) trailing d");
        check(Float.parseFloat("2.5D") == 2.5f, "parseFloat(2.5D) trailing D");
        check(Float.parseFloat("1.0F") == 1.0f, "parseFloat(1.0F) trailing F");
        check(Float.parseFloat("1.23E-10") == 1.23E-10f, "parseFloat(1.23E-10)");
        check(Float.parseFloat("0x1.8P1") == 3.0f, "parseFloat(0x1.8P1) uppercase P");
        check(Float.parseFloat("0x.8p1") == 1.0f, "parseFloat(0x.8p1) no integer digit");
        check(Float.parseFloat("+1.5") == 1.5f, "parseFloat(+1.5) leading plus");
        check(Float.parseFloat("+Infinity") == Float.POSITIVE_INFINITY, "parseFloat(+Infinity)");
        check(Float.isNaN(Float.parseFloat("  NaN  ")), "parseFloat(  NaN  ) trimmed");
        check(Float.parseFloat("\t1.5\n") == 1.5f, "parseFloat tab/newline trimmed");
        check(Float.floatToRawIntBits(Float.parseFloat("-0.0")) == 0x80000000,
              "parseFloat(-0.0) keeps sign bit");
        check(Float.parseFloat("0x1.0p-126") == Float.MIN_NORMAL, "parseFloat hex MIN_NORMAL");
        check(Float.parseFloat("0x1.0p128") == Float.POSITIVE_INFINITY, "parseFloat hex overflow");
        check(Float.parseFloat("3.4028235E38") == Float.MAX_VALUE, "parseFloat(MAX_VALUE)");
        check(Float.parseFloat("1.4E-45") == Float.MIN_VALUE, "parseFloat(MIN_VALUE)");
        check(Float.parseFloat("3.5E38") == Float.POSITIVE_INFINITY, "parseFloat overflow -> +Inf");
        check(Float.parseFloat("-3.5E38") == Float.NEGATIVE_INFINITY, "parseFloat overflow -> -Inf");
        check(Float.parseFloat("1e-50") == 0.0f, "parseFloat underflow -> +0.0");
        check(Float.floatToRawIntBits(Float.parseFloat("-1e-50")) == 0x80000000,
              "parseFloat(-1e-50) underflow keeps sign -> -0.0");

        // round-trip: parseFloat(toString(x)) == x for representative values.
        float[] vals = {0.0f, -0.0f, 1.5f, -2.75f, 1e10f, 1.23E-10f,
                        Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE,
                        Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (float v : vals) {
            check(Float.parseFloat(Float.toString(v)) == v, "round-trip " + v);
        }
        check(Float.isNaN(Float.parseFloat(Float.toString(Float.NaN))), "round-trip NaN");

        expectNFE(() -> Float.parseFloat("1.2.3"), "parseFloat(1.2.3)");
        expectNFE(() -> Float.parseFloat("+"), "parseFloat(+)");
        expectNFE(() -> Float.parseFloat("e5"), "parseFloat(e5)");
        expectNFE(() -> Float.parseFloat("1..2"), "parseFloat(1..2)");
        expectNFE(() -> Float.parseFloat("1,5"), "parseFloat(1,5)");
        expectNFE(() -> Float.parseFloat("1.0e"), "parseFloat(1.0e)");
        expectNFE(() -> Float.parseFloat("0x1.8"), "parseFloat(0x1.8 without p exponent)");
        expectNFE(() -> Float.parseFloat("   "), "parseFloat(whitespace only)");
        expectNFE(() -> Float.parseFloat("1e+"), "parseFloat(1e+)");
        expectNFE(() -> Float.parseFloat("Infinity1"), "parseFloat(Infinity1)");
        expectNFE(() -> Float.parseFloat("--1"), "parseFloat(--1)");
        expectNPE(() -> Float.parseFloat(null), "parseFloat(null) throws NPE not NFE");
        expectNPE(() -> Float.valueOf((String) null), "valueOf((String) null)");
    }

    static void testBitRoundTrip() {
        float[] vals = {0.0f, -0.0f, 1.0f, -1.0f, 1.5f, 0.1f,
                        Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE,
                        Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (float v : vals) {
            int raw = Float.floatToRawIntBits(v);
            float back = Float.intBitsToFloat(raw);
            if (Float.isNaN(v)) {
                check(Float.isNaN(back), "NaN survives raw-bits round-trip");
            } else {
                check(back == v && Float.floatToRawIntBits(back) == raw,
                      "bit round-trip " + v);
            }
        }
        check(Float.floatToIntBits(Float.intBitsToFloat(0x7fc12345)) == 0x7fc00000,
              "floatToIntBits canonicalizes NaN payload");
        check(Float.floatToRawIntBits(Float.intBitsToFloat(0x7fc12345)) == 0x7fc12345,
              "floatToRawIntBits keeps NaN payload");
        check(Float.isNaN(Float.intBitsToFloat(0x7f800001)),
              "signaling NaN bit pattern is NaN");
        check(Float.intBitsToFloat(0x7f800001) != Float.intBitsToFloat(0x7f800001),
              "NaN of any bit pattern != itself");
        check(Float.isNaN(Float.intBitsToFloat(0xffc00000)), "negative quiet NaN is NaN");
        check(Float.floatToIntBits(Float.MAX_VALUE) == 0x7f7fffff, "bits(MAX_VALUE) == 0x7f7fffff");
        check(Float.floatToIntBits(Float.MIN_VALUE) == 0x00000001, "bits(MIN_VALUE) == 1");
        check(Float.floatToIntBits(Float.MIN_NORMAL) == 0x00800000, "bits(MIN_NORMAL) == 0x00800000");
        check(Float.floatToRawIntBits(1.5f) == Float.floatToIntBits(1.5f),
              "raw == non-raw conversion for non-NaN");
    }

    static void testCompareSemantics() {
        check(Float.compare(-0.0f, 0.0f) == -1, "compare(-0.0, +0.0) == -1 exactly");
        check(Float.compare(0.0f, -0.0f) == 1, "compare(+0.0, -0.0) == +1 exactly");
        check(0.0f == -0.0f, "== says +0.0 == -0.0 (JLS 4.2.3)");
        check(Float.compare(Float.NaN, Float.POSITIVE_INFINITY) == 1, "compare(NaN, Inf) == 1");
        check(Float.compare(Float.POSITIVE_INFINITY, Float.NaN) == -1, "compare(Inf, NaN) == -1");
        check(Float.compare(Float.NaN, Float.NEGATIVE_INFINITY) == 1, "compare(NaN, -Inf) == 1");
        check(Float.compare(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE) < 0, "compare(-Inf, -MAX) < 0");
        check(Float.compare(Float.MAX_VALUE, Float.POSITIVE_INFINITY) < 0, "compare(MAX, Inf) < 0");
        check(Float.intBitsToFloat(0x7fc01234) != Float.intBitsToFloat(0x7fc01234),
              "NaN with payload still != itself");
        check(Float.valueOf(Float.NaN).equals(Float.intBitsToFloat(0x7fc01234)),
              "equals: any NaN bit pattern equals canonical NaN");
    }

    static void testClassificationMatrix() {
        float[] specials = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                            1.5f, -0.0f};
        boolean[] nanExp = {true, false, false, false, false};
        boolean[] infExp = {false, true, true, false, false};
        boolean[] finExp = {false, false, false, true, true};
        for (int i = 0; i < specials.length; i++) {
            check(Float.isNaN(specials[i]) == nanExp[i], "static isNaN matrix " + i);
            check(Float.isInfinite(specials[i]) == infExp[i], "static isInfinite matrix " + i);
            check(Float.isFinite(specials[i]) == finExp[i], "static isFinite matrix " + i);
            Float box = Float.valueOf(specials[i]);
            check(box.isNaN() == nanExp[i], "instance isNaN matrix " + i);
            check(box.isInfinite() == infExp[i], "instance isInfinite matrix " + i);
        }
        check(Float.isFinite(Float.MAX_VALUE), "isFinite(MAX_VALUE)");
        check(Float.isFinite(Float.MIN_VALUE), "isFinite(MIN_VALUE)");
    }

    static void testSumMinMaxDetails() {
        check(Float.sum(1.5f, 2.5f) == 4.0f, "sum(1.5,2.5)");
        check(Float.sum(-1.5f, -2.5f) == -4.0f, "sum(-1.5,-2.5)");
        check(Float.floatToRawIntBits(Float.sum(0.0f, -0.0f)) == 0, "sum(+0,-0) == +0.0 (JLS 4.2.4)");
        check(Float.floatToRawIntBits(Float.sum(-0.0f, -0.0f)) == 0x80000000,
              "sum(-0,-0) == -0.0 (JLS 4.2.4)");
        check(Float.isNaN(Float.sum(Float.NaN, 0.0f)), "sum(NaN, x) is NaN");
        check(Float.isNaN(Float.sum(0.0f, Float.NaN)), "sum(x, NaN) is NaN");
        check(Float.isNaN(Float.sum(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)),
              "sum(Inf, -Inf) is NaN");
        check(Float.sum(Float.MAX_VALUE, Float.MAX_VALUE) == Float.POSITIVE_INFINITY,
              "sum(MAX,MAX) overflows to +Inf");
        check(Float.sum(-Float.MAX_VALUE, -Float.MAX_VALUE) == Float.NEGATIVE_INFINITY,
              "sum(-MAX,-MAX) overflows to -Inf");
        check(Float.max(1.5f, 2.5f) == 2.5f && Float.min(1.5f, 2.5f) == 1.5f,
              "max/min pick larger/smaller");
        check(Float.max(2.5f, 1.5f) == 2.5f, "max commutes");
        check(Float.isNaN(Float.max(Float.NaN, 1.0f)), "max(NaN, x) is NaN");
        check(Float.isNaN(Float.max(1.0f, Float.NaN)), "max(x, NaN) is NaN");
        check(Float.isNaN(Float.min(Float.NaN, 1.0f)), "min(NaN, x) is NaN");
        check(Float.isNaN(Float.min(1.0f, Float.NaN)), "min(x, NaN) is NaN");
        check(Float.floatToRawIntBits(Float.max(0.0f, -0.0f)) == 0, "max(+0,-0) == +0.0");
        check(Float.floatToRawIntBits(Float.max(-0.0f, 0.0f)) == 0, "max(-0,+0) == +0.0");
        check(Float.floatToRawIntBits(Float.min(-0.0f, 0.0f)) == 0x80000000, "min(-0,+0) == -0.0");
        check(Float.floatToRawIntBits(Float.min(0.0f, -0.0f)) == 0x80000000, "min(+0,-0) == -0.0");
        check(Float.max(Float.POSITIVE_INFINITY, Float.MAX_VALUE) == Float.POSITIVE_INFINITY,
              "max(Inf, MAX) == Inf");
        check(Float.min(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE) == Float.NEGATIVE_INFINITY,
              "min(-Inf, -MAX) == -Inf");
        check(Float.max(Float.NEGATIVE_INFINITY, Float.MAX_VALUE) == Float.MAX_VALUE,
              "max(-Inf, MAX) == MAX");
    }

    static void testHashCodeContract() {
        check(Float.hashCode(1.5f) == Float.floatToIntBits(1.5f), "static hashCode == bits");
        check(new Float(1.5f).hashCode() == Float.hashCode(1.5f),
              "instance hashCode == static hashCode (ctor deprecated on 9+)");
        check(Float.valueOf(1.5f).hashCode() == Float.hashCode(1.5f), "boxed hashCode == static");
        float[] vals = {0.0f, -0.0f, 1.5f, Float.NaN, Float.POSITIVE_INFINITY,
                        Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL};
        for (float v : vals) {
            check(Float.hashCode(v) == Float.floatToIntBits(v), "hashCode formula " + v);
        }
        check(Float.valueOf(Float.NaN).hashCode() == 0x7fc00000,
              "hashCode(NaN) == canonical NaN bits");
        check(Float.valueOf(-0.0f).hashCode() != Float.valueOf(0.0f).hashCode(),
              "hashCode distinguishes signed zero");
    }

    static void testToStringAndHex() {
        check(Float.toHexString(1.0f).equals("0x1.0p0"), "toHexString(1.0f)");
        check(Float.toHexString(3.0f).equals("0x1.8p1"), "toHexString(3.0f)");
        check(Float.toHexString(-3.0f).equals("-0x1.8p1"), "toHexString(-3.0f)");
        check(Float.toHexString(0.0f).equals("0x0.0p0"), "toHexString(+0.0f)");
        check(Float.toHexString(-0.0f).equals("-0x0.0p0"), "toHexString(-0.0f)");
        check(Float.toHexString(Float.NaN).equals("NaN"), "toHexString(NaN)");
        check(Float.toHexString(Float.POSITIVE_INFINITY).equals("Infinity"), "toHexString(+Inf)");
        check(Float.toHexString(Float.NEGATIVE_INFINITY).equals("-Infinity"), "toHexString(-Inf)");
        check(Float.toHexString(Float.MAX_VALUE).equals("0x1.fffffep127"), "toHexString(MAX)");
        check(Float.toHexString(Float.MIN_VALUE).equals("0x0.000002p-126"), "toHexString(MIN_VALUE)");
        check(Float.toHexString(Float.MIN_NORMAL).equals("0x1.0p-126"), "toHexString(MIN_NORMAL)");
        float[] hexable = {1.0f, 3.0f, -3.0f, 0.0f, -0.0f,
                           Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL};
        for (float v : hexable) {
            check(Float.parseFloat(Float.toHexString(v)) == v, "parse(toHexString) round-trip " + v);
        }
        check(Float.toString(0.1f).equals("0.1"), "toString(0.1f)");
        check(Float.toString(100.0f).equals("100.0"), "toString(100.0f)");
        check(Float.toString(0.002f).equals("0.002"), "toString(2e-3f)");
        check(Float.toString(1.23E-10f).equals("1.23E-10"), "toString(1.23E-10f)");
        check(Float.toString(3.0E-5f).equals("3.0E-5"), "toString(3.0E-5f)");
        // JDK-19+ switched Float.toString to the shortest (Ryu) representation:
        // identical bits, different (shorter) text on 19+; lock each side's actual value.
        float tricky = Float.intBitsToFloat(0x4c013604);
        check(Float.toString(tricky).equals(MODERN ? "3.387189E7" : "3.3871888E7"),
              "toString(bits 0x4c013604) is version-dependent");
        check(Float.parseFloat(Float.toString(tricky)) == tricky,
              "tricky value still round-trips on both versions");
        check(Float.toString(Float.MIN_NORMAL).equals(MODERN ? "1.1754944E-38" : "1.17549435E-38"),
              "toString(MIN_NORMAL) is version-dependent");
        check(Float.parseFloat(Float.toString(Float.MIN_NORMAL)) == Float.MIN_NORMAL,
              "MIN_NORMAL round-trips on both versions");
    }

    static void testNumericConversions() {
        check((int) Float.NaN == 0, "(int) NaN == 0 (JLS 5.1.3)");
        check((int) Float.POSITIVE_INFINITY == Integer.MAX_VALUE, "(int) +Inf saturates to MAX");
        check((int) Float.NEGATIVE_INFINITY == Integer.MIN_VALUE, "(int) -Inf saturates to MIN");
        check((int) 3.9f == 3, "(int) 3.9f truncates toward zero");
        check((int) -3.9f == -3, "(int) -3.9f truncates toward zero");
        check(Float.valueOf(3.9f).intValue() == 3, "Number.intValue truncates");
        check(Float.valueOf(3.9f).longValue() == 3L, "Number.longValue truncates");
        check(Float.valueOf(3.9f).byteValue() == 3, "Number.byteValue truncates");
        check(Float.valueOf(3.9f).shortValue() == 3, "Number.shortValue truncates");
        check(Float.valueOf(3.9f).floatValue() == 3.9f, "Number.floatValue identity");
        check(Float.valueOf(3.9f).doubleValue() == (double) 3.9f, "Number.doubleValue widening");
        check(Float.valueOf(Float.MAX_VALUE).intValue() == Integer.MAX_VALUE,
              "MAX_VALUE.intValue saturates");
        check(Float.valueOf(Float.NaN).intValue() == 0, "NaN.intValue() == 0");
        check(Float.valueOf(-3.9f).intValue() == -3, "-3.9f.intValue() == -3");
        // float -> double widening is lossless; double -> float rounds.
        check((double) 0.1f == 0.10000000149011612d, "0.1f widened exactly");
        check((float) (double) Float.MAX_VALUE == Float.MAX_VALUE, "f->d->f lossless for MAX");
        check((float) 0.1 == 0.1f, "double->float rounding of 0.1");
        check((float) 0.2 == 0.2f, "double->float rounding of 0.2");
        check((float) Math.PI == 3.1415927f, "(float) PI rounds to 3.1415927f");
        check((float) 1e300 == Float.POSITIVE_INFINITY, "(float) 1e300 overflows to +Inf");
        check((float) Double.MAX_VALUE == Float.POSITIVE_INFINITY, "(float) double MAX -> +Inf");
        // no valueOf caching: fresh instances are distinct but equal.
        check(new Float(1.5f) != new Float(1.5f) && new Float(1.5f).equals(new Float(1.5f)),
              "constructor instances distinct-but-equal (no cache, ctor deprecated on 9+)");
        check(Float.valueOf(1.5f).equals(Float.valueOf(1.5f)), "valueOf equals");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testParseMatrix();
        testBitRoundTrip();
        testCompareSemantics();
        testClassificationMatrix();
        testSumMinMaxDetails();
        testHashCodeContract();
        testToStringAndHex();
        testNumericConversions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

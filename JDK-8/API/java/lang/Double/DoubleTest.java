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

    // ---- Contract deepening (2026-11): parse matrix, bit-level round-trip,
    // compare semantics, sum/min/max zero+NaN details, hashCode, toString
    // version gates (JDK-19+ Ryu shortest representation), numeric conversions.
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    static void testParseMatrix() {
        check(Double.parseDouble("1e3") == 1000.0, "parseDouble(1e3)");
        check(Double.parseDouble("1.0f") == 1.0, "parseDouble(1.0f) trailing f");
        check(Double.parseDouble("2.5d") == 2.5, "parseDouble(2.5d) trailing d");
        check(Double.parseDouble("2.5D") == 2.5, "parseDouble(2.5D) trailing D");
        check(Double.parseDouble("1.0F") == 1.0, "parseDouble(1.0F) trailing F");
        check(Double.parseDouble("1.23E-10") == 1.23E-10, "parseDouble(1.23E-10)");
        check(Double.parseDouble("0x1.8P1") == 3.0, "parseDouble(0x1.8P1) uppercase P");
        check(Double.parseDouble("0x.8p1") == 1.0, "parseDouble(0x.8p1) no integer digit");
        check(Double.parseDouble("+1.5") == 1.5, "parseDouble(+1.5) leading plus");
        check(Double.parseDouble("+Infinity") == Double.POSITIVE_INFINITY, "parseDouble(+Infinity)");
        check(Double.isNaN(Double.parseDouble("  NaN  ")), "parseDouble(  NaN  ) trimmed");
        check(Double.parseDouble("\t1.5\n") == 1.5, "parseDouble tab/newline trimmed");
        check(Double.doubleToRawLongBits(Double.parseDouble("-0.0")) == 0x8000000000000000L,
              "parseDouble(-0.0) keeps sign bit");
        check(Double.parseDouble("0x1.0p-1022") == Double.MIN_NORMAL, "parseDouble hex MIN_NORMAL");
        check(Double.parseDouble("0x1.0p1024") == Double.POSITIVE_INFINITY, "parseDouble hex overflow");
        check(Double.parseDouble("1e309") == Double.POSITIVE_INFINITY, "parseDouble overflow -> +Inf");
        check(Double.parseDouble("-1e309") == Double.NEGATIVE_INFINITY, "parseDouble overflow -> -Inf");
        check(Double.parseDouble("1e-400") == 0.0, "parseDouble underflow -> +0.0");
        check(Double.doubleToRawLongBits(Double.parseDouble("-1e-400")) == 0x8000000000000000L,
              "parseDouble(-1e-400) underflow keeps sign -> -0.0");
        check(Double.parseDouble("1.7976931348623157E308") == Double.MAX_VALUE,
              "parseDouble(MAX_VALUE)");
        check(Double.parseDouble("4.9E-324") == Double.MIN_VALUE, "parseDouble(MIN_VALUE)");

        // round-trip: parseDouble(toString(x)) == x for representative values.
        double[] vals = {0.0, -0.0, 1.5, -2.75, 0.1, 1e10, 1e23, 1.23E-10,
                         Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE,
                         Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e-300};
        for (double v : vals) {
            check(Double.parseDouble(Double.toString(v)) == v, "round-trip " + v);
        }
        check(Double.isNaN(Double.parseDouble(Double.toString(Double.NaN))), "round-trip NaN");
        check(Double.parseDouble("1.0E23") == 1e23, "parseDouble(1.0E23) == 1e23 bits");

        expectNFE(() -> Double.parseDouble("1.2.3"), "parseDouble(1.2.3)");
        expectNFE(() -> Double.parseDouble("+"), "parseDouble(+)");
        expectNFE(() -> Double.parseDouble("e5"), "parseDouble(e5)");
        expectNFE(() -> Double.parseDouble("1..2"), "parseDouble(1..2)");
        expectNFE(() -> Double.parseDouble("1,5"), "parseDouble(1,5)");
        expectNFE(() -> Double.parseDouble("1.0e"), "parseDouble(1.0e)");
        expectNFE(() -> Double.parseDouble("0x1.8"), "parseDouble(0x1.8 without p exponent)");
        expectNFE(() -> Double.parseDouble("   "), "parseDouble(whitespace only)");
        expectNFE(() -> Double.parseDouble("1e+"), "parseDouble(1e+)");
        expectNFE(() -> Double.parseDouble("Infinity1"), "parseDouble(Infinity1)");
        expectNFE(() -> Double.parseDouble("--1"), "parseDouble(--1)");
        expectNPE(() -> Double.parseDouble(null), "parseDouble(null) throws NPE not NFE");
        expectNPE(() -> Double.valueOf((String) null), "valueOf((String) null)");
    }

    static void testBitRoundTrip() {
        double[] vals = {0.0, -0.0, 1.0, -1.0, 1.5, 0.1,
                         Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE,
                         Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e23};
        for (double v : vals) {
            long raw = Double.doubleToRawLongBits(v);
            double back = Double.longBitsToDouble(raw);
            if (Double.isNaN(v)) {
                check(Double.isNaN(back), "NaN survives raw-bits round-trip");
            } else {
                check(back == v && Double.doubleToRawLongBits(back) == raw,
                      "bit round-trip " + v);
            }
        }
        check(Double.doubleToLongBits(Double.longBitsToDouble(0x7ff8123456789abcL))
              == 0x7ff8000000000000L, "doubleToLongBits canonicalizes NaN payload");
        check(Double.doubleToRawLongBits(Double.longBitsToDouble(0x7ff8123456789abcL))
              == 0x7ff8123456789abcL, "doubleToRawLongBits keeps NaN payload");
        check(Double.isNaN(Double.longBitsToDouble(0x7ff0000000000001L)),
              "signaling NaN bit pattern is NaN");
        check(Double.longBitsToDouble(0x7ff8abcdef123456L)
              != Double.longBitsToDouble(0x7ff8abcdef123456L),
              "NaN of any bit pattern != itself");
        check(Double.isNaN(Double.longBitsToDouble(0xfff8000000000000L)),
              "negative quiet NaN is NaN");
        check(Double.doubleToLongBits(Double.MAX_VALUE) == 0x7fefffffffffffffL,
              "bits(MAX_VALUE) == 0x7fefffffffffffff");
        check(Double.doubleToLongBits(Double.MIN_VALUE) == 0x0000000000000001L,
              "bits(MIN_VALUE) == 1");
        check(Double.doubleToLongBits(Double.MIN_NORMAL) == 0x0010000000000000L,
              "bits(MIN_NORMAL) == 0x0010000000000000");
        check(Double.doubleToRawLongBits(1.5) == Double.doubleToLongBits(1.5),
              "raw == non-raw conversion for non-NaN");
    }

    static void testCompareSemantics() {
        check(Double.compare(-0.0, 0.0) == -1, "compare(-0.0, +0.0) == -1 exactly");
        check(Double.compare(0.0, -0.0) == 1, "compare(+0.0, -0.0) == +1 exactly");
        check(0.0 == -0.0, "== says +0.0 == -0.0 (JLS 4.2.3)");
        check(Double.compare(Double.NaN, Double.POSITIVE_INFINITY) == 1, "compare(NaN, Inf) == 1");
        check(Double.compare(Double.POSITIVE_INFINITY, Double.NaN) == -1, "compare(Inf, NaN) == -1");
        check(Double.compare(Double.NaN, Double.NEGATIVE_INFINITY) == 1, "compare(NaN, -Inf) == 1");
        check(Double.compare(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE) < 0,
              "compare(-Inf, -MAX) < 0");
        check(Double.compare(Double.MAX_VALUE, Double.POSITIVE_INFINITY) < 0,
              "compare(MAX, Inf) < 0");
        check(Double.longBitsToDouble(0x7ff8123456789abcL)
              != Double.longBitsToDouble(0x7ff8123456789abcL),
              "NaN with payload still != itself");
        check(Double.valueOf(Double.NaN).equals(Double.longBitsToDouble(0x7ff8123456789abcL)),
              "equals: any NaN bit pattern equals canonical NaN");
    }

    static void testClassificationMatrix() {
        double[] specials = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                             1.5, -0.0};
        boolean[] nanExp = {true, false, false, false, false};
        boolean[] infExp = {false, true, true, false, false};
        boolean[] finExp = {false, false, false, true, true};
        for (int i = 0; i < specials.length; i++) {
            check(Double.isNaN(specials[i]) == nanExp[i], "static isNaN matrix " + i);
            check(Double.isInfinite(specials[i]) == infExp[i], "static isInfinite matrix " + i);
            check(Double.isFinite(specials[i]) == finExp[i], "static isFinite matrix " + i);
            Double box = Double.valueOf(specials[i]);
            check(box.isNaN() == nanExp[i], "instance isNaN matrix " + i);
            check(box.isInfinite() == infExp[i], "instance isInfinite matrix " + i);
        }
        check(Double.isFinite(Double.MAX_VALUE), "isFinite(MAX_VALUE)");
        check(Double.isFinite(Double.MIN_VALUE), "isFinite(MIN_VALUE)");
    }

    static void testSumMinMaxDetails() {
        check(Double.sum(1.5, 2.5) == 4.0, "sum(1.5,2.5)");
        check(Double.sum(-1.5, -2.5) == -4.0, "sum(-1.5,-2.5)");
        check(Double.doubleToRawLongBits(Double.sum(0.0, -0.0)) == 0,
              "sum(+0,-0) == +0.0 (JLS 4.2.4)");
        check(Double.doubleToRawLongBits(Double.sum(-0.0, -0.0)) == 0x8000000000000000L,
              "sum(-0,-0) == -0.0 (JLS 4.2.4)");
        check(Double.isNaN(Double.sum(Double.NaN, 0.0)), "sum(NaN, x) is NaN");
        check(Double.isNaN(Double.sum(0.0, Double.NaN)), "sum(x, NaN) is NaN");
        check(Double.isNaN(Double.sum(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)),
              "sum(Inf, -Inf) is NaN");
        check(Double.isNaN(Double.sum(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)),
              "sum(-Inf, Inf) is NaN");
        check(Double.sum(Double.MAX_VALUE, Double.MAX_VALUE) == Double.POSITIVE_INFINITY,
              "sum(MAX,MAX) overflows to +Inf");
        check(Double.sum(-Double.MAX_VALUE, -Double.MAX_VALUE) == Double.NEGATIVE_INFINITY,
              "sum(-MAX,-MAX) overflows to -Inf");
        check(Double.max(1.5, 2.5) == 2.5 && Double.min(1.5, 2.5) == 1.5,
              "max/min pick larger/smaller");
        check(Double.max(2.5, 1.5) == 2.5, "max commutes");
        check(Double.isNaN(Double.max(Double.NaN, 1.0)), "max(NaN, x) is NaN");
        check(Double.isNaN(Double.max(1.0, Double.NaN)), "max(x, NaN) is NaN");
        check(Double.isNaN(Double.min(Double.NaN, 1.0)), "min(NaN, x) is NaN");
        check(Double.isNaN(Double.min(1.0, Double.NaN)), "min(x, NaN) is NaN");
        check(Double.doubleToRawLongBits(Double.max(0.0, -0.0)) == 0, "max(+0,-0) == +0.0");
        check(Double.doubleToRawLongBits(Double.max(-0.0, 0.0)) == 0, "max(-0,+0) == +0.0");
        check(Double.doubleToRawLongBits(Double.min(-0.0, 0.0)) == 0x8000000000000000L,
              "min(-0,+0) == -0.0");
        check(Double.doubleToRawLongBits(Double.min(0.0, -0.0)) == 0x8000000000000000L,
              "min(+0,-0) == -0.0");
        check(Double.max(Double.POSITIVE_INFINITY, Double.MAX_VALUE)
              == Double.POSITIVE_INFINITY, "max(Inf, MAX) == Inf");
        check(Double.min(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE)
              == Double.NEGATIVE_INFINITY, "min(-Inf, -MAX) == -Inf");
        check(Double.max(Double.NEGATIVE_INFINITY, Double.MAX_VALUE) == Double.MAX_VALUE,
              "max(-Inf, MAX) == MAX");
    }

    static void testHashCodeContract() {
        check(Double.hashCode(1.5) == Double.valueOf(1.5).hashCode(), "static == boxed hashCode");
        check(new Double(1.5).hashCode() == Double.hashCode(1.5),
              "ctor hashCode == static (ctor deprecated on 9+)");
        double[] vals = {0.0, -0.0, 1.5, Double.NaN, Double.POSITIVE_INFINITY,
                         Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL};
        for (double v : vals) {
            long bits = Double.doubleToLongBits(v);
            check(Double.hashCode(v) == (int) (bits ^ (bits >>> 32)), "hashCode formula " + v);
            check(Double.valueOf(v).hashCode() == Double.hashCode(v), "boxed hashCode " + v);
        }
        long nanBits = 0x7ff8000000000000L;
        check(Double.hashCode(Double.NaN) == (int) (nanBits ^ (nanBits >>> 32)),
              "hashCode(NaN) formula on canonical bits");
        check(Double.valueOf(-0.0).hashCode() != Double.valueOf(0.0).hashCode(),
              "hashCode distinguishes signed zero");
    }

    static void testToStringAndHex() {
        check(Double.toString(1.0).equals("1.0"), "toString(1.0)");
        check(Double.toString(0.1).equals("0.1"), "toString(0.1)");
        check(Double.toString(100.0).equals("100.0"), "toString(100.0)");
        check(Double.toString(2e-3).equals("0.002"), "toString(2e-3)");
        check(Double.toString(3.0E-5).equals("3.0E-5"), "toString(3.0E-5)");
        check(Double.toString(1.0E-4).equals("1.0E-4"), "toString(1.0E-4)");
        check(Double.toString(1.23E-10).equals("1.23E-10"), "toString(1.23E-10)");
        check(Double.toString(1.0E17).equals("1.0E17"), "toString(1.0E17)");
        check(Double.toString(1.0E21).equals("1.0E21"), "toString(1.0E21)");
        check(Double.toString(0.001).equals("0.001"), "toString(0.001)");
        check(Double.toString(1.0E-7).equals("1.0E-7"), "toString(1.0E-7)");
        check(Double.toString(Double.MIN_VALUE).equals("4.9E-324"), "toString(MIN_VALUE)");
        check(Double.toString(Double.MAX_VALUE).equals("1.7976931348623157E308"),
              "toString(MAX_VALUE)");
        check(Double.toString(Double.MIN_NORMAL).equals("2.2250738585072014E-308"),
              "toString(MIN_NORMAL)");
        // JDK-19+ switched Double.toString to the shortest (Ryu) representation:
        // identical bits, different text on 19+; lock each side's actual value.
        check(Double.toString(1e23).equals(MODERN ? "1.0E23" : "9.999999999999999E22"),
              "toString(1e23) is version-dependent");
        check(Double.parseDouble(Double.toString(1e23)) == 1e23,
              "1e23 round-trips on both versions");
        check(Double.toString(Double.longBitsToDouble(0x0000000000000002L))
              .equals(MODERN ? "9.9E-324" : "1.0E-323"),
              "toString(2*MIN_VALUE subnormal) is version-dependent");
        check(Double.parseDouble(Double.toString(Double.longBitsToDouble(0x0000000000000002L)))
              == Double.longBitsToDouble(0x0000000000000002L),
              "2*MIN_VALUE round-trips on both versions");
        check(Double.valueOf(1.5).toString().equals("1.5"), "instance toString");
        check(Double.toHexString(1.0).equals("0x1.0p0"), "toHexString(1.0)");
        check(Double.toHexString(3.0).equals("0x1.8p1"), "toHexString(3.0)");
        check(Double.toHexString(-3.0).equals("-0x1.8p1"), "toHexString(-3.0)");
        check(Double.toHexString(0.0).equals("0x0.0p0"), "toHexString(+0.0)");
        check(Double.toHexString(-0.0).equals("-0x0.0p0"), "toHexString(-0.0)");
        check(Double.toHexString(Double.NaN).equals("NaN"), "toHexString(NaN)");
        check(Double.toHexString(Double.POSITIVE_INFINITY).equals("Infinity"), "toHexString(+Inf)");
        check(Double.toHexString(Double.NEGATIVE_INFINITY).equals("-Infinity"), "toHexString(-Inf)");
        check(Double.toHexString(Double.MAX_VALUE).equals("0x1.fffffffffffffp1023"),
              "toHexString(MAX)");
        check(Double.toHexString(Double.MIN_VALUE).equals("0x0.0000000000001p-1022"),
              "toHexString(MIN_VALUE)");
        double[] hexable = {1.5, 3.0, -3.0, 0.0, -0.0,
                            Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_NORMAL};
        for (double v : hexable) {
            check(Double.parseDouble(Double.toHexString(v)) == v,
                  "parse(toHexString) round-trip " + v);
        }
    }

    static void testNumericConversions() {
        check((int) Double.NaN == 0, "(int) NaN == 0 (JLS 5.1.3)");
        check((int) Double.POSITIVE_INFINITY == Integer.MAX_VALUE, "(int) +Inf saturates to MAX");
        check((int) Double.NEGATIVE_INFINITY == Integer.MIN_VALUE, "(int) -Inf saturates to MIN");
        check((int) 3.9 == 3, "(int) 3.9 truncates toward zero");
        check((int) -3.9 == -3, "(int) -3.9 truncates toward zero");
        check((long) Double.NaN == 0L, "(long) NaN == 0");
        check((long) Double.POSITIVE_INFINITY == Long.MAX_VALUE, "(long) +Inf saturates to MAX");
        check((long) Double.NEGATIVE_INFINITY == Long.MIN_VALUE, "(long) -Inf saturates to MIN");
        check(Double.valueOf(3.9).intValue() == 3, "Number.intValue truncates");
        check(Double.valueOf(3.9).longValue() == 3L, "Number.longValue truncates");
        check(Double.valueOf(3.9).byteValue() == 3, "Number.byteValue truncates");
        check(Double.valueOf(3.9).shortValue() == 3, "Number.shortValue truncates");
        check(Double.valueOf(3.9).floatValue() == 3.9f, "Number.floatValue narrows");
        check(Double.valueOf(3.9).doubleValue() == 3.9, "Number.doubleValue identity");
        check(Double.valueOf(Double.MAX_VALUE).longValue() == Long.MAX_VALUE,
              "MAX_VALUE.longValue saturates");
        check(Double.valueOf(Double.NaN).intValue() == 0, "NaN.intValue() == 0");
        check(Double.valueOf(-3.9).intValue() == -3, "-3.9.intValue() == -3");
        // double -> float rounds (and can overflow); float -> double widening is lossless.
        check((float) 0.1 == 0.1f, "double->float rounding of 0.1");
        check((float) Math.PI == 3.1415927f, "(float) PI rounds to 3.1415927f");
        check((float) 1e300 == Float.POSITIVE_INFINITY, "(float) 1e300 overflows to +Inf");
        check((float) Double.MAX_VALUE == Float.POSITIVE_INFINITY,
              "(float) Double.MAX_VALUE -> +Inf");
        check((double) 0.1f == 0.10000000149011612d, "0.1f widened exactly");
        check((float) (double) Float.MAX_VALUE == Float.MAX_VALUE, "f->d->f lossless for MAX");
        // no valueOf caching: fresh instances are distinct but equal.
        check(new Double(1.5) != new Double(1.5) && new Double(1.5).equals(new Double(1.5)),
              "constructor instances distinct-but-equal (no cache, ctor deprecated on 9+)");
        check(Double.valueOf(1.5).equals(Double.valueOf(1.5)), "valueOf equals");
    }

    public static void main(String[] args) {
        testConstants();
        testParse();
        testNaNInfinity();
        testSignedZero();
        testBitConversion();
        testCompareEquals();
        testHashCodeToString();
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

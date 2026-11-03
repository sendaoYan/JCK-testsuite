/*
 * @test
 * @summary java.lang.Short conformance (white-box structural + black-box
 *          behavioral): constants, parsing, decode, string/unsigned conversion,
 *          comparison (incl. unsigned), boxing.
 *
 * Design case
 * -----------
 * 1. Contract: Short is an immutable wrapper over a 16-bit signed short; parse/
 *    decode factories reject malformed input with NumberFormatException.
 * 2. Usage: valueOf/parseShort/decode -> shortValue()/intValue() -> toString/
 *    toUnsignedInt/toUnsignedLong -> compare/compareUnsigned/compareTo.
 * 3. Composition: valueOf(short) [cache -128..127] -> shortValue -> toString ->
 *    parseShort round-trip; autobox -> arithmetic -> rebox -> compareTo -> equals;
 *    compareUnsigned treats the sign bit as magnitude.
 * 4. Invariants: immutability; valueOf identity for -128..127; equals by short
 *    value only; hashCode == value.
 * 5. Boundary/exception: MIN_VALUE==-32768/MAX_VALUE==32767/SIZE==16/BYTES==2;
 *    parse overflow NFE; reverseBytes swaps bytes.
 * @run main ShortTest
 */

public class ShortTest {
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

    static void testConstants() {
        check(Short.MIN_VALUE == -32768, "MIN_VALUE == -32768");
        check(Short.MIN_VALUE == (short) 0x8000, "MIN_VALUE == 0x8000");
        check(Short.MAX_VALUE == 32767, "MAX_VALUE == 32767");
        check(Short.MAX_VALUE == 0x7fff, "MAX_VALUE == 0x7fff");
        check(Short.SIZE == 16, "SIZE == 16");
        check(Short.BYTES == 2, "BYTES == 2");
        check(Short.TYPE == short.class, "TYPE == short.class");
    }

    static void testParseShort() {
        check(Short.parseShort("42") == 42, "parseShort(42)");
        check(Short.parseShort("-42") == -42, "parseShort(-42)");
        check(Short.parseShort("+1") == 1, "parseShort(+1)");
        check(Short.parseShort("32767") == Short.MAX_VALUE, "parseShort(MAX)");
        check(Short.parseShort("-32768") == Short.MIN_VALUE, "parseShort(MIN)");
        check(Short.parseShort("-0") == 0, "parseShort(-0)");

        expectNFE(() -> Short.parseShort(null), "parseShort(null)");
        expectNFE(() -> Short.parseShort(""), "parseShort(empty)");
        expectNFE(() -> Short.parseShort(" 42"), "parseShort(leading space)");
        expectNFE(() -> Short.parseShort("42 "), "parseShort(trailing space)");
        expectNFE(() -> Short.parseShort("32768"), "parseShort(32768 overflow)");
        expectNFE(() -> Short.parseShort("-32769"), "parseShort(-32769 overflow)");
        expectNFE(() -> Short.parseShort("1_0"), "parseShort(underscore)");
    }

    static void testParseShortRadix() {
        check(Short.parseShort("1010", 2) == 10, "parseShort(1010,2)");
        check(Short.parseShort("ff", 16) == 255, "parseShort(ff,16)");
        check(Short.parseShort("7fff", 16) == 32767, "parseShort(7fff,16)");
        check(Short.parseShort("-8000", 16) == -32768, "parseShort(-8000,16)");
        check(Short.parseShort("z", 36) == 35, "parseShort(z,36)");

        expectNFE(() -> Short.parseShort("10", 1), "radix 1");
        expectNFE(() -> Short.parseShort("10", 37), "radix 37");
        expectNFE(() -> Short.parseShort("8000", 16), "positive overflow hex");
        expectNFE(() -> Short.parseShort("-8001", 16), "negative overflow hex");
    }

    static void testValueOf() {
        check(Short.valueOf("42") == 42, "valueOf(42)");
        check(Short.valueOf("-32768") == Short.MIN_VALUE, "valueOf(-32768)");
        check(Short.valueOf("32767") == Short.MAX_VALUE, "valueOf(32767)");
        check(Short.valueOf("42", 16) == 66, "valueOf(42,16)");

        // Only -128..127 are cached by valueOf(short), like Integer.
        check(Short.valueOf((short) -128) == Short.valueOf((short) -128), "valueOf(-128) cached");
        check(Short.valueOf((short) 127) == Short.valueOf((short) 127), "valueOf(127) cached");
        check(Short.valueOf((short) 128) != Short.valueOf((short) 128), "valueOf(128) not cached");
        check(Short.valueOf((short) -129) != Short.valueOf((short) -129), "valueOf(-129) not cached");

        expectNFE(() -> Short.valueOf("x"), "valueOf(x)");
        expectNFE(() -> Short.valueOf("32768"), "valueOf(32768 overflow)");
    }

    static void testDecode() {
        check(Short.decode("42") == 42, "decode(42)");
        check(Short.decode("-42") == -42, "decode(-42)");
        check(Short.decode("+10") == 10, "decode(+10)");
        check(Short.decode("0x7fff") == 32767, "decode(0x7fff)");
        check(Short.decode("0X7FFF") == 32767, "decode(0X7FFF)");
        check(Short.decode("-0x8000") == -32768, "decode(-0x8000)");
        check(Short.decode("#7fff") == 32767, "decode(#7fff)");
        check(Short.decode("017") == 15, "decode(017) octal");

        expectNFE(() -> Short.decode("0x"), "decode(0x bare)");
        expectNFE(() -> Short.decode("#"), "decode(# bare)");
        expectNFE(() -> Short.decode("0x8000"), "decode(0x8000 overflow)");
        expectNFE(() -> Short.decode("0xFFFF"), "decode(0xFFFF overflow)");
        expectNFE(() -> Short.decode("10L"), "decode(L suffix not accepted)");
    }

    static void testToStringUnsigned() {
        check(Short.toString((short) 42).equals("42"), "toString(42)");
        check(Short.toString(Short.MIN_VALUE).equals("-32768"), "toString(MIN)");
        check(Short.toUnsignedInt((short) -1) == 65535, "toUnsignedInt(-1) == 65535");
        check(Short.toUnsignedInt(Short.MIN_VALUE) == 32768, "toUnsignedInt(-32768) == 32768");
        check(Short.toUnsignedLong((short) -1) == 65535L, "toUnsignedLong(-1) == 65535");
    }

    static void testCompare() {
        check(Short.compare((short) 1, (short) 2) < 0, "compare(1,2) < 0");
        check(Short.compare((short) 2, (short) 1) > 0, "compare(2,1) > 0");
        check(Short.compare((short) 1, (short) 1) == 0, "compare(1,1) == 0");
        check(Short.valueOf((short) 1).compareTo(Short.valueOf((short) 2)) < 0, "compareTo < 0");

        // Unsigned comparison treats the sign bit as magnitude (JDK 9+).
        check(Short.compareUnsigned((short) -1, (short) 0) > 0, "compareUnsigned(-1,0) > 0");
        check(Short.compareUnsigned((short) 1, (short) -1) < 0, "compareUnsigned(1,-1) < 0");
        check(Short.compareUnsigned((short) 1, (short) 1) == 0, "compareUnsigned(1,1) == 0");
    }

    static void testReverseBytes() {
        check(Short.reverseBytes((short) 0x1234) == 0x3412, "reverseBytes(0x1234)");
        check(Short.reverseBytes((short) 0x00ff) == (short) 0xff00, "reverseBytes(0x00ff)");
    }

    static void testHashCodeEquals() {
        check(Short.valueOf((short) 42).hashCode() == 42, "hashCode(42) == 42");
        check(Short.valueOf(Short.MIN_VALUE).hashCode() == -32768, "hashCode(MIN) == -32768");
        check(Short.valueOf((short) 42).equals(Short.valueOf((short) 42)), "equals same");
        check(!Short.valueOf((short) 42).equals(Short.valueOf((short) 43)), "!equals different");
        check(!Short.valueOf((short) 42).equals(Integer.valueOf(42)), "!equals Integer");
        check(Short.valueOf((short) 42).byteValue() == 42, "byteValue");
        check(Short.valueOf((short) 42).shortValue() == 42, "shortValue");
        check(Short.valueOf((short) 42).intValue() == 42, "intValue");
        check(Short.valueOf((short) 42).longValue() == 42L, "longValue");
        check(Short.valueOf((short) 42).floatValue() == 42.0f, "floatValue");
        check(Short.valueOf((short) 42).doubleValue() == 42.0, "doubleValue");
    }

    // ---- White-box / structural ----
    static void testWhiteBox() {
        try {
            Class<?> c = Class.forName("java.lang.Short");
            check(!c.isInterface(), "Short is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Short is final");
            check(c.getSuperclass() == Number.class, "Short extends Number");
            check(Comparable.class.isAssignableFrom(c), "Short implements Comparable");
            check(c.getField("MAX_VALUE").getType() == short.class
                  && java.lang.reflect.Modifier.isStatic(c.getField("MAX_VALUE").getModifiers()),
                  "MAX_VALUE is public static final short");
            check(c.getMethod("parseShort", String.class).getReturnType() == short.class,
                  "parseShort(String) returns short");
            check(c.getMethod("valueOf", short.class).getReturnType() == Short.class,
                  "valueOf(short) returns Short");
            check(c.getMethod("compareUnsigned", short.class, short.class).getReturnType()
                  == int.class, "compareUnsigned(short,short) returns int");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testBoxing() {
        // JLS 5.1.7: autoboxing maps the short literal through Short.valueOf(short).
        Short boxed = 42;
        check(boxed == Short.valueOf((short) 42), "autobox 42 uses cached valueOf");
        short unboxed = boxed;                     // unboxing via shortValue()
        check(unboxed == 42, "unboxing recovers short");
        Short sum = (short) (boxed + 8);           // unbox, add, rebox
        check(sum == Short.valueOf((short) 50), "boxed arithmetic re-boxes");
        check(sum.compareTo(boxed) > 0, "compareTo after arithmetic");
        check(!sum.equals(boxed), "!equals distinct values");
        check(sum.hashCode() == 50, "hashCode follows value");
    }

    // ---- Black-box / behavioral ----
    static void testBlackBox() {
        testConstants();
        testParseShort();
        testParseShortRadix();
        testValueOf();
        testDecode();
        testToStringUnsigned();
        testCompare();
        testReverseBytes();
        testHashCodeEquals();
        testBoxing();
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

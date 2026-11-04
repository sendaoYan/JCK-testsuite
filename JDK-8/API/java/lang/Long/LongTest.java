/*
 * @test
 * @summary java.lang.Long API conformance: constants, parsing (parseLong,
 *          parseUnsignedLong, valueOf, decode), string conversion (toString and
 *          the to{Hex,Octal,Binary,Unsigned}String family), bit operations,
 *          unsigned arithmetic, comparison, and the boxed-value cache.
 * @run main LongTest
 */

public class LongTest {
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

    static void expectArithmeticException(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected ArithmeticException");
        } catch (ArithmeticException expected) {
            passed++;
        }
    }

    static void testConstants() {
        check(Long.MIN_VALUE == 0x8000000000000000L, "MIN_VALUE == 0x8000000000000000L");
        check(Long.MIN_VALUE == -9223372036854775808L, "MIN_VALUE == -9223372036854775808");
        check(Long.MAX_VALUE == 0x7fffffffffffffffL, "MAX_VALUE == 0x7fffffffffffffffL");
        check(Long.MAX_VALUE == 9223372036854775807L, "MAX_VALUE == 9223372036854775807");
        check(Long.SIZE == 64, "SIZE == 64");
        check(Long.BYTES == 8, "BYTES == 8");
        check(Long.TYPE == long.class, "TYPE == long.class");
    }

    static void testParseLong() {
        check(Long.parseLong("42") == 42L, "parseLong(42)");
        check(Long.parseLong("-42") == -42L, "parseLong(-42)");
        check(Long.parseLong("+1") == 1L, "parseLong(+1)");
        check(Long.parseLong("0") == 0L, "parseLong(0)");
        check(Long.parseLong("-0") == 0L, "parseLong(-0)");
        check(Long.parseLong("9223372036854775807") == Long.MAX_VALUE, "parseLong(MAX)");
        check(Long.parseLong("-9223372036854775808") == Long.MIN_VALUE, "parseLong(MIN)");

        expectNFE(() -> Long.parseLong(null), "parseLong(null)");
        expectNFE(() -> Long.parseLong(""), "parseLong(empty)");
        expectNFE(() -> Long.parseLong(" 42"), "parseLong(leading space)");
        expectNFE(() -> Long.parseLong("42 "), "parseLong(trailing space)");
        expectNFE(() -> Long.parseLong("1_0"), "parseLong(underscore)");
        expectNFE(() -> Long.parseLong("9223372036854775808"), "parseLong(overflow +)");
        expectNFE(() -> Long.parseLong("-9223372036854775809"), "parseLong(overflow -)");
        expectNFE(() -> Long.parseLong("1L"), "parseLong(L suffix)");
        expectNFE(() -> Long.parseLong("0x10"), "parseLong(0x prefix)");
        expectNFE(() -> Long.parseLong("abc"), "parseLong(alpha)");
    }

    static void testParseLongRadix() {
        check(Long.parseLong("1010", 2) == 10L, "parseLong(1010,2)");
        check(Long.parseLong("-1010", 2) == -10L, "parseLong(-1010,2)");
        check(Long.parseLong("ff", 16) == 255L, "parseLong(ff,16)");
        check(Long.parseLong("FF", 16) == 255L, "parseLong(FF,16)");
        check(Long.parseLong("z", 36) == 35L, "parseLong(z,36)");
        check(Long.parseLong("10", 36) == 36L, "parseLong(10,36)");
        check(Long.parseLong("7fffffffffffffff", 16) == Long.MAX_VALUE, "parseLong(7fff..,16)");
        check(Long.parseLong("-8000000000000000", 16) == Long.MIN_VALUE, "parseLong(-8000..,16)");
        // 63 one-bits == 2^63 - 1 == MAX_VALUE
        check(Long.parseLong("111111111111111111111111111111111111111111111111111111111111111", 2)
              == Long.MAX_VALUE, "parseLong(63 ones,2)");
        check(Long.parseLong("-1000000000000000000000000000000000000000000000000000000000000000", 2)
              == Long.MIN_VALUE, "parseLong(-2^63,2)");

        expectNFE(() -> Long.parseLong("10", 1), "radix 1");
        expectNFE(() -> Long.parseLong("10", 0), "radix 0");
        expectNFE(() -> Long.parseLong("10", 37), "radix 37");
        expectNFE(() -> Long.parseLong("8000000000000000", 16), "positive overflow hex");
        expectNFE(() -> Long.parseLong("ffffffffffffffff", 16), "0xffffffffffffffff > MAX");
        expectNFE(() -> Long.parseLong("-ffffffffffffffff", 16), "negative overflow hex");
    }

    static void testParseUnsignedLong() {
        check(Long.parseUnsignedLong("0") == 0L, "parseUnsignedLong(0)");
        check(Long.parseUnsignedLong("42") == 42L, "parseUnsignedLong(42)");
        check(Long.parseUnsignedLong("+1") == 1L, "parseUnsignedLong(+1)");
        check(Long.parseUnsignedLong("9223372036854775807") == Long.MAX_VALUE,
              "parseUnsignedLong(9223372036854775807)");
        check(Long.parseUnsignedLong("9223372036854775808") == 0x8000000000000000L,
              "parseUnsignedLong(2^63) == MIN bits");
        check(Long.parseUnsignedLong("18446744073709551615") == -1L,
              "parseUnsignedLong(2^64-1) == -1");
        check(Long.parseUnsignedLong("ffffffffffffffff", 16) == -1L,
              "parseUnsignedLong(ffffffffffffffff,16)");

        expectNFE(() -> Long.parseUnsignedLong("-1"), "parseUnsignedLong(-1)");
        expectNFE(() -> Long.parseUnsignedLong("18446744073709551616"), "parseUnsignedLong(2^64)");
        expectNFE(() -> Long.parseUnsignedLong(""), "parseUnsignedLong(empty)");
    }

    static void testValueOf() {
        check(Long.valueOf("42") == 42L, "valueOf(42)");
        check(Long.valueOf("-42") == -42L, "valueOf(-42)");
        check(Long.valueOf("42", 16) == 66L, "valueOf(42,16)");

        // JLS 5.1.7 / Long cache: -128..127 are cached by valueOf(long).
        check(Long.valueOf(-128) == Long.valueOf(-128), "valueOf(-128) cached");
        check(Long.valueOf(127) == Long.valueOf(127), "valueOf(127) cached");
        check(Long.valueOf(0) == Long.valueOf(0), "valueOf(0) cached");
        check(Long.valueOf(128) != Long.valueOf(128), "valueOf(128) not cached");
        check(Long.valueOf(-129) != Long.valueOf(-129), "valueOf(-129) not cached");

        expectNFE(() -> Long.valueOf("x"), "valueOf(x)");
        expectNFE(() -> Long.valueOf("9223372036854775808"), "valueOf(overflow)");
    }

    static void testToString() {
        check(Long.toString(0L).equals("0"), "toString(0)");
        check(Long.toString(42L).equals("42"), "toString(42)");
        check(Long.toString(-42L).equals("-42"), "toString(-42)");
        check(Long.toString(Long.MAX_VALUE).equals("9223372036854775807"), "toString(MAX)");
        check(Long.toString(Long.MIN_VALUE).equals("-9223372036854775808"), "toString(MIN)");

        check(Long.toString(255L, 16).equals("ff"), "toString(255,16)");
        check(Long.toString(255L, 2).equals("11111111"), "toString(255,2)");
        check(Long.toString(255L, 8).equals("377"), "toString(255,8)");
        check(Long.toString(-1L, 16).equals("-1"), "toString(-1,16)");
        check(Long.toString(Long.MIN_VALUE, 16).equals("-8000000000000000"), "toString(MIN,16)");
        check(Long.toString(255L, 37).equals("255"), "toString(255,37) defaults to 10");
        check(Long.toString(255L, 1).equals("255"), "toString(255,1) defaults to 10");
    }

    static void testRadixStrings() {
        check(Long.toHexString(255L).equals("ff"), "toHexString(255)");
        check(Long.toHexString(-1L).equals("ffffffffffffffff"), "toHexString(-1)");
        check(Long.toHexString(Long.MIN_VALUE).equals("8000000000000000"), "toHexString(MIN)");

        check(Long.toOctalString(8L).equals("10"), "toOctalString(8)");
        check(Long.toOctalString(-1L).equals("1777777777777777777777"), "toOctalString(-1)");

        check(Long.toBinaryString(5L).equals("101"), "toBinaryString(5)");
        check(Long.toBinaryString(-1L).length() == 64, "toBinaryString(-1) 64 bits");

        check(Long.toUnsignedString(-1L).equals("18446744073709551615"), "toUnsignedString(-1)");
        check(Long.toUnsignedString(0L).equals("0"), "toUnsignedString(0)");
        check(Long.toUnsignedString(-1L, 16).equals("ffffffffffffffff"), "toUnsignedString(-1,16)");
        check(Long.toUnsignedString(0x8000000000000000L).equals("9223372036854775808"),
              "toUnsignedString(0x8000000000000000)");
    }

    static void testDecode() {
        check(Long.decode("42") == 42L, "decode(42)");
        check(Long.decode("-42") == -42L, "decode(-42)");
        check(Long.decode("+10") == 10L, "decode(+10)");
        check(Long.decode("0x10") == 16L, "decode(0x10)");
        check(Long.decode("0X10") == 16L, "decode(0X10)");
        check(Long.decode("+0x10") == 16L, "decode(+0x10)");
        check(Long.decode("-0X10") == -16L, "decode(-0X10)");
        check(Long.decode("#ff") == 255L, "decode(#ff)");
        check(Long.decode("010") == 8L, "decode(010) octal");
        check(Long.decode("0x7fffffffffffffff") == Long.MAX_VALUE, "decode(0x7fff..)");
        check(Long.decode("-0x8000000000000000") == Long.MIN_VALUE, "decode(-0x8000..)");

        expectNFE(() -> Long.decode(""), "decode(empty)");
        expectNFE(() -> Long.decode("0x"), "decode(0x bare)");
        expectNFE(() -> Long.decode("#"), "decode(# bare)");
        expectNFE(() -> Long.decode("0x8000000000000000"), "decode(positive overflow)");
        expectNFE(() -> Long.decode("0xFFFFFFFFFFFFFFFF"), "decode(0xFFFF..)");
        expectNFE(() -> Long.decode("10L"), "decode(L suffix not accepted)");
    }

    static void testConversions() {
        Long v = Long.valueOf(0x1122334455667788L);
        check(v.byteValue() == (byte) 0x88, "byteValue truncates");
        check(v.shortValue() == (short) 0x7788, "shortValue truncates");
        check(v.intValue() == 0x55667788, "intValue truncates high half");
        check(v.longValue() == 0x1122334455667788L, "longValue identity");
        check(v.doubleValue() == (double) 0x1122334455667788L, "doubleValue converts");
    }

    static void testHashCodeEquals() {
        // Long.hashCode is (int)(value ^ (value >>> 32)) per the API contract.
        check(Long.valueOf(42L).hashCode() == 42, "hashCode(42) == 42");
        check(Long.valueOf(0x1122334455667788L).hashCode()
              == (int) (0x1122334455667788L ^ (0x1122334455667788L >>> 32)),
              "hashCode == XOR of halves");
        check(Long.valueOf(42L).equals(Long.valueOf(42L)), "equals same value");
        check(!Long.valueOf(42L).equals(Long.valueOf(43L)), "!equals different value");
        check(!Long.valueOf(42L).equals(Integer.valueOf(42)), "!equals Integer");
        check(!Long.valueOf(42L).equals(null), "!equals null");
    }

    static void testCompare() {
        check(Long.compare(1L, 2L) < 0, "compare(1,2) < 0");
        check(Long.compare(2L, 1L) > 0, "compare(2,1) > 0");
        check(Long.compare(1L, 1L) == 0, "compare(1,1) == 0");
        check(Long.compare(Long.MIN_VALUE, Long.MAX_VALUE) < 0, "compare(MIN,MAX) < 0");
        check(Long.valueOf(1L).compareTo(Long.valueOf(2L)) < 0, "compareTo < 0");

        check(Long.compareUnsigned(-1L, 0L) > 0, "compareUnsigned(-1,0) > 0");
        check(Long.compareUnsigned(1L, -1L) < 0, "compareUnsigned(1,-1) < 0");
        check(Long.compareUnsigned(0x8000000000000000L, Long.MAX_VALUE) > 0,
              "compareUnsigned(2^63,MAX) > 0");
        check(Long.compareUnsigned(1L, 1L) == 0, "compareUnsigned(1,1) == 0");
    }

    static void testBitOps() {
        check(Long.highestOneBit(0L) == 0L, "highestOneBit(0)");
        check(Long.highestOneBit(5L) == 4L, "highestOneBit(5)");
        check(Long.highestOneBit(-1L) == Long.MIN_VALUE, "highestOneBit(-1)");

        check(Long.lowestOneBit(0L) == 0L, "lowestOneBit(0)");
        check(Long.lowestOneBit(6L) == 2L, "lowestOneBit(6)");
        check(Long.lowestOneBit(-1L) == 1L, "lowestOneBit(-1)");

        check(Long.numberOfLeadingZeros(0L) == 64, "numberOfLeadingZeros(0)");
        check(Long.numberOfLeadingZeros(1L) == 63, "numberOfLeadingZeros(1)");
        check(Long.numberOfLeadingZeros(0x8000000000000000L) == 0,
              "numberOfLeadingZeros(2^63)");

        check(Long.numberOfTrailingZeros(0L) == 64, "numberOfTrailingZeros(0)");
        check(Long.numberOfTrailingZeros(1L) == 0, "numberOfTrailingZeros(1)");
        check(Long.numberOfTrailingZeros(0x8000000000000000L) == 63,
              "numberOfTrailingZeros(2^63)");

        check(Long.bitCount(0L) == 0, "bitCount(0)");
        check(Long.bitCount(-1L) == 64, "bitCount(-1)");
        check(Long.bitCount(5L) == 2, "bitCount(5)");

        check(Long.rotateLeft(1L, 1) == 2L, "rotateLeft(1,1)");
        check(Long.rotateLeft(0x8000000000000000L, 1) == 1L, "rotateLeft(2^63,1)");
        check(Long.rotateLeft(1L, -1) == Long.rotateLeft(1L, 63), "rotateLeft negative dist");
        check(Long.rotateLeft(1L, 64) == 1L, "rotateLeft dist 64 == identity");

        check(Long.rotateRight(1L, 1) == 0x8000000000000000L, "rotateRight(1,1)");
        check(Long.rotateRight(0x8000000000000000L, 1) == 0x4000000000000000L,
              "rotateRight(2^63,1)");

        check(Long.reverse(1L) == 0x8000000000000000L, "reverse(1)");
        check(Long.reverse(0x8000000000000000L) == 1L, "reverse(2^63)");
        check(Long.reverseBytes(0x0102030405060708L) == 0x0807060504030201L,
              "reverseBytes(0x0102030405060708)");

        check(Long.signum(0L) == 0, "signum(0)");
        check(Long.signum(5L) == 1, "signum(5)");
        check(Long.signum(-5L) == -1, "signum(-5)");
        check(Long.signum(Long.MIN_VALUE) == -1, "signum(MIN)");
    }

    static void testUnsignedOps() {
        check(Long.divideUnsigned(-1L, 2L) == 9223372036854775807L, "divideUnsigned(-1,2)");
        check(Long.divideUnsigned(10L, 3L) == 3L, "divideUnsigned(10,3)");
        check(Long.remainderUnsigned(-1L, 2L) == 1L, "remainderUnsigned(-1,2)");
        check(Long.remainderUnsigned(10L, 3L) == 1L, "remainderUnsigned(10,3)");
        expectArithmeticException(() -> Long.divideUnsigned(1L, 0L), "divideUnsigned by zero");
        expectArithmeticException(() -> Long.remainderUnsigned(1L, 0L), "remainderUnsigned by zero");
    }

    static void testSumMaxMin() {
        check(Long.sum(1L, 2L) == 3L, "sum(1,2)");
        check(Long.sum(Long.MAX_VALUE, 1L) == Long.MIN_VALUE, "sum overflow wraps");
        check(Long.max(1L, 2L) == 2L, "max(1,2)");
        check(Long.min(1L, 2L) == 1L, "min(1,2)");
    }

    public static void main(String[] args) {
        testConstants();
        testParseLong();
        testParseLongRadix();
        testParseUnsignedLong();
        testValueOf();
        testToString();
        testRadixStrings();
        testDecode();
        testConversions();
        testHashCodeEquals();
        testCompare();
        testBitOps();
        testUnsignedOps();
        testSumMaxMin();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.Integer API conformance: constants, parsing (parseInt,
 *          parseUnsignedInt, valueOf, decode), string conversion (toString and
 *          the to{Hex,Octal,Binary,Unsigned}String family), bit operations,
 *          unsigned arithmetic, comparison, and the boxed-value cache.
 * @run main IntegerTest
 */

public class IntegerTest {
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
        check(Integer.MIN_VALUE == 0x80000000, "MIN_VALUE == 0x80000000");
        check(Integer.MIN_VALUE == -2147483648, "MIN_VALUE == -2147483648");
        check(Integer.MAX_VALUE == 0x7fffffff, "MAX_VALUE == 0x7fffffff");
        check(Integer.MAX_VALUE == 2147483647, "MAX_VALUE == 2147483647");
        check(Integer.SIZE == 32, "SIZE == 32");
        check(Integer.BYTES == 4, "BYTES == 4");
        check(Integer.TYPE == int.class, "TYPE == int.class");
        check(Character.MIN_RADIX == 2, "Character.MIN_RADIX == 2");
        check(Character.MAX_RADIX == 36, "Character.MAX_RADIX == 36");
    }

    static void testParseInt() {
        check(Integer.parseInt("42") == 42, "parseInt(42)");
        check(Integer.parseInt("-42") == -42, "parseInt(-42)");
        check(Integer.parseInt("+1") == 1, "parseInt(+1)");
        check(Integer.parseInt("0") == 0, "parseInt(0)");
        check(Integer.parseInt("-0") == 0, "parseInt(-0)");
        check(Integer.parseInt("2147483647") == Integer.MAX_VALUE, "parseInt(MAX)");
        check(Integer.parseInt("-2147483648") == Integer.MIN_VALUE, "parseInt(MIN)");

        expectNFE(() -> Integer.parseInt(null), "parseInt(null)");
        expectNFE(() -> Integer.parseInt(""), "parseInt(empty)");
        expectNFE(() -> Integer.parseInt(" 42"), "parseInt(leading space)");
        expectNFE(() -> Integer.parseInt("42 "), "parseInt(trailing space)");
        expectNFE(() -> Integer.parseInt("1_0"), "parseInt(underscore)");
        expectNFE(() -> Integer.parseInt("2147483648"), "parseInt(overflow +)");
        expectNFE(() -> Integer.parseInt("-2147483649"), "parseInt(overflow -)");
        expectNFE(() -> Integer.parseInt("1.0"), "parseInt(fraction)");
        expectNFE(() -> Integer.parseInt("abc"), "parseInt(alpha)");
        expectNFE(() -> Integer.parseInt("12a"), "parseInt(trailing alpha)");
        expectNFE(() -> Integer.parseInt("0x10"), "parseInt(0x prefix)");
        expectNFE(() -> Integer.parseInt("+-1"), "parseInt(double sign)");
        expectNFE(() -> Integer.parseInt("+"), "parseInt(bare +)");
        expectNFE(() -> Integer.parseInt("-"), "parseInt(bare -)");
    }

    static void testParseIntRadix() {
        check(Integer.parseInt("1010", 2) == 10, "parseInt(1010,2)");
        check(Integer.parseInt("-1010", 2) == -10, "parseInt(-1010,2)");
        check(Integer.parseInt("ff", 16) == 255, "parseInt(ff,16)");
        check(Integer.parseInt("FF", 16) == 255, "parseInt(FF,16)");
        check(Integer.parseInt("+ff", 16) == 255, "parseInt(+ff,16)");
        check(Integer.parseInt("z", 36) == 35, "parseInt(z,36)");
        check(Integer.parseInt("10", 36) == 36, "parseInt(10,36)");
        check(Integer.parseInt("10", 10) == 10, "parseInt(10,10)");
        check(Integer.parseInt("7fffffff", 16) == Integer.MAX_VALUE, "parseInt(7fffffff,16)");
        check(Integer.parseInt("-80000000", 16) == Integer.MIN_VALUE, "parseInt(-80000000,16)");
        // 31 one-bits == 2^31 - 1 == MAX_VALUE
        check(Integer.parseInt("1111111111111111111111111111111", 2) == Integer.MAX_VALUE,
              "parseInt(31 ones,2)");
        // "1" followed by 31 zero-bits == 2^31, representable only as negative
        check(Integer.parseInt("-10000000000000000000000000000000", 2) == Integer.MIN_VALUE,
              "parseInt(-2^31,2)");

        expectNFE(() -> Integer.parseInt("10", 1), "radix 1");
        expectNFE(() -> Integer.parseInt("10", 0), "radix 0");
        expectNFE(() -> Integer.parseInt("10", 37), "radix 37");
        expectNFE(() -> Integer.parseInt("80000000", 16), "positive overflow hex");
        expectNFE(() -> Integer.parseInt("FFFFFFFF", 16), "0xffffffff unsigned > MAX");
        expectNFE(() -> Integer.parseInt("-FFFFFFFF", 16), "negative overflow hex");
        expectNFE(() -> Integer.parseInt("0x10", 16), "0x prefix with radix 16");
        expectNFE(() -> Integer.parseInt("10000000000000000000000000000000", 2),
                  "positive 2^31 in binary");
        expectNFE(() -> Integer.parseInt("-10000000000000000000000000000001", 2),
                  "-(2^31+1) in binary");
    }

    static void testParseUnsignedInt() {
        check(Integer.parseUnsignedInt("0") == 0, "parseUnsignedInt(0)");
        check(Integer.parseUnsignedInt("42") == 42, "parseUnsignedInt(42)");
        check(Integer.parseUnsignedInt("+1") == 1, "parseUnsignedInt(+1)");
        check(Integer.parseUnsignedInt("2147483647") == Integer.MAX_VALUE,
              "parseUnsignedInt(2147483647)");
        check(Integer.parseUnsignedInt("2147483648") == 0x80000000,
              "parseUnsignedInt(2147483648) == MIN bits");
        check(Integer.parseUnsignedInt("4294967295") == -1, "parseUnsignedInt(4294967295) == -1");
        check(Integer.parseUnsignedInt("ffffffff", 16) == -1, "parseUnsignedInt(ffffffff,16)");

        expectNFE(() -> Integer.parseUnsignedInt("-1"), "parseUnsignedInt(-1)");
        expectNFE(() -> Integer.parseUnsignedInt("4294967296"), "parseUnsignedInt(2^32)");
        expectNFE(() -> Integer.parseUnsignedInt(""), "parseUnsignedInt(empty)");
        expectNFE(() -> Integer.parseUnsignedInt("10", 1), "parseUnsignedInt(radix 1)");
    }

    static void testValueOf() {
        check(Integer.valueOf("42") == 42, "valueOf(42)");
        check(Integer.valueOf("-42") == -42, "valueOf(-42)");
        check(Integer.valueOf("42", 16) == 66, "valueOf(42,16)");
        check(Integer.valueOf("42", 16).intValue() == 66, "valueOf(42,16).intValue()");

        // JLS 5.1.7 / Integer cache: -128..127 are cached by valueOf(int).
        check(Integer.valueOf(-128) == Integer.valueOf(-128), "valueOf(-128) cached");
        check(Integer.valueOf(127) == Integer.valueOf(127), "valueOf(127) cached");
        check(Integer.valueOf(0) == Integer.valueOf(0), "valueOf(0) cached");
        check(Integer.valueOf(128) != Integer.valueOf(128), "valueOf(128) not cached");
        check(Integer.valueOf(-129) != Integer.valueOf(-129), "valueOf(-129) not cached");
        check(Integer.valueOf(127).equals(Integer.valueOf(127)), "valueOf(127).equals");

        expectNFE(() -> Integer.valueOf("x"), "valueOf(x)");
        expectNFE(() -> Integer.valueOf("2147483648"), "valueOf(overflow)");
    }

    static void testToString() {
        check(Integer.toString(0).equals("0"), "toString(0)");
        check(Integer.toString(42).equals("42"), "toString(42)");
        check(Integer.toString(-42).equals("-42"), "toString(-42)");
        check(Integer.toString(Integer.MAX_VALUE).equals("2147483647"), "toString(MAX)");
        check(Integer.toString(Integer.MIN_VALUE).equals("-2147483648"), "toString(MIN)");

        check(Integer.toString(255, 16).equals("ff"), "toString(255,16)");
        check(Integer.toString(255, 2).equals("11111111"), "toString(255,2)");
        check(Integer.toString(255, 8).equals("377"), "toString(255,8)");
        check(Integer.toString(-1, 16).equals("-1"), "toString(-1,16)");
        check(Integer.toString(Integer.MIN_VALUE, 16).equals("-80000000"), "toString(MIN,16)");
        // invalid radix (outside 2..36) defaults to radix 10
        check(Integer.toString(255, 37).equals("255"), "toString(255,37) defaults to 10");
        check(Integer.toString(255, 1).equals("255"), "toString(255,1) defaults to 10");
    }

    static void testRadixStrings() {
        check(Integer.toHexString(255).equals("ff"), "toHexString(255)");
        check(Integer.toHexString(-1).equals("ffffffff"), "toHexString(-1)");
        check(Integer.toHexString(Integer.MIN_VALUE).equals("80000000"), "toHexString(MIN)");

        check(Integer.toOctalString(8).equals("10"), "toOctalString(8)");
        check(Integer.toOctalString(-1).equals("37777777777"), "toOctalString(-1)");

        check(Integer.toBinaryString(5).equals("101"), "toBinaryString(5)");
        check(Integer.toBinaryString(-1).length() == 32, "toBinaryString(-1) 32 bits");
        check(Integer.toBinaryString(Integer.MIN_VALUE).equals(
              "10000000000000000000000000000000"), "toBinaryString(MIN)");

        check(Integer.toUnsignedString(-1).equals("4294967295"), "toUnsignedString(-1)");
        check(Integer.toUnsignedString(0).equals("0"), "toUnsignedString(0)");
        check(Integer.toUnsignedString(-1, 16).equals("ffffffff"), "toUnsignedString(-1,16)");
        check(Integer.toUnsignedString(0x80000000).equals("2147483648"),
              "toUnsignedString(0x80000000)");
    }

    static void testDecode() {
        check(Integer.decode("42") == 42, "decode(42)");
        check(Integer.decode("-42") == -42, "decode(-42)");
        check(Integer.decode("+10") == 10, "decode(+10)");
        check(Integer.decode("0x10") == 16, "decode(0x10)");
        check(Integer.decode("0X10") == 16, "decode(0X10)");
        check(Integer.decode("+0x10") == 16, "decode(+0x10)");
        check(Integer.decode("-0X10") == -16, "decode(-0X10)");
        check(Integer.decode("#ff") == 255, "decode(#ff)");
        check(Integer.decode("010") == 8, "decode(010) octal");
        check(Integer.decode("0x7fffffff") == Integer.MAX_VALUE, "decode(0x7fffffff)");
        check(Integer.decode("-0x80000000") == Integer.MIN_VALUE, "decode(-0x80000000)");

        expectNFE(() -> Integer.decode(""), "decode(empty)");
        expectNFE(() -> Integer.decode("0x"), "decode(0x bare)");
        expectNFE(() -> Integer.decode("#"), "decode(# bare)");
        expectNFE(() -> Integer.decode("0x80000000"), "decode(positive overflow)");
        expectNFE(() -> Integer.decode("0xFFFFFFFF"), "decode(0xFFFFFFFF)");
    }

    static void testGetInteger() {
        System.setProperty("jck.int.get", "42");
        check(Integer.getInteger("jck.int.get") == 42, "getInteger(property)");
        check(Integer.getInteger("jck.int.def", 7) == 7, "getInteger(default int)");
        check(Integer.getInteger("jck.int.def", Integer.valueOf(9)) == 9,
              "getInteger(default Integer)");
        check(Integer.getInteger("jck.int.missing") == null, "getInteger(missing) == null");
        System.setProperty("jck.int.bad", "not-a-number");
        check(Integer.getInteger("jck.int.bad") == null, "getInteger(bad value) == null");
        System.clearProperty("jck.int.get");
        System.clearProperty("jck.int.bad");
    }

    static void testConversions() {
        Integer i = Integer.valueOf(0x12345678);
        check(i.byteValue() == (byte) 0x78, "byteValue truncates");
        check(i.shortValue() == (short) 0x5678, "shortValue truncates");
        check(i.intValue() == 0x12345678, "intValue identity");
        check(i.longValue() == 0x12345678L, "longValue widens");
        check(i.floatValue() == (float) 0x12345678, "floatValue converts");
        check(i.doubleValue() == (double) 0x12345678, "doubleValue converts");
    }

    static void testHashCodeEquals() {
        check(Integer.valueOf(42).hashCode() == 42, "hashCode == int value");
        check(Integer.valueOf(Integer.MIN_VALUE).hashCode() == Integer.MIN_VALUE,
              "hashCode(MIN)");
        check(Integer.valueOf(42).equals(Integer.valueOf(42)), "equals same value");
        check(!Integer.valueOf(42).equals(Integer.valueOf(43)), "!equals different value");
        check(!Integer.valueOf(42).equals(Long.valueOf(42)), "!equals Long (type mismatch)");
        check(!Integer.valueOf(42).equals(null), "!equals null");
    }

    static void testCompare() {
        check(Integer.compare(1, 2) < 0, "compare(1,2) < 0");
        check(Integer.compare(2, 1) > 0, "compare(2,1) > 0");
        check(Integer.compare(1, 1) == 0, "compare(1,1) == 0");
        check(Integer.compare(Integer.MIN_VALUE, Integer.MAX_VALUE) < 0, "compare(MIN,MAX) < 0");
        check(Integer.valueOf(1).compareTo(Integer.valueOf(2)) < 0, "compareTo < 0");
        check(Integer.valueOf(1).compareTo(Integer.valueOf(1)) == 0, "compareTo == 0");
        check(Integer.valueOf(2).compareTo(Integer.valueOf(1)) > 0, "compareTo > 0");

        // unsigned comparison treats the sign bit as magnitude
        check(Integer.compareUnsigned(-1, 0) > 0, "compareUnsigned(-1,0) > 0");
        check(Integer.compareUnsigned(-1, 1) > 0, "compareUnsigned(-1,1) > 0");
        check(Integer.compareUnsigned(1, -1) < 0, "compareUnsigned(1,-1) < 0");
        check(Integer.compareUnsigned(0x80000000, 0x7fffffff) > 0,
              "compareUnsigned(0x80000000,MAX) > 0");
        check(Integer.compareUnsigned(1, 1) == 0, "compareUnsigned(1,1) == 0");
    }

    static void testBitOps() {
        check(Integer.highestOneBit(0) == 0, "highestOneBit(0)");
        check(Integer.highestOneBit(5) == 4, "highestOneBit(5)");
        check(Integer.highestOneBit(-1) == Integer.MIN_VALUE, "highestOneBit(-1)");
        check(Integer.highestOneBit(Integer.MIN_VALUE) == Integer.MIN_VALUE,
              "highestOneBit(MIN)");

        check(Integer.lowestOneBit(0) == 0, "lowestOneBit(0)");
        check(Integer.lowestOneBit(6) == 2, "lowestOneBit(6)");
        check(Integer.lowestOneBit(-1) == 1, "lowestOneBit(-1)");
        check(Integer.lowestOneBit(Integer.MIN_VALUE) == Integer.MIN_VALUE, "lowestOneBit(MIN)");

        check(Integer.numberOfLeadingZeros(0) == 32, "numberOfLeadingZeros(0)");
        check(Integer.numberOfLeadingZeros(1) == 31, "numberOfLeadingZeros(1)");
        check(Integer.numberOfLeadingZeros(0x80000000) == 0, "numberOfLeadingZeros(0x80000000)");

        check(Integer.numberOfTrailingZeros(0) == 32, "numberOfTrailingZeros(0)");
        check(Integer.numberOfTrailingZeros(1) == 0, "numberOfTrailingZeros(1)");
        check(Integer.numberOfTrailingZeros(0x80000000) == 31, "numberOfTrailingZeros(0x80000000)");

        check(Integer.bitCount(0) == 0, "bitCount(0)");
        check(Integer.bitCount(-1) == 32, "bitCount(-1)");
        check(Integer.bitCount(5) == 2, "bitCount(5)");
        check(Integer.bitCount(0x80000000) == 1, "bitCount(0x80000000)");

        check(Integer.rotateLeft(1, 1) == 2, "rotateLeft(1,1)");
        check(Integer.rotateLeft(0x80000000, 1) == 1, "rotateLeft(0x80000000,1)");
        check(Integer.rotateLeft(1, -1) == Integer.rotateLeft(1, 31), "rotateLeft negative dist");
        check(Integer.rotateLeft(1, 32) == 1, "rotateLeft dist 32 == identity");

        check(Integer.rotateRight(1, 1) == 0x80000000, "rotateRight(1,1)");
        check(Integer.rotateRight(0x80000000, 1) == 0x40000000, "rotateRight(0x80000000,1)");
        check(Integer.rotateRight(1, -1) == Integer.rotateRight(1, 31), "rotateRight negative dist");

        check(Integer.reverse(1) == 0x80000000, "reverse(1)");
        check(Integer.reverse(0x80000000) == 1, "reverse(0x80000000)");
        check(Integer.reverse(0x55555555) == 0xaaaaaaaa, "reverse(0x55555555)");

        check(Integer.reverseBytes(0x01020304) == 0x04030201, "reverseBytes(0x01020304)");

        check(Integer.signum(0) == 0, "signum(0)");
        check(Integer.signum(5) == 1, "signum(5)");
        check(Integer.signum(-5) == -1, "signum(-5)");
        check(Integer.signum(Integer.MIN_VALUE) == -1, "signum(MIN)");
    }

    static void testUnsignedOps() {
        check(Integer.divideUnsigned(-1, 2) == 2147483647, "divideUnsigned(-1,2)");
        check(Integer.divideUnsigned(10, 3) == 3, "divideUnsigned(10,3)");
        check(Integer.remainderUnsigned(-1, 2) == 1, "remainderUnsigned(-1,2)");
        check(Integer.remainderUnsigned(10, 3) == 1, "remainderUnsigned(10,3)");
        check(Integer.toUnsignedLong(-1) == 4294967295L, "toUnsignedLong(-1)");
        check(Integer.toUnsignedLong(0x80000000) == 2147483648L, "toUnsignedLong(0x80000000)");
        expectArithmeticException(() -> Integer.divideUnsigned(1, 0), "divideUnsigned by zero");
        expectArithmeticException(() -> Integer.remainderUnsigned(1, 0), "remainderUnsigned by zero");
    }

    static void testSumMaxMin() {
        check(Integer.sum(1, 2) == 3, "sum(1,2)");
        check(Integer.sum(Integer.MAX_VALUE, 1) == Integer.MIN_VALUE, "sum overflow wraps");
        check(Integer.max(1, 2) == 2, "max(1,2)");
        check(Integer.max(Integer.MAX_VALUE, Integer.MIN_VALUE) == Integer.MAX_VALUE, "max");
        check(Integer.min(1, 2) == 1, "min(1,2)");
        check(Integer.min(Integer.MIN_VALUE, Integer.MAX_VALUE) == Integer.MIN_VALUE, "min");
    }

    public static void main(String[] args) {
        testConstants();
        testParseInt();
        testParseIntRadix();
        testParseUnsignedInt();
        testValueOf();
        testToString();
        testRadixStrings();
        testDecode();
        testGetInteger();
        testConversions();
        testHashCodeEquals();
        testCompare();
        testBitOps();
        testUnsignedOps();
        testSumMaxMin();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    // ================= 契约深化（2026-11）追加 =================

    /** true 当运行在 JDK 9+（版本串不再以 "1." 开头），用于锁定异常消息文本差异。 */
    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    /** 捕获 NumberFormatException 并逐字比对消息文本（双侧实测锁定）。 */
    static void checkNfeMsg(String expected, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected NumberFormatException with <" + expected + ">");
        } catch (NumberFormatException e) {
            check(expected.equals(e.getMessage()),
                  label + ": expected message <" + expected + "> but got <" + e.getMessage() + ">");
        }
    }

    /** 捕获 ArithmeticException 并逐字比对消息文本。 */
    static void checkArithMsg(String expected, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected ArithmeticException with <" + expected + ">");
        } catch (ArithmeticException e) {
            check(expected.equals(e.getMessage()),
                  label + ": expected message <" + expected + "> but got <" + e.getMessage() + ">");
        }
    }

    static String nullParseMsg() {
        return MODERN ? "Cannot parse null string" : "null";
    }

    static String radixMsg(String s, int radix) {
        return MODERN ? ("For input string: \"" + s + "\" under radix " + radix)
                      : ("For input string: \"" + s + "\"");
    }

    static void testNfeMessageContract() {
        // radix 越界消息（两侧文本一致，实测逐字锁定）
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Long.parseLong("10", 1), "radix MIN_RADIX-1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX",
              () -> Long.parseLong("10", 37), "radix MAX_RADIX+1");
        checkNfeMsg("radix 0 less than Character.MIN_RADIX",
              () -> Long.parseLong("10", 0), "radix 0");
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Long.parseLong("10", Character.MIN_RADIX - 1), "radix Character.MIN_RADIX-1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX",
              () -> Long.parseLong("10", Character.MAX_RADIX + 1), "radix Character.MAX_RADIX+1");
        // 非法字符 / 空 / 空白（10 进制两侧一致）
        checkNfeMsg("For input string: \"z\"", () -> Long.parseLong("z"), "alpha");
        checkNfeMsg("For input string: \"\"", () -> Long.parseLong(""), "empty");
        checkNfeMsg("For input string: \" 42\"", () -> Long.parseLong(" 42"), "leading space");
        checkNfeMsg("For input string: \"42 \"", () -> Long.parseLong("42 "), "trailing space");
        // 超范围消息含原文（19 位十进制边界）
        checkNfeMsg("For input string: \"9223372036854775808\"",
              () -> Long.parseLong("9223372036854775808"), "MAX+1");
        checkNfeMsg("For input string: \"-9223372036854775809\"",
              () -> Long.parseLong("-9223372036854775809"), "MIN-1");
        // null → NFE（而非 NPE）
        checkNfeMsg(nullParseMsg(), () -> Long.parseLong(null), "parseLong(null)");
        checkNfeMsg(nullParseMsg(), () -> Long.parseUnsignedLong(null), "parseUnsignedLong(null)");
        // 非 10 进制失败消息：JDK-19+ 追加 " under radix N"
        checkNfeMsg(radixMsg("8000000000000000", 16),
              () -> Long.parseLong("8000000000000000", 16), "hex overflow");
        checkNfeMsg(radixMsg("z", 16), () -> Long.parseLong("z", 16), "hex alpha");
        checkNfeMsg(radixMsg("1000000000000000000000000000000000000000000000000000000000000000", 2),
              () -> Long.parseLong(
                    "1000000000000000000000000000000000000000000000000000000000000000", 2),
              "binary overflow");
        // 无符号解析消息（两侧一致）
        checkNfeMsg("Illegal leading minus sign on unsigned string -1.",
              () -> Long.parseUnsignedLong("-1"), "unsigned minus");
        checkNfeMsg("String value 18446744073709551616 exceeds range of unsigned long.",
              () -> Long.parseUnsignedLong("18446744073709551616"), "unsigned 2^64");
        checkNfeMsg("For input string: \"z\"", () -> Long.parseUnsignedLong("z"), "unsigned alpha");
        // decode 负例消息
        checkNfeMsg("Zero length string", () -> Long.decode(""), "decode empty");
        checkNfeMsg(radixMsg("", 16), () -> Long.decode("0x"), "decode 0x bare");
        checkNfeMsg(radixMsg("", 16), () -> Long.decode("#"), "decode # bare");
        checkNfeMsg("For input string: \"\"", () -> Long.decode("+"), "decode bare +");
        checkNfeMsg("For input string: \"-\"", () -> Long.decode("-"), "decode bare -");
        checkNfeMsg("For input string: \"123y\"", () -> Long.decode("123y"), "decode 123y");
        checkNfeMsg("For input string: \"10l\"", () -> Long.decode("10l"), "decode l suffix");
        checkNfeMsg("For input string: \"10L\"", () -> Long.decode("10L"), "decode L suffix");
        checkNfeMsg(radixMsg("fl", 16), () -> Long.decode("0xfl"), "decode 0xfl");
        checkNfeMsg(radixMsg("8000000000000000", 16),
              () -> Long.decode("0x8000000000000000"), "decode hex overflow");
        // 无符号除零 ArithmeticException 消息逐字锁定：
        // divideUnsigned(1,0) 两侧均为 "/ by zero"；JDK-8 的 remainderUnsigned/
        // 负被除数路径委托 BigInteger（消息 "BigInteger divide by zero"），JDK-19+ 全为 "/ by zero"
        checkArithMsg("/ by zero", () -> Long.divideUnsigned(1L, 0L), "divideUnsigned(1,0) by 0");
        checkArithMsg(MODERN ? "/ by zero" : "BigInteger divide by zero",
              () -> Long.divideUnsigned(-1L, 0L), "divideUnsigned(-1,0) by 0");
        checkArithMsg(MODERN ? "/ by zero" : "BigInteger divide by zero",
              () -> Long.divideUnsigned(0L, 0L), "divideUnsigned(0,0) by 0");
        checkArithMsg(MODERN ? "/ by zero" : "BigInteger divide by zero",
              () -> Long.remainderUnsigned(1L, 0L), "remainderUnsigned(1,0) by 0");
        checkArithMsg(MODERN ? "/ by zero" : "BigInteger divide by zero",
              () -> Long.remainderUnsigned(-1L, 0L), "remainderUnsigned(-1,0) by 0");
        checkArithMsg(MODERN ? "/ by zero" : "BigInteger divide by zero",
              () -> Long.remainderUnsigned(0L, 0L), "remainderUnsigned(0,0) by 0");
    }

    static void testParseRadixRoundTrip() {
        long[] values = {0L, 1L, -1L, 7L, 42L, 127L, -128L, 255L, 4095L, 65536L,
                         0x7ffffffffffffffeL, 0x8000000000000001L,
                         Long.MAX_VALUE, Long.MIN_VALUE};
        int[] radixes = {2, 8, 10, 16, 36};
        // parse(toString(v, radix), radix) == v 对全进制表闭环
        for (int radix : radixes) {
            for (long v : values) {
                check(Long.parseLong(Long.toString(v, radix), radix) == v,
                      "round-trip toString/parse " + v + " radix " + radix);
            }
        }
        // "+5" 的各进制写法均被接受
        for (int radix : radixes) {
            check(Long.parseLong("+" + Long.toString(5L, radix), radix) == 5L,
                  "parseLong(+5," + radix + ")");
        }
        // "0" / "-0" 在所有进制下均为 0
        for (int radix : radixes) {
            check(Long.parseLong("0", radix) == 0L, "parseLong(0," + radix + ")");
            check(Long.parseLong("-0", radix) == 0L, "parseLong(-0," + radix + ")");
        }
        // 进制边界
        check(Long.parseLong("10", Character.MIN_RADIX) == 2L, "parse at MIN_RADIX");
        check(Long.parseLong("1z", Character.MAX_RADIX) == 71L, "parse 1z at MAX_RADIX");
        check(Long.parseLong("+9223372036854775807") == Long.MAX_VALUE, "parse +MAX");
        // 16 进制补码边界
        check(Long.parseLong("-8000000000000000", 16) == Long.MIN_VALUE, "hex -8000000000000000");
        check(Long.parseLong("7fffffffffffffff", 16) == Long.MAX_VALUE, "hex 7fffffffffffffff");
    }

    static void testToStringExactTable() {
        // Long.MAX_VALUE 十进制 19 位全等 "9223372036854775807"
        check(Long.toString(Long.MAX_VALUE).length() == 19, "MAX decimal length 19");
        check(Long.toString(-255L, 2).equals("-11111111"), "toString(-255,2)");
        check(Long.toString(0L, 2).equals("0"), "toString(0,2)");
        check(Long.toString(0L, 8).equals("0"), "toString(0,8)");
        check(Long.toString(-255L, 8).equals("-377"), "toString(-255,8)");
        check(Long.toString(0L, 16).equals("0"), "toString(0,16)");
        check(Long.toString(-255L, 16).equals("-ff"), "toString(-255,16)");
        check(Long.toString(-1L, 36).equals("-1"), "toString(-1,36)");
        check(Long.toString(0L, 36).equals("0"), "toString(0,36)");
        check(Long.toString(35L, 36).equals("z"), "toString(35,36)");
        check(Long.toString(36L, 36).equals("10"), "toString(36,36)");
        check(Long.toString(Long.MIN_VALUE, 8).equals("-1000000000000000000000"), "toString(MIN,8)");
        // 64 位二进制/十六进制/八进制对负数的精确串
        check(Long.toBinaryString(-255L).equals(
              "1111111111111111111111111111111111111111111111111111111100000001"),
              "toBinaryString(-255)");
        check(Long.toHexString(-255L).equals("ffffffffffffff01"), "toHexString(-255)");
        check(Long.toOctalString(-255L).equals("1777777777777777777401"), "toOctalString(-255)");
        check(Long.toBinaryString(255L).equals("11111111"), "toBinaryString(255)");
        check(Long.toHexString(0L).equals("0"), "toHexString(0)");
        check(Long.toOctalString(0L).equals("0"), "toOctalString(0)");
        // toUnsignedString(long, radix) 精确表
        check(Long.toUnsignedString(0L, 36).equals("0"), "toUnsignedString(0,36)");
        check(Long.toUnsignedString(255L, 36).equals("73"), "toUnsignedString(255,36)");
        check(Long.toUnsignedString(-1L, 8).equals("1777777777777777777777"), "toUnsignedString(-1,8)");
        check(Long.toUnsignedString(-1L, 2).length() == 64, "toUnsignedString(-1,2) 64 bits");
        check(Long.toUnsignedString(-2L, 16).equals("fffffffffffffffe"), "toUnsignedString(-2,16)");
        check(Long.toUnsignedString(-2L).equals("18446744073709551614"), "toUnsignedString(-2)");
    }

    static void testUnsignedBigIntegerClosure() {
        // Long.toUnsignedString 与 BigInteger 的十进制表示一致（类型间闭环）
        java.math.BigInteger uMax = new java.math.BigInteger("18446744073709551615");
        check(Long.toUnsignedString(-1L).equals(uMax.toString()), "toUnsignedString(-1) == 2^64-1");
        java.math.BigInteger u2 = new java.math.BigInteger("18446744073709551614");
        check(Long.toUnsignedString(-2L).equals(u2.toString()), "toUnsignedString(-2)");
        java.math.BigInteger uMin = new java.math.BigInteger("9223372036854775808");
        check(Long.toUnsignedString(Long.MIN_VALUE).equals(uMin.toString()), "toUnsignedString(MIN) == 2^63");
        // 逐样本：toUnsignedString(v) == BigInteger(toBinaryString(v), 2).toString()
        long[] samples = {-1L, -2L, Long.MIN_VALUE, Long.MAX_VALUE, 255L, 0L, 0x8000000000000001L};
        for (long v : samples) {
            check(Long.toUnsignedString(v).equals(
                  new java.math.BigInteger(Long.toBinaryString(v), 2).toString()),
                  "unsigned decimal closure " + v);
        }
        // 无符号除法/取余与 BigInteger 对照
        java.math.BigInteger u = new java.math.BigInteger("18446744073709551615");
        check(Long.divideUnsigned(-1L, 10L) == u.divide(java.math.BigInteger.TEN).longValue(),
              "divideUnsigned(-1,10) matches BigInteger");
        check(Long.remainderUnsigned(-1L, 10L) == u.mod(java.math.BigInteger.TEN).longValue(),
              "remainderUnsigned(-1,10) matches BigInteger");
    }

    static void testBitOpExactTable() {
        check(Long.bitCount(1L) == 1, "bitCount(1)");
        check(Long.bitCount(255L) == 8, "bitCount(255)");
        check(Long.bitCount(0x7fffffffffffffffL) == 63, "bitCount(MAX)");
        check(Long.bitCount(Long.MIN_VALUE) == 1, "bitCount(MIN)");
        check(Long.bitCount(0xF0F0F0F0F0F0F0F0L) == 32, "bitCount(0xF0F0F0F0F0F0F0F0)");
        check(Long.highestOneBit(0x7fffffffffffffffL) == 0x4000000000000000L, "highestOneBit(MAX)");
        check(Long.highestOneBit(Long.MIN_VALUE) == Long.MIN_VALUE, "highestOneBit(MIN)");
        check(Long.lowestOneBit(0x7fffffffffffffffL) == 1L, "lowestOneBit(MAX)");
        check(Long.numberOfLeadingZeros(-1L) == 0, "numberOfLeadingZeros(-1)");
        check(Long.numberOfLeadingZeros(0x7fffffffffffffffL) == 1, "numberOfLeadingZeros(MAX)");
        check(Long.numberOfTrailingZeros(-1L) == 0, "numberOfTrailingZeros(-1)");
        check(Long.numberOfTrailingZeros(0x7fffffffffffffffL) == 0, "numberOfTrailingZeros(MAX)");
        check(Long.reverse(0x00000000000000FFL) == 0xFF00000000000000L, "reverse(0xFF)");
        check(Long.reverse(0xFFFFFFFFFFFFFFFFL) == 0xFFFFFFFFFFFFFFFFL, "reverse(-1)");
        check(Long.reverseBytes(0x00000000000000FFL) == 0xFF00000000000000L, "reverseBytes(0xFF)");
        check(Long.reverseBytes(0xFFFFFFFFFFFFFFFFL) == 0xFFFFFFFFFFFFFFFFL, "reverseBytes(-1)");
        check(Long.reverse(Long.reverse(0x0f0f0f0f0f0f0f0fL)) == 0x0f0f0f0f0f0f0f0fL, "reverse involution");
        check(Long.reverseBytes(Long.reverseBytes(0x0f0f0f0f0f0f0f0fL)) == 0x0f0f0f0f0f0f0f0fL,
              "reverseBytes involution");
        // rotate 恒等律
        long x = 0x123456789abcdef0L;
        check(Long.rotateLeft(x, 0) == x, "rotateLeft(x,0) identity");
        check(Long.rotateLeft(x, 64) == x, "rotateLeft(x,64) identity");
        check(Long.rotateLeft(x, -64) == x, "rotateLeft(x,-64) identity");
        check(Long.rotateRight(x, 0) == x, "rotateRight(x,0) identity");
        check(Long.rotateRight(x, 64) == x, "rotateRight(x,64) identity");
        check(Long.rotateLeft(x, 8) == 0x3456789abcdef012L, "rotateLeft(0x123456789abcdef0,8)");
        check(Long.rotateRight(x, 8) == 0xf0123456789abcdeL, "rotateRight(0x123456789abcdef0,8)");
        check(Long.rotateLeft(0L, 11) == 0L, "rotateLeft(0,11)");
        check(Long.rotateRight(0L, 11) == 0L, "rotateRight(0,11)");
        // 对偶关系抽查
        int[] dists = {1, 7, 31, 32, 63};
        for (int n : dists) {
            check(Long.rotateLeft(x, n) == Long.rotateRight(x, 64 - n),
                  "rotateLeft/rotateRight duality n=" + n);
        }
        check(Long.signum(0x7fffffffffffffffL) == 1, "signum(MAX)");
        check(Long.bitCount(x) + Long.bitCount(~x) == 64, "bitCount complement identity");
        check(Long.highestOneBit(x) == (1L << (63 - Long.numberOfLeadingZeros(x))),
              "highestOneBit derivation");
        check(Long.lowestOneBit(x) == (1L << Long.numberOfTrailingZeros(x)),
              "lowestOneBit derivation");
    }

    static void testUnsignedArithmeticTable() {
        // compareUnsigned 各方向
        check(Long.compareUnsigned(0L, -1L) < 0, "compareUnsigned(0,-1) < 0");
        check(Long.compareUnsigned(-2L, -1L) < 0, "compareUnsigned(-2,-1) < 0");
        check(Long.compareUnsigned(-1L, -2L) > 0, "compareUnsigned(-1,-2) > 0");
        check(Long.compareUnsigned(-1L, -1L) == 0, "compareUnsigned(-1,-1) == 0");
        check(Long.compareUnsigned(Long.MIN_VALUE, -1L) < 0, "compareUnsigned(MIN,-1) < 0");
        check(Long.compareUnsigned(-1L, Long.MAX_VALUE) > 0, "compareUnsigned(-1,MAX) > 0");
        // 2^64-1 / 1 = 2^64-1（补码即 -1，实测锁定；非 MAX_VALUE）
        check(Long.divideUnsigned(-1L, 1L) == -1L, "divideUnsigned(-1,1) == 0xFFFF..FF");
        check(Long.divideUnsigned(-2L, 1L) == -2L, "divideUnsigned(-2,1)");
        check(Long.divideUnsigned(0L, 5L) == 0L, "divideUnsigned(0,5)");
        check(Long.divideUnsigned(-1L, 3L) == 6148914691236517205L, "divideUnsigned(-1,3)");
        check(Long.remainderUnsigned(-1L, 10L) == 5L, "remainderUnsigned(-1,10)");
        check(Long.remainderUnsigned(-1L, 3L) == 0L, "remainderUnsigned(-1,3) == 0");
        check(Long.remainderUnsigned(0L, 7L) == 0L, "remainderUnsigned(0,7)");
    }

    static void testValueOfCacheBoundary() {
        // Long 缓存 -128..127：同一实例（==）、缓存外不同实例但 equals
        check(Long.valueOf(-128L) == Long.valueOf(-128L), "cache -128 identity");
        check(Long.valueOf(127L) == Long.valueOf(127L), "cache 127 identity");
        check(Long.valueOf(128L) != Long.valueOf(128L), "128 not cached");
        check(Long.valueOf(-129L) != Long.valueOf(-129L), "-129 not cached");
        check(Long.valueOf(128L).equals(Long.valueOf(128L)), "128 equals by value");
        check(Long.valueOf(-129L).equals(Long.valueOf(-129L)), "-129 equals by value");
        // 与 new Long 跨缓存实例（构造器已弃用但两侧仍存在）
        @SuppressWarnings("deprecation")
        Long fresh127 = new Long(127L);
        check(fresh127 != Long.valueOf(127L), "new Long(127) bypasses cache");
        check(fresh127.equals(Long.valueOf(127L)), "new Long(127).equals cached");
        @SuppressWarnings("deprecation")
        Long freshMax = new Long(Long.MAX_VALUE);
        check(freshMax.longValue() == Long.MAX_VALUE, "new Long(MAX).longValue");
        check(freshMax.equals(Long.valueOf(Long.MAX_VALUE)), "new Long(MAX).equals");
        // 静态 hashCode 与实例一致（含 -1 → 0 的折叠公式）
        check(Long.hashCode(12345L) == Long.valueOf(12345L).hashCode(), "static hashCode == instance");
        check(Long.hashCode(-1L) == 0, "hashCode(-1) == 0");
        check(Long.hashCode(0L) == 0, "hashCode(0) == 0");
        check(Long.hashCode(1L << 32) == 1, "hashCode(2^32) == 1");
    }

    // ---- White-box / structural ----
    static void testWhiteBoxStructural() {
        try {
            Class<?> c = Class.forName("java.lang.Long");
            check(!c.isInterface(), "Long is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Long is final");
            check(c.getSuperclass() == Number.class, "Long extends Number");
            check(Number.class.getSuperclass() == Object.class, "Number extends Object");
            check(Comparable.class.isAssignableFrom(c), "Long implements Comparable");
            check(java.io.Serializable.class.isAssignableFrom(c), "Long implements Serializable");
            check(c.getField("MAX_VALUE").getType() == long.class
                  && java.lang.reflect.Modifier.isStatic(c.getField("MAX_VALUE").getModifiers())
                  && java.lang.reflect.Modifier.isFinal(c.getField("MAX_VALUE").getModifiers()),
                  "MAX_VALUE is public static final long");
            check(c.getMethod("parseLong", String.class).getReturnType() == long.class,
                  "parseLong(String) returns long");
            check(c.getMethod("valueOf", long.class).getReturnType() == Long.class,
                  "valueOf(long) returns Long");
            check(c.getMethod("toString", long.class, int.class).getReturnType() == String.class,
                  "toString(long,int) returns String");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testBoxing() {
        // JLS 5.1.7: autoboxing maps the long literal through Long.valueOf(long).
        Long boxed = 42L;
        check(boxed == Long.valueOf(42L), "autobox 42 uses cached valueOf");
        long unboxed = boxed;                      // unboxing via longValue()
        check(unboxed == 42L, "unboxing recovers long");
        Long sum = boxed + 8L;                     // unbox, add, rebox
        check(sum == Long.valueOf(50L), "boxed arithmetic re-boxes");
        check(sum.compareTo(boxed) > 0, "compareTo after arithmetic");
        check(boxed.compareTo(Long.valueOf(42L)) == 0, "compareTo equal");
        check(!sum.equals(boxed), "!equals distinct values");
        check(sum.hashCode() == 50, "hashCode follows value");
        Long big1 = 128L, big2 = 128L;
        check(big1 != big2, "autobox 128 not cached (distinct references)");
        check(big1.equals(big2) && big1.longValue() == big2.longValue(),
              "distinct autobox instances are equal by value");
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
        // 契约深化（2026-11）
        testNfeMessageContract();
        testParseRadixRoundTrip();
        testToStringExactTable();
        testUnsignedBigIntegerClosure();
        testBitOpExactTable();
        testUnsignedArithmeticTable();
        testValueOfCacheBoundary();
        testWhiteBoxStructural();
        testBoxing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

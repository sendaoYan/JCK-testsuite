/*
 * @test
 * @summary java.lang.Integer conformance (white-box structural + black-box
 *          behavioral): constants, parsing, string conversion, bit operations,
 *          unsigned arithmetic, comparison, boxing, and the valueOf cache.
 *
 * Design case
 * -----------
 * 1. Contract: Integer is an immutable wrapper over a 32-bit signed int; parse/
 *    decode factories reject malformed input with NumberFormatException.
 * 2. Usage: valueOf/parseInt/decode -> intValue()/longValue() -> toString(radix)/
 *    to{Hex,Octal,Binary,Unsigned}String -> compare/compareUnsigned/compareTo.
 * 3. Composition: valueOf(int) [cache -128..127] -> intValue -> toString(radix) ->
 *    parseInt(radix) round-trip; autobox -> arithmetic -> rebox -> compareTo ->
 *    equals/hashCode; bitCount/highestOneBit/lowestOneBit/numberOfLeadingZeros.
 * 4. Invariants: immutability; valueOf identity for -128..127, distinct-but-equal
 *    otherwise; equals by int value only; hashCode == value.
 * 5. Boundary/exception: MIN_VALUE/MAX_VALUE/SIZE==32/BYTES==4; parse overflow NFE;
 *    divideUnsigned/remainderUnsigned by zero throw ArithmeticException.
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
        System.setProperty("jck.int.bad", "not-a-number");
        try {
            check(Integer.getInteger("jck.int.get") == 42, "getInteger(property)");
            check(Integer.getInteger("jck.int.def", 7) == 7, "getInteger(default int)");
            check(Integer.getInteger("jck.int.def", Integer.valueOf(9)) == 9,
                  "getInteger(default Integer)");
            check(Integer.getInteger("jck.int.missing") == null, "getInteger(missing) == null");
            check(Integer.getInteger("jck.int.bad") == null, "getInteger(bad value) == null");
        } finally {
            System.clearProperty("jck.int.get");
            System.clearProperty("jck.int.bad");
        }
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

    static void testRadixExtremesAndWrap() {
        // radix equivalence classes: MIN_RADIX == 2, MAX_RADIX == 36
        check(Integer.parseInt("10", Character.MIN_RADIX) == 2, "parseInt at MIN_RADIX");
        check(Integer.parseInt("z", Character.MAX_RADIX) == 35, "parseInt z at MAX_RADIX");
        check(Integer.parseInt("+z", Character.MAX_RADIX) == 35, "parseInt +z at MAX_RADIX");
        check(Integer.parseInt(Integer.toString(Integer.MAX_VALUE, Character.MAX_RADIX),
                              Character.MAX_RADIX) == Integer.MAX_VALUE, "MAX round-trip radix 36");
        check(Integer.parseInt(Integer.toString(Integer.MIN_VALUE, Character.MIN_RADIX),
                              Character.MIN_RADIX) == Integer.MIN_VALUE, "MIN round-trip radix 2");
        check(Integer.toString(Integer.MAX_VALUE, Character.MAX_RADIX).equals("zik0zj"),
              "MAX in base 36");
        check(Integer.toString(2, Character.MIN_RADIX).equals("10"), "toString 2 at MIN_RADIX");
        check(Integer.toString(35, Character.MAX_RADIX).equals("z"), "toString 35 at MAX_RADIX");
        // valueOf(int) cache ends at 127/-128; the extremes are never cached
        check(Integer.valueOf(Integer.MAX_VALUE) != Integer.valueOf(Integer.MAX_VALUE),
              "valueOf(MAX) not cached");
        check(Integer.valueOf(Integer.MIN_VALUE) != Integer.valueOf(Integer.MIN_VALUE),
              "valueOf(MIN) not cached");
        check(Integer.valueOf(Integer.MIN_VALUE).equals(Integer.valueOf(Integer.MIN_VALUE)),
              "valueOf(MIN) equals by value");
        // leading plus on the extreme values
        check(Integer.parseInt("+2147483647") == Integer.MAX_VALUE, "parseInt(+MAX)");
        expectNFE(() -> Integer.parseInt("+2147483648"), "parseInt(+MAX+1) overflow");
        // reverse/reverseBytes at the extremes
        check(Integer.reverse(Integer.MIN_VALUE) == 1, "reverse(MIN) == 1");
        check(Integer.reverse(Integer.MAX_VALUE) == -2, "reverse(MAX) == -2");
        check(Integer.reverseBytes(Integer.MAX_VALUE) == 0xffffff7f, "reverseBytes(MAX)");
        check(Integer.reverseBytes(Integer.MIN_VALUE) == 0x80, "reverseBytes(MIN)");
    }

    // ---- White-box / structural ----
    static void testWhiteBox() {
        try {
            Class<?> c = Class.forName("java.lang.Integer");
            check(!c.isInterface(), "Integer is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Integer is final");
            check(c.getSuperclass() == Number.class, "Integer extends Number");
            check(Number.class.getSuperclass() == Object.class, "Number extends Object");
            check(Comparable.class.isAssignableFrom(c), "Integer implements Comparable");
            check(c.getField("MAX_VALUE").getType() == int.class
                  && java.lang.reflect.Modifier.isStatic(c.getField("MAX_VALUE").getModifiers())
                  && java.lang.reflect.Modifier.isFinal(c.getField("MAX_VALUE").getModifiers()),
                  "MAX_VALUE is public static final int");
            check(c.getMethod("parseInt", String.class).getReturnType() == int.class,
                  "parseInt(String) returns int");
            check(c.getMethod("valueOf", int.class).getReturnType() == Integer.class,
                  "valueOf(int) returns Integer");
            check(c.getMethod("toString", int.class, int.class).getReturnType() == String.class,
                  "toString(int,int) returns String");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testBoxing() {
        // JLS 5.1.7: autoboxing maps the int literal through Integer.valueOf(int).
        Integer boxed = 42;
        check(boxed == Integer.valueOf(42), "autobox 42 uses cached valueOf");
        int unboxed = boxed;                       // unboxing via intValue()
        check(unboxed == 42, "unboxing recovers int");
        Integer sum = boxed + 8;                   // unbox, add, rebox
        check(sum == Integer.valueOf(50), "boxed arithmetic re-boxes");
        check(sum.compareTo(boxed) > 0, "compareTo after arithmetic");
        check(!sum.equals(boxed), "!equals distinct values");
        check(sum.hashCode() == 50, "hashCode follows value");
        Integer big1 = 128, big2 = 128;
        check(big1 != big2, "autobox 128 not cached (distinct references)");
        check(big1.equals(big2) && big1.intValue() == big2.intValue(),
              "distinct autobox instances are equal by value");
    }

    // ---- Black-box / behavioral ----
    static void testBlackBox() {
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
        testRadixExtremesAndWrap();
        testBoxing();
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

    /** parseInt(null) 的消息：JDK-8 为 "null"，JDK-19+ 为 "Cannot parse null string"。 */
    static String nullParseMsg() {
        return MODERN ? "Cannot parse null string" : "null";
    }

    /** 非 10 进制的解析失败消息：JDK-19+ 追加 " under radix N"。 */
    static String radixMsg(String s, int radix) {
        return MODERN ? ("For input string: \"" + s + "\" under radix " + radix)
                      : ("For input string: \"" + s + "\"");
    }

    static void testNfeMessageContract() {
        // radix 越界消息（两侧文本一致，实测逐字锁定）
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Integer.parseInt("10", 1), "radix MIN_RADIX-1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX",
              () -> Integer.parseInt("10", 37), "radix MAX_RADIX+1");
        checkNfeMsg("radix 0 less than Character.MIN_RADIX",
              () -> Integer.parseInt("10", 0), "radix 0");
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Integer.parseInt("10", Character.MIN_RADIX - 1), "radix Character.MIN_RADIX-1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX",
              () -> Integer.parseInt("10", Character.MAX_RADIX + 1), "radix Character.MAX_RADIX+1");
        // 非法字符 NFE 消息 `For input string: "..."` 逐字锁定（10 进制两侧一致）
        checkNfeMsg("For input string: \"z\"", () -> Integer.parseInt("z"), "alpha");
        checkNfeMsg("For input string: \"\"", () -> Integer.parseInt(""), "empty");
        checkNfeMsg("For input string: \" 42\"", () -> Integer.parseInt(" 42"), "leading space");
        checkNfeMsg("For input string: \"42 \"", () -> Integer.parseInt("42 "), "trailing space");
        checkNfeMsg("For input string: \"12a\"", () -> Integer.parseInt("12a"), "trailing alpha");
        // 超范围 NFE 消息含原文
        checkNfeMsg("For input string: \"2147483648\"", () -> Integer.parseInt("2147483648"), "MAX+1");
        checkNfeMsg("For input string: \"-2147483649\"", () -> Integer.parseInt("-2147483649"), "MIN-1");
        checkNfeMsg("For input string: \"999999999999999999999999\"",
              () -> Integer.parseInt("999999999999999999999999"), "far overflow");
        // null → NFE（而非 NPE），消息含 "null"
        checkNfeMsg(nullParseMsg(), () -> Integer.parseInt(null), "parseInt(null)");
        checkNfeMsg(nullParseMsg(), () -> Integer.parseUnsignedInt(null), "parseUnsignedInt(null)");
        // 非 10 进制失败消息：JDK-19+ 追加 " under radix N"
        checkNfeMsg(radixMsg("80000000", 16), () -> Integer.parseInt("80000000", 16), "hex overflow");
        checkNfeMsg(radixMsg("z", 16), () -> Integer.parseInt("z", 16), "hex alpha");
        checkNfeMsg(radixMsg("10000000000000000000000000000000", 2),
              () -> Integer.parseInt("10000000000000000000000000000000", 2), "binary overflow");
        // 无符号解析消息（两侧一致）
        checkNfeMsg("Illegal leading minus sign on unsigned string -1.",
              () -> Integer.parseUnsignedInt("-1"), "unsigned minus");
        checkNfeMsg("String value 4294967296 exceeds range of unsigned int.",
              () -> Integer.parseUnsignedInt("4294967296"), "unsigned 2^32");
        checkNfeMsg("For input string: \"z\"", () -> Integer.parseUnsignedInt("z"), "unsigned alpha");
        // decode 负例消息
        checkNfeMsg("Zero length string", () -> Integer.decode(""), "decode empty");
        checkNfeMsg(radixMsg("", 16), () -> Integer.decode("0x"), "decode 0x bare");
        checkNfeMsg(radixMsg("", 16), () -> Integer.decode("#"), "decode # bare");
        checkNfeMsg("For input string: \"\"", () -> Integer.decode("+"), "decode bare +");
        checkNfeMsg("For input string: \"-\"", () -> Integer.decode("-"), "decode bare -");
        checkNfeMsg("For input string: \"123y\"", () -> Integer.decode("123y"), "decode 123y");
        checkNfeMsg("For input string: \"10l\"", () -> Integer.decode("10l"), "decode l suffix");
        checkNfeMsg("For input string: \"10L\"", () -> Integer.decode("10L"), "decode L suffix");
        checkNfeMsg(radixMsg("g", 16), () -> Integer.decode("0xg"), "decode 0xg");
        checkNfeMsg(radixMsg("80000000", 16), () -> Integer.decode("0x80000000"), "decode hex overflow");
        checkNfeMsg(radixMsg("2147483647", 8), () -> Integer.decode("02147483647"), "decode octal overflow");
        // 无符号除零 ArithmeticException "/ by zero" 逐字
        checkArithMsg("/ by zero", () -> Integer.divideUnsigned(1, 0), "divideUnsigned by 0");
        checkArithMsg("/ by zero", () -> Integer.remainderUnsigned(1, 0), "remainderUnsigned by 0");
    }

    static void testParseRadixRoundTrip() {
        int[] values = {0, 1, -1, 7, 42, 127, -128, 255, 4095,
                        0x7ffffffe, 0x80000001, Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] radixes = {2, 8, 10, 16, 36};
        // parse(toString(v, radix), radix) == v 对全进制表闭环
        for (int radix : radixes) {
            for (int v : values) {
                check(Integer.parseInt(Integer.toString(v, radix), radix) == v,
                      "round-trip toString/parse " + v + " radix " + radix);
            }
        }
        // "+5" 的各进制写法均被接受（如 radix 2 的 "+101"）
        for (int radix : radixes) {
            check(Integer.parseInt("+" + Integer.toString(5, radix), radix) == 5,
                  "parseInt(+5," + radix + ")");
        }
        // "0" / "-0" 在所有进制下均为 0
        for (int radix : radixes) {
            check(Integer.parseInt("0", radix) == 0, "parseInt(0," + radix + ")");
            check(Integer.parseInt("-0", radix) == 0, "parseInt(-0," + radix + ")");
        }
        // 进制边界
        check(Integer.parseInt("10", Character.MIN_RADIX) == 2, "parse at MIN_RADIX");
        check(Integer.parseInt("1z", Character.MAX_RADIX) == 71, "parse 1z at MAX_RADIX");
        check(Integer.parseInt("+2147483647") == Integer.MAX_VALUE, "parse +MAX");
        // 16 进制补码边界（负数经符号位解析）
        check(Integer.parseInt("-80000000", 16) == Integer.MIN_VALUE, "hex -80000000");
        check(Integer.parseInt("7fffffff", 16) == Integer.MAX_VALUE, "hex 7fffffff");
    }

    static void testToStringExactTable() {
        // toString(int, radix) 精确全等表
        check(Integer.toString(0, 2).equals("0"), "toString(0,2)");
        check(Integer.toString(1, 2).equals("1"), "toString(1,2)");
        check(Integer.toString(-255, 2).equals("-11111111"), "toString(-255,2)");
        check(Integer.toString(0, 8).equals("0"), "toString(0,8)");
        check(Integer.toString(-255, 8).equals("-377"), "toString(-255,8)");
        check(Integer.toString(0, 16).equals("0"), "toString(0,16)");
        check(Integer.toString(-255, 16).equals("-ff"), "toString(-255,16)");
        check(Integer.toString(-1, 36).equals("-1"), "toString(-1,36)");
        check(Integer.toString(0, 36).equals("0"), "toString(0,36)");
        check(Integer.toString(35, 36).equals("z"), "toString(35,36)");
        check(Integer.toString(36, 36).equals("10"), "toString(36,36)");
        check(Integer.toString(Integer.MAX_VALUE, 2).equals(
              "1111111111111111111111111111111"), "toString(MAX,2)");
        check(Integer.toString(Integer.MIN_VALUE, 8).equals("-20000000000"), "toString(MIN,8)");
        // toBinaryString/toHexString/toOctalString 对 -1/255/MIN 的精确串
        check(Integer.toBinaryString(-1).equals(
              "11111111111111111111111111111111"), "toBinaryString(-1)");
        check(Integer.toBinaryString(255).equals("11111111"), "toBinaryString(255)");
        check(Integer.toBinaryString(-255).equals(
              "11111111111111111111111100000001"), "toBinaryString(-255)");
        check(Integer.toHexString(0).equals("0"), "toHexString(0)");
        check(Integer.toHexString(-255).equals("ffffff01"), "toHexString(-255)");
        check(Integer.toOctalString(0).equals("0"), "toOctalString(0)");
        check(Integer.toOctalString(-255).equals("37777777401"), "toOctalString(-255)");
        // toUnsignedString(int, radix) 精确表
        check(Integer.toUnsignedString(0, 36).equals("0"), "toUnsignedString(0,36)");
        check(Integer.toUnsignedString(255, 36).equals("73"), "toUnsignedString(255,36)");
        check(Integer.toUnsignedString(-1, 8).equals("37777777777"), "toUnsignedString(-1,8)");
        check(Integer.toUnsignedString(-1, 2).equals(
              "11111111111111111111111111111111"), "toUnsignedString(-1,2)");
        check(Integer.toUnsignedString(-256, 16).equals("ffffff00"), "toUnsignedString(-256,16)");
        check(Integer.toUnsignedString(-2).equals("4294967294"), "toUnsignedString(-2)");
    }

    static void testDecodeMatrix() {
        // 0x/0X/#/前导 0(八进制)/符号矩阵
        check(Integer.decode("0x1f") == 31, "decode(0x1f)");
        check(Integer.decode("0X1F") == 31, "decode(0X1F)");
        check(Integer.decode("017") == 15, "decode(017) octal");
        check(Integer.decode("#f") == 15, "decode(#f)");
        check(Integer.decode("#F") == 15, "decode(#F)");
        check(Integer.decode("-#f") == -15, "decode(-#f)");
        check(Integer.decode("+#f") == 15, "decode(+#f)");
        check(Integer.decode("-017") == -15, "decode(-017)");
        check(Integer.decode("+0x7fffffff") == Integer.MAX_VALUE, "decode(+0x7fffffff)");
        // 2^32-1 超 int 范围 → NFE
        expectNFE(() -> Integer.decode("#ffffffff"), "decode(#ffffffff) overflow");
        check(Integer.decode("00") == 0, "decode(00)");
        check(Integer.decode("0x0") == 0, "decode(0x0)");
        check(Integer.decode("#0") == 0, "decode(#0)");
        // 尾部 l/L 后缀不被 decode 接受（Integer 语义）
        expectNFE(() -> Integer.decode("10l"), "decode(10l)");
        expectNFE(() -> Integer.decode("0xfl"), "decode(0xfl)");
        expectNFE(() -> Integer.decode("02147483647"), "decode octal overflow");
    }

    static void testBitOpExactTable() {
        check(Integer.bitCount(1) == 1, "bitCount(1)");
        check(Integer.bitCount(255) == 8, "bitCount(255)");
        check(Integer.bitCount(0x7fffffff) == 31, "bitCount(MAX)");
        check(Integer.bitCount(Integer.MIN_VALUE) == 1, "bitCount(MIN)");
        check(Integer.bitCount(0xF0F0F0F0) == 16, "bitCount(0xF0F0F0F0)");
        check(Integer.highestOneBit(0x7fffffff) == 0x40000000, "highestOneBit(MAX)");
        check(Integer.highestOneBit(255) == 128, "highestOneBit(255)");
        check(Integer.highestOneBit(Integer.MIN_VALUE) == Integer.MIN_VALUE, "highestOneBit(MIN)");
        check(Integer.lowestOneBit(255) == 1, "lowestOneBit(255)");
        check(Integer.lowestOneBit(0x7fffffff) == 1, "lowestOneBit(MAX)");
        check(Integer.numberOfLeadingZeros(-1) == 0, "numberOfLeadingZeros(-1)");
        check(Integer.numberOfLeadingZeros(0x7fffffff) == 1, "numberOfLeadingZeros(MAX)");
        check(Integer.numberOfTrailingZeros(-1) == 0, "numberOfTrailingZeros(-1)");
        check(Integer.numberOfTrailingZeros(0x7fffffff) == 0, "numberOfTrailingZeros(MAX)");
        check(Integer.reverse(0x000000FF) == 0xFF000000, "reverse(0x000000FF)");
        check(Integer.reverse(0xFFFFFFFF) == 0xFFFFFFFF, "reverse(0xFFFFFFFF)");
        check(Integer.reverse(0x12345678) == 0x1e6a2c48, "reverse(0x12345678)");
        check(Integer.reverseBytes(0x000000FF) == 0xFF000000, "reverseBytes(0x000000FF)");
        check(Integer.reverseBytes(0xFFFFFFFF) == 0xFFFFFFFF, "reverseBytes(0xFFFFFFFF)");
        check(Integer.reverse(Integer.reverse(0x0f0f0f0f)) == 0x0f0f0f0f, "reverse involution");
        check(Integer.reverseBytes(Integer.reverseBytes(0x0f0f0f0f)) == 0x0f0f0f0f, "reverseBytes involution");
        // rotate 恒等律：0、32、-32 均为恒等
        int x = 0x12345678;
        check(Integer.rotateLeft(x, 0) == x, "rotateLeft(x,0) identity");
        check(Integer.rotateLeft(x, 32) == x, "rotateLeft(x,32) identity");
        check(Integer.rotateLeft(x, -32) == x, "rotateLeft(x,-32) identity");
        check(Integer.rotateRight(x, 0) == x, "rotateRight(x,0) identity");
        check(Integer.rotateRight(x, 32) == x, "rotateRight(x,32) identity");
        check(Integer.rotateLeft(x, 8) == 0x34567812, "rotateLeft(0x12345678,8)");
        check(Integer.rotateRight(x, 8) == 0x78123456, "rotateRight(0x12345678,8)");
        check(Integer.rotateLeft(0, 9) == 0, "rotateLeft(0,9)");
        check(Integer.rotateRight(0, 9) == 0, "rotateRight(0,9)");
        // rotateLeft(x,n) == rotateRight(x,32-n) 对偶关系全 n 扫描
        for (int n = 1; n <= 31; n++) {
            check(Integer.rotateLeft(x, n) == Integer.rotateRight(x, 32 - n),
                  "rotateLeft/rotateRight duality n=" + n);
        }
        check(Integer.signum(0x7fffffff) == 1, "signum(MAX)");
        // bitCount 补恒等式与 highest/lowestOneBit 的推导关系
        check(Integer.bitCount(x) + Integer.bitCount(~x) == 32, "bitCount complement identity");
        check(Integer.highestOneBit(x) == (1 << (31 - Integer.numberOfLeadingZeros(x))),
              "highestOneBit derivation");
        check(Integer.lowestOneBit(x) == (1 << Integer.numberOfTrailingZeros(x)),
              "lowestOneBit derivation");
    }

    static void testUnsignedArithmeticTable() {
        // compareUnsigned 各方向（-1 视为最大无符号值）
        check(Integer.compareUnsigned(0, -1) < 0, "compareUnsigned(0,-1) < 0");
        check(Integer.compareUnsigned(-2, -1) < 0, "compareUnsigned(-2,-1) < 0");
        check(Integer.compareUnsigned(-1, -2) > 0, "compareUnsigned(-1,-2) > 0");
        check(Integer.compareUnsigned(-1, -1) == 0, "compareUnsigned(-1,-1) == 0");
        check(Integer.compareUnsigned(Integer.MIN_VALUE, -1) < 0, "compareUnsigned(MIN,-1) < 0");
        check(Integer.compareUnsigned(-1, Integer.MAX_VALUE) > 0, "compareUnsigned(-1,MAX) > 0");
        // 0xFFFFFFFF / 1 = 0xFFFFFFFF（补码即 -1，实测锁定；非 MAX_VALUE）
        check(Integer.divideUnsigned(-1, 1) == -1, "divideUnsigned(-1,1) == 0xFFFFFFFF");
        check(Integer.divideUnsigned(-1, 3) == 1431655765, "divideUnsigned(-1,3)");
        check(Integer.divideUnsigned(-2, 1) == -2, "divideUnsigned(-2,1)");
        check(Integer.divideUnsigned(0, 5) == 0, "divideUnsigned(0,5)");
        check(Integer.remainderUnsigned(-1, 10) == 5, "remainderUnsigned(-1,10)");
        check(Integer.remainderUnsigned(-1, 3) == 0, "remainderUnsigned(-1,3) == 0");
        check(Integer.remainderUnsigned(0, 7) == 0, "remainderUnsigned(0,7)");
        // toUnsignedLong 精确表
        check(Integer.toUnsignedLong(255) == 255L, "toUnsignedLong(255)");
        check(Integer.toUnsignedLong(-2) == 4294967294L, "toUnsignedLong(-2)");
        // 无符号除法与 BigInteger 对照闭环
        java.math.BigInteger u = new java.math.BigInteger("4294967295");
        check(Integer.divideUnsigned(-1, 7) == u.divide(java.math.BigInteger.valueOf(7)).intValue(),
              "divideUnsigned(-1,7) matches BigInteger");
        check(Integer.remainderUnsigned(-1, 7) == u.mod(java.math.BigInteger.valueOf(7)).intValue(),
              "remainderUnsigned(-1,7) matches BigInteger");
    }

    static void testValueOfCacheBoundary() {
        // -128 与 127 同一实例（==）、-129 与 128 不同实例但 equals
        check(Integer.valueOf(-128) == Integer.valueOf(-128), "cache -128 identity");
        check(Integer.valueOf(127) == Integer.valueOf(127), "cache 127 identity");
        check(Integer.valueOf(128) != Integer.valueOf(128), "128 not cached");
        check(Integer.valueOf(-129) != Integer.valueOf(-129), "-129 not cached");
        check(Integer.valueOf(128).equals(Integer.valueOf(128)), "128 equals by value");
        check(Integer.valueOf(-129).equals(Integer.valueOf(-129)), "-129 equals by value");
        check(Integer.valueOf(127).equals(Integer.valueOf(127)), "127 equals");
        // 与 new Integer 跨缓存实例：引用不同、值相等（构造器已弃用但两侧仍存在）
        @SuppressWarnings("deprecation")
        Integer fresh127 = new Integer(127);
        check(fresh127 != Integer.valueOf(127), "new Integer(127) bypasses cache");
        check(fresh127.equals(Integer.valueOf(127)), "new Integer(127).equals cached");
        @SuppressWarnings("deprecation")
        Integer freshMin = new Integer(Integer.MIN_VALUE);
        check(freshMin.intValue() == Integer.MIN_VALUE, "new Integer(MIN).intValue");
        check(freshMin.equals(Integer.valueOf(Integer.MIN_VALUE)), "new Integer(MIN).equals");
        // 静态 hashCode(int) 与实例 hashCode 一致
        check(Integer.hashCode(12345) == Integer.valueOf(12345).hashCode(), "static hashCode == instance");
        check(Integer.hashCode(-1) == -1, "hashCode(-1) == -1");
        check(Integer.hashCode(0) == 0, "hashCode(0) == 0");
    }

    /** JDK 9+ 独有：parseInt(CharSequence, beginIndex, endIndex, radix) 子串解析。 */
    static void testParseCharSequenceRange() {
        check(Integer.parseInt("42", 1, 2, 10) == 2, "parseInt sub \"42\"[1,2)");
        check(Integer.parseInt("-42", 0, 3, 10) == -42, "parseInt sub \"-42\"[0,3)");
        check(Integer.parseInt("7fffffff", 0, 8, 16) == Integer.MAX_VALUE, "CS hex MAX");
        check(Integer.parseInt("ff", 0, 2, 16) == 255, "CS hex ff");
        check(Integer.parseInt("+5", 0, 2, 10) == 5, "CS +5");
        check(Integer.parseInt("x42x", 1, 3, 10) == 42, "CS inner window");
        check(Integer.parseInt("0042", 0, 4, 10) == 42, "CS leading zeros");
        check(Long.parseLong("9223372036854775807", 0, 19, 10) == Long.MAX_VALUE, "CS long MAX");
        // JDK 19+ 消息格式：带出错下标
        checkNfeMsg("Error at index 0 in: \"abc\"", () -> Integer.parseInt("abc", 0, 3, 10), "CS abc");
        checkNfeMsg("Error at index 2 in: \"12x\"", () -> Integer.parseInt("12x", 0, 3, 10), "CS 12x");
        checkNfeMsg("Error at index 9 in: \"2147483648\"",
              () -> Integer.parseInt("2147483648", 0, 10, 10), "CS overflow");
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Integer.parseInt("10", 0, 2, 1), "CS radix 1");
        // 窗口越界 → IndexOutOfBoundsException（JDK-19+ Preconditions 文本）
        checkIndexMsg("Range [0, 5) out of bounds for length 2",
              () -> Integer.parseInt("42", 0, 5, 10), "CS end beyond length");
        checkIndexMsg("Range [2, 0) out of bounds for length 2",
              () -> Integer.parseInt("42", 2, 0, 10), "CS begin > end");
        checkIndexMsg("Range [-1, 1) out of bounds for length 2",
              () -> Integer.parseInt("42", -1, 1, 10), "CS negative begin");
        // 无符号 CharSequence 形态
        check(Integer.parseUnsignedInt("ffffffff", 0, 8, 16) == -1, "CS unsigned hex ffffffff");
        check(Integer.parseUnsignedInt("4294967295", 0, 10, 10) == -1, "CS unsigned decimal 2^32-1");
        check(Long.parseUnsignedLong("18446744073709551615", 0, 20, 10) == -1L, "CS unsigned long 2^64-1");
        checkNfeMsg("Illegal leading minus sign on unsigned string -1.",
              () -> Integer.parseUnsignedInt("-1", 0, 2, 10), "CS unsigned minus");
    }

    static void checkIndexMsg(String expected, Runnable r, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            check(expected.equals(e.getMessage()),
                  label + ": expected <" + expected + "> got <" + e.getMessage() + ">");
        }
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        // 契约深化（2026-11）
        testNfeMessageContract();
        testParseRadixRoundTrip();
        testToStringExactTable();
        testDecodeMatrix();
        testBitOpExactTable();
        testUnsignedArithmeticTable();
        testValueOfCacheBoundary();
        testParseCharSequenceRange();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.Byte API conformance: constants, parseByte (decimal and
 *          radix, overflow, no whitespace trim), valueOf (full -128..127 cache),
 *          decode (0x/#/octal/sign), toString/toUnsignedInt/toUnsignedLong,
 *          comparison (incl. unsigned), and hashCode/equals.
 * @run main ByteTest
 */

public class ByteTest {
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
        check(Byte.MIN_VALUE == -128, "MIN_VALUE == -128");
        check(Byte.MIN_VALUE == (byte) 0x80, "MIN_VALUE == 0x80");
        check(Byte.MAX_VALUE == 127, "MAX_VALUE == 127");
        check(Byte.MAX_VALUE == 0x7f, "MAX_VALUE == 0x7f");
        check(Byte.SIZE == 8, "SIZE == 8");
        check(Byte.BYTES == 1, "BYTES == 1");
        check(Byte.TYPE == byte.class, "TYPE == byte.class");
    }

    static void testParseByte() {
        check(Byte.parseByte("42") == 42, "parseByte(42)");
        check(Byte.parseByte("-42") == -42, "parseByte(-42)");
        check(Byte.parseByte("+1") == 1, "parseByte(+1)");
        check(Byte.parseByte("127") == Byte.MAX_VALUE, "parseByte(MAX)");
        check(Byte.parseByte("-128") == Byte.MIN_VALUE, "parseByte(MIN)");
        check(Byte.parseByte("-0") == 0, "parseByte(-0)");

        expectNFE(() -> Byte.parseByte(null), "parseByte(null)");
        expectNFE(() -> Byte.parseByte(""), "parseByte(empty)");
        expectNFE(() -> Byte.parseByte(" 42"), "parseByte(leading space)");
        expectNFE(() -> Byte.parseByte("42 "), "parseByte(trailing space)");
        expectNFE(() -> Byte.parseByte("128"), "parseByte(128 overflow)");
        expectNFE(() -> Byte.parseByte("-129"), "parseByte(-129 overflow)");
        expectNFE(() -> Byte.parseByte("1_0"), "parseByte(underscore)");
    }

    static void testParseByteRadix() {
        check(Byte.parseByte("1010", 2) == 10, "parseByte(1010,2)");
        check(Byte.parseByte("7f", 16) == 127, "parseByte(7f,16)");
        check(Byte.parseByte("-80", 16) == -128, "parseByte(-80,16)");
        check(Byte.parseByte("-1", 16) == -1, "parseByte(-1,16)");
        check(Byte.parseByte("z", 36) == 35, "parseByte(z,36)");
        check(Byte.parseByte("10", 36) == 36, "parseByte(10,36)");

        expectNFE(() -> Byte.parseByte("10", 1), "radix 1");
        expectNFE(() -> Byte.parseByte("10", 37), "radix 37");
        expectNFE(() -> Byte.parseByte("80", 16), "positive overflow hex");
        expectNFE(() -> Byte.parseByte("-81", 16), "negative overflow hex");
        expectNFE(() -> Byte.parseByte("ff", 16), "ff,16 == 255 out of byte range");
    }

    static void testValueOf() {
        check(Byte.valueOf("42") == 42, "valueOf(42)");
        check(Byte.valueOf("-128") == Byte.MIN_VALUE, "valueOf(-128)");
        check(Byte.valueOf("127") == Byte.MAX_VALUE, "valueOf(127)");
        check(Byte.valueOf("42", 16) == 66, "valueOf(42,16)");

        // The entire byte range is cached by valueOf(byte) per the API contract.
        check(Byte.valueOf((byte) -128) == Byte.valueOf((byte) -128), "valueOf(-128) cached");
        check(Byte.valueOf((byte) 0) == Byte.valueOf((byte) 0), "valueOf(0) cached");
        check(Byte.valueOf((byte) 127) == Byte.valueOf((byte) 127), "valueOf(127) cached");

        expectNFE(() -> Byte.valueOf("x"), "valueOf(x)");
        expectNFE(() -> Byte.valueOf("128"), "valueOf(128 overflow)");
    }

    static void testDecode() {
        check(Byte.decode("42") == 42, "decode(42)");
        check(Byte.decode("-42") == -42, "decode(-42)");
        check(Byte.decode("+10") == 10, "decode(+10)");
        check(Byte.decode("0x7f") == 127, "decode(0x7f)");
        check(Byte.decode("0X7F") == 127, "decode(0X7F)");
        check(Byte.decode("-0x80") == -128, "decode(-0x80)");
        check(Byte.decode("#7f") == 127, "decode(#7f)");
        check(Byte.decode("017") == 15, "decode(017) octal");

        expectNFE(() -> Byte.decode("0x"), "decode(0x bare)");
        expectNFE(() -> Byte.decode("#"), "decode(# bare)");
        expectNFE(() -> Byte.decode("0x80"), "decode(0x80 overflow)");
        expectNFE(() -> Byte.decode("0xFF"), "decode(0xFF overflow)");
        expectNFE(() -> Byte.decode("10L"), "decode(L suffix not accepted)");
    }

    static void testToStringUnsigned() {
        check(Byte.toString((byte) 42).equals("42"), "toString(42)");
        check(Byte.toString(Byte.MIN_VALUE).equals("-128"), "toString(MIN)");
        check(Byte.toUnsignedInt((byte) -1) == 255, "toUnsignedInt(-1) == 255");
        check(Byte.toUnsignedInt(Byte.MAX_VALUE) == 127, "toUnsignedInt(127)");
        check(Byte.toUnsignedInt(Byte.MIN_VALUE) == 128, "toUnsignedInt(-128) == 128");
        check(Byte.toUnsignedLong((byte) -1) == 255L, "toUnsignedLong(-1) == 255");
    }

    static void testCompare() {
        check(Byte.compare((byte) 1, (byte) 2) < 0, "compare(1,2) < 0");
        check(Byte.compare((byte) 2, (byte) 1) > 0, "compare(2,1) > 0");
        check(Byte.compare((byte) 1, (byte) 1) == 0, "compare(1,1) == 0");
        check(Byte.valueOf((byte) 1).compareTo(Byte.valueOf((byte) 2)) < 0, "compareTo < 0");

        // Byte.compareUnsigned was added in Java 9; JDK 8 expresses the same
        // unsigned ordering via Integer.compareUnsigned(Byte.toUnsignedInt(...)).
        try {
            java.lang.reflect.Method m = Byte.class.getMethod("compareUnsigned",
                                                              byte.class, byte.class);
            check((Integer) m.invoke(null, (byte) -1, (byte) 0) > 0, "compareUnsigned(-1,0) > 0");
            check((Integer) m.invoke(null, (byte) 1, (byte) -1) < 0, "compareUnsigned(1,-1) < 0");
            check((Integer) m.invoke(null, (byte) 1, (byte) 1) == 0, "compareUnsigned(1,1) == 0");
        } catch (NoSuchMethodException e) {
            check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) -1),
                                          Byte.toUnsignedInt((byte) 0)) > 0,
                  "unsigned(-1) > unsigned(0) [JDK8]");
            check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) 1),
                                          Byte.toUnsignedInt((byte) -1)) < 0,
                  "unsigned(1) < unsigned(-1) [JDK8]");
            check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) 1),
                                          Byte.toUnsignedInt((byte) 1)) == 0,
                  "unsigned(1) == unsigned(1) [JDK8]");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testHashCodeEquals() {
        check(Byte.valueOf((byte) 42).hashCode() == 42, "hashCode(42) == 42");
        check(Byte.valueOf(Byte.MIN_VALUE).hashCode() == -128, "hashCode(MIN) == -128");
        check(Byte.valueOf((byte) 42).equals(Byte.valueOf((byte) 42)), "equals same");
        check(!Byte.valueOf((byte) 42).equals(Byte.valueOf((byte) 43)), "!equals different");
        check(!Byte.valueOf((byte) 42).equals(Integer.valueOf(42)), "!equals Integer");
        check(Byte.valueOf((byte) 42).byteValue() == 42, "byteValue");
        check(Byte.valueOf((byte) 42).shortValue() == 42, "shortValue");
        check(Byte.valueOf((byte) 42).intValue() == 42, "intValue");
        check(Byte.valueOf((byte) 42).longValue() == 42L, "longValue");
        check(Byte.valueOf((byte) 42).floatValue() == 42.0f, "floatValue");
        check(Byte.valueOf((byte) 42).doubleValue() == 42.0, "doubleValue");
    }

    public static void main(String[] args) {
        testConstants();
        testParseByte();
        testParseByteRadix();
        testValueOf();
        testDecode();
        testToStringUnsigned();
        testCompare();
        testHashCodeEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

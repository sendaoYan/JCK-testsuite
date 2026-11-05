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

    static String nullParseMsg() {
        return MODERN ? "Cannot parse null string" : "null";
    }

    static String radixMsg(String s, int radix) {
        return MODERN ? ("For input string: \"" + s + "\" under radix " + radix)
                      : ("For input string: \"" + s + "\"");
    }

    static void testNfeMessageContract() {
        // radix 越界消息（两侧文本一致，实测逐字锁定）
        checkNfeMsg("radix 1 less than Character.MIN_RADIX", () -> Byte.parseByte("10", 1), "radix 1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX", () -> Byte.parseByte("10", 37), "radix 37");
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Byte.parseByte("10", Character.MIN_RADIX - 1), "radix MIN_RADIX-1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX",
              () -> Byte.parseByte("10", Character.MAX_RADIX + 1), "radix MAX_RADIX+1");
        // 非法字符 / 空 / 空白（10 进制两侧一致）
        checkNfeMsg("For input string: \"z\"", () -> Byte.parseByte("z"), "alpha");
        checkNfeMsg("For input string: \"\"", () -> Byte.parseByte(""), "empty");
        checkNfeMsg("For input string: \" 42\"", () -> Byte.parseByte(" 42"), "leading space");
        // 超范围消息：Byte 自有格式 `Value out of range. Value:"..." Radix:N`
        checkNfeMsg("Value out of range. Value:\"128\" Radix:10", () -> Byte.parseByte("128"), "decimal overflow");
        checkNfeMsg("Value out of range. Value:\"-129\" Radix:10", () -> Byte.parseByte("-129"), "decimal underflow");
        checkNfeMsg("Value out of range. Value:\"80\" Radix:16", () -> Byte.parseByte("80", 16), "hex overflow");
        checkNfeMsg("Value out of range. Value:\"10000000\" Radix:2",
              () -> Byte.parseByte("10000000", 2), "binary overflow");
        // null → NFE（而非 NPE）
        checkNfeMsg(nullParseMsg(), () -> Byte.parseByte(null), "parseByte(null)");
        // decode 负例消息
        checkNfeMsg("Zero length string", () -> Byte.decode(""), "decode empty");
        checkNfeMsg(radixMsg("", 16), () -> Byte.decode("0x"), "decode 0x bare");
        checkNfeMsg(radixMsg("", 16), () -> Byte.decode("#"), "decode # bare");
        checkNfeMsg("For input string: \"123y\"", () -> Byte.decode("123y"), "decode 123y");
        checkNfeMsg("For input string: \"10l\"", () -> Byte.decode("10l"), "decode l suffix");
    }

    static void testParseRadixRoundTrip() {
        byte[] values = {0, 1, -1, 7, 42, 64, 127, -128, 100};
        int[] radixes = {2, 8, 10, 16, 36};
        // parse(toString(v, radix), radix) == v 对全进制表闭环
        for (int radix : radixes) {
            for (byte v : values) {
                String signed = (v < 0 ? "-" : "") + Integer.toString(Math.abs(v), radix);
                check(Byte.parseByte(signed, radix) == v,
                      "round-trip toString/parse " + v + " radix " + radix);
            }
        }
        // "+5" 的各进制写法均被接受
        for (int radix : radixes) {
            check(Byte.parseByte("+" + Integer.toString(5, radix), radix) == 5,
                  "parseByte(+5," + radix + ")");
        }
        // "0" / "-0" 在所有进制下均为 0
        for (int radix : radixes) {
            check(Byte.parseByte("0", radix) == 0, "parseByte(0," + radix + ")");
            check(Byte.parseByte("-0", radix) == 0, "parseByte(-0," + radix + ")");
        }
        // 进制边界
        check(Byte.parseByte("10", Character.MIN_RADIX) == 2, "parse at MIN_RADIX");
        check(Byte.parseByte("1z", Character.MAX_RADIX) == 71, "parse 1z at MAX_RADIX");
        check(Byte.parseByte("+127") == Byte.MAX_VALUE, "parse +MAX");
        check(Byte.parseByte("1111111", 2) == 127, "7 ones radix 2");
        check(Byte.parseByte("-1111111", 2) == -127, "-7 ones radix 2");
        check(Byte.parseByte("-10000000", 2) == -128, "-2^7 radix 2");
    }

    static void testToStringUnsignedTable() {
        // toString(byte) 精确串
        check(Byte.toString((byte) 0).equals("0"), "toString(0)");
        check(Byte.toString((byte) 1).equals("1"), "toString(1)");
        check(Byte.toString((byte) -1).equals("-1"), "toString(-1)");
        check(Byte.toString((byte) 127).equals("127"), "toString(127)");
        check(Byte.toString((byte) -128).equals("-128"), "toString(-128)");
        check(Byte.toString((byte) 100).equals("100"), "toString(100)");
        // (byte)128 经窄化为 -128 的文本
        check(Byte.toString((byte) 128).equals("-128"), "toString((byte)128) == \"-128\"");
        check(Byte.valueOf((byte) 128) == Byte.valueOf((byte) -128), "(byte)128 wraps to cached -128 instance");
        // toUnsignedInt 全范围精确表（0/1/127/128/255）
        check(Byte.toUnsignedInt((byte) 0) == 0, "toUnsignedInt(0)");
        check(Byte.toUnsignedInt((byte) 1) == 1, "toUnsignedInt(1)");
        check(Byte.toUnsignedInt((byte) 126) == 126, "toUnsignedInt(126)");
        check(Byte.toUnsignedInt(Byte.MAX_VALUE) == 127, "toUnsignedInt(MAX)");
        check(Byte.toUnsignedInt(Byte.MIN_VALUE) == 128, "toUnsignedInt(MIN)");
        check(Byte.toUnsignedInt((byte) -2) == 254, "toUnsignedInt(-2)");
        check(Byte.toUnsignedInt((byte) -1) == 255, "toUnsignedInt(-1)");
        // toUnsignedLong 同表
        check(Byte.toUnsignedLong((byte) 0) == 0L, "toUnsignedLong(0)");
        check(Byte.toUnsignedLong(Byte.MAX_VALUE) == 127L, "toUnsignedLong(MAX)");
        check(Byte.toUnsignedLong(Byte.MIN_VALUE) == 128L, "toUnsignedLong(MIN)");
        check(Byte.toUnsignedLong((byte) -1) == 255L, "toUnsignedLong(-1)");
    }

    static void testFullRangeCache() {
        // Byte.valueOf 缓存全部 256 个值：valueOf(b) == valueOf(b) 对整个字节范围成立
        for (int i = -128; i <= 127; i++) {
            byte b = (byte) i;
            check(Byte.valueOf(b) == Byte.valueOf(b), "valueOf identity " + i);
        }
        // toString/parseByte 对全部 256 个值的往返
        for (int i = -128; i <= 127; i++) {
            byte b = (byte) i;
            check(Byte.parseByte(Byte.toString(b)) == b, "toString/parseByte round-trip " + i);
        }
    }

    static void testNarrowingWidening() {
        // 宽化链（Byte 值域小，所有宽化精确）
        check(Byte.valueOf((byte) -1).shortValue() == -1, "byte -1 shortValue");
        check(Byte.valueOf((byte) -1).intValue() == -1, "byte -1 intValue");
        check(Byte.valueOf((byte) -1).longValue() == -1L, "byte -1 longValue");
        check(Byte.valueOf((byte) -1).floatValue() == -1.0f, "byte -1 floatValue");
        check(Byte.valueOf((byte) -1).doubleValue() == -1.0, "byte -1 doubleValue");
        check(Byte.valueOf(Byte.MIN_VALUE).doubleValue() == -128.0, "MIN doubleValue");
        check(Byte.valueOf(Byte.MAX_VALUE).intValue() == 127, "MAX intValue");
        // byteValue 恒等
        check(Byte.valueOf((byte) 42).byteValue() == 42, "byteValue identity");
        // (byte)127 + 1 编译期窄化
        check((byte) (Byte.MAX_VALUE + 1) == -128, "(byte)(127+1) == -128 编译期窄化");
        check(Byte.valueOf((byte) (Byte.MAX_VALUE + 1)).intValue() == -128, "wrapped intValue -128");
    }

    static void testValueOfCacheBoundary() {
        // 与 new Byte 跨缓存实例（构造器已弃用但两侧仍存在）
        @SuppressWarnings("deprecation")
        Byte fresh127 = new Byte((byte) 127);
        check(fresh127 != Byte.valueOf((byte) 127), "new Byte(127) bypasses cache");
        check(fresh127.equals(Byte.valueOf((byte) 127)), "new Byte(127).equals cached");
        @SuppressWarnings("deprecation")
        Byte freshMin = new Byte(Byte.MIN_VALUE);
        check(freshMin.byteValue() == Byte.MIN_VALUE, "new Byte(MIN).byteValue");
        check(freshMin.equals(Byte.valueOf(Byte.MIN_VALUE)), "new Byte(MIN).equals");
        // 静态 hashCode(byte) 与实例一致
        check(Byte.hashCode((byte) 123) == Byte.valueOf((byte) 123).hashCode(), "static hashCode == instance");
        check(Byte.hashCode((byte) -1) == -1, "hashCode(-1) == -1");
        check(Byte.hashCode((byte) 0) == 0, "hashCode(0) == 0");
        // equals 对称性与不同类型 Number
        check(!Byte.valueOf((byte) 42).equals(Short.valueOf((short) 42)), "!equals Short");
        check(!Byte.valueOf((byte) 42).equals(Long.valueOf(42L)), "!equals Long");
    }

    static void testCompareUnsignedTable() {
        // JDK-8 无 Byte.compareUnsigned（JDK 9+）：经 Integer.compareUnsigned(toUnsignedInt) 等价表达
        check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) 0), Byte.toUnsignedInt((byte) -1)) < 0,
              "unsigned(0) < unsigned(-1)");
        check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) -2), Byte.toUnsignedInt((byte) -1)) < 0,
              "unsigned(-2) < unsigned(-1)");
        check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) -1), Byte.toUnsignedInt((byte) -2)) > 0,
              "unsigned(-1) > unsigned(-2)");
        check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) -1), Byte.toUnsignedInt((byte) -1)) == 0,
              "unsigned(-1) == unsigned(-1)");
        check(Integer.compareUnsigned(Byte.toUnsignedInt(Byte.MIN_VALUE),
              Byte.toUnsignedInt((byte) -1)) < 0, "unsigned(MIN) < unsigned(-1)");
        check(Integer.compareUnsigned(Byte.toUnsignedInt((byte) -1), Byte.toUnsignedInt(Byte.MAX_VALUE)) > 0,
              "unsigned(-1) > unsigned(MAX)");
        // compareTo 全方向
        check(Byte.valueOf((byte) 5).compareTo(Byte.valueOf((byte) 5)) == 0, "compareTo(5,5)");
        check(Byte.compare(Byte.MIN_VALUE, Byte.MAX_VALUE) < 0, "compare(MIN,MAX)");
        check(Byte.compare(Byte.MAX_VALUE, Byte.MIN_VALUE) > 0, "compare(MAX,MIN)");
    }

    // ---- White-box / structural ----
    static void testWhiteBoxStructural() {
        try {
            Class<?> c = Class.forName("java.lang.Byte");
            check(!c.isInterface(), "Byte is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Byte is final");
            check(c.getSuperclass() == Number.class, "Byte extends Number");
            check(Comparable.class.isAssignableFrom(c), "Byte implements Comparable");
            check(java.io.Serializable.class.isAssignableFrom(c), "Byte implements Serializable");
            check(c.getField("MAX_VALUE").getType() == byte.class
                  && java.lang.reflect.Modifier.isStatic(c.getField("MAX_VALUE").getModifiers()),
                  "MAX_VALUE is public static final byte");
            check(c.getMethod("parseByte", String.class).getReturnType() == byte.class,
                  "parseByte(String) returns byte");
            check(c.getMethod("valueOf", byte.class).getReturnType() == Byte.class,
                  "valueOf(byte) returns Byte");
            // JDK-8 无 compareUnsigned(byte,byte)（JDK 9+ 才有）
            boolean hasCompareUnsigned = true;
            try {
                c.getMethod("compareUnsigned", byte.class, byte.class);
            } catch (NoSuchMethodException e) {
                hasCompareUnsigned = false;
            }
            check(!hasCompareUnsigned, "JDK-8 Byte has no compareUnsigned (JDK 9+)");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testBoxing() {
        // JLS 5.1.7: autoboxing maps the byte literal through Byte.valueOf(byte).
        Byte boxed = 42;
        check(boxed == Byte.valueOf((byte) 42), "autobox 42 uses cached valueOf");
        byte unboxed = boxed;                      // unboxing via byteValue()
        check(unboxed == 42, "unboxing recovers byte");
        Byte sum = (byte) (boxed + 8);             // unbox, add, rebox
        check(sum == Byte.valueOf((byte) 50), "boxed arithmetic re-boxes");
        check(sum.compareTo(boxed) > 0, "compareTo after arithmetic");
        check(!sum.equals(boxed), "!equals distinct values");
        check(sum.hashCode() == 50, "hashCode follows value");
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
        // 契约深化（2026-11）
        testNfeMessageContract();
        testParseRadixRoundTrip();
        testToStringUnsignedTable();
        testFullRangeCache();
        testNarrowingWidening();
        testValueOfCacheBoundary();
        testCompareUnsignedTable();
        testWhiteBoxStructural();
        testBoxing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

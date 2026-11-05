/*
 * @test
 * @summary java.lang.Short API conformance: constants, parseShort (decimal and
 *          radix, overflow, no whitespace trim), valueOf (-128..127 cache only),
 *          decode (0x/#/octal/sign), toString/toUnsignedInt/toUnsignedLong,
 *          comparison (incl. unsigned), reverseBytes, and hashCode/equals.
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

        // Short.compareUnsigned was added in Java 9; JDK 8 expresses the same
        // unsigned ordering via Integer.compareUnsigned(Short.toUnsignedInt(...)).
        try {
            java.lang.reflect.Method m = Short.class.getMethod("compareUnsigned",
                                                               short.class, short.class);
            check((Integer) m.invoke(null, (short) -1, (short) 0) > 0, "compareUnsigned(-1,0) > 0");
            check((Integer) m.invoke(null, (short) 1, (short) -1) < 0, "compareUnsigned(1,-1) < 0");
            check((Integer) m.invoke(null, (short) 1, (short) 1) == 0, "compareUnsigned(1,1) == 0");
        } catch (NoSuchMethodException e) {
            check(Integer.compareUnsigned(Short.toUnsignedInt((short) -1),
                                          Short.toUnsignedInt((short) 0)) > 0,
                  "unsigned(-1) > unsigned(0) [JDK8]");
            check(Integer.compareUnsigned(Short.toUnsignedInt((short) 1),
                                          Short.toUnsignedInt((short) -1)) < 0,
                  "unsigned(1) < unsigned(-1) [JDK8]");
            check(Integer.compareUnsigned(Short.toUnsignedInt((short) 1),
                                          Short.toUnsignedInt((short) 1)) == 0,
                  "unsigned(1) == unsigned(1) [JDK8]");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
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
        checkNfeMsg("radix 1 less than Character.MIN_RADIX", () -> Short.parseShort("10", 1), "radix 1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX", () -> Short.parseShort("10", 37), "radix 37");
        checkNfeMsg("radix 1 less than Character.MIN_RADIX",
              () -> Short.parseShort("10", Character.MIN_RADIX - 1), "radix MIN_RADIX-1");
        checkNfeMsg("radix 37 greater than Character.MAX_RADIX",
              () -> Short.parseShort("10", Character.MAX_RADIX + 1), "radix MAX_RADIX+1");
        // 非法字符 / 空 / 空白（10 进制两侧一致）
        checkNfeMsg("For input string: \"z\"", () -> Short.parseShort("z"), "alpha");
        checkNfeMsg("For input string: \"\"", () -> Short.parseShort(""), "empty");
        checkNfeMsg("For input string: \" 42\"", () -> Short.parseShort(" 42"), "leading space");
        // 超范围消息：Short 自有格式 `Value out of range. Value:"..." Radix:N`
        checkNfeMsg("Value out of range. Value:\"32768\" Radix:10",
              () -> Short.parseShort("32768"), "decimal overflow");
        checkNfeMsg("Value out of range. Value:\"-32769\" Radix:10",
              () -> Short.parseShort("-32769"), "decimal underflow");
        checkNfeMsg("Value out of range. Value:\"8000\" Radix:16",
              () -> Short.parseShort("8000", 16), "hex overflow");
        checkNfeMsg("Value out of range. Value:\"1000000000000000\" Radix:2",
              () -> Short.parseShort("1000000000000000", 2), "binary overflow");
        // null → NFE（而非 NPE）
        checkNfeMsg(nullParseMsg(), () -> Short.parseShort(null), "parseShort(null)");
        // decode 负例消息
        checkNfeMsg("Zero length string", () -> Short.decode(""), "decode empty");
        checkNfeMsg(radixMsg("", 16), () -> Short.decode("0x"), "decode 0x bare");
        checkNfeMsg(radixMsg("", 16), () -> Short.decode("#"), "decode # bare");
        checkNfeMsg("For input string: \"123y\"", () -> Short.decode("123y"), "decode 123y");
        checkNfeMsg("For input string: \"10l\"", () -> Short.decode("10l"), "decode l suffix");
    }

    static void testParseRadixRoundTrip() {
        short[] values = {0, 1, -1, 7, 42, 127, -128, 255, 4095, Short.MAX_VALUE, Short.MIN_VALUE};
        int[] radixes = {2, 8, 10, 16, 36};
        // parse(toString(v, radix), radix) == v 对全进制表闭环（Short.toString 无 radix 形态，经 Integer.toString 转换）
        for (int radix : radixes) {
            for (short v : values) {
                String signed = (v < 0 ? "-" : "") + Integer.toString(Math.abs(v), radix);
                check(Short.parseShort(signed, radix) == v,
                      "round-trip toString/parse " + v + " radix " + radix);
            }
        }
        // "+5" 的各进制写法均被接受
        for (int radix : radixes) {
            check(Short.parseShort("+" + Integer.toString(5, radix), radix) == 5,
                  "parseShort(+5," + radix + ")");
        }
        // "0" / "-0" 在所有进制下均为 0
        for (int radix : radixes) {
            check(Short.parseShort("0", radix) == 0, "parseShort(0," + radix + ")");
            check(Short.parseShort("-0", radix) == 0, "parseShort(-0," + radix + ")");
        }
        // 进制边界与 16 进制补码边界
        check(Short.parseShort("10", Character.MIN_RADIX) == 2, "parse at MIN_RADIX");
        check(Short.parseShort("1z", Character.MAX_RADIX) == 71, "parse 1z at MAX_RADIX");
        check(Short.parseShort("+32767") == Short.MAX_VALUE, "parse +MAX");
        check(Short.parseShort("111111111111111", 2) == Short.MAX_VALUE, "15 ones radix 2");
        check(Short.parseShort("-1000000000000000", 2) == Short.MIN_VALUE, "-2^15 radix 2");
    }

    static void testToStringUnsignedTable() {
        // toString(short) 精确串
        check(Short.toString((short) 0).equals("0"), "toString(0)");
        check(Short.toString((short) 1).equals("1"), "toString(1)");
        check(Short.toString((short) -1).equals("-1"), "toString(-1)");
        check(Short.toString((short) 127).equals("127"), "toString(127)");
        check(Short.toString((short) 128).equals("128"), "toString(128)");
        check(Short.toString((short) -128).equals("-128"), "toString(-128)");
        check(Short.toString(Short.MAX_VALUE).equals("32767"), "toString(MAX)");
        check(Short.toString((short) 100).equals("100"), "toString(100)");
        // toUnsignedInt 全范围精确表（0/1/127/128/32767/32768/65535）
        check(Short.toUnsignedInt((short) 0) == 0, "toUnsignedInt(0)");
        check(Short.toUnsignedInt((short) 1) == 1, "toUnsignedInt(1)");
        check(Short.toUnsignedInt((short) 127) == 127, "toUnsignedInt(127)");
        check(Short.toUnsignedInt((short) 128) == 128, "toUnsignedInt(128)");
        check(Short.toUnsignedInt((short) 129) == 129, "toUnsignedInt(129)");
        check(Short.toUnsignedInt(Short.MAX_VALUE) == 32767, "toUnsignedInt(MAX)");
        check(Short.toUnsignedInt(Short.MIN_VALUE) == 32768, "toUnsignedInt(MIN)");
        check(Short.toUnsignedInt((short) -1) == 65535, "toUnsignedInt(-1)");
        // toUnsignedLong 同表
        check(Short.toUnsignedLong((short) 0) == 0L, "toUnsignedLong(0)");
        check(Short.toUnsignedLong((short) 128) == 128L, "toUnsignedLong(128)");
        check(Short.toUnsignedLong(Short.MIN_VALUE) == 32768L, "toUnsignedLong(MIN)");
        check(Short.toUnsignedLong((short) -2) == 65534L, "toUnsignedLong(-2)");
        check(Short.toUnsignedLong(Short.MAX_VALUE) == 32767L, "toUnsignedLong(MAX)");
    }

    static void testReverseBytesTable() {
        check(Short.reverseBytes((short) 0x0102) == 0x0201, "reverseBytes(0x0102)");
        check(Short.reverseBytes((short) 0) == 0, "reverseBytes(0)");
        check(Short.reverseBytes((short) -1) == -1, "reverseBytes(-1)");
        check(Short.reverseBytes(Short.MIN_VALUE) == 128, "reverseBytes(MIN) == 0x0080");
        check(Short.reverseBytes(Short.MAX_VALUE) == (short) 0xff7f, "reverseBytes(MAX) == 0xff7f");
        check(Short.reverseBytes((short) 0x00ff) == (short) 0xff00, "reverseBytes(0x00ff)");
        check(Short.reverseBytes(Short.reverseBytes((short) 0xabcd)) == (short) 0xabcd,
              "reverseBytes involution");
    }

    static void testNarrowingConversions() {
        // (byte)128 → -128 的窄化经 Short 实例观察（JLS 5.1.3 截断高位）
        check(Short.valueOf((short) 128).byteValue() == -128, "short 128 narrows to byte -128");
        check(Short.valueOf((short) 300).byteValue() == 44, "short 300 narrows to byte 44");
        check(Short.valueOf(Short.MAX_VALUE).byteValue() == -1, "short MAX narrows to byte -1");
        check(Short.valueOf(Short.MIN_VALUE).byteValue() == 0, "short MIN narrows to byte 0");
        // 宽化链
        check(Short.valueOf((short) -1).intValue() == -1, "short -1 intValue");
        check(Short.valueOf((short) -1).longValue() == -1L, "short -1 longValue");
        check(Short.valueOf((short) -1).floatValue() == -1.0f, "short -1 floatValue");
        check(Short.valueOf((short) -1).doubleValue() == -1.0, "short -1 doubleValue");
        check(Short.valueOf(Short.MIN_VALUE).doubleValue() == -32768.0, "MIN doubleValue");
        check(Short.valueOf(Short.MAX_VALUE).intValue() == 32767, "MAX intValue");
        // (byte)(Byte.MAX_VALUE + 1) 溢出经编译期窄化观察
        check((byte) (Byte.MAX_VALUE + 1) == -128, "(byte)(127+1) == -128 编译期窄化");
        check(Short.valueOf((short) (byte) (Byte.MAX_VALUE + 1)).byteValue() == -128, "round-trip byte 窄化");
    }

    static void testValueOfCacheBoundary() {
        // -128..127 缓存：同一实例（==）、缓存外不同实例但 equals
        check(Short.valueOf((short) -128) == Short.valueOf((short) -128), "cache -128 identity");
        check(Short.valueOf((short) 127) == Short.valueOf((short) 127), "cache 127 identity");
        check(Short.valueOf((short) 128) != Short.valueOf((short) 128), "128 not cached");
        check(Short.valueOf((short) -129) != Short.valueOf((short) -129), "-129 not cached");
        check(Short.valueOf((short) 128).equals(Short.valueOf((short) 128)), "128 equals by value");
        check(Short.valueOf((short) -129).equals(Short.valueOf((short) -129)), "-129 equals by value");
        // 与 new Short 跨缓存实例（构造器已弃用但两侧仍存在）
        @SuppressWarnings("deprecation")
        Short fresh127 = new Short((short) 127);
        check(fresh127 != Short.valueOf((short) 127), "new Short(127) bypasses cache");
        check(fresh127.equals(Short.valueOf((short) 127)), "new Short(127).equals cached");
        @SuppressWarnings("deprecation")
        Short freshMin = new Short(Short.MIN_VALUE);
        check(freshMin.shortValue() == Short.MIN_VALUE, "new Short(MIN).shortValue");
        // 静态 hashCode(short) 与实例一致
        check(Short.hashCode((short) 12345) == Short.valueOf((short) 12345).hashCode(),
              "static hashCode == instance");
        check(Short.hashCode((short) -1) == -1, "hashCode(-1) == -1");
        check(Short.hashCode((short) 0) == 0, "hashCode(0) == 0");
    }

    static void testCompareUnsignedTable() {
        // JDK-8 无 Short.compareUnsigned（JDK 9+）：经 Integer.compareUnsigned(toUnsignedInt) 等价表达
        check(Integer.compareUnsigned(Short.toUnsignedInt((short) 0), Short.toUnsignedInt((short) -1)) < 0,
              "unsigned(0) < unsigned(-1)");
        check(Integer.compareUnsigned(Short.toUnsignedInt((short) -2), Short.toUnsignedInt((short) -1)) < 0,
              "unsigned(-2) < unsigned(-1)");
        check(Integer.compareUnsigned(Short.toUnsignedInt((short) -1), Short.toUnsignedInt((short) -2)) > 0,
              "unsigned(-1) > unsigned(-2)");
        check(Integer.compareUnsigned(Short.toUnsignedInt((short) -1), Short.toUnsignedInt((short) -1)) == 0,
              "unsigned(-1) == unsigned(-1)");
        check(Integer.compareUnsigned(Short.toUnsignedInt(Short.MIN_VALUE),
              Short.toUnsignedInt((short) -1)) < 0, "unsigned(MIN) < unsigned(-1)");
        check(Integer.compareUnsigned(Short.toUnsignedInt((short) -1), Short.toUnsignedInt(Short.MAX_VALUE)) > 0,
              "unsigned(-1) > unsigned(MAX)");
        // compareTo 全方向
        check(Short.valueOf((short) 3).compareTo(Short.valueOf((short) 5)) < 0, "compareTo(3,5)");
        check(Short.valueOf((short) 5).compareTo(Short.valueOf((short) 3)) > 0, "compareTo(5,3)");
        check(Short.valueOf((short) 5).compareTo(Short.valueOf((short) 5)) == 0, "compareTo(5,5)");
        check(Short.compare(Short.MIN_VALUE, Short.MAX_VALUE) < 0, "compare(MIN,MAX)");
        check(Short.compare(Short.MAX_VALUE, Short.MIN_VALUE) > 0, "compare(MAX,MIN)");
    }

    // ---- White-box / structural ----
    static void testWhiteBoxStructural() {
        try {
            Class<?> c = Class.forName("java.lang.Short");
            check(!c.isInterface(), "Short is not an interface");
            check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Short is final");
            check(c.getSuperclass() == Number.class, "Short extends Number");
            check(Comparable.class.isAssignableFrom(c), "Short implements Comparable");
            check(java.io.Serializable.class.isAssignableFrom(c), "Short implements Serializable");
            check(c.getField("MAX_VALUE").getType() == short.class
                  && java.lang.reflect.Modifier.isStatic(c.getField("MAX_VALUE").getModifiers()),
                  "MAX_VALUE is public static final short");
            check(c.getMethod("parseShort", String.class).getReturnType() == short.class,
                  "parseShort(String) returns short");
            check(c.getMethod("valueOf", short.class).getReturnType() == Short.class,
                  "valueOf(short) returns Short");
            // JDK-8 无 compareUnsigned(short,short)（JDK 9+ 才有）
            boolean hasCompareUnsigned = true;
            try {
                c.getMethod("compareUnsigned", short.class, short.class);
            } catch (NoSuchMethodException e) {
                hasCompareUnsigned = false;
            }
            check(!hasCompareUnsigned, "JDK-8 Short has no compareUnsigned (JDK 9+)");
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

    public static void main(String[] args) {
        testConstants();
        testParseShort();
        testParseShortRadix();
        testValueOf();
        testDecode();
        testToStringUnsigned();
        testCompare();
        testReverseBytes();
        testHashCodeEquals();
        // 契约深化（2026-11）
        testNfeMessageContract();
        testParseRadixRoundTrip();
        testToStringUnsignedTable();
        testReverseBytesTable();
        testNarrowingConversions();
        testValueOfCacheBoundary();
        testCompareUnsignedTable();
        testWhiteBoxStructural();
        testBoxing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

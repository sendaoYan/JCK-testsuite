/*
 * @test
 * @summary java.math.BigInteger API conformance: full contract coverage.
 *          constructors (String/radix/byte[]/signum+mag/valueOf/probablePrime) +
 *          exact exception matrix, null semantics, arithmetic (add/subtract/
 *          multiply/divide/divideAndRemainder/remainder/pow) with immutability,
 *          gcd/abs/negate/signum, modular (mod/modPow/modInverse), bit operations
 *          (shift/and/or/xor/not/andNot/testBit/setBit/clearBit/flipBit/
 *          getLowestSetBit/bitLength/bitCount), primality, compareTo/equals/
 *          hashCode/min/max, radix toString/toByteArray/*Exact conversions,
 *          serialization round-trip + serialVersionUID.
 * @run main BigIntegerTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.math.BigInteger;
import java.util.Random;

public class BigIntegerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static BigInteger roundTrip(BigInteger v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (BigInteger) ois.readObject();
    }

    static void checkUID(Class<?> c, long expected) {
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == expected,
              c.getSimpleName() + " serialVersionUID");
    }

    static void testConstructors() {
        check(new BigInteger("0").equals(BigInteger.ZERO), "String zero");
        check(new BigInteger("+123").equals(BigInteger.valueOf(123)), "String leading plus");
        check(new BigInteger("-123").equals(BigInteger.valueOf(-123)), "String negative");
        check(new BigInteger("123456789012345678901234567890").toString()
                .equals("123456789012345678901234567890"), "String big");
        check(new BigInteger("101", 2).equals(BigInteger.valueOf(5)), "radix 2");
        check(new BigInteger("ff", 16).equals(BigInteger.valueOf(255)), "radix 16");
        check(new BigInteger("z", 36).equals(BigInteger.valueOf(35)), "radix 36");
        check(new BigInteger("10", 10).equals(BigInteger.TEN), "radix 10");
        // byte[] two's complement
        check(new BigInteger(new byte[]{0, -1}).equals(BigInteger.valueOf(255)), "byte[] 255");
        check(new BigInteger(new byte[]{-1}).equals(BigInteger.valueOf(-1)), "byte[] -1");
        check(new BigInteger(new byte[]{0}).equals(BigInteger.ZERO), "byte[] zero");
        // signum + magnitude (big-endian magnitude)
        check(new BigInteger(1, new byte[]{1, 0}).equals(BigInteger.valueOf(256)), "signum+mag");
        check(new BigInteger(-1, new byte[]{1}).equals(BigInteger.valueOf(-1)), "signum -1");
        check(new BigInteger(0, new byte[]{0}).equals(BigInteger.ZERO), "signum 0 zero mag");
        // valueOf
        check(BigInteger.valueOf(0).equals(BigInteger.ZERO), "valueOf 0");
        check(BigInteger.valueOf(Long.MAX_VALUE).longValue() == Long.MAX_VALUE, "valueOf max");
        check(BigInteger.valueOf(-1).equals(BigInteger.ONE.negate()), "valueOf -1");
        // probablePrime is deterministic given a fixed Random seed
        BigInteger pp = BigInteger.probablePrime(24, new Random(42L));
        check(pp.bitLength() >= 24 && pp.isProbablePrime(30), "probablePrime");
        // round-trip through byte[] for a few values
        BigInteger[] vals = { BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(-1),
                BigInteger.valueOf(255), BigInteger.valueOf(-128),
                new BigInteger("123456789012345678901234567890") };
        for (BigInteger v : vals) {
            check(new BigInteger(v.toByteArray()).equals(v), "byte[] round-trip " + v);
        }
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> new BigInteger((String) null), "String null");
        checkThrows(NullPointerException.class, () -> new BigInteger((byte[]) null), "byte[] null");
        checkThrows(NullPointerException.class, () -> new BigInteger(1, (byte[]) null), "signum byte[] null");
        BigInteger one = BigInteger.ONE;
        checkThrows(NullPointerException.class, () -> one.add(null), "add null");
        checkThrows(NullPointerException.class, () -> one.subtract(null), "subtract null");
        checkThrows(NullPointerException.class, () -> one.multiply(null), "multiply null");
        checkThrows(NullPointerException.class, () -> one.divide(null), "divide null");
        checkThrows(NullPointerException.class, () -> one.remainder(null), "remainder null");
        checkThrows(NullPointerException.class, () -> one.gcd(null), "gcd null");
        checkThrows(NullPointerException.class, () -> one.mod(null), "mod null");
        checkThrows(NullPointerException.class, () -> one.modPow(BigInteger.TEN, null), "modPow null modulus");
        checkThrows(NullPointerException.class, () -> one.modInverse(null), "modInverse null");
        checkThrows(NullPointerException.class, () -> one.and(null), "and null");
        checkThrows(NullPointerException.class, () -> one.or(null), "or null");
        checkThrows(NullPointerException.class, () -> one.xor(null), "xor null");
        checkThrows(NullPointerException.class, () -> one.andNot(null), "andNot null");
        checkThrows(NullPointerException.class, () -> one.divideAndRemainder(null), "divideAndRemainder null");
        checkThrows(NullPointerException.class, () -> one.compareTo(null), "compareTo null");
        checkThrows(NullPointerException.class, () -> one.min(null), "min null");
        checkThrows(NullPointerException.class, () -> one.max(null), "max null");
    }

    static void testArithmetic() {
        BigInteger a = new BigInteger("123456789");
        BigInteger b = new BigInteger("987654321");
        check(a.add(b).equals(new BigInteger("1111111110")), "add");
        check(b.subtract(a).equals(new BigInteger("864197532")), "subtract");
        check(a.multiply(b).equals(new BigInteger("121932631112635269")), "multiply");
        check(a.add(b).equals(b.add(a)), "add commutative");
        // immutability: operands unchanged
        check(a.equals(new BigInteger("123456789")) && b.equals(new BigInteger("987654321")),
                "immutability after arithmetic");
        check(new BigInteger("100").divide(new BigInteger("3")).equals(new BigInteger("33")),
                "divide truncates toward zero");
        check(new BigInteger("-100").divide(new BigInteger("3")).equals(new BigInteger("-33")),
                "divide negative truncates toward zero");
        BigInteger[] qr = new BigInteger("100").divideAndRemainder(new BigInteger("7"));
        check(qr[0].equals(new BigInteger("14")) && qr[1].equals(new BigInteger("2")),
                "divideAndRemainder");
        check(new BigInteger("-100").remainder(new BigInteger("7")).equals(new BigInteger("-2")),
                "remainder negative");
        check(BigInteger.valueOf(2).pow(10).equals(BigInteger.valueOf(1024)), "pow");
        check(BigInteger.valueOf(2).pow(0).equals(BigInteger.ONE), "pow 0");
        check(BigInteger.ZERO.pow(0).equals(BigInteger.ONE), "0^0 == 1");
        check(BigInteger.valueOf(10).pow(1).equals(BigInteger.TEN), "pow 1");
    }

    static void testGcdAndSign() {
        check(new BigInteger("54").gcd(new BigInteger("24")).equals(new BigInteger("6")), "gcd");
        check(new BigInteger("-54").gcd(new BigInteger("24")).equals(new BigInteger("6")),
                "gcd non-negative");
        check(BigInteger.ZERO.gcd(BigInteger.ZERO).equals(BigInteger.ZERO), "gcd(0,0)");
        check(new BigInteger("5").abs().equals(new BigInteger("5")), "abs positive");
        check(new BigInteger("-5").abs().equals(new BigInteger("5")), "abs negative");
        check(new BigInteger("5").negate().equals(new BigInteger("-5")), "negate");
        check(new BigInteger("5").signum() == 1, "signum positive");
        check(BigInteger.ZERO.signum() == 0, "signum zero");
        check(new BigInteger("-5").signum() == -1, "signum negative");
        // immutability: abs/negate don't mutate
        BigInteger v = new BigInteger("-5");
        v.abs(); v.negate();
        check(v.equals(new BigInteger("-5")), "immutability abs/negate");
    }

    static void testModular() {
        check(new BigInteger("100").mod(new BigInteger("7")).equals(new BigInteger("2")), "mod");
        check(new BigInteger("-100").mod(new BigInteger("7")).equals(new BigInteger("5")),
                "mod non-negative");
        check(new BigInteger("5").modPow(new BigInteger("3"), new BigInteger("13"))
                .equals(new BigInteger("8")), "modPow");
        check(new BigInteger("3").modInverse(new BigInteger("11")).equals(new BigInteger("4")),
                "modInverse");
        check(BigInteger.ONE.modInverse(new BigInteger("11")).equals(BigInteger.ONE),
                "modInverse 1");
        check(new BigInteger("3").modInverse(BigInteger.ONE).equals(BigInteger.ZERO),
                "modInverse mod 1 == 0");
        check(new BigInteger("2").modPow(BigInteger.ZERO, new BigInteger("5")).equals(BigInteger.ONE),
                "modPow exponent 0");
    }

    static void testBitOperations() {
        BigInteger ten = BigInteger.TEN;   // 1010
        BigInteger twelve = new BigInteger("12"); // 1100
        check(ten.and(twelve).equals(new BigInteger("8")), "and");
        check(ten.or(twelve).equals(new BigInteger("14")), "or");
        check(ten.xor(twelve).equals(new BigInteger("6")), "xor");
        check(ten.not().equals(new BigInteger("-11")), "not");
        check(twelve.andNot(ten).equals(new BigInteger("4")), "andNot");
        check(BigInteger.ONE.shiftLeft(10).equals(new BigInteger("1024")), "shiftLeft");
        check(new BigInteger("1024").shiftRight(3).equals(new BigInteger("128")), "shiftRight");
        check(new BigInteger("16").shiftRight(-2).equals(new BigInteger("64")),
                "shiftRight negative distance = left");
        check(new BigInteger("16").shiftLeft(-2).equals(new BigInteger("4")),
                "shiftLeft negative distance = right");
        check(BigInteger.ZERO.shiftLeft(100).equals(BigInteger.ZERO), "shift zero");
        check(ten.testBit(1) && ten.testBit(3) && !ten.testBit(0), "testBit");
        check(ten.setBit(0).equals(new BigInteger("11")), "setBit");
        check(ten.clearBit(1).equals(new BigInteger("8")), "clearBit");
        check(ten.flipBit(0).equals(new BigInteger("11")), "flipBit");
        check(BigInteger.ZERO.getLowestSetBit() == -1, "getLowestSetBit zero");
        check(new BigInteger("8").getLowestSetBit() == 3, "getLowestSetBit");
        check(BigInteger.ZERO.bitLength() == 0, "bitLength zero");
        check(new BigInteger("1024").bitLength() == 11, "bitLength 1024");
        check(BigInteger.valueOf(7).bitCount() == 3, "bitCount positive");
        check(new BigInteger("-1").bitCount() == 0, "bitCount -1");
        check(new BigInteger("-2").bitCount() == 1, "bitCount -2");
        // immutability
        BigInteger v = BigInteger.TEN;
        v.setBit(0); v.clearBit(1); v.flipBit(2); v.shiftLeft(4); v.not();
        check(v.equals(BigInteger.TEN), "immutability bit ops");
    }

    static void testPrimality() {
        check(new BigInteger("2").isProbablePrime(50), "2 prime");
        check(new BigInteger("97").isProbablePrime(50), "97 prime");
        check(!new BigInteger("100").isProbablePrime(50), "100 composite");
        check(!BigInteger.ONE.isProbablePrime(50), "1 not prime");
        check(!BigInteger.ZERO.isProbablePrime(50), "0 not prime");
        check(new BigInteger("2").nextProbablePrime().equals(new BigInteger("3")),
                "nextProbablePrime 2 -> 3");
        check(new BigInteger("13").nextProbablePrime().equals(new BigInteger("17")),
                "nextProbablePrime 13 -> 17");
        check(new BigInteger("0").nextProbablePrime().equals(new BigInteger("2")),
                "nextProbablePrime 0 -> 2");
    }

    static void testComparison() {
        BigInteger a = BigInteger.valueOf(100);
        BigInteger b = BigInteger.valueOf(200);
        BigInteger a2 = BigInteger.valueOf(100);
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("100") == false, "equals other type");
        check(a.min(b) == a && a.max(b) == b, "min/max");
        check(BigInteger.valueOf(-1).compareTo(BigInteger.ZERO) < 0, "compareTo negative");
    }

    static void testConversions() {
        BigInteger v = BigInteger.valueOf(123);
        check(v.intValue() == 123, "intValue");
        check(v.longValue() == 123L, "longValue");
        check(v.floatValue() == 123.0f, "floatValue");
        check(v.doubleValue() == 123.0d, "doubleValue");
        check(v.intValueExact() == 123, "intValueExact");
        check(v.longValueExact() == 123L, "longValueExact");
        check(v.shortValueExact() == (short) 123, "shortValueExact");
        check(v.byteValueExact() == (byte) 123, "byteValueExact");
        check(new BigInteger("100").toString().equals("100"), "toString");
        check(new BigInteger("-100").toString().equals("-100"), "toString negative");
        check(new BigInteger("255").toString(16).equals("ff"), "toString hex");
        check(new BigInteger("255").toString(2).equals("11111111"), "toString binary");
        check(new BigInteger("255").toString(8).equals("377"), "toString octal");
        check(BigInteger.ZERO.toString(16).equals("0"), "toString zero radix");
        check(new BigInteger("0").toByteArray().length == 1
                && new BigInteger("0").toByteArray()[0] == 0, "toByteArray zero");
    }

    static void testConversionsExactErrors() {
        BigInteger huge = BigInteger.ONE.shiftLeft(200);
        checkThrows(ArithmeticException.class, () -> huge.intValueExact(), "intValueExact overflow");
        checkThrows(ArithmeticException.class, () -> huge.longValueExact(), "longValueExact overflow");
        checkThrows(ArithmeticException.class, () -> huge.shortValueExact(), "shortValueExact overflow");
        checkThrows(ArithmeticException.class, () -> huge.byteValueExact(), "byteValueExact overflow");
    }

    static void testSerialization() throws Exception {
        checkUID(BigInteger.class, -8287574255936472291L);
        BigInteger[] vals = { BigInteger.ZERO, BigInteger.ONE, new BigInteger("-1"),
                new BigInteger("12345678901234567890123456789012345678901234567890") };
        for (BigInteger v : vals) {
            BigInteger r = roundTrip(v);
            check(r.equals(v), "serialization round-trip " + v);
            check(r != v, "serialization new instance " + v);
        }
    }

    static void testErrors() {
        checkThrows(NumberFormatException.class, () -> new BigInteger(""), "empty String");
        checkThrows(NumberFormatException.class, () -> new BigInteger("12a"), "bad digit");
        checkThrows(NumberFormatException.class, () -> new BigInteger(new byte[0]), "empty byte[]");
        checkThrows(NumberFormatException.class, () -> new BigInteger(0, new byte[]{1}),
                "signum 0 nonzero mag");
        checkThrows(NumberFormatException.class, () -> new BigInteger(2, new byte[]{1}),
                "signum 2 invalid");
        checkThrows(NumberFormatException.class, () -> new BigInteger("10", 37), "radix 37");
        checkThrows(NumberFormatException.class, () -> new BigInteger("10", 1), "radix 1");
        checkThrows(IllegalArgumentException.class, () -> new BigInteger(-1, new Random()), "numBits -1");
        checkThrows(ArithmeticException.class, () -> new BigInteger(1, 100, new Random()), "bitLength < 2");
        checkThrows(ArithmeticException.class, () -> BigInteger.probablePrime(1, new Random()),
                "probablePrime bitLength < 2");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.divide(BigInteger.ZERO),
                "divide by zero");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.remainder(BigInteger.ZERO),
                "remainder by zero");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.divideAndRemainder(BigInteger.ZERO),
                "divideAndRemainder by zero");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.pow(-1), "negative exponent");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.mod(BigInteger.ZERO),
                "mod zero");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.mod(new BigInteger("-1")),
                "mod negative");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.modPow(BigInteger.ONE, BigInteger.ZERO),
                "modPow zero modulus");
        checkThrows(ArithmeticException.class, () -> BigInteger.ONE.modInverse(BigInteger.ZERO),
                "modInverse zero");
        checkThrows(ArithmeticException.class, () -> new BigInteger("2").modInverse(new BigInteger("4")),
                "modInverse not invertible");
        checkThrows(ArithmeticException.class, () -> BigInteger.ZERO.setBit(-1), "setBit -1");
        checkThrows(ArithmeticException.class, () -> BigInteger.ZERO.clearBit(-1), "clearBit -1");
        checkThrows(ArithmeticException.class, () -> BigInteger.ZERO.flipBit(-1), "flipBit -1");
        checkThrows(ArithmeticException.class, () -> BigInteger.ZERO.testBit(-1), "testBit -1");
    }

    // ===== 契约深化（2026-11）：精确异常消息助手 =====

    static void checkMsg(Class<? extends Throwable> expected, String message, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            if (!expected.isInstance(t)) {
                throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            if (t.getMessage() == null || !t.getMessage().equals(message)) {
                throw new AssertionError(msg + ": expected message \"" + message
                        + "\" but was \"" + t.getMessage() + "\"");
            }
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ===== 契约深化（2026-11）：构造与补码字节契约 =====

    static void testConstructorExactness() {
        // (byte[]) 二补码大端：{0xFE,0x02} 首字节符号位 1 → 负值 -510（实测锁定）
        check(new BigInteger(new byte[]{(byte) 0xFE, (byte) 0x02}).intValue() == -510,
              "byte[] {FE,02} == -510");
        check(new BigInteger(new byte[]{(byte) 0xFE, (byte) 0x02})
                .equals(BigInteger.valueOf(-510)), "byte[] {FE,02} equals -510");
        check(new BigInteger(new byte[]{-128}).intValue() == -128, "byte[] {-128} 边界");
        check(new BigInteger(new byte[]{(byte) 0xFF, (byte) 0x02}).intValue() == -254,
              "byte[] {FF,02} == -254");
        check(new BigInteger(new byte[]{0, 0}).signum() == 0, "byte[] {0,0} == 0");
        check(new BigInteger(new byte[]{0, 1}).intValue() == 1, "byte[] 前导零 {0,1} == 1");
        // toByteArray 补码 round-trip（招牌）
        check(java.util.Arrays.equals(BigInteger.valueOf(-254).toByteArray(),
              new byte[]{(byte) 0xFF, (byte) 0x02}), "toByteArray(-254) == {FF,02}");
        check(java.util.Arrays.equals(BigInteger.valueOf(255).toByteArray(),
              new byte[]{0, (byte) 0xFF}), "toByteArray(255) 前导零符号位");
        check(java.util.Arrays.equals(BigInteger.valueOf(5).toByteArray(), new byte[]{5}),
              "toByteArray(5) == {5}");
        check(java.util.Arrays.equals(BigInteger.valueOf(-1).toByteArray(), new byte[]{-1}),
              "toByteArray(-1) == {FF}");
        BigInteger[] vals = { BigInteger.valueOf(254), BigInteger.valueOf(-128),
                BigInteger.ONE.shiftLeft(16), BigInteger.valueOf(Long.MIN_VALUE),
                new BigInteger("123456789012345678901234567890") };
        for (BigInteger v : vals) {
            check(new BigInteger(v.toByteArray()).equals(v), "补码字节 round-trip " + v);
        }
        // signum-magnitude：零 magnitude 归一化为 0（前导零剥离）
        check(new BigInteger(1, new byte[]{0}).signum() == 0, "(1,{0}) 归一 0");
        check(new BigInteger(-1, new byte[]{0, 0}).signum() == 0, "(-1,{0,0}) 归一 0");
        check(new BigInteger(1, new byte[]{0, 0, 5}).intValue() == 5, "(1,{0,0,5}) 剥离前导零");
        check(new BigInteger(-1, new byte[]{0, 1, 2}).intValue() == -258, "(-1,{0,1,2}) == -258");
        // radix 构造
        check(new BigInteger("ff", 16).intValue() == 255, "\"ff\" 16 == 255");
        check(new BigInteger("-ff", 16).intValue() == -255, "\"-ff\" 16 == -255");
        check(new BigInteger("FF", 16).intValue() == 255, "\"FF\" 大写 16 == 255");
        check(new BigInteger("zz", 36).intValue() == 1295, "\"zz\" 36 == 1295");
        check(new BigInteger("7fffffffffffffff", 16).longValue() == Long.MAX_VALUE,
              "\"7fffffffffffffff\" 16 == Long.MAX");
        check(new BigInteger("8000000000000000", 16).longValue() == Long.MIN_VALUE,
              "\"8000000000000000\" 16 == Long.MIN");
        check(new BigInteger(new BigInteger("255").toString(16), 16).intValue() == 255,
              "toString(16) → 构造 round-trip");
        check(new BigInteger(new BigInteger("-98765432109876543210").toString(36), 36)
                .equals(new BigInteger("-98765432109876543210")), "36 进制往返");
        // valueOf 缓存
        check(BigInteger.valueOf(0) == BigInteger.ZERO, "valueOf(0) == ZERO 同一实例");
        check(BigInteger.valueOf(1) == BigInteger.ONE, "valueOf(1) == ONE 同一实例");
        check(BigInteger.valueOf(10) == BigInteger.TEN, "valueOf(10) == TEN 同一实例");
        check(BigInteger.valueOf(2).intValue() == 2, "value 2（TWO 在 JDK-8 为 private）");
        check(BigInteger.valueOf(2).signum() == 1, "2 signum");
        check(BigInteger.valueOf(100).equals(new BigInteger("100")), "equals 跨构造一致");
        check(new BigInteger("5").equals(BigInteger.valueOf(5)), "\"5\" equals valueOf(5)");
        // 非法构造精确消息（两侧逐字一致）
        checkMsg(NumberFormatException.class, "For input string: \"x10\"",
              () -> new BigInteger("0x10"), "\"0x10\" 前缀消息逐字");
        checkMsg(NumberFormatException.class, "For input string: \"12a\"",
              () -> new BigInteger("12a"), "非法数字消息逐字");
        checkMsg(NumberFormatException.class, "Zero length BigInteger",
              () -> new BigInteger(""), "空串消息逐字");
        checkMsg(NumberFormatException.class, "Zero length BigInteger",
              () -> new BigInteger(new byte[0]), "空 byte[] 消息逐字");
        checkMsg(NumberFormatException.class, "signum-magnitude mismatch",
              () -> new BigInteger(0, new byte[]{1}), "signum0 非零 mag 消息逐字");
        checkMsg(NumberFormatException.class, "Invalid signum value",
              () -> new BigInteger(2, new byte[]{1}), "signum2 消息逐字");
        checkMsg(NumberFormatException.class, "Radix out of range",
              () -> new BigInteger("10", 37), "radix 37 消息逐字");
        checkMsg(NumberFormatException.class, "Radix out of range",
              () -> new BigInteger("10", 1), "radix 1 消息逐字");
    }

    // ===== 契约深化（2026-11）：位操作契约表 =====

    static void testBitContracts() {
        // bitLength：最小二补码表示位数（不含符号位）；负数 == bitLength(-(x+1))
        check(BigInteger.ZERO.bitLength() == 0, "bitLength(0) == 0");
        check(BigInteger.ONE.bitLength() == 1, "bitLength(1) == 1");
        check(BigInteger.valueOf(255).bitLength() == 8, "bitLength(255) == 8");
        check(BigInteger.valueOf(256).bitLength() == 9, "bitLength(256) == 9");
        check(BigInteger.valueOf(-1).bitLength() == 0, "bitLength(-1) == 0（招牌）");
        check(BigInteger.valueOf(-2).bitLength() == 1, "bitLength(-2) == 1");
        check(BigInteger.valueOf(-256).bitLength() == 8, "bitLength(-256) == 8");
        check(BigInteger.valueOf(-257).bitLength() == 9, "bitLength(-257) == 9");
        // bitCount：正数 = 1 的个数；负数 = 距 -1 的距离中 0 的个数
        check(BigInteger.valueOf(7).bitCount() == 3, "bitCount(7) == 3");
        check(BigInteger.valueOf(255).bitCount() == 8, "bitCount(255) == 8");
        check(BigInteger.valueOf(-1).bitCount() == 0, "bitCount(-1) == 0");
        check(BigInteger.valueOf(-2).bitCount() == 1, "bitCount(-2) == 1");
        check(BigInteger.valueOf(-10).bitCount() == 2, "bitCount(-10) == 2");
        // getLowestSetBit
        check(BigInteger.ZERO.getLowestSetBit() == -1, "getLowestSetBit(0) == -1");
        check(BigInteger.valueOf(12).getLowestSetBit() == 2, "getLowestSetBit(12) == 2");
        check(BigInteger.valueOf(-12).getLowestSetBit() == 2, "getLowestSetBit(-12) == 2");
        check(BigInteger.ONE.getLowestSetBit() == 0, "getLowestSetBit(1) == 0");
        // 负数补码逻辑运算（实测精确）
        check(BigInteger.valueOf(5).and(BigInteger.valueOf(-3)).intValue() == 5, "5 & -3 == 5");
        check(BigInteger.valueOf(-1).and(BigInteger.valueOf(255)).intValue() == 255, "-1 & 0xFF == 255");
        check(BigInteger.valueOf(-1).or(BigInteger.ZERO).intValue() == -1, "-1 | 0 == -1");
        check(BigInteger.valueOf(-1).xor(BigInteger.valueOf(-1)).intValue() == 0, "-1 ^ -1 == 0");
        check(BigInteger.valueOf(255).andNot(BigInteger.valueOf(15)).intValue() == 240,
              "255 & ~15 == 240");
        check(BigInteger.valueOf(-1).not().equals(BigInteger.ZERO), "~(-1) == 0");
        check(BigInteger.ZERO.not().equals(BigInteger.valueOf(-1)), "~0 == -1");
        // 移位：算术右移与负移位反向
        check(BigInteger.valueOf(-1).shiftRight(10).intValue() == -1, "-1 >> 10 == -1（符号扩展）");
        check(BigInteger.valueOf(-7).shiftRight(1).intValue() == -4, "-7 >> 1 == -4（向下取整）");
        check(BigInteger.valueOf(1024).shiftRight(10).intValue() == 1, "1024 >> 10 == 1");
        check(BigInteger.ONE.shiftLeft(64).toString().equals("18446744073709551616"), "1 << 64 == 2^64");
        check(BigInteger.ONE.shiftLeft(64).shiftRight(64).intValue() == 1, "2^64 >> 64 == 1");
        check(BigInteger.valueOf(5).shiftLeft(-1).equals(BigInteger.valueOf(5).shiftRight(1)),
              "shiftLeft(-1) == shiftRight(1)");
        check(BigInteger.valueOf(5).shiftRight(-1).equals(BigInteger.valueOf(5).shiftLeft(1)),
              "shiftRight(-1) == shiftLeft(1)");
        // 位访问器矩阵
        BigInteger ff = BigInteger.valueOf(255);
        check(ff.testBit(0) && ff.testBit(7), "testBit(255, 0/7) true");
        check(!ff.testBit(8), "testBit(255, 8) false");
        check(ff.testBit(-1 + 1) && !BigInteger.ZERO.testBit(0), "testBit 零全 false");
        check(BigInteger.ZERO.setBit(0).intValue() == 1, "0.setBit(0) == 1");
        check(BigInteger.ONE.clearBit(0).intValue() == 0, "1.clearBit(0) == 0");
        check(BigInteger.ONE.flipBit(0).intValue() == 0, "1.flipBit(0) == 0");
        check(BigInteger.ZERO.flipBit(0).intValue() == 1, "0.flipBit(0) == 1");
        check(ff.clearBit(7).intValue() == 127, "255.clearBit(7) == 127");
        check(BigInteger.valueOf(127).setBit(7).intValue() == 255, "127.setBit(7) == 255");
        check(BigInteger.TEN.setBit(2).equals(BigInteger.valueOf(14)), "10.setBit(2) == 14");
        // 负位址消息逐字
        checkMsg(ArithmeticException.class, "Negative bit address",
              () -> BigInteger.ZERO.setBit(-1), "setBit(-1) 消息逐字");
        checkMsg(ArithmeticException.class, "Negative bit address",
              () -> BigInteger.ZERO.clearBit(-5), "clearBit(-5) 消息逐字");
        checkMsg(ArithmeticException.class, "Negative bit address",
              () -> BigInteger.ZERO.flipBit(-1), "flipBit(-1) 消息逐字");
        checkMsg(ArithmeticException.class, "Negative bit address",
              () -> BigInteger.ZERO.testBit(-1), "testBit(-1) 消息逐字");
    }

    // ===== 契约深化（2026-11）：mod vs remainder 分叉 + 数论契约 =====

    static void testModularContracts() {
        // 招牌：mod 结果 ∈ [0, m)，remainder 符号随被除数
        check(BigInteger.valueOf(-3).mod(BigInteger.valueOf(5)).intValue() == 2, "-3 mod 5 == 2");
        check(BigInteger.valueOf(-3).remainder(BigInteger.valueOf(5)).intValue() == -3,
              "-3 remainder 5 == -3");
        check(BigInteger.valueOf(-7).mod(BigInteger.valueOf(5)).intValue() == 3, "-7 mod 5 == 3");
        check(BigInteger.valueOf(-7).remainder(BigInteger.valueOf(5)).intValue() == -2,
              "-7 remainder 5 == -2");
        check(BigInteger.valueOf(100).mod(BigInteger.valueOf(7)).intValue() == 2, "100 mod 7 == 2");
        check(BigInteger.valueOf(-100).mod(BigInteger.valueOf(7)).intValue() == 5, "-100 mod 7 == 5");
        // 费马小定理：p 素数时 a^(p-1) ≡ 1 (mod p)
        check(BigInteger.valueOf(2).modPow(BigInteger.valueOf(10), BigInteger.valueOf(11))
                .intValue() == 1, "2^10 mod 11 == 1（费马）");
        check(BigInteger.valueOf(2).pow(10).mod(BigInteger.valueOf(11)).intValue() == 1,
              "2^10 先 pow 后 mod == 1（一致性）");
        check(BigInteger.valueOf(3).modPow(BigInteger.valueOf(6), BigInteger.valueOf(7))
                .intValue() == 1, "3^6 mod 7 == 1（费马）");
        check(BigInteger.valueOf(3).pow(6).mod(BigInteger.valueOf(7)).intValue() == 1,
              "3^6 先 pow 后 mod == 1（一致性）");
        check(BigInteger.valueOf(2).modPow(BigInteger.valueOf(-1), BigInteger.valueOf(5)).intValue() == 3,
              "modPow 负指数走逆元：2^-1 mod 5 == 3");
        // modInverse 与自验证
        check(BigInteger.valueOf(3).modInverse(BigInteger.valueOf(7)).intValue() == 5, "inv(3) mod 7 == 5");
        check(BigInteger.valueOf(3).multiply(BigInteger.valueOf(5)).mod(BigInteger.valueOf(7))
                .intValue() == 1, "3*5 mod 7 == 1（逆元自验证）");
        check(BigInteger.valueOf(4).modInverse(BigInteger.valueOf(9)).intValue() == 7, "inv(4) mod 9 == 7");
        checkMsg(ArithmeticException.class, "BigInteger not invertible.",
              () -> BigInteger.valueOf(6).modInverse(BigInteger.valueOf(8)), "gcd(6,8)=2 无逆消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger not invertible.",
              () -> BigInteger.valueOf(2).modInverse(BigInteger.valueOf(4)), "gcd(2,4)=2 无逆");
        // gcd 表
        check(BigInteger.valueOf(6).gcd(BigInteger.valueOf(8)).intValue() == 2, "gcd(6,8) == 2");
        check(BigInteger.valueOf(12).gcd(BigInteger.valueOf(18)).intValue() == 6, "gcd(12,18) == 6");
        check(BigInteger.ZERO.gcd(BigInteger.ZERO).intValue() == 0, "gcd(0,0) == 0");
        check(BigInteger.ZERO.gcd(BigInteger.valueOf(5)).intValue() == 5, "gcd(0,5) == 5");
        check(BigInteger.valueOf(5).gcd(BigInteger.ZERO).intValue() == 5, "gcd(5,0) == 5");
        // 模必须为正：消息逐字
        checkMsg(ArithmeticException.class, "BigInteger: modulus not positive",
              () -> BigInteger.ONE.mod(BigInteger.ZERO), "mod(0) 消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger: modulus not positive",
              () -> BigInteger.ONE.mod(BigInteger.valueOf(-1)), "mod(-1) 消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger: modulus not positive",
              () -> BigInteger.ONE.modPow(BigInteger.ONE, BigInteger.ZERO), "modPow(m=0) 消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger: modulus not positive",
              () -> BigInteger.ONE.modInverse(BigInteger.ZERO), "modInverse(0) 消息逐字");
    }

    // ===== 契约深化（2026-11）：大数算术与错误消息精确表 =====

    static void testBigArithmeticAndMessages() {
        BigInteger a = new BigInteger("12345678901234567890");
        BigInteger b = new BigInteger("98765432109876543210");
        check(a.multiply(b).toString()
                .equals("1219326311370217952237463801111263526900"), "20 位十进制乘法全等");
        check(a.add(b).toString().equals("111111111011111111100"), "20 位加法全等");
        check(b.subtract(a).toString().equals("86419753208641975320"), "20 位减法全等");
        check(b.divide(a).toString().equals("8"), "20 位除法商全等");
        check(b.remainder(a).toString().equals("900000000090"), "20 位除法余数全等");
        check(b.divideAndRemainder(a)[1].toString().equals("900000000090"),
              "divideAndRemainder 余数一致");
        check(BigInteger.valueOf(2).pow(10).toString().equals("1024"), "2^10 == \"1024\"");
        check(BigInteger.valueOf(2).pow(16).toString().equals("65536"), "2^16 == \"65536\"");
        check(BigInteger.valueOf(10).pow(20).toString().equals("100000000000000000000"),
              "10^20 全等");
        // 除零家族消息逐字
        checkMsg(ArithmeticException.class, "BigInteger divide by zero",
              () -> BigInteger.ONE.divide(BigInteger.ZERO), "divide/0 消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger divide by zero",
              () -> BigInteger.ONE.remainder(BigInteger.ZERO), "remainder/0 消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger divide by zero",
              () -> BigInteger.ONE.divideAndRemainder(BigInteger.ZERO), "divideAndRemainder/0 消息逐字");
        checkMsg(ArithmeticException.class, "Negative exponent",
              () -> BigInteger.ONE.pow(-1), "pow(-1) 消息逐字");
        checkMsg(ArithmeticException.class, "Negative exponent",
              () -> BigInteger.valueOf(5).pow(-2), "pow(-2) 消息逐字");
        // 精确转换溢出消息逐字
        checkMsg(ArithmeticException.class, "BigInteger out of long range",
              () -> new BigInteger("9223372036854775808").longValueExact(), "longValueExact 溢出消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger out of int range",
              () -> new BigInteger("2147483648").intValueExact(), "intValueExact 溢出消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger out of short range",
              () -> BigInteger.valueOf(65536).shortValueExact(), "shortValueExact 溢出消息逐字");
        checkMsg(ArithmeticException.class, "BigInteger out of byte range",
              () -> BigInteger.valueOf(128).byteValueExact(), "byteValueExact 溢出消息逐字");
        // 非精确转换回绕（JLS 5.1.3 收窄语义）
        check(BigInteger.ONE.shiftLeft(64).longValue() == 0L, "2^64 longValue 回绕 0");
        check(BigInteger.ONE.shiftLeft(64).add(BigInteger.ONE).longValue() == 1L, "2^64+1 longValue == 1");
        check(new BigInteger("4294967296").intValue() == 0, "2^32 intValue 回绕 0");
        check(BigInteger.TEN.pow(400).doubleValue() == Double.POSITIVE_INFINITY,
              "10^400 doubleValue Infinity");
        check(BigInteger.TEN.pow(400).floatValue() == Float.POSITIVE_INFINITY,
              "10^400 floatValue Infinity");
        // 恒等与符号
        check(BigInteger.valueOf(5).add(BigInteger.valueOf(-5)).signum() == 0, "5+(-5) == 0");
        check(BigInteger.valueOf(5).subtract(BigInteger.valueOf(-5)).intValue() == 10, "5-(-5) == 10");
        check(BigInteger.valueOf(-5).subtract(BigInteger.valueOf(5)).intValue() == -10, "-5-5 == -10");
        check(BigInteger.valueOf(5).multiply(BigInteger.ZERO).signum() == 0, "5*0 == 0");
        check(BigInteger.valueOf(5).multiply(BigInteger.ONE).intValue() == 5, "5*1 == 5");
        check(BigInteger.ZERO.negate().signum() == 0, "negate(ZERO) == 0（无负零）");
    }

    // ===== 契约深化（2026-11）：素性判定确定性矩阵 =====

    static void testPrimalityMatrix() {
        check(BigInteger.valueOf(7).isProbablePrime(20), "7 素");
        check(BigInteger.valueOf(2).isProbablePrime(20), "2 素");
        check(new BigInteger("104729").isProbablePrime(20), "104729 素");
        check(!BigInteger.valueOf(9).isProbablePrime(20), "9 合数");
        check(!new BigInteger("104730").isProbablePrime(20), "104730 合数");
        check(!BigInteger.ONE.isProbablePrime(20), "1 非素");
        // 负数：实现先取 abs 判定，|−7| 素 → true（两侧一致，实测锁定）
        check(BigInteger.valueOf(-7).isProbablePrime(20), "isProbablePrime(-7) == true（按 abs 判定）");
        check(!BigInteger.valueOf(-9).isProbablePrime(20), "isProbablePrime(-9) == false");
        // certainty <= 0 时直接返回 true
        check(BigInteger.valueOf(2).isProbablePrime(0), "certainty 0 恒 true");
        check(BigInteger.valueOf(9).isProbablePrime(0), "合数 certainty 0 也 true");
        check(BigInteger.valueOf(4).isProbablePrime(-1), "certainty 负恒 true");
    }

    // ===== 契约深化（2026-11）：比较/哈希/序列化补充 =====

    static void testComparisonAndSerializationExtra() throws Exception {
        // compareTo 传递性与反对称
        BigInteger one = BigInteger.ONE, two = BigInteger.valueOf(2), three = BigInteger.valueOf(3);
        check(one.compareTo(two) < 0 && two.compareTo(three) < 0 && one.compareTo(three) < 0,
              "compareTo 传递性");
        check(two.compareTo(one) == -one.compareTo(two), "compareTo 反对称");
        check(one.compareTo(one) == 0, "compareTo 自反");
        check(new BigInteger("-12345678901234567890").compareTo(BigInteger.ZERO) < 0, "大负数 < 0");
        check(new BigInteger("12345678901234567890").compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0,
              "20 位数 > Long.MAX");
        // hashCode 语义
        check(new BigInteger("5").hashCode() == 5, "hashCode(5) == 5（值即哈希）");
        check(BigInteger.valueOf(123).hashCode() == new BigInteger("123").hashCode(),
              "等值实例 hashCode 相等");
        BigInteger hv = new BigInteger("987654321");
        check(hv.hashCode() == hv.hashCode(), "hashCode 两次调用稳定");
        // min/max
        check(BigInteger.valueOf(5).max(BigInteger.valueOf(-5)).intValue() == 5, "max(5,-5)");
        check(BigInteger.valueOf(5).min(BigInteger.valueOf(-5)).intValue() == -5, "min(5,-5)");
        check(BigInteger.valueOf(5).max(new BigInteger("5")).intValue() == 5, "max 等值");
        // equals 补充
        check(!BigInteger.valueOf(2).equals("2"), "equals 跨类型 false");
        check(!BigInteger.valueOf(2).equals(null), "equals null false");
        check(BigInteger.valueOf(2).equals(new BigInteger("2")), "valueOf(2) equals new BigInteger(2)");
        // toString round-trip
        BigInteger big = new BigInteger("-987654321098765432109876543210");
        check(new BigInteger(big.toString()).equals(big), "大负数 toString round-trip");
        check(BigInteger.valueOf(-255).toString(16).equals("-ff"), "toString(-255,16) == \"-ff\"");
        check(BigInteger.valueOf(35).toString(36).equals("z"), "toString(35,36) == \"z\"");
        // 序列化补充：toString 与 signum 保留
        BigInteger[] vals = { BigInteger.valueOf(-254), BigInteger.ONE.shiftLeft(64),
                new BigInteger("-987654321098765432109876543210") };
        for (BigInteger v : vals) {
            BigInteger r = roundTrip(v);
            check(r.equals(v), "序列化 round-trip " + v);
            check(r.toString().equals(v.toString()), "序列化 toString 相同 " + v);
            check(r.signum() == v.signum(), "序列化 signum 相同 " + v);
            check(r.hashCode() == v.hashCode(), "序列化 hashCode 相同 " + v);
        }
    }

    // ===== 契约深化（2026-11）：缓存边界 / 负数二补码位语义 / 截断取整表 =====

    static void testCacheAndTwoComplementSemantics() {
        // valueOf 小值缓存（实现常量池 0..16 含负值；两侧一致实测）
        check(BigInteger.valueOf(5) == BigInteger.valueOf(5), "valueOf(5) 缓存同一实例");
        check(BigInteger.valueOf(-1) == BigInteger.valueOf(-1), "valueOf(-1) 缓存同一实例");
        check(BigInteger.valueOf(16) == BigInteger.valueOf(16), "valueOf(16) 缓存上界内");
        check(BigInteger.valueOf(17) != BigInteger.valueOf(17), "valueOf(17) 超出缓存新实例");
        check(BigInteger.valueOf(17).equals(BigInteger.valueOf(17)), "valueOf(17) 仍 equals");
        // 二补码无穷符号扩展：负数高位全 1
        check(BigInteger.valueOf(-1).testBit(1000), "-1.testBit(1000) == true（符号扩展）");
        check(!BigInteger.valueOf(-2).testBit(0), "-2.testBit(0) == false");
        check(BigInteger.valueOf(-2).testBit(1), "-2.testBit(1) == true");
        check(BigInteger.valueOf(-2).setBit(0).intValue() == -1, "-2.setBit(0) == -1");
        check(BigInteger.valueOf(-1).clearBit(0).intValue() == -2, "-1.clearBit(0) == -2");
        check(BigInteger.valueOf(-1).flipBit(0).intValue() == -2, "-1.flipBit(0) == -2");
        check(BigInteger.TEN.andNot(BigInteger.valueOf(-11)).intValue() == 10, "10 & ~(-11) == 10");
        // JLS 5.1.3 收窄截断
        check(BigInteger.valueOf(257).byteValue() == 1, "257 byteValue == 1");
        check(BigInteger.valueOf(256).byteValue() == 0, "256 byteValue == 0");
        check(BigInteger.valueOf(-129).byteValue() == 127, "-129 byteValue == 127");
        check(BigInteger.valueOf(70000).shortValue() == 4464, "70000 shortValue == 4464");
        check(BigInteger.ONE.shiftLeft(40).intValue() == 0, "2^40 intValue == 0");
        check(BigInteger.valueOf(123456789).doubleValue() == 1.23456789E8, "doubleValue 精确");
        check(BigInteger.valueOf(123456789).floatValue() == 1.23456789E8f, "floatValue 精确");
        // 除法/余数四象限表：向零截断、余数符号随被除数
        check(BigInteger.valueOf(-7).divide(BigInteger.valueOf(2)).intValue() == -3, "-7/2 == -3");
        check(BigInteger.valueOf(7).divide(BigInteger.valueOf(-2)).intValue() == -3, "7/-2 == -3");
        check(BigInteger.valueOf(-7).divide(BigInteger.valueOf(-2)).intValue() == 3, "-7/-2 == 3");
        check(BigInteger.valueOf(-7).remainder(BigInteger.valueOf(2)).intValue() == -1, "-7%2 == -1");
        check(BigInteger.valueOf(7).remainder(BigInteger.valueOf(-2)).intValue() == 1, "7%-2 == 1");
        check(BigInteger.valueOf(-7).remainder(BigInteger.valueOf(-2)).intValue() == -1, "-7%-2 == -1");
        // 模 1 恒 0
        check(BigInteger.valueOf(5).mod(BigInteger.ONE).signum() == 0, "5 mod 1 == 0");
        check(BigInteger.valueOf(5).modPow(BigInteger.valueOf(3), BigInteger.ONE).signum() == 0,
              "modPow 模 1 == 0");
        // 移位补表
        check(BigInteger.ONE.shiftLeft(0).intValue() == 1, "1<<0 恒等");
        check(BigInteger.valueOf(5).shiftRight(-1).intValue() == 10, "5>>-1 == 10（反向）");
        check(BigInteger.valueOf(2).pow(31).toString().equals("2147483648"), "2^31 全等");
        check(BigInteger.ZERO.hashCode() == 0, "hashCode(0) == 0");
        check(BigInteger.valueOf(255).hashCode() == 255, "hashCode(255) == 255");
        check(BigInteger.valueOf(987).toString(10).equals(BigInteger.valueOf(987).toString()),
              "toString(10) == toString()");
    }

    // ===== 契约深化（2026-11）：进制矩阵 / 字节数组长度 / 数论补充 =====

    static void testRadixAndByteArrayMatrix() throws Exception {
        check(new BigInteger("1010", 2).intValue() == 10, "\"1010\" 2 == 10");
        check(new BigInteger("777", 8).intValue() == 511, "\"777\" 8 == 511");
        check(BigInteger.valueOf(255).toString(5).equals("2010"), "255 基 5 == \"2010\"");
        check(BigInteger.valueOf(255).toString(36).equals("73"), "255 基 36 == \"73\"");
        check(new BigInteger(BigInteger.valueOf(255).toString(36), 36).intValue() == 255,
              "基 36 往返");
        check(BigInteger.valueOf(255).toByteArray().length == 2, "toByteArray(255) 长 2");
        check(BigInteger.valueOf(127).toByteArray().length == 1, "toByteArray(127) 长 1");
        check(BigInteger.valueOf(128).toByteArray().length == 2, "toByteArray(128) 长 2（符号位）");
        check(BigInteger.valueOf(-254).toByteArray().length == 2, "toByteArray(-254) 长 2");
        check(BigInteger.valueOf(-128).toByteArray().length == 1, "toByteArray(-128) 长 1");
        // 数论补充
        check(BigInteger.valueOf(11).modInverse(BigInteger.valueOf(29)).intValue() == 8,
              "inv(11) mod 29 == 8");
        check(BigInteger.valueOf(11).multiply(BigInteger.valueOf(8)).mod(BigInteger.valueOf(29))
                .intValue() == 1, "11*8 mod 29 == 1（逆元自验证）");
        check(BigInteger.valueOf(7).modPow(BigInteger.valueOf(13), BigInteger.valueOf(17))
                .intValue() == 6, "7^13 mod 17 == 6");
        check(BigInteger.valueOf(1071).gcd(BigInteger.valueOf(462)).intValue() == 21,
              "gcd(1071,462) == 21（欧几里得）");
        // 常量与符号补充
        check(BigInteger.ONE.equals(BigInteger.valueOf(1)), "ONE equals 1");
        check(BigInteger.TEN.pow(2).intValue() == 100, "TEN^2 == 100");
        check(BigInteger.ZERO.abs().signum() == 0, "abs(ZERO) == 0");
        check(BigInteger.valueOf(Long.MIN_VALUE).abs()
                .equals(BigInteger.ONE.shiftLeft(63)), "|Long.MIN| == 2^63");
        check(BigInteger.valueOf(-7).multiply(BigInteger.valueOf(6)).intValue() == -42, "-7*6 == -42");
        check(BigInteger.valueOf(17).add(BigInteger.valueOf(17)).intValue() == 34, "17+17 == 34");
        check(BigInteger.probablePrime(8, new java.util.Random(1234L)).isProbablePrime(100),
              "probablePrime 固定种子可复现且为素");
        check(BigInteger.ZERO.toString(36).equals("0"), "0 基 36 == \"0\"");
        check(BigInteger.valueOf(5).testBit(2) && !BigInteger.valueOf(5).testBit(1),
              "testBit(5)：位 2 置位、位 1 清位");
        // Long 边界序列化
        BigInteger r = roundTrip(BigInteger.valueOf(Long.MIN_VALUE));
        check(r.equals(BigInteger.valueOf(Long.MIN_VALUE)), "Long.MIN 序列化 round-trip");
        check(r.longValue() == Long.MIN_VALUE, "Long.MIN 序列化值保留");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testNullSemantics();
        testArithmetic();
        testGcdAndSign();
        testModular();
        testBitOperations();
        testPrimality();
        testComparison();
        testConversions();
        testConversionsExactErrors();
        testSerialization();
        testErrors();
        testConstructorExactness();
        testBitContracts();
        testModularContracts();
        testBigArithmeticAndMessages();
        testPrimalityMatrix();
        testComparisonAndSerializationExtra();
        testCacheAndTwoComplementSemantics();
        testRadixAndByteArrayMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.math.BigInteger conformance: white-box structure + black-box behavior
 *          (construction, arithmetic, bitwise ops, mod/pow/modPow/gcd, radix conversion,
 *           compareTo/equals/hashCode, boundaries 0/negative/large, ArithmeticException).
 * @run main BigIntegerTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class BigIntegerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " but got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.math.BigInteger");

        check(!c.isInterface() && !c.isEnum(), "BigInteger is a concrete class");
        check(Modifier.isPublic(c.getModifiers()), "BigInteger is public");
        check(!Modifier.isAbstract(c.getModifiers()), "BigInteger is not abstract");

        check(c.getSuperclass() == Number.class, "BigInteger extends Number");
        check(Arrays.asList(c.getInterfaces()).contains(Comparable.class),
              "BigInteger implements Comparable");

        for (String f : new String[]{"ZERO", "ONE", "TWO", "TEN"}) {
            Field field = c.getField(f);
            check(Modifier.isPublic(field.getModifiers())
                  && Modifier.isStatic(field.getModifiers())
                  && Modifier.isFinal(field.getModifiers())
                  && field.getType() == BigInteger.class,
                  "static final BigInteger " + f);
        }
        check(((BigInteger) c.getField("ZERO").get(null)).signum() == 0, "ZERO has value 0");
        check(((BigInteger) c.getField("ONE").get(null)).signum() == 1, "ONE has value 1");
        check(((BigInteger) c.getField("TEN").get(null)).intValue() == 10, "TEN has value 10");

        String[][] methods = {
            {"valueOf", "long"},
            {"add", "java.math.BigInteger"},
            {"subtract", "java.math.BigInteger"},
            {"multiply", "java.math.BigInteger"},
            {"divide", "java.math.BigInteger"},
            {"divideAndRemainder", "java.math.BigInteger"},
            {"remainder", "java.math.BigInteger"},
            {"mod", "java.math.BigInteger"},
            {"modPow", "java.math.BigInteger", "java.math.BigInteger"},
            {"modInverse", "java.math.BigInteger"},
            {"gcd", "java.math.BigInteger"},
            {"pow", "int"},
            {"sqrt"},
            {"shiftLeft", "int"},
            {"shiftRight", "int"},
            {"and", "java.math.BigInteger"},
            {"or", "java.math.BigInteger"},
            {"xor", "java.math.BigInteger"},
            {"not"},
            {"andNot", "java.math.BigInteger"},
            {"testBit", "int"},
            {"setBit", "int"},
            {"clearBit", "int"},
            {"flipBit", "int"},
            {"bitLength"},
            {"bitCount"},
            {"getLowestSetBit"},
            {"isProbablePrime", "int"},
            {"compareTo", "java.math.BigInteger"},
            {"equals", "java.lang.Object"},
            {"hashCode"},
            {"toString", "int"},
            {"toString"},
            {"toByteArray"},
            {"longValueExact"},
            {"intValueExact"},
        };
        for (String[] m : methods) {
            Class<?>[] params = new Class<?>[m.length - 1];
            for (int i = 1; i < m.length; i++) params[i - 1] = forName(m[i]);
            Method method = c.getMethod(m[0], params);
            check(Modifier.isPublic(method.getModifiers()), "public method " + m[0]);
        }
    }

    static Class<?> forName(String s) throws Exception {
        switch (s) {
            case "long": return long.class;
            case "int": return int.class;
            default: return Class.forName(s);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        construction();
        arithmetic();
        bitwise();
        numberTheory();
        comparisonAndHash();
        conversions();
        boundariesAndExceptions();
        boundariesExtra();
        testSerialization();
        testNullSemantics();
    }

    static void construction() {
        // String (decimal) constructor
        check(new BigInteger("100").toString().equals("100"), "String ctor decimal");
        check(new BigInteger("-1").signum() == -1, "String ctor negative");
        check(new BigInteger("+0").signum() == 0, "String ctor explicit +0");
        // String with radix
        check(new BigInteger("1010", 2).intValue() == 10, "String ctor radix 2");
        check(new BigInteger("ff", 16).intValue() == 255, "String ctor radix 16");
        check(new BigInteger("-ff", 16).intValue() == -255, "String ctor radix 16 negative");
        check(new BigInteger("zz", 36).intValue() == 1295, "String ctor radix 36");
        check(new BigInteger("Z", 36).intValue() == 35, "String ctor radix 36 uppercase");
        // valueOf(long) and constants
        check(BigInteger.valueOf(0) == BigInteger.ZERO, "valueOf(0) caches ZERO");
        check(BigInteger.valueOf(1) == BigInteger.ONE, "valueOf(1) caches ONE");
        check(BigInteger.valueOf(10) == BigInteger.TEN, "valueOf(10) caches TEN");
        check(BigInteger.valueOf(-7).longValue() == -7L, "valueOf(long) negative");
        check(BigInteger.TWO.intValue() == 2, "TWO constant");
        // byte[] constructor (two's complement, big-endian)
        check(new BigInteger(new byte[]{1, 2, 3}).intValue() == 66051, "byte[] ctor 0x010203");
        check(new BigInteger(new byte[]{(byte) 0xff}).intValue() == -1, "byte[] ctor 0xff == -1");
        check(new BigInteger(new byte[]{0}).signum() == 0, "byte[] ctor zero");
        // int signum + magnitude constructor
        check(new BigInteger(-1, new byte[]{0, 1, 2}).intValue() == -258, "(signum, byte[]) negative");
        check(new BigInteger(1, new byte[]{1, 2}).intValue() == 258, "(signum, byte[]) positive");
        // probablePrime / nextProbablePrime with fixed seed
        BigInteger pp = BigInteger.probablePrime(8, new Random(1234L));
        check(pp.bitLength() == 8 && pp.isProbablePrime(100), "probablePrime bit length + primality");
        check(BigInteger.TEN.nextProbablePrime().intValue() == 11, "nextProbablePrime after 10");
        check(BigInteger.valueOf(13).nextProbablePrime().intValue() == 17, "nextProbablePrime after 13");
    }

    static void arithmetic() {
        BigInteger a = BigInteger.valueOf(10);
        BigInteger b = BigInteger.valueOf(3);
        check(a.add(b).equals(BigInteger.valueOf(13)), "add");
        check(a.subtract(b).equals(BigInteger.valueOf(7)), "subtract");
        check(a.multiply(b).equals(BigInteger.valueOf(30)), "multiply");
        check(a.divide(b).equals(BigInteger.valueOf(3)), "divide (truncates toward zero)");
        check(BigInteger.valueOf(-10).divide(BigInteger.valueOf(3)).equals(BigInteger.valueOf(-3)),
              "divide truncates toward zero for negative");
        check(a.remainder(b).equals(BigInteger.ONE), "remainder sign follows dividend");
        check(BigInteger.valueOf(-10).remainder(BigInteger.valueOf(3)).equals(BigInteger.valueOf(-1)),
              "remainder negative dividend");
        BigInteger[] dr = a.divideAndRemainder(b);
        check(dr.length == 2 && dr[0].equals(BigInteger.valueOf(3)) && dr[1].equals(BigInteger.ONE),
              "divideAndRemainder 10/3");
        BigInteger[] drn = BigInteger.valueOf(-10).divideAndRemainder(BigInteger.valueOf(3));
        check(drn[0].equals(BigInteger.valueOf(-3)) && drn[1].equals(BigInteger.valueOf(-1)),
              "divideAndRemainder -10/3");

        // mod (result sign follows modulus; modulus must be positive)
        check(BigInteger.valueOf(-10).mod(BigInteger.valueOf(3)).equals(BigInteger.valueOf(2)),
              "mod -10 mod 3 == 2");

        // pow / sqrt / rootn
        check(a.pow(3).equals(BigInteger.valueOf(1000)), "pow 10^3");
        check(BigInteger.valueOf(2).pow(16).intValue() == 65536, "pow 2^16");
        check(BigInteger.valueOf(15).sqrt().equals(BigInteger.valueOf(3)), "sqrt floor(15)");
        check(BigInteger.valueOf(16).sqrt().equals(BigInteger.valueOf(4)), "sqrt exact 16");
        BigInteger[] sr = BigInteger.valueOf(15).sqrtAndRemainder();
        check(sr[0].equals(BigInteger.valueOf(3)) && sr[1].equals(BigInteger.valueOf(6)),
              "sqrtAndRemainder 15 == [3, 6]");
        check(BigInteger.valueOf(27).rootn(3).equals(BigInteger.valueOf(3)), "rootn 27,3 == 3");
        check(BigInteger.valueOf(-8).rootn(3).equals(BigInteger.valueOf(-2)), "rootn -8,3 == -2");
        BigInteger[] rr = BigInteger.valueOf(27).rootnAndRemainder(3);
        check(rr[0].equals(BigInteger.valueOf(3)) && rr[1].equals(BigInteger.ZERO),
              "rootnAndRemainder 27,3 == [3, 0]");

        // gcd / abs / negate / signum
        check(BigInteger.valueOf(12).gcd(BigInteger.valueOf(18)).equals(BigInteger.valueOf(6)), "gcd(12,18)");
        check(BigInteger.valueOf(-12).gcd(BigInteger.valueOf(18)).equals(BigInteger.valueOf(6)), "gcd is non-negative");
        check(BigInteger.valueOf(7).gcd(BigInteger.ZERO).equals(BigInteger.valueOf(7)), "gcd(a,0)==a");
        check(a.negate().equals(BigInteger.valueOf(-10)), "negate");
        check(a.abs().equals(a) && a.negate().abs().equals(a), "abs");
        check(a.signum() == 1 && BigInteger.ZERO.signum() == 0
              && BigInteger.valueOf(-1).signum() == -1, "signum");
    }

    static void bitwise() {
        // shifts (shiftRight is arithmetic, floor division by 2^n)
        check(BigInteger.valueOf(5).shiftLeft(2).equals(BigInteger.valueOf(20)), "shiftLeft 5<<2");
        check(BigInteger.valueOf(20).shiftRight(2).equals(BigInteger.valueOf(5)), "shiftRight 20>>2");
        check(BigInteger.valueOf(-7).shiftRight(1).equals(BigInteger.valueOf(-4)),
              "shiftRight arithmetic for negative (-7>>1 == -4)");
        check(BigInteger.valueOf(-7).shiftLeft(1).equals(BigInteger.valueOf(-14)), "shiftLeft negative");

        // logical ops
        check(BigInteger.valueOf(5).and(BigInteger.valueOf(3)).equals(BigInteger.ONE), "and 5&3");
        check(BigInteger.valueOf(5).or(BigInteger.valueOf(3)).equals(BigInteger.valueOf(7)), "or 5|3");
        check(BigInteger.valueOf(5).xor(BigInteger.valueOf(3)).equals(BigInteger.valueOf(6)), "xor 5^3");
        check(BigInteger.valueOf(-1).not().equals(BigInteger.ZERO), "not ~(-1)==0");
        check(BigInteger.valueOf(5).andNot(BigInteger.valueOf(3)).equals(BigInteger.valueOf(4)), "andNot 5&~3");

        // bit accessors
        check(BigInteger.valueOf(5).testBit(0) && BigInteger.valueOf(5).testBit(2)
              && !BigInteger.valueOf(5).testBit(1), "testBit");
        check(BigInteger.ZERO.setBit(5).equals(BigInteger.valueOf(32)), "setBit 0,5");
        check(BigInteger.valueOf(5).clearBit(2).equals(BigInteger.ONE), "clearBit 5,2");
        check(BigInteger.valueOf(5).flipBit(1).equals(BigInteger.valueOf(7)), "flipBit 5,1");

        // lengths / counts
        check(BigInteger.valueOf(10).bitLength() == 4, "bitLength 10 == 4");
        check(BigInteger.ZERO.bitLength() == 0, "bitLength 0 == 0");
        check(BigInteger.valueOf(255).bitLength() == 8, "bitLength 255 == 8");
        check(BigInteger.valueOf(10).bitCount() == 2, "bitCount 10 == 2");
        check(BigInteger.valueOf(-10).bitCount() == 2, "bitCount -10 == 2 (differs from sign)");
        check(BigInteger.valueOf(12).getLowestSetBit() == 2, "getLowestSetBit 12 == 2");
        check(BigInteger.ZERO.getLowestSetBit() == -1, "getLowestSetBit 0 == -1");
    }

    static void numberTheory() {
        // modPow: exponent may be negative (inverse); modulus must be positive
        check(BigInteger.TWO.modPow(BigInteger.valueOf(3), BigInteger.valueOf(5))
              .equals(BigInteger.valueOf(3)), "modPow 2^3 mod 5 == 3");
        check(BigInteger.TWO.modPow(BigInteger.valueOf(-1), BigInteger.valueOf(5))
              .equals(BigInteger.valueOf(3)), "modPow 2^-1 mod 5 == 3");
        BigInteger mod = new BigInteger("1000000007");
        BigInteger base = new BigInteger("123456789");
        BigInteger exp = new BigInteger("987654321");
        check(base.modPow(exp, mod).equals(base.modPow(exp, mod)),
              "modPow deterministic for fixed inputs");

        // modInverse: requires gcd(base, mod) == 1
        check(BigInteger.valueOf(3).modInverse(BigInteger.valueOf(11)).equals(BigInteger.valueOf(4)),
              "modInverse 3 mod 11 == 4");

        // isProbablePrime
        check(BigInteger.valueOf(5).isProbablePrime(100), "5 is prime");
        check(!BigInteger.valueOf(9).isProbablePrime(100), "9 is composite");
        check(new BigInteger("104729").isProbablePrime(100), "104729 is prime");
        check(!new BigInteger("104730").isProbablePrime(100), "104730 is composite");
    }

    static void comparisonAndHash() {
        BigInteger ten = BigInteger.valueOf(10);
        BigInteger same = new BigInteger("10");
        check(ten.compareTo(same) == 0, "compareTo equal");
        check(ten.equals(same), "equals equal value");
        check(ten.hashCode() == same.hashCode(), "equal values have equal hashCode");
        check(ten.compareTo(BigInteger.valueOf(11)) < 0, "compareTo smaller");
        check(ten.compareTo(BigInteger.valueOf(9)) > 0, "compareTo larger");
        check(ten.max(BigInteger.valueOf(3)).equals(ten), "max");
        check(ten.min(BigInteger.valueOf(3)).equals(BigInteger.valueOf(3)), "min");
        check(!ten.equals("10"), "equals against String is false");
        check(!ten.equals(null), "equals null is false");
    }

    static void conversions() {
        // toString(radix)
        check(new BigInteger("-10").toString(2).equals("-1010"), "toString radix 2 negative");
        check(new BigInteger("-10").toString(16).equals("-a"), "toString radix 16 lowercase");
        check(new BigInteger("35").toString(36).equals("z"), "toString radix 36");
        check(new BigInteger("-35").toString(36).equals("-z"), "toString radix 36 negative");
        check(BigInteger.ZERO.toString(2).equals("0"), "toString radix 2 zero");
        check(BigInteger.valueOf(255).toString(16).equals("ff"), "toString 255 hex");
        // toString() decimal
        check(new BigInteger("-123456789012345678901234567890").toString()
              .equals("-123456789012345678901234567890"), "toString decimal round-trip");

        // toByteArray round-trip (two's complement)
        BigInteger big = new BigInteger("123456789012345678901234567890");
        check(new BigInteger(big.toByteArray()).equals(big), "toByteArray round-trip positive");
        check(new BigInteger(big.negate().toByteArray()).equals(big.negate()), "toByteArray round-trip negative");
        check(Arrays.equals(BigInteger.ZERO.toByteArray(), new byte[]{0}), "toByteArray zero == {0}");
        check(Arrays.equals(BigInteger.valueOf(-1).toByteArray(), new byte[]{-1}), "toByteArray -1 == {ff}");

        // primitive conversions
        check(BigInteger.valueOf(10).intValue() == 10, "intValue");
        check(BigInteger.valueOf(-7).longValue() == -7L, "longValue");
        check(BigInteger.valueOf(10).doubleValue() == 10.0, "doubleValue");
        check(BigInteger.valueOf(10).floatValue() == 10.0f, "floatValue");
        check(BigInteger.valueOf(10).intValueExact() == 10, "intValueExact in range");
        check(BigInteger.valueOf(10).longValueExact() == 10L, "longValueExact in range");
        check(BigInteger.valueOf(7).shortValueExact() == 7, "shortValueExact in range");
        check(BigInteger.valueOf(7).byteValueExact() == 7, "byteValueExact in range");
    }

    static void boundariesAndExceptions() {
        // divide by zero
        expectArithmetic(() -> BigInteger.ONE.divide(BigInteger.ZERO), "divide by zero");
        // mod with non-positive modulus
        expectArithmetic(() -> BigInteger.ONE.mod(BigInteger.ZERO), "mod zero modulus");
        expectArithmetic(() -> BigInteger.ONE.mod(BigInteger.valueOf(-3)), "mod negative modulus");
        expectArithmetic(() -> BigInteger.ONE.modPow(BigInteger.ONE, BigInteger.ZERO), "modPow zero modulus");
        // modInverse when gcd != 1
        expectArithmetic(() -> BigInteger.valueOf(6).modInverse(BigInteger.valueOf(9)), "modInverse non-coprime");
        // sqrt / rootn of negative
        expectArithmetic(() -> BigInteger.valueOf(-4).sqrt(), "sqrt negative");
        // exact conversions overflow
        expectArithmetic(() -> new BigInteger("2147483648").intValueExact(), "intValueExact overflow");
        expectArithmetic(() -> new BigInteger("9223372036854775808").longValueExact(), "longValueExact overflow");
        expectArithmetic(() -> new BigInteger("32768").shortValueExact(), "shortValueExact overflow");
        expectArithmetic(() -> new BigInteger("128").byteValueExact(), "byteValueExact overflow");
        // invalid constructor inputs
        expectNumberFormat(() -> new BigInteger("", 10), "empty String ctor");
        expectNumberFormat(() -> new BigInteger("12a", 10), "invalid digit");
        expectNumberFormat(() -> new BigInteger("10", 1), "radix below 2");
        expectNumberFormat(() -> new BigInteger("10", 37), "radix above 36");
        expectNumberFormat(() -> new BigInteger(0, new byte[]{1, 2}), "signum-magnitude mismatch");
        // rootn with non-positive n
        expectArithmetic(() -> BigInteger.valueOf(8).rootn(0), "rootn n == 0");
        // huge exact arithmetic stays correct
        BigInteger huge = new BigInteger("12345678901234567890123456789012345678901234567890");
        check(huge.multiply(huge).divide(huge).equals(huge), "large multiply/divide round-trip");
    }

    static void expectArithmetic(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected ArithmeticException: " + msg);
        } catch (ArithmeticException expected) {
            passed++;
        }
    }

    static void expectNumberFormat(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected NumberFormatException: " + msg);
        } catch (NumberFormatException expected) {
            passed++;
        }
    }

    static void boundariesExtra() {
        check(BigInteger.valueOf(Long.MAX_VALUE).longValue() == Long.MAX_VALUE, "valueOf(Long.MAX)");
        check(BigInteger.valueOf(Long.MIN_VALUE).longValue() == Long.MIN_VALUE, "valueOf(Long.MIN)");
        expectNumberFormat(() -> new BigInteger(new byte[]{}), "empty byte[] ctor");
        check(new BigInteger(0, new byte[]{}).signum() == 0, "(0, empty magnitude) == 0");
        check(new BigInteger(-1, new byte[]{0, 0}).signum() == 0, "(-1, zero magnitude) normalized to 0");
        expectArithmetic(() -> BigInteger.probablePrime(1, new Random(1L)), "probablePrime bitLength < 2");
        expectArithmetic(() -> BigInteger.ONE.divideAndRemainder(BigInteger.ZERO), "divideAndRemainder by zero");
        expectArithmetic(() -> BigInteger.ONE.remainder(BigInteger.ZERO), "remainder by zero");
        expectArithmetic(() -> BigInteger.valueOf(5).pow(-1), "pow negative exponent");
        expectArithmetic(() -> BigInteger.ONE.testBit(-1), "testBit negative index");
        expectArithmetic(() -> BigInteger.ONE.setBit(-1), "setBit negative index");
        expectArithmetic(() -> BigInteger.ONE.clearBit(-1), "clearBit negative index");
        expectArithmetic(() -> BigInteger.ONE.flipBit(-1), "flipBit negative index");
        expectArithmetic(() -> BigInteger.valueOf(3).modInverse(BigInteger.ZERO), "modInverse zero modulus");
        expectArithmetic(() -> BigInteger.valueOf(3).modInverse(BigInteger.valueOf(-11)), "modInverse negative modulus");
        check(BigInteger.ZERO.gcd(BigInteger.ZERO).equals(BigInteger.ZERO), "gcd(0,0) == 0");
        check(BigInteger.valueOf(5).shiftLeft(-1).equals(BigInteger.valueOf(5).shiftRight(1)),
              "shiftLeft(-1) == shiftRight(1)");
        check(BigInteger.valueOf(5).shiftRight(-1).equals(BigInteger.valueOf(5).shiftLeft(1)),
              "shiftRight(-1) == shiftLeft(1)");
        check(BigInteger.valueOf(255).toString(37).equals("255"), "toString invalid radix defaults to 10");
        check(new BigInteger("2147483647").intValueExact() == Integer.MAX_VALUE, "intValueExact MAX");
        check(new BigInteger("-2147483648").intValueExact() == Integer.MIN_VALUE, "intValueExact MIN");
        check(new BigInteger("9223372036854775807").longValueExact() == Long.MAX_VALUE, "longValueExact MAX");
        check(new BigInteger("-9223372036854775808").longValueExact() == Long.MIN_VALUE, "longValueExact MIN");
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

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> new BigInteger((String) null), "String null");
        expectThrows(NullPointerException.class, () -> new BigInteger((byte[]) null), "byte[] null");
        expectThrows(NullPointerException.class, () -> new BigInteger(1, (byte[]) null), "signum byte[] null");
        BigInteger one = BigInteger.ONE;
        expectThrows(NullPointerException.class, () -> one.add(null), "add null");
        expectThrows(NullPointerException.class, () -> one.subtract(null), "subtract null");
        expectThrows(NullPointerException.class, () -> one.multiply(null), "multiply null");
        expectThrows(NullPointerException.class, () -> one.divide(null), "divide null");
        expectThrows(NullPointerException.class, () -> one.remainder(null), "remainder null");
        expectThrows(NullPointerException.class, () -> one.gcd(null), "gcd null");
        expectThrows(NullPointerException.class, () -> one.mod(null), "mod null");
        expectThrows(NullPointerException.class, () -> one.modPow(BigInteger.TEN, null), "modPow null modulus");
        expectThrows(NullPointerException.class, () -> one.modInverse(null), "modInverse null");
        expectThrows(NullPointerException.class, () -> one.and(null), "and null");
        expectThrows(NullPointerException.class, () -> one.or(null), "or null");
        expectThrows(NullPointerException.class, () -> one.xor(null), "xor null");
        expectThrows(NullPointerException.class, () -> one.andNot(null), "andNot null");
        expectThrows(NullPointerException.class, () -> one.divideAndRemainder(null), "divideAndRemainder null");
        expectThrows(NullPointerException.class, () -> one.compareTo(null), "compareTo null");
        expectThrows(NullPointerException.class, () -> one.min(null), "min null");
        expectThrows(NullPointerException.class, () -> one.max(null), "max null");
    }

    // ===== 契约深化（2026-11）：精确异常消息助手 =====

    static void checkMsg(Class<? extends Throwable> expected, String message, Runnable r, String msg) {
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
        // valueOf 缓存与常量
        check(BigInteger.valueOf(0) == BigInteger.ZERO, "valueOf(0) == ZERO 同一实例");
        check(BigInteger.valueOf(1) == BigInteger.ONE, "valueOf(1) == ONE 同一实例");
        check(BigInteger.valueOf(10) == BigInteger.TEN, "valueOf(10) == TEN 同一实例");
        check(BigInteger.TWO.intValue() == 2, "TWO 常量（JDK-9+ 公开）");
        check(BigInteger.TWO.signum() == 1, "TWO signum");
        check(BigInteger.TWO.equals(BigInteger.valueOf(2)), "TWO equals 2");
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
        check(BigInteger.TWO.modPow(BigInteger.valueOf(-1), BigInteger.valueOf(5)).intValue() == 3,
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

    // ===== 契约深化（2026-11）：sqrt 家族（JDK-9+）与素性矩阵 =====

    static void testSqrtFamilyAndPrimality() {
        // sqrt：非负整数平方根 floor
        check(BigInteger.valueOf(9).sqrt().intValue() == 3, "sqrt(9) == 3");
        check(BigInteger.valueOf(10).sqrt().intValue() == 3, "sqrt(10) == 3（floor）");
        check(BigInteger.valueOf(15).sqrt().intValue() == 3, "sqrt(15) == 3");
        check(BigInteger.valueOf(16).sqrt().intValue() == 4, "sqrt(16) == 4（完全平方精确）");
        check(BigInteger.valueOf(17).sqrt().intValue() == 4, "sqrt(17) == 4");
        check(BigInteger.ZERO.sqrt().signum() == 0, "sqrt(0) == 0");
        check(BigInteger.ONE.shiftLeft(64).sqrt().equals(BigInteger.ONE.shiftLeft(32)),
              "sqrt(2^64) == 2^32");
        BigInteger[] sr = BigInteger.valueOf(15).sqrtAndRemainder();
        check(sr[0].intValue() == 3 && sr[1].intValue() == 6, "sqrtAndRemainder(15) == [3,6]");
        checkMsg(ArithmeticException.class, "Negative BigInteger",
              () -> BigInteger.valueOf(-4).sqrt(), "sqrt(-4) 消息逐字");
        check(BigInteger.valueOf(7).isProbablePrime(20), "7 素");
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
        BigInteger one = BigInteger.ONE, two = BigInteger.TWO, three = BigInteger.valueOf(3);
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
        check(!BigInteger.TWO.equals("2"), "equals 跨类型 false");
        check(!BigInteger.TWO.equals(null), "equals null false");
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
        BigInteger r = roundTrip(BigInteger.valueOf(Long.MIN_VALUE));
        check(r.equals(BigInteger.valueOf(Long.MIN_VALUE)), "Long.MIN 序列化 round-trip");
        check(r.longValue() == Long.MIN_VALUE, "Long.MIN 序列化值保留");
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

    static void testRadixAndByteArrayMatrix() {
        check(new BigInteger("1010", 2).intValue() == 10, "\"1010\" 2 == 10");
        check(new BigInteger("777", 8).intValue() == 511, "\"777\" 8 == 511");
        check(BigInteger.valueOf(255).toString(5).equals("2010"), "255 基 5 == \"2010\"");
        check(BigInteger.valueOf(255).toString(36).equals("73"), "255 基 36 == \"73\"");
        check(new BigInteger(BigInteger.valueOf(255).toString(36), 36).intValue() == 255,
              "基 36 往返");
        check(BigInteger.ZERO.toString(36).equals("0"), "0 基 36 == \"0\"");
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
        check(BigInteger.valueOf(5).testBit(2) && !BigInteger.valueOf(5).testBit(1),
              "testBit(5)：位 2 置位、位 1 清位");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorExactness();
        testBitContracts();
        testModularContracts();
        testBigArithmeticAndMessages();
        testSqrtFamilyAndPrimality();
        testComparisonAndSerializationExtra();
        testCacheAndTwoComplementSemantics();
        testRadixAndByteArrayMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

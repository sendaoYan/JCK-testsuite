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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.math.BigInteger conformance: white-box structure + black-box behavior
 *          (construction, arithmetic, bitwise ops, mod/pow/modPow/gcd, radix conversion,
 *           compareTo/equals/hashCode, boundaries 0/negative/large, ArithmeticException).
 * @run main BigIntegerTest
 */

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
    static void testBlackBox() {
        construction();
        arithmetic();
        bitwise();
        numberTheory();
        comparisonAndHash();
        conversions();
        boundariesAndExceptions();
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

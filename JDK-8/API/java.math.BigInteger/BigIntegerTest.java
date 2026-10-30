/*
 * @test
 * @summary java.math.BigInteger API conformance: add/subtract/multiply/divide/mod/gcd,
 *          signum, bitLength, intValue, toString, isProbablePrime, max/min/negate/pow.
 * @run main BigIntegerTest
 */

import java.math.BigInteger;

public class BigIntegerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBigInteger() {
        BigInteger a = BigInteger.valueOf(10);
        BigInteger b = BigInteger.valueOf(3);
        check(a.add(b).equals(BigInteger.valueOf(13)), "add");
        check(a.subtract(b).equals(BigInteger.valueOf(7)), "subtract");
        check(a.multiply(b).equals(BigInteger.valueOf(30)), "multiply");
        check(a.divide(b).equals(BigInteger.valueOf(3)), "divide");
        check(a.mod(b).equals(BigInteger.ONE), "mod");
        check(a.gcd(b).equals(BigInteger.ONE), "gcd");
        check(a.signum() == 1 && BigInteger.ZERO.signum() == 0
              && BigInteger.valueOf(-1).signum() == -1, "signum");
        check(a.bitLength() == 4, "bitLength of 10 (1010) is 4");
        check(BigInteger.valueOf(10).intValue() == 10, "intValue");
        check(new BigInteger("100").toString().equals("100"), "String ctor/toString");
        check(BigInteger.valueOf(5).isProbablePrime(100), "isProbablePrime 5");
        check(!BigInteger.valueOf(9).isProbablePrime(100), "isProbablePrime 9");
        check(a.max(b).equals(a) && a.min(b).equals(b), "max/min");
        check(a.negate().equals(BigInteger.valueOf(-10)), "negate");
        check(a.pow(3).equals(BigInteger.valueOf(1000)), "pow");
    }

    public static void main(String[] args) {
        testBigInteger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

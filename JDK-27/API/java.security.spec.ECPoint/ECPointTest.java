/*
 * @test
 * @summary java.security.spec.ECPoint conformance: constructors, accessors, POINT_INFINITY.
 * @run main ECPointTest
 */

import java.security.spec.ECPoint;
import java.math.BigInteger;

public class ECPointTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        ECPoint p = new ECPoint(BigInteger.ONE, BigInteger.TWO);
        check(BigInteger.ONE.equals(p.getAffineX()), "getAffineX");
        check(BigInteger.TWO.equals(p.getAffineY()), "getAffineY");
        check(ECPoint.POINT_INFINITY != null, "POINT_INFINITY non-null");
        check(ECPoint.POINT_INFINITY.equals(new ECPoint(BigInteger.ZERO, BigInteger.ZERO)) == false, "POINT_INFINITY distinct");
        check(p.equals(new ECPoint(BigInteger.ONE, BigInteger.TWO)), "equals");
        check(p.hashCode() == new ECPoint(BigInteger.ONE, BigInteger.TWO).hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

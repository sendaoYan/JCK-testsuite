/*
 * @test
 * @summary java.security.spec.EdECPoint conformance: constructor and accessors.
 * @run main EdECPointTest
 */

import java.security.spec.EdECPoint;
import java.math.BigInteger;

public class EdECPointTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        EdECPoint p = new EdECPoint(true, BigInteger.TEN);
        check(p.isXOdd(), "isXOdd");
        check(BigInteger.TEN.equals(p.getY()), "getY");
        EdECPoint p2 = new EdECPoint(false, BigInteger.ONE);
        check(!p2.isXOdd(), "isXOdd false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.security.spec.ECPoint conformance: constructor and getters.
 * @run main ECPointTest
 */

public class ECPointTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.ECPoint p = new java.security.spec.ECPoint(new java.math.BigInteger("3"), new java.math.BigInteger("10"));
        check(new java.math.BigInteger("3").equals(p.getAffineX()), "getAffineX");
        check(new java.math.BigInteger("10").equals(p.getAffineY()), "getAffineY");
        check(java.security.spec.ECPoint.POINT_INFINITY.equals(java.security.spec.ECPoint.POINT_INFINITY), "POINT_INFINITY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

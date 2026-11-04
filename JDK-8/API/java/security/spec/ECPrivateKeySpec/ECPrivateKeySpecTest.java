/*
 * @test
 * @summary java.security.spec.ECPrivateKeySpec conformance: constructor and getters.
 * @run main ECPrivateKeySpecTest
 */

public class ECPrivateKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.EllipticCurve curve = new java.security.spec.EllipticCurve(
            new java.security.spec.ECFieldFp(new java.math.BigInteger("23")),
            new java.math.BigInteger("1"), new java.math.BigInteger("1"));
        java.security.spec.ECPoint g = new java.security.spec.ECPoint(new java.math.BigInteger("3"), new java.math.BigInteger("10"));
        java.security.spec.ECParameterSpec ps = new java.security.spec.ECParameterSpec(curve, g, new java.math.BigInteger("7"), 1);
        java.math.BigInteger s = new java.math.BigInteger("5");
        java.security.spec.ECPrivateKeySpec spec = new java.security.spec.ECPrivateKeySpec(s, ps);
        check(s.equals(spec.getS()), "getS");
        check(ps.equals(spec.getParams()), "getParams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.security.spec.ECPublicKeySpec conformance: constructor and getters.
 * @run main ECPublicKeySpecTest
 */

public class ECPublicKeySpecTest {
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
        java.security.spec.ECPoint w = new java.security.spec.ECPoint(new java.math.BigInteger("6"), new java.math.BigInteger("4"));
        java.security.spec.ECPublicKeySpec spec = new java.security.spec.ECPublicKeySpec(w, ps);
        check(w.equals(spec.getW()), "getW");
        check(ps.equals(spec.getParams()), "getParams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

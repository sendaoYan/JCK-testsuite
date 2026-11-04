/*
 * @test
 * @summary java.security.spec.ECParameterSpec conformance: constructor and getters.
 * @run main ECParameterSpecTest
 */

public class ECParameterSpecTest {
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
        java.security.spec.ECParameterSpec s = new java.security.spec.ECParameterSpec(curve, g, new java.math.BigInteger("7"), 1);
        check(curve.equals(s.getCurve()), "getCurve");
        check(g.equals(s.getGenerator()), "getGenerator");
        check(new java.math.BigInteger("7").equals(s.getOrder()), "getOrder");
        check(s.getCofactor() == 1, "getCofactor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.security.spec.EllipticCurve conformance: constructor and getters.
 * @run main EllipticCurveTest
 */

public class EllipticCurveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.ECFieldFp field = new java.security.spec.ECFieldFp(new java.math.BigInteger("23"));
        java.security.spec.EllipticCurve c = new java.security.spec.EllipticCurve(field, new java.math.BigInteger("1"), new java.math.BigInteger("1"));
        check(field.equals(c.getField()), "getField");
        check(new java.math.BigInteger("1").equals(c.getA()), "getA");
        check(new java.math.BigInteger("1").equals(c.getB()), "getB");
        check(c.getSeed() == null, "getSeed null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

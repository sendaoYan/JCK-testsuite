/*
 * @test
 * @summary java.security.spec.ECFieldFp conformance: constructor and getters.
 * @run main ECFieldFpTest
 */

public class ECFieldFpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger p = new java.math.BigInteger("23");
        java.security.spec.ECFieldFp f = new java.security.spec.ECFieldFp(p);
        check(p.equals(f.getP()), "getP");
        check(f.getFieldSize() == 5, "getFieldSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

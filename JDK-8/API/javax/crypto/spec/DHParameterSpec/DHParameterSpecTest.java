/*
 * @test
 * @summary javax.crypto.spec.DHParameterSpec conformance: constructor and getters.
 * @run main DHParameterSpecTest
 */

public class DHParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger p = new java.math.BigInteger("23");
        java.math.BigInteger g = new java.math.BigInteger("5");
        javax.crypto.spec.DHParameterSpec s = new javax.crypto.spec.DHParameterSpec(p, g, 160);
        check(p.equals(s.getP()) && g.equals(s.getG()), "getP/getG");
        check(s.getL() == 160, "getL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

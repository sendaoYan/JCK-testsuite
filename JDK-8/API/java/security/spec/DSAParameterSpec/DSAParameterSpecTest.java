/*
 * @test
 * @summary java.security.spec.DSAParameterSpec conformance: constructor and getters.
 * @run main DSAParameterSpecTest
 */

public class DSAParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger p = new java.math.BigInteger("11");
        java.math.BigInteger q = new java.math.BigInteger("5");
        java.math.BigInteger g = new java.math.BigInteger("2");
        java.security.spec.DSAParameterSpec s = new java.security.spec.DSAParameterSpec(p, q, g);
        check(p.equals(s.getP()) && q.equals(s.getQ()) && g.equals(s.getG()), "getP/getQ/getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.security.spec.DSAPublicKeySpec conformance: constructor and getters.
 * @run main DSAPublicKeySpecTest
 */

public class DSAPublicKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger y = new java.math.BigInteger("2");
        java.math.BigInteger p = new java.math.BigInteger("11");
        java.math.BigInteger q = new java.math.BigInteger("5");
        java.math.BigInteger g = new java.math.BigInteger("2");
        java.security.spec.DSAPublicKeySpec s = new java.security.spec.DSAPublicKeySpec(y, p, q, g);
        check(y.equals(s.getY()) && p.equals(s.getP()) && q.equals(s.getQ()) && g.equals(s.getG()), "getY/getP/getQ/getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.crypto.spec.DHPrivateKeySpec conformance: constructor and getters.
 * @run main DHPrivateKeySpecTest
 */

public class DHPrivateKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger x = new java.math.BigInteger("1");
        java.math.BigInteger p = new java.math.BigInteger("23");
        java.math.BigInteger g = new java.math.BigInteger("5");
        javax.crypto.spec.DHPrivateKeySpec s = new javax.crypto.spec.DHPrivateKeySpec(x, p, g);
        check(x.equals(s.getX()) && p.equals(s.getP()) && g.equals(s.getG()), "getX/getP/getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.crypto.spec.DHPublicKeySpec conformance: constructor and getters.
 * @run main DHPublicKeySpecTest
 */

public class DHPublicKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger y = new java.math.BigInteger("2");
        java.math.BigInteger p = new java.math.BigInteger("23");
        java.math.BigInteger g = new java.math.BigInteger("5");
        javax.crypto.spec.DHPublicKeySpec s = new javax.crypto.spec.DHPublicKeySpec(y, p, g);
        check(y.equals(s.getY()) && p.equals(s.getP()) && g.equals(s.getG()), "getY/getP/getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

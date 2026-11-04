/*
 * @test
 * @summary java.security.spec.DSAPrivateKeySpec conformance: constructor and getters.
 * @run main DSAPrivateKeySpecTest
 */

public class DSAPrivateKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger x = new java.math.BigInteger("1");
        java.math.BigInteger p = new java.math.BigInteger("11");
        java.math.BigInteger q = new java.math.BigInteger("5");
        java.math.BigInteger g = new java.math.BigInteger("2");
        java.security.spec.DSAPrivateKeySpec s = new java.security.spec.DSAPrivateKeySpec(x, p, q, g);
        check(x.equals(s.getX()) && p.equals(s.getP()) && q.equals(s.getQ()) && g.equals(s.getG()), "getX/getP/getQ/getG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

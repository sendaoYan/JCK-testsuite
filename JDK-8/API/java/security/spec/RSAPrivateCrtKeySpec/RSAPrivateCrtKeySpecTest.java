/*
 * @test
 * @summary java.security.spec.RSAPrivateCrtKeySpec conformance: constructor and getters.
 * @run main RSAPrivateCrtKeySpecTest
 */

public class RSAPrivateCrtKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger one = java.math.BigInteger.ONE;
        java.math.BigInteger two = java.math.BigInteger.valueOf(2);
        java.security.spec.RSAPrivateCrtKeySpec s = new java.security.spec.RSAPrivateCrtKeySpec(
            two, one, one, two, two, one, one, one);
        check(two.equals(s.getModulus()), "getModulus");
        check(one.equals(s.getPrivateExponent()), "getPrivateExponent");
        check(two.equals(s.getPrimeP()), "getPrimeP");
        check(two.equals(s.getPrimeQ()), "getPrimeQ");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

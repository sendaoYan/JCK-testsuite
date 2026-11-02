/*
 * @test
 * @summary java.security.spec.RSAMultiPrimePrivateCrtKeySpec conformance: constructor and getters.
 * @run main RSAMultiPrimePrivateCrtKeySpecTest
 */

public class RSAMultiPrimePrivateCrtKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger one = java.math.BigInteger.ONE;
        java.math.BigInteger two = java.math.BigInteger.valueOf(2);
        java.security.spec.RSAOtherPrimeInfo[] others = new java.security.spec.RSAOtherPrimeInfo[]{
            new java.security.spec.RSAOtherPrimeInfo(java.math.BigInteger.valueOf(3), java.math.BigInteger.ONE, java.math.BigInteger.valueOf(2))
        };
        java.security.spec.RSAMultiPrimePrivateCrtKeySpec s = new java.security.spec.RSAMultiPrimePrivateCrtKeySpec(
            two, one, one, two, two, one, one, one, others);
        check(two.equals(s.getModulus()), "getModulus");
        check(one.equals(s.getPublicExponent()), "getPublicExponent");
        check(one.equals(s.getPrivateExponent()), "getPrivateExponent");
        check(s.getOtherPrimeInfo().length == 1, "getOtherPrimeInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

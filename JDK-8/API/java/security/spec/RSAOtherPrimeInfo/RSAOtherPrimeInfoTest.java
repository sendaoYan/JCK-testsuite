/*
 * @test
 * @summary java.security.spec.RSAOtherPrimeInfo conformance: constructor and getters.
 * @run main RSAOtherPrimeInfoTest
 */

public class RSAOtherPrimeInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger prime = java.math.BigInteger.valueOf(7);
        java.security.spec.RSAOtherPrimeInfo info = new java.security.spec.RSAOtherPrimeInfo(
            prime, java.math.BigInteger.ONE, java.math.BigInteger.valueOf(3));
        check(prime.equals(info.getPrime()), "getPrime");
        check(java.math.BigInteger.ONE.equals(info.getExponent()), "getExponent");
        check(java.math.BigInteger.valueOf(3).equals(info.getCrtCoefficient()), "getCrtCoefficient");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

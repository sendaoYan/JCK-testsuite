/*
 * @test
 * @summary java.security.spec.RSAMultiPrimePrivateCrtKeySpec conformance: constructor and accessors.
 * @run main RSAMultiPrimePrivateCrtKeySpecTest
 */

import java.security.spec.RSAMultiPrimePrivateCrtKeySpec;
import java.security.spec.RSAOtherPrimeInfo;
import java.math.BigInteger;

public class RSAMultiPrimePrivateCrtKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger[] v = new BigInteger[8];
        for (int i = 0; i < 8; i++) v[i] = BigInteger.valueOf(i + 1);
        RSAOtherPrimeInfo[] other = new RSAOtherPrimeInfo[] { new RSAOtherPrimeInfo(new BigInteger("11"), new BigInteger("13"), new BigInteger("17")) };
        RSAMultiPrimePrivateCrtKeySpec spec = new RSAMultiPrimePrivateCrtKeySpec(
            v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], other);
        check(v[0].equals(spec.getModulus()), "getModulus");
        check(v[1].equals(spec.getPublicExponent()), "getPublicExponent");
        check(v[2].equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(v[3].equals(spec.getPrimeP()), "getPrimeP");
        check(v[4].equals(spec.getPrimeQ()), "getPrimeQ");
        check(v[5].equals(spec.getPrimeExponentP()), "getPrimeExponentP");
        check(v[6].equals(spec.getPrimeExponentQ()), "getPrimeExponentQ");
        check(v[7].equals(spec.getCrtCoefficient()), "getCrtCoefficient");
        check(spec.getOtherPrimeInfo().length == 1, "getOtherPrimeInfo length");
        check(new BigInteger("11").equals(spec.getOtherPrimeInfo()[0].getPrime()), "other prime");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

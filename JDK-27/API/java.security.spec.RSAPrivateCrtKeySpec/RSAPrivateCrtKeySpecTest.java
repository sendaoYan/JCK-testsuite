/*
 * @test
 * @summary java.security.spec.RSAPrivateCrtKeySpec conformance: constructor and accessors.
 * @run main RSAPrivateCrtKeySpecTest
 */

import java.security.spec.RSAPrivateCrtKeySpec;
import java.math.BigInteger;

public class RSAPrivateCrtKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        BigInteger[] v = new BigInteger[8];
        for (int i = 0; i < 8; i++) v[i] = BigInteger.valueOf(i + 1);
        RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
        check(v[0].equals(spec.getModulus()), "getModulus");
        check(v[1].equals(spec.getPublicExponent()), "getPublicExponent");
        check(v[2].equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(v[3].equals(spec.getPrimeP()), "getPrimeP");
        check(v[4].equals(spec.getPrimeQ()), "getPrimeQ");
        check(v[5].equals(spec.getPrimeExponentP()), "getPrimeExponentP");
        check(v[6].equals(spec.getPrimeExponentQ()), "getPrimeExponentQ");
        check(v[7].equals(spec.getCrtCoefficient()), "getCrtCoefficient");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

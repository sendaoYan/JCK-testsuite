/*
 * @test
 * @summary java.security.spec.RSAOtherPrimeInfo conformance: constructor and accessors.
 * @run main RSAOtherPrimeInfoTest
 */

import java.security.spec.RSAOtherPrimeInfo;
import java.math.BigInteger;

public class RSAOtherPrimeInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        RSAOtherPrimeInfo info = new RSAOtherPrimeInfo(new BigInteger("5"), new BigInteger("7"), new BigInteger("9"));
        check(new BigInteger("5").equals(info.getPrime()), "getPrime");
        check(new BigInteger("7").equals(info.getExponent()), "getExponent");
        check(new BigInteger("9").equals(info.getCrtCoefficient()), "getCrtCoefficient");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

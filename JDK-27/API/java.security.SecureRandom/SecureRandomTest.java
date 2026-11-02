/*
 * @test
 * @summary java.security.SecureRandom conformance: getInstanceStrong, DRBG, nextBytes, getAlgorithm/getProvider.
 * @run main SecureRandomTest
 */

import java.security.SecureRandom;

public class SecureRandomTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecureRandom strong = SecureRandom.getInstanceStrong();
        check(strong.getAlgorithm() != null, "getInstanceStrong getAlgorithm");
        check(strong.getProvider() != null, "getInstanceStrong getProvider");
        byte[] b = new byte[32];
        strong.nextBytes(b);
        passed++; // nextBytes succeeded
        check(strong.generateSeed(16).length == 16, "generateSeed length");

        SecureRandom drbg = SecureRandom.getInstance("DRBG");
        check("DRBG".equals(drbg.getAlgorithm()), "DRBG getAlgorithm");
        byte[] b2 = new byte[16];
        drbg.nextBytes(b2);
        passed++; // DRBG nextBytes succeeded
        drbg.reseed();
        passed++; // reseed succeeded

        SecureRandom def = new SecureRandom();
        byte[] b3 = new byte[8];
        def.nextBytes(b3);
        passed++; // default ctor nextBytes succeeded
        System.out.println("PASS (" + passed + " assertions)");
    }
}

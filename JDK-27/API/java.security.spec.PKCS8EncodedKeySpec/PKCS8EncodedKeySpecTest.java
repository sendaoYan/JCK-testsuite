/*
 * @test
 * @summary java.security.spec.PKCS8EncodedKeySpec conformance: constructor, getFormat, getEncoded, getAlgorithm.
 * @run main PKCS8EncodedKeySpecTest
 */

import java.security.spec.PKCS8EncodedKeySpec;

public class PKCS8EncodedKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] key = new byte[] { 1, 2, 3, 4 };
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        check("PKCS#8".equals(spec.getFormat()), "getFormat");
        check(java.util.Arrays.equals(key, spec.getEncoded()), "getEncoded");
        check(spec.getAlgorithm() == null, "getAlgorithm null without algorithm");
        PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(key, "RSA");
        check("RSA".equals(spec2.getAlgorithm()), "getAlgorithm RSA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

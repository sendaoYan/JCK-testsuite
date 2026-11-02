/*
 * @test
 * @summary java.security.spec.X509EncodedKeySpec conformance: constructor, getFormat, getEncoded, getAlgorithm.
 * @run main X509EncodedKeySpecTest
 */

import java.security.spec.X509EncodedKeySpec;

public class X509EncodedKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] key = new byte[] { 1, 2, 3, 4 };
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        check("X.509".equals(spec.getFormat()), "getFormat");
        check(java.util.Arrays.equals(key, spec.getEncoded()), "getEncoded");
        check(spec.getAlgorithm() == null, "getAlgorithm null without algorithm");
        X509EncodedKeySpec spec2 = new X509EncodedKeySpec(key, "RSA");
        check("RSA".equals(spec2.getAlgorithm()), "getAlgorithm RSA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

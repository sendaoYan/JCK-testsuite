/*
 * @test
 * @summary java.security.spec.PKCS8EncodedKeySpec conformance: constructor, getEncoded, getFormat.
 * @run main PKCS8EncodedKeySpecTest
 */

public class PKCS8EncodedKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] der = new byte[]{1,2,3};
        java.security.spec.PKCS8EncodedKeySpec s = new java.security.spec.PKCS8EncodedKeySpec(der);
        check(java.util.Arrays.equals(der, s.getEncoded()), "getEncoded");
        check("PKCS#8".equals(s.getFormat()), "getFormat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

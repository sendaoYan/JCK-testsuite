/*
 * @test
 * @summary java.security.spec.X509EncodedKeySpec conformance: constructor, getEncoded, getFormat.
 * @run main X509EncodedKeySpecTest
 */

public class X509EncodedKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] der = new byte[]{1,2,3};
        java.security.spec.X509EncodedKeySpec s = new java.security.spec.X509EncodedKeySpec(der);
        check(java.util.Arrays.equals(der, s.getEncoded()), "getEncoded");
        check("X.509".equals(s.getFormat()), "getFormat");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

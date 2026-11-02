/*
 * @test
 * @summary javax.crypto.spec.SecretKeySpec conformance: constructor and getters.
 * @run main SecretKeySpecTest
 */

public class SecretKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] key = new byte[16];
        javax.crypto.spec.SecretKeySpec s = new javax.crypto.spec.SecretKeySpec(key, "AES");
        check("AES".equals(s.getAlgorithm()), "getAlgorithm");
        check("RAW".equals(s.getFormat()), "getFormat");
        check(java.util.Arrays.equals(key, s.getEncoded()), "getEncoded");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.crypto.spec.DESedeKeySpec conformance: constructor and getters.
 * @run main DESedeKeySpecTest
 */

public class DESedeKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] key = new byte[24];
        javax.crypto.spec.DESedeKeySpec s = new javax.crypto.spec.DESedeKeySpec(key);
        check(java.util.Arrays.equals(key, s.getKey()), "getKey");
        check(javax.crypto.spec.DESedeKeySpec.DES_EDE_KEY_LEN == 24, "DES_EDE_KEY_LEN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

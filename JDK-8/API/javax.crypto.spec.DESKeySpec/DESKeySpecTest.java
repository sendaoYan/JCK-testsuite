/*
 * @test
 * @summary javax.crypto.spec.DESKeySpec conformance: constructor and getters.
 * @run main DESKeySpecTest
 */

public class DESKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] key = new byte[]{1,2,3,4,5,6,7,8};
        javax.crypto.spec.DESKeySpec s = new javax.crypto.spec.DESKeySpec(key);
        check(java.util.Arrays.equals(key, s.getKey()), "getKey");
        check(s.getKey().length == javax.crypto.spec.DESKeySpec.DES_KEY_LEN, "DES_KEY_LEN");
        check(!javax.crypto.spec.DESKeySpec.isParityAdjusted(key, 0), "isParityAdjusted false for mixed-parity key");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.crypto.spec.DESKeySpec conformance: DES key spec parity and weak checks.
 * @run main DESKeySpecTest
 */

import javax.crypto.spec.*;

public class DESKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(DESKeySpec.DES_KEY_LEN == 8, "DES_KEY_LEN 8");
        byte[] key = new byte[8];
        DESKeySpec s = new DESKeySpec(key);
        check(s.getKey().length == 8, "getKey length");
        byte[] parity = new byte[]{1, 1, 1, 1, 1, 1, 1, 1};
        check(DESKeySpec.isParityAdjusted(parity, 0), "isParityAdjusted true");
        check(!DESKeySpec.isParityAdjusted(key, 0), "isParityAdjusted false");
        check(!DESKeySpec.isWeak(key, 0), "isWeak false");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

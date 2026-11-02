/*
 * @test
 * @summary javax.crypto.spec.DESedeKeySpec conformance: triple-DES key spec.
 * @run main DESedeKeySpecTest
 */

import javax.crypto.spec.*;

public class DESedeKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(DESedeKeySpec.DES_EDE_KEY_LEN == 24, "DES_EDE_KEY_LEN 24");
        DESedeKeySpec s = new DESedeKeySpec(new byte[24]);
        check(s.getKey().length == 24, "getKey length");
        byte[] parity = new byte[24];
        java.util.Arrays.fill(parity, (byte) 1);
        check(DESedeKeySpec.isParityAdjusted(parity, 0), "isParityAdjusted");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

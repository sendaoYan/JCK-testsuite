/*
 * @test
 * @summary javax.crypto.spec.IvParameterSpec conformance: constructor and getIV.
 * @run main IvParameterSpecTest
 */

public class IvParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[16];
        javax.crypto.spec.IvParameterSpec s = new javax.crypto.spec.IvParameterSpec(iv);
        check(java.util.Arrays.equals(iv, s.getIV()), "getIV");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

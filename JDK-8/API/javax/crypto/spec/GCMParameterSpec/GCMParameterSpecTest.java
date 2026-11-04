/*
 * @test
 * @summary javax.crypto.spec.GCMParameterSpec conformance: constructor and getters.
 * @run main GCMParameterSpecTest
 */

public class GCMParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[12];
        javax.crypto.spec.GCMParameterSpec s = new javax.crypto.spec.GCMParameterSpec(128, iv);
        check(s.getTLen() == 128, "getTLen");
        check(java.util.Arrays.equals(iv, s.getIV()), "getIV");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

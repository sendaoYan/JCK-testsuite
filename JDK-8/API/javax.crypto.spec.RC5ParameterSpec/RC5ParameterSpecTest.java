/*
 * @test
 * @summary javax.crypto.spec.RC5ParameterSpec conformance: constructor and getters.
 * @run main RC5ParameterSpecTest
 */

public class RC5ParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[16];
        javax.crypto.spec.RC5ParameterSpec s = new javax.crypto.spec.RC5ParameterSpec(16, 12, 64, iv);
        check(s.getVersion() == 16, "getVersion");
        check(s.getRounds() == 12, "getRounds");
        check(s.getWordSize() == 64, "getWordSize");
        check(java.util.Arrays.equals(iv, s.getIV()), "getIV");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

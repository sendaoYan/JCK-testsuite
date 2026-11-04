/*
 * @test
 * @summary javax.crypto.spec.RC2ParameterSpec conformance: constructor and getters.
 * @run main RC2ParameterSpecTest
 */

public class RC2ParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] iv = new byte[8];
        javax.crypto.spec.RC2ParameterSpec s = new javax.crypto.spec.RC2ParameterSpec(128, iv);
        check(s.getEffectiveKeyBits() == 128, "getEffectiveKeyBits");
        check(java.util.Arrays.equals(iv, s.getIV()), "getIV");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

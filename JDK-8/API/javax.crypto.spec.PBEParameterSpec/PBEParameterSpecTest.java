/*
 * @test
 * @summary javax.crypto.spec.PBEParameterSpec conformance: constructor and getters.
 * @run main PBEParameterSpecTest
 */

public class PBEParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] salt = new byte[8];
        javax.crypto.spec.PBEParameterSpec s = new javax.crypto.spec.PBEParameterSpec(salt, 100);
        check(java.util.Arrays.equals(salt, s.getSalt()), "getSalt");
        check(s.getIterationCount() == 100, "getIterationCount");
        check(s.getParameterSpec() == null, "getParameterSpec null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

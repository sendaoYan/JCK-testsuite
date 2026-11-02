/*
 * @test
 * @summary javax.crypto.spec.DHGenParameterSpec conformance: constructor and getters.
 * @run main DHGenParameterSpecTest
 */

public class DHGenParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.spec.DHGenParameterSpec s = new javax.crypto.spec.DHGenParameterSpec(512, 160);
        check(s.getPrimeSize() == 512, "getPrimeSize");
        check(s.getExponentSize() == 160, "getExponentSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

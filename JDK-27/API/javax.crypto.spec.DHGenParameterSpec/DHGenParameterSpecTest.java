/*
 * @test
 * @summary javax.crypto.spec.DHGenParameterSpec conformance: DH generator parameter accessors.
 * @run main DHGenParameterSpecTest
 */

import javax.crypto.spec.*;

public class DHGenParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DHGenParameterSpec s = new DHGenParameterSpec(2048, 256);
        check(s.getPrimeSize() == 2048, "getPrimeSize");
        check(s.getExponentSize() == 256, "getExponentSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

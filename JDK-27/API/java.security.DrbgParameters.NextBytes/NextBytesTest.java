/*
 * @test
 * @summary java.security.DrbgParameters.NextBytes conformance: accessors.
 * @run main NextBytesTest
 */

import java.security.DrbgParameters;

public class NextBytesTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] ai = new byte[] { 9, 8 };
        DrbgParameters.NextBytes nb = DrbgParameters.nextBytes(128, true, ai);
        check(nb.getStrength() == 128, "getStrength");
        check(nb.getPredictionResistance(), "getPredictionResistance");
        check(java.util.Arrays.equals(ai, nb.getAdditionalInput()), "getAdditionalInput");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

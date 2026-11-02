/*
 * @test
 * @summary java.security.DrbgParameters.Reseed conformance: accessors.
 * @run main ReseedTest
 */

import java.security.DrbgParameters;

public class ReseedTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] ai = new byte[] { 7 };
        DrbgParameters.Reseed rs = DrbgParameters.reseed(false, ai);
        check(!rs.getPredictionResistance(), "getPredictionResistance");
        check(java.util.Arrays.equals(ai, rs.getAdditionalInput()), "getAdditionalInput");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

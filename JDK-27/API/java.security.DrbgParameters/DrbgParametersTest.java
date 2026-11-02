/*
 * @test
 * @summary java.security.DrbgParameters conformance: instantiation/nextBytes/reseed factory methods.
 * @run main DrbgParametersTest
 */

import java.security.DrbgParameters;

public class DrbgParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        DrbgParameters.Instantiation inst = DrbgParameters.instantiation(256, DrbgParameters.Capability.PR_AND_RESEED, new byte[] { 1, 2, 3 });
        check(inst.getStrength() == 256, "instantiation strength");
        check(inst.getCapability() == DrbgParameters.Capability.PR_AND_RESEED, "instantiation capability");

        DrbgParameters.NextBytes nb = DrbgParameters.nextBytes(128, true, new byte[] { 4 });
        check(nb.getStrength() == 128, "nextBytes strength");
        check(nb.getPredictionResistance(), "nextBytes prediction resistance");

        DrbgParameters.Reseed rs = DrbgParameters.reseed(true, new byte[] { 5 });
        check(rs.getPredictionResistance(), "reseed prediction resistance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

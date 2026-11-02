/*
 * @test
 * @summary java.security.DrbgParameters.Capability conformance: enum constants and support predicates.
 * @run main CapabilityTest
 */

import java.security.DrbgParameters.Capability;

public class CapabilityTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(Capability.values().length == 3, "three constants");
        check(Capability.valueOf("NONE") == Capability.NONE, "valueOf NONE");
        check(Capability.PR_AND_RESEED.supportsReseeding(), "PR_AND_RESEED supportsReseeding");
        check(Capability.PR_AND_RESEED.supportsPredictionResistance(), "PR_AND_RESEED supportsPredictionResistance");
        check(Capability.RESEED_ONLY.supportsReseeding(), "RESEED_ONLY supportsReseeding");
        check(!Capability.RESEED_ONLY.supportsPredictionResistance(), "RESEED_ONLY no prediction resistance");
        check(!Capability.NONE.supportsReseeding(), "NONE no reseeding");
        check(!Capability.NONE.supportsPredictionResistance(), "NONE no prediction resistance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

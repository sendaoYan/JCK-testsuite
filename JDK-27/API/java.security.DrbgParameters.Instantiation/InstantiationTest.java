/*
 * @test
 * @summary java.security.DrbgParameters.Instantiation conformance: accessors.
 * @run main InstantiationTest
 */

import java.security.DrbgParameters;

public class InstantiationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] ps = new byte[] { 1, 2, 3 };
        DrbgParameters.Instantiation inst = DrbgParameters.instantiation(192, DrbgParameters.Capability.RESEED_ONLY, ps);
        check(inst.getStrength() == 192, "getStrength");
        check(inst.getCapability() == DrbgParameters.Capability.RESEED_ONLY, "getCapability");
        check(java.util.Arrays.equals(ps, inst.getPersonalizationString()), "getPersonalizationString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

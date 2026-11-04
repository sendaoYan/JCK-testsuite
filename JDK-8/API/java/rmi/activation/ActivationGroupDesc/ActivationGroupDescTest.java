/*
 * @test
 * @summary java.rmi.activation.ActivationGroupDesc conformance: property overrides.
 * @run main ActivationGroupDescTest
 */

import java.rmi.activation.*;
public class ActivationGroupDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.Properties p = new java.util.Properties();
        p.setProperty("k", "v");
        ActivationGroupDesc agd = new ActivationGroupDesc(p, null);
        check("v".equals(agd.getPropertyOverrides().getProperty("k")), "property overrides");
        check(agd.getCommandEnvironment() == null, "command environment null");
        check(agd.getClassName() == null, "class name null for defaults ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

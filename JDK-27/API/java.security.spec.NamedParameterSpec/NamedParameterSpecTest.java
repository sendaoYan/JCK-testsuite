/*
 * @test
 * @summary java.security.spec.NamedParameterSpec conformance: constructor, getName, static constants.
 * @run main NamedParameterSpecTest
 */

import java.security.spec.NamedParameterSpec;

public class NamedParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        NamedParameterSpec spec = new NamedParameterSpec("X25519");
        check("X25519".equals(spec.getName()), "getName");
        check("X25519".equals(NamedParameterSpec.X25519.getName()), "X25519 constant");
        check("X448".equals(NamedParameterSpec.X448.getName()), "X448 constant");
        check("Ed25519".equals(NamedParameterSpec.ED25519.getName()), "ED25519 constant");
        check("Ed448".equals(NamedParameterSpec.ED448.getName()), "ED448 constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

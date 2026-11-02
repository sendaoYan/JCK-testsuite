/*
 * @test
 * @summary java.security.spec.EdDSAParameterSpec conformance: constructors and accessors.
 * @run main EdDSAParameterSpecTest
 */

import java.security.spec.EdDSAParameterSpec;

public class EdDSAParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        EdDSAParameterSpec spec = new EdDSAParameterSpec(false);
        check(!spec.isPrehash(), "isPrehash false");
        check(!spec.getContext().isPresent(), "no context");
        EdDSAParameterSpec spec2 = new EdDSAParameterSpec(true, new byte[] { 1, 2 });
        check(spec2.isPrehash(), "isPrehash true");
        check(spec2.getContext().isPresent(), "context present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

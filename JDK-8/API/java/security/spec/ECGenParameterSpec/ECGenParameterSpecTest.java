/*
 * @test
 * @summary java.security.spec.ECGenParameterSpec conformance: constructor and getName.
 * @run main ECGenParameterSpecTest
 */

public class ECGenParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.ECGenParameterSpec s = new java.security.spec.ECGenParameterSpec("secp256r1");
        check("secp256r1".equals(s.getName()), "getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

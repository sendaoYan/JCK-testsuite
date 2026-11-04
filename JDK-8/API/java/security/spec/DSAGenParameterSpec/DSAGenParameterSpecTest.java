/*
 * @test
 * @summary java.security.spec.DSAGenParameterSpec conformance: constructor and getters.
 * @run main DSAGenParameterSpecTest
 */

public class DSAGenParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.DSAGenParameterSpec s = new java.security.spec.DSAGenParameterSpec(2048, 256);
        check(s.getPrimePLength() == 2048, "getPrimePLength");
        check(s.getSubprimeQLength() == 256, "getSubprimeQLength");
        check(s.getSeedLength() == 256, "getSeedLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

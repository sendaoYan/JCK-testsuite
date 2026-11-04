/*
 * @test
 * @summary java.security.AlgorithmParameterGenerator conformance: getInstance/init/generateParameters.
 * @run main AlgorithmParameterGeneratorTest
 */

public class AlgorithmParameterGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.AlgorithmParameterGenerator apg = java.security.AlgorithmParameterGenerator.getInstance("DiffieHellman");
        check("DiffieHellman".equals(apg.getAlgorithm()), "getAlgorithm");
        check(apg.getProvider() != null, "getProvider");
        apg.init(512);
        check(apg.generateParameters() != null, "generateParameters");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

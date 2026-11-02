/*
 * @test
 * @summary java.security.AlgorithmParameterGenerator conformance: DH getInstance/init/generateParameters.
 * @run main AlgorithmParameterGeneratorTest
 */

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;

public class AlgorithmParameterGeneratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AlgorithmParameterGenerator apg = AlgorithmParameterGenerator.getInstance("DH");
        check("DH".equals(apg.getAlgorithm()), "getAlgorithm");
        check(apg.getProvider() != null, "getProvider");
        apg.init(2048);
        AlgorithmParameters params = apg.generateParameters();
        check(params != null, "generateParameters non-null");
        check("DH".equals(params.getAlgorithm()), "generated algorithm");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.security.AlgorithmParameters conformance: EC getInstance/init/getParameterSpec.
 * @run main AlgorithmParametersTest
 */

import java.security.AlgorithmParameters;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;

public class AlgorithmParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
        check("EC".equals(params.getAlgorithm()), "getAlgorithm");
        check(params.getProvider() != null, "getProvider");
        params.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec spec = params.getParameterSpec(ECParameterSpec.class);
        check(spec.getCurve().getField().getFieldSize() == 256, "secp256r1 field size 256");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

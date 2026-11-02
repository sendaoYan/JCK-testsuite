/*
 * @test
 * @summary javax.crypto.spec.OAEPParameterSpec conformance: OAEP parameter spec accessors and DEFAULT.
 * @run main OAEPParameterSpecTest
 */

import javax.crypto.spec.*;
import java.security.spec.*;

public class OAEPParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(OAEPParameterSpec.DEFAULT != null, "DEFAULT non-null");
        OAEPParameterSpec s = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        check("SHA-256".equals(s.getDigestAlgorithm()), "getDigestAlgorithm");
        check("MGF1".equals(s.getMGFAlgorithm()), "getMGFAlgorithm");
        check(s.getMGFParameters() == MGF1ParameterSpec.SHA256, "getMGFParameters");
        check(s.getPSource() == PSource.PSpecified.DEFAULT, "getPSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

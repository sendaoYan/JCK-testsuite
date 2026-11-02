/*
 * @test
 * @summary javax.crypto.KDF conformance: HKDF-SHA256 derive key and data.
 * @run main KDFTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;

public class KDFTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        check("HKDF-SHA256".equals(kdf.getAlgorithm()), "getAlgorithm");
        check(kdf.getProviderName() != null, "getProviderName non-null");
        check(kdf.getParameters() == null, "no-arg getInstance has null parameters");
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3, 4, 5}, "Generic");
        HKDFParameterSpec spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(new byte[]{9}).thenExpand(new byte[]{7}, 32);
        SecretKey out = kdf.deriveKey("AES", spec);
        check("AES".equals(out.getAlgorithm()), "deriveKey algorithm");
        check(out.getEncoded().length == 32, "deriveKey length 32");
        check(kdf.deriveData(spec).length == 32, "deriveData length 32");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

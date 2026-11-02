/*
 * @test
 * @summary javax.crypto.spec.HPKEParameterSpec conformance: HPKE parameter spec builder and accessors.
 * @run main HPKEParameterSpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class HPKEParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(HPKEParameterSpec.KEM_DHKEM_X25519_HKDF_SHA256 != HPKEParameterSpec.KEM_DHKEM_P_256_HKDF_SHA256, "distinct KEM ids");
        HPKEParameterSpec hp = HPKEParameterSpec.of(HPKEParameterSpec.KEM_DHKEM_X25519_HKDF_SHA256, HPKEParameterSpec.KDF_HKDF_SHA256, HPKEParameterSpec.AEAD_AES_128_GCM);
        check(hp.kem_id() == HPKEParameterSpec.KEM_DHKEM_X25519_HKDF_SHA256, "kem_id");
        check(hp.kdf_id() == HPKEParameterSpec.KDF_HKDF_SHA256, "kdf_id");
        check(hp.aead_id() == HPKEParameterSpec.AEAD_AES_128_GCM, "aead_id");
        HPKEParameterSpec hp2 = hp.withInfo(new byte[]{1, 2});
        check(Arrays.equals(new byte[]{1, 2}, hp2.info()), "withInfo");
        check(hp.toString() != null, "toString non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

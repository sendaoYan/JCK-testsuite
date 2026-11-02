/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.Extract conformance: HKDF extract-only spec accessors.
 * @run main ExtractTest
 */

import javax.crypto.spec.*;
import javax.crypto.*;

public class ExtractTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3}, "Generic");
        HKDFParameterSpec.Extract ex = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(new byte[]{4}).extractOnly();
        check(ex.ikms().size() == 1, "ikms size");
        check(ex.salts().size() == 1, "salts size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

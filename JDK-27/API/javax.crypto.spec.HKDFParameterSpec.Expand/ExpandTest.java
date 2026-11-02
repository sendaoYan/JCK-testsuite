/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.Expand conformance: HKDF expand-only spec accessors.
 * @run main ExpandTest
 */

import javax.crypto.spec.*;
import javax.crypto.*;
import java.util.Arrays;

public class ExpandTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKeySpec prk = new SecretKeySpec(new byte[32], "Generic");
        byte[] info = new byte[]{1, 2};
        HKDFParameterSpec.Expand ex = HKDFParameterSpec.expandOnly(prk, info, 32);
        check(ex.prk().getEncoded().length == 32, "prk accessor");
        check(Arrays.equals(info, ex.info()), "info accessor");
        check(ex.length() == 32, "length accessor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

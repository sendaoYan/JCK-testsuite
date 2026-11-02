/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.ExtractThenExpand conformance: HKDF extract-then-expand spec accessors.
 * @run main ExtractThenExpandTest
 */

import javax.crypto.spec.*;
import javax.crypto.*;
import java.util.Arrays;

public class ExtractThenExpandTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3}, "Generic");
        byte[] info = new byte[]{7};
        HKDFParameterSpec.ExtractThenExpand ete = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(new byte[]{4}).thenExpand(info, 16);
        check(ete.ikms().size() == 1, "ikms size");
        check(ete.salts().size() == 1, "salts size");
        check(Arrays.equals(info, ete.info()), "info accessor");
        check(ete.length() == 16, "length accessor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

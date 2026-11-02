/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.Builder conformance: HKDF extract-then-expand builder.
 * @run main BuilderTest
 */

import javax.crypto.spec.*;
import javax.crypto.*;
import java.util.List;

public class BuilderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3}, "Generic");
        HKDFParameterSpec.Builder b = HKDFParameterSpec.ofExtract();
        b.addIKM(ikm).addSalt(new byte[]{9, 8});
        HKDFParameterSpec.ExtractThenExpand ete = b.thenExpand(new byte[]{0}, 32);
        check(ete.ikms().size() == 1, "ikms size");
        check(ete.salts().size() == 1, "salts size");
        check(ete.length() == 32, "expand length");
        HKDFParameterSpec.Extract ex = HKDFParameterSpec.ofExtract().extractOnly();
        check(ex.ikms().isEmpty() && ex.salts().isEmpty(), "extractOnly empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

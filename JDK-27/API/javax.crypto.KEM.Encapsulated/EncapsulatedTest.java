/*
 * @test
 * @summary javax.crypto.KEM.Encapsulated conformance: encapsulated key/encapsulation/params accessors.
 * @run main EncapsulatedTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Arrays;

public class EncapsulatedTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKeySpec key = new SecretKeySpec(new byte[16], "AES");
        byte[] enc = new byte[]{1, 2, 3};
        byte[] params = new byte[]{4, 5};
        KEM.Encapsulated e = new KEM.Encapsulated(key, enc, params);
        check("AES".equals(e.key().getAlgorithm()), "key accessor");
        check(Arrays.equals(enc, e.encapsulation()), "encapsulation accessor");
        check(Arrays.equals(params, e.params()), "params accessor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

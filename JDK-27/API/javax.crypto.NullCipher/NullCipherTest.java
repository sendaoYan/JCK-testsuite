/*
 * @test
 * @summary javax.crypto.NullCipher conformance: identity cipher no-op.
 * @run main NullCipherTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Arrays;

public class NullCipherTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        NullCipher nc = new NullCipher();
        check(nc instanceof Cipher, "extends Cipher");
        check(nc.getBlockSize() == 1, "block size 1");
        nc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"));
        byte[] in = new byte[]{1, 2, 3};
        check(Arrays.equals(in, nc.doFinal(in)), "identity doFinal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

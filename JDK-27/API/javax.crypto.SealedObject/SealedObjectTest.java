/*
 * @test
 * @summary javax.crypto.SealedObject conformance: AES sealed object round trip.
 * @run main SealedObjectTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;

public class SealedObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Cipher aes = Cipher.getInstance("AES");
        SecretKeySpec key = new SecretKeySpec(new byte[16], "AES");
        aes.init(Cipher.ENCRYPT_MODE, key);
        SealedObject so = new SealedObject("secret", aes);
        check("AES".equals(so.getAlgorithm()), "getAlgorithm");
        Cipher d = Cipher.getInstance("AES");
        d.init(Cipher.DECRYPT_MODE, key);
        check("secret".equals(so.getObject(d)), "getObject(cipher)");
        check("secret".equals(so.getObject(key)), "getObject(key)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

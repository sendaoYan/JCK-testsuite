/*
 * @test
 * @summary javax.crypto.Cipher conformance: AES/GCM cipher round trip and mode constants.
 * @run main CipherTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Arrays;

public class CipherTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        check(Cipher.ENCRYPT_MODE == 1 && Cipher.DECRYPT_MODE == 2 && Cipher.WRAP_MODE == 3 && Cipher.UNWRAP_MODE == 4, "mode constants");
        check(Cipher.PUBLIC_KEY == 1 && Cipher.PRIVATE_KEY == 2 && Cipher.SECRET_KEY == 3, "key type constants");
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        check("AES/GCM/NoPadding".equals(c.getAlgorithm()), "getAlgorithm");
        check(c.getProvider() != null, "getProvider non-null");
        check(c.getBlockSize() == 16, "getBlockSize 16");
        Cipher c2 = Cipher.getInstance("AES/GCM/NoPadding", c.getProvider());
        check(c2.getAlgorithm().equals("AES/GCM/NoPadding"), "provider getInstance");
        SecretKeySpec key = new SecretKeySpec(new byte[16], "AES");
        GCMParameterSpec gcm = new GCMParameterSpec(128, new byte[12]);
        c.init(Cipher.ENCRYPT_MODE, key, gcm);
        byte[] pt = "hello".getBytes("UTF-8");
        byte[] ct = c.doFinal(pt);
        check(ct.length == pt.length + 16, "GCM ciphertext length (payload + tag)");
        Cipher d = Cipher.getInstance("AES/GCM/NoPadding");
        d.init(Cipher.DECRYPT_MODE, key, gcm);
        check(Arrays.equals(pt, d.doFinal(ct)), "GCM round trip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

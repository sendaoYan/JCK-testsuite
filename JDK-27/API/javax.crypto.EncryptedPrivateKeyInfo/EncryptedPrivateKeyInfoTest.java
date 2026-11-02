/*
 * @test
 * @summary javax.crypto.EncryptedPrivateKeyInfo conformance: encrypted PKCS8 encoding and accessors.
 * @run main EncryptedPrivateKeyInfoTest
 */

import javax.crypto.*;
import java.security.*;

public class EncryptedPrivateKeyInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo("DES", new byte[]{1, 2, 3});
        check(epki.getAlgName() != null, "getAlgName non-null");
        check(epki.getEncryptedData().length == 3, "getEncryptedData length");
        check(epki.getEncoded().length > 0, "getEncoded DER non-empty");
        EncryptedPrivateKeyInfo epki2 = new EncryptedPrivateKeyInfo(AlgorithmParameters.getInstance("DES"), new byte[]{4, 5});
        check(epki2.getEncryptedData().length == 2, "AlgorithmParameters ctor");
        check(epki2.getAlgParameters() != null, "getAlgParameters non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

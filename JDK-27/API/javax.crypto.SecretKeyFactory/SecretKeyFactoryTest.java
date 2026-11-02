/*
 * @test
 * @summary javax.crypto.SecretKeyFactory conformance: DES secret key factory translate and derive.
 * @run main SecretKeyFactoryTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;

public class SecretKeyFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        check("DES".equals(skf.getAlgorithm()), "getAlgorithm");
        check(skf.getProvider() != null, "getProvider non-null");
        SecretKeySpec sks = new SecretKeySpec(new byte[8], "DES");
        SecretKey sk = skf.translateKey(sks);
        check("DES".equals(sk.getAlgorithm()), "translateKey algorithm");
        check(skf.generateSecret(new DESKeySpec(new byte[8])).getEncoded().length == 8, "generateSecret length");
        check(skf.getKeySpec(sk, DESKeySpec.class) != null, "getKeySpec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

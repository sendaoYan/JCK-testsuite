/*
 * @test
 * @summary javax.crypto.KeyGenerator conformance: AES key generation 128-bit.
 * @run main KeyGeneratorTest
 */

import javax.crypto.*;

public class KeyGeneratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        check("AES".equals(kg.getAlgorithm()), "getAlgorithm");
        check(kg.getProvider() != null, "getProvider non-null");
        kg.init(128);
        SecretKey sk = kg.generateKey();
        check("AES".equals(sk.getAlgorithm()), "generated key algorithm");
        check(sk.getEncoded().length == 16, "128-bit key length 16");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

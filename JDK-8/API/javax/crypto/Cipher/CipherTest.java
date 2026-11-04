/*
 * @test
 * @summary javax.crypto.Cipher conformance: getInstance/init/doFinal roundtrip.
 * @run main CipherTest
 */

public class CipherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
        check("AES".equals(c.getAlgorithm()), "getAlgorithm");
        check(c.getProvider() != null, "getProvider");
        check(c.getBlockSize() == 16, "getBlockSize");
        javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
        c.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
        byte[] ct = c.doFinal("hello world 123".getBytes());
        c.init(javax.crypto.Cipher.DECRYPT_MODE, key);
        byte[] pt = c.doFinal(ct);
        check(java.util.Arrays.equals("hello world 123".getBytes(), pt), "roundtrip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

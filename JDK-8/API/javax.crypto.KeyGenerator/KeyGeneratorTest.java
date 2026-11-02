/*
 * @test
 * @summary javax.crypto.KeyGenerator conformance: getInstance/init/generateKey.
 * @run main KeyGeneratorTest
 */

public class KeyGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.KeyGenerator kg = javax.crypto.KeyGenerator.getInstance("AES");
        kg.init(128);
        javax.crypto.SecretKey k = kg.generateKey();
        check(k != null && "AES".equals(k.getAlgorithm()), "generateKey");
        check("AES".equals(kg.getAlgorithm()), "getAlgorithm");
        check(kg.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

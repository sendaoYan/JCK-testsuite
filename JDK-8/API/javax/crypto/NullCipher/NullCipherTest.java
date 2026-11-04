/*
 * @test
 * @summary javax.crypto.NullCipher conformance: constructor and doFinal identity.
 * @run main NullCipherTest
 */

public class NullCipherTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.NullCipher c = new javax.crypto.NullCipher();
        check(c.getAlgorithm() == null, "getAlgorithm null");
        byte[] out = c.doFinal(new byte[]{1,2,3});
        check(java.util.Arrays.equals(new byte[]{1,2,3}, out), "doFinal identity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

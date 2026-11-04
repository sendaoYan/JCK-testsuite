/*
 * @test
 * @summary javax.crypto.SecretKeyFactory conformance: getInstance/generateSecret/getKeySpec.
 * @run main SecretKeyFactoryTest
 */

public class SecretKeyFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("DES");
        byte[] keyBytes = new byte[]{1,2,3,4,5,6,7,8};
        javax.crypto.SecretKey sk = skf.generateSecret(new javax.crypto.spec.DESKeySpec(keyBytes));
        check(sk != null, "generateSecret");
        javax.crypto.spec.DESKeySpec spec = (javax.crypto.spec.DESKeySpec) skf.getKeySpec(sk, javax.crypto.spec.DESKeySpec.class);
        check(spec != null, "getKeySpec");
        check("DES".equals(skf.getAlgorithm()), "getAlgorithm");
        check(skf.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

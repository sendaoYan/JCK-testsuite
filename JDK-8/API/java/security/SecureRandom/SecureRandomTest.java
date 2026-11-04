/*
 * @test
 * @summary java.security.SecureRandom conformance: nextBytes and getInstance.
 * @run main SecureRandomTest
 */

public class SecureRandomTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.SecureRandom sr = new java.security.SecureRandom();
        sr.setSeed(new byte[]{1,2,3});
        byte[] out = new byte[16];
        sr.nextBytes(out);
        check(out.length == 16, "nextBytes");
        java.security.SecureRandom sr2 = java.security.SecureRandom.getInstance("SHA1PRNG");
        check("SHA1PRNG".equals(sr2.getAlgorithm()), "getAlgorithm");
        check(sr2.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

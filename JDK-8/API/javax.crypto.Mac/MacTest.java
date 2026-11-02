/*
 * @test
 * @summary javax.crypto.Mac conformance: getInstance/init/doFinal/clone.
 * @run main MacTest
 */

public class MacTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(new byte[16], "HmacSHA256");
        mac.init(key);
        byte[] out = mac.doFinal("data".getBytes());
        check(out.length == 32, "doFinal length");
        check(mac.getMacLength() == 32, "getMacLength");
        check("HmacSHA256".equals(mac.getAlgorithm()), "getAlgorithm");
        check(mac.getProvider() != null, "getProvider");
        check(mac.clone() instanceof javax.crypto.Mac, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

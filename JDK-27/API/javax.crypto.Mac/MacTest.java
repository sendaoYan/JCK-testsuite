/*
 * @test
 * @summary javax.crypto.Mac conformance: HmacSHA256 MAC compute and clone.
 * @run main MacTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;

public class MacTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        check("HmacSHA256".equals(mac.getAlgorithm()), "getAlgorithm");
        check(mac.getProvider() != null, "getProvider non-null");
        check(mac.getMacLength() == 32, "getMacLength 32");
        SecretKeySpec key = new SecretKeySpec(new byte[32], "HmacSHA256");
        mac.init(key);
        mac.update("data".getBytes(StandardCharsets.UTF_8));
        byte[] tag = mac.doFinal();
        check(tag.length == 32, "doFinal length 32");
        Mac mac2 = (Mac) mac.clone();
        check("HmacSHA256".equals(mac2.getAlgorithm()), "clone algorithm");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

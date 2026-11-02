/*
 * @test
 * @summary javax.crypto.spec.SecretKeySpec conformance: raw secret key spec accessors and equality.
 * @run main SecretKeySpecTest
 */

import javax.crypto.spec.*;
import java.util.Arrays;

public class SecretKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SecretKeySpec s = new SecretKeySpec(new byte[16], "AES");
        check("AES".equals(s.getAlgorithm()), "getAlgorithm");
        check("RAW".equals(s.getFormat()), "getFormat RAW");
        check(s.getEncoded().length == 16, "getEncoded length");
        check(s.equals(new SecretKeySpec(new byte[16], "AES")), "equals");
        check(s.hashCode() == new SecretKeySpec(new byte[16], "AES").hashCode(), "hashCode");
        SecretKeySpec s2 = new SecretKeySpec(new byte[32], 8, 16, "AES");
        check(s2.getEncoded().length == 16, "offset ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

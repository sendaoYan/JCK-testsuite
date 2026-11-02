/*
 * @test
 * @summary java.security.MessageDigest conformance: SHA-256/MD5 digest, isEqual, getAlgorithm/getProvider/getDigestLength.
 * @run main MessageDigestTest
 */

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class MessageDigestTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        check("SHA-256".equals(sha.getAlgorithm()), "getAlgorithm");
        check(sha.getProvider() != null, "getProvider");
        check(sha.getDigestLength() == 32, "SHA-256 digest length 32");
        byte[] d = sha.digest("x".getBytes(StandardCharsets.UTF_8));
        check(d.length == 32, "digest() length 32");
        check(MessageDigest.isEqual(d, d), "isEqual self");
        MessageDigest sha2 = MessageDigest.getInstance("SHA-256");
        check(MessageDigest.isEqual(d, sha2.digest("x".getBytes(StandardCharsets.UTF_8))), "isEqual same input");

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        check(md5.getDigestLength() == 16, "MD5 digest length 16");
        check(md5.digest("x".getBytes(StandardCharsets.UTF_8)).length == 16, "MD5 digest() length 16");

        sha.reset();
        sha.update((byte) 'a');
        byte[] afterReset = sha.digest("a".getBytes(StandardCharsets.UTF_8));
        check(afterReset.length == 32, "digest after reset/update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

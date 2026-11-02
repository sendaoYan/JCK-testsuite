/*
 * @test
 * @summary java.security.DigestOutputStream conformance: digesting stream content.
 * @run main DigestOutputStreamTest
 */

import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class DigestOutputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DigestOutputStream dos = new DigestOutputStream(out, md);
        check(dos.getMessageDigest() == md, "getMessageDigest");
        dos.write(data);
        check(out.size() == data.length, "wrote all bytes");
        check(MessageDigest.isEqual(md.digest(), MessageDigest.getInstance("SHA-256").digest(data)), "digest matches");
        dos.on(false);
        passed++; // on(false) succeeded
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.security.DigestInputStream conformance: digesting stream content.
 * @run main DigestInputStreamTest
 */

import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class DigestInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(data), md);
        check(dis.getMessageDigest() == md, "getMessageDigest");
        byte[] buf = new byte[data.length];
        int n = 0, off = 0;
        while ((n = dis.read(buf, off, buf.length - off)) != -1) off += n;
        check(off == data.length, "read all bytes");
        check(MessageDigest.isEqual(md.digest(), MessageDigest.getInstance("SHA-256").digest(data)), "digest matches");
        dis.on(false);
        passed++; // on(false) succeeded
        System.out.println("PASS (" + passed + " assertions)");
    }
}

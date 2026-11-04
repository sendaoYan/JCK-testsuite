/*
 * @test
 * @summary java.security.DigestOutputStream conformance: full contract coverage.
 *          constructor, getMessageDigest/setMessageDigest, on(boolean) toggle,
 *          default-on digest accumulation via write(int)/write(byte[],off,len), and
 *          equality against a directly-computed digest.
 * @run main DigestOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class DigestOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] data = "the quick brown fox".getBytes("UTF-8");

        // ---- default on: write(byte[]) updates digest ----
        MessageDigest md = MessageDigest.getInstance("MD5");
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        DigestOutputStream dos = new DigestOutputStream(sink, md);
        check(dos.getMessageDigest() == md, "getMessageDigest returns passed digest");
        dos.write(data);
        byte[] expected = MessageDigest.getInstance("MD5").digest(data);
        check(Arrays.equals(md.digest(), expected), "default-on digest matches");
        check(Arrays.equals(sink.toByteArray(), data), "underlying stream receives data");

        // ---- on(false) disables accumulation ----
        MessageDigest md2 = MessageDigest.getInstance("MD5");
        DigestOutputStream dos2 = new DigestOutputStream(new ByteArrayOutputStream(), md2);
        dos2.on(false);
        dos2.write(data);
        check(Arrays.equals(md2.digest(), MessageDigest.getInstance("MD5").digest(new byte[0])),
                "on(false) does not accumulate");

        // ---- on(true) re-enables ----
        MessageDigest md3 = MessageDigest.getInstance("MD5");
        DigestOutputStream dos3 = new DigestOutputStream(new ByteArrayOutputStream(), md3);
        dos3.on(false);
        dos3.on(true);
        dos3.write(data);
        check(Arrays.equals(md3.digest(), expected), "on(true) re-enables accumulation");

        // ---- setMessageDigest replaces ----
        MessageDigest md4 = MessageDigest.getInstance("SHA-256");
        DigestOutputStream dos4 = new DigestOutputStream(new ByteArrayOutputStream(),
                MessageDigest.getInstance("MD5"));
        dos4.setMessageDigest(md4);
        check(dos4.getMessageDigest() == md4, "setMessageDigest replaces");
        dos4.write(data);
        check(Arrays.equals(md4.digest(), MessageDigest.getInstance("SHA-256").digest(data)),
                "replaced digest accumulates");

        // ---- write(int) accumulates single bytes ----
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        DigestOutputStream dos5 = new DigestOutputStream(new ByteArrayOutputStream(), md5);
        for (byte b : data) dos5.write(b);
        check(Arrays.equals(md5.digest(), expected), "write(int) accumulates");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

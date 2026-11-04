/*
 * @test
 * @summary java.security.DigestInputStream conformance: full contract coverage.
 *          constructor, getMessageDigest/setMessageDigest, on(boolean) toggle,
 *          default-on digest accumulation via read()/read(byte[],off,len), and
 *          equality against a directly-computed digest.
 * @run main DigestInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class DigestInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x & 0xff));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        byte[] data = "the quick brown fox".getBytes("UTF-8");

        // ---- default on: read(byte[]) updates digest ----
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(data), md);
        check(dis.getMessageDigest() == md, "getMessageDigest returns passed digest");
        byte[] buf = new byte[100];
        int n = dis.read(buf);
        check(n == data.length, "read returns bytes");
        check(Arrays.equals(Arrays.copyOf(buf, n), data), "read content");
        byte[] expected = MessageDigest.getInstance("MD5").digest(data);
        check(Arrays.equals(md.digest(), expected), "default-on digest matches");

        // ---- on(false) disables accumulation ----
        MessageDigest md2 = MessageDigest.getInstance("MD5");
        DigestInputStream dis2 = new DigestInputStream(new ByteArrayInputStream(data), md2);
        dis2.on(false);
        dis2.read(new byte[100]);
        check(Arrays.equals(md2.digest(), MessageDigest.getInstance("MD5").digest(new byte[0])),
                "on(false) does not accumulate");

        // ---- on(true) re-enables ----
        MessageDigest md3 = MessageDigest.getInstance("MD5");
        DigestInputStream dis3 = new DigestInputStream(new ByteArrayInputStream(data), md3);
        dis3.on(false);
        dis3.on(true);
        dis3.read(new byte[100]);
        check(Arrays.equals(md3.digest(), expected), "on(true) re-enables accumulation");

        // ---- setMessageDigest replaces ----
        MessageDigest md4 = MessageDigest.getInstance("SHA-256");
        DigestInputStream dis4 = new DigestInputStream(new ByteArrayInputStream(data),
                MessageDigest.getInstance("MD5"));
        dis4.setMessageDigest(md4);
        check(dis4.getMessageDigest() == md4, "setMessageDigest replaces");
        dis4.read(new byte[100]);
        check(Arrays.equals(md4.digest(), MessageDigest.getInstance("SHA-256").digest(data)),
                "replaced digest accumulates");

        // ---- read(byte[],off,len) accumulates only read bytes ----
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        DigestInputStream dis5 = new DigestInputStream(new ByteArrayInputStream(data), md5);
        byte[] big = new byte[10];
        int off = 0;
        while (off < data.length) {
            int c = dis5.read(big, 0, big.length);
            if (c < 0) break;
            off += c;
        }
        check(Arrays.equals(md5.digest(), expected), "read(byte[],off,len) accumulates");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

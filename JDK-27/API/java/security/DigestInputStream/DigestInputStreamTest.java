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

        // ==== contract deepening: single-byte read, empty stream, EOF behavior, toString ====

        // ---- read() single byte accumulates and returns the byte value ----
        MessageDigest sb1 = MessageDigest.getInstance("SHA-256");
        DigestInputStream d_single = new DigestInputStream(new ByteArrayInputStream(data), sb1);
        int first = d_single.read();
        check(first == (data[0] & 0xff), "read() returns first byte value");
        while (d_single.read() != -1) { /* drain */ }
        check(Arrays.equals(sb1.digest(), MessageDigest.getInstance("SHA-256").digest(data)),
                "read() accumulates all bytes");

        // ---- empty stream: immediate EOF, digest of empty input ----
        MessageDigest esb = MessageDigest.getInstance("MD5");
        DigestInputStream d_empty = new DigestInputStream(new ByteArrayInputStream(new byte[0]), esb);
        check(d_empty.read() == -1, "empty stream EOF");
        check(d_empty.read(new byte[4]) == -1, "empty stream read(buf) EOF");
        check(Arrays.equals(esb.digest(), MessageDigest.getInstance("MD5").digest(new byte[0])),
                "empty-stream digest is digest of empty");

        // ---- read at EOF does not corrupt state ----
        MessageDigest eofMd = MessageDigest.getInstance("MD5");
        DigestInputStream d_eof = new DigestInputStream(new ByteArrayInputStream(data), eofMd);
        byte[] sink = new byte[1000];
        int total = 0, r;
        while ((r = d_eof.read(sink)) != -1) total += r;
        check(total == data.length, "drained exactly data length");
        check(d_eof.read() == -1, "extra read() stays EOF");
        check(Arrays.equals(eofMd.digest(), expected), "digest intact after EOF reads");

        // ---- mixed on/off windows accumulate exactly the on-window bytes ----
        MessageDigest win = MessageDigest.getInstance("MD5");
        DigestInputStream d_win = new DigestInputStream(new ByteArrayInputStream(data), win);
        d_win.on(false);
        byte[] skip1 = new byte[5];
        d_win.read(skip1);                       // off: 5 bytes skipped
        d_win.on(true);
        byte[] take = new byte[data.length - 10];
        int got = d_win.read(take);              // on: middle bytes
        d_win.on(false);
        byte[] skip2 = new byte[5];
        d_win.read(skip2);                       // off: 5 bytes skipped
        check(got == data.length - 10, "on-window read length");
        byte[] window = Arrays.copyOfRange(data, 5, data.length - 5);
        check(Arrays.equals(win.digest(), MessageDigest.getInstance("MD5").digest(window)),
                "digest covers only on-window bytes");

        // ---- toString mentions digest when set ----
        MessageDigest tmd = MessageDigest.getInstance("MD5");
        DigestInputStream d_ts = new DigestInputStream(new ByteArrayInputStream(data), tmd);
        check(d_ts.toString() != null && d_ts.toString().contains("MD5"), "toString contains algorithm");

        // ---- null digest: construction allowed, but read() throws NPE (recorded) ----
        DigestInputStream d_null = new DigestInputStream(new ByteArrayInputStream(data), null);
        check(d_null.getMessageDigest() == null, "null digest stored");
        try {
            d_null.read(new byte[100]);
            check(false, "read with null digest should NPE");
        } catch (NullPointerException e) {
            check(true, "read with null digest NPE (recorded)");
        }
        d_null.setMessageDigest(MessageDigest.getInstance("MD5"));
        check(d_null.getMessageDigest() != null, "setMessageDigest after null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

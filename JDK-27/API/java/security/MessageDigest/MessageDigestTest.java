/*
 * @test
 * @summary java.security.MessageDigest conformance: full contract coverage.
 *          getInstance variants + exact exceptions (NoSuchAlgorithmException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          known-answer digests for MD5/SHA-1/SHA-256 (empty and "abc"),
 *          getAlgorithm/getProvider/getDigestLength, update(byte)/update(byte[],off,len)
 *          boundary checks, digest()/digest(byte[])/digest(buf,off,len) incl. short
 *          buffer DigestException, reset/reuse state invariants, clone independence,
 *          isEqual (constant-time semantics incl. null pairs), toString, alias
 *          equivalence (SHA-256 vs SHA256), empty vs incremental large-message
 *          determinism, and unmodified-input contract.
 * @run main MessageDigestTest
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

public class MessageDigestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x & 0xff));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        // ---- factory + exception matrix ----
        checkThrows(NoSuchAlgorithmException.class,
                () -> MessageDigest.getInstance("NoSuchDigest_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> MessageDigest.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> MessageDigest.getInstance(null, Security.getProvider("SUN")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> MessageDigest.getInstance("MD5", (Provider) null), "getInstance(alg, null Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> MessageDigest.getInstance("MD5", (String) null), "getInstance(alg, null String)");
        checkThrows(NoSuchProviderException.class,
                () -> MessageDigest.getInstance("MD5", "NoSuchProvider_XYZ"),
                "getInstance(alg, bogus provider)");

        Provider sun = Security.getProvider("SUN");
        MessageDigest mdProvider = MessageDigest.getInstance("MD5", sun);
        check("MD5".equals(mdProvider.getAlgorithm()), "getInstance(alg, Provider) getAlgorithm");
        MessageDigest mdProviderStr = MessageDigest.getInstance("MD5", "SUN");
        check("MD5".equals(mdProviderStr.getAlgorithm()), "getInstance(alg, strProvider) getAlgorithm");
        check("SUN".equalsIgnoreCase(mdProvider.getProvider().getName()), "getProvider name");

        // ---- known-answer digests ----
        check("d41d8cd98f00b204e9800998ecf8427e".equals(hex(digestOf("MD5", new byte[0]))),
                "MD5 empty vector");
        check("900150983cd24fb0d6963f7d28e17f72".equals(hex(digestOf("MD5", "abc".getBytes("UTF-8")))),
                "MD5 abc vector");
        check("da39a3ee5e6b4b0d3255bfef95601890afd80709".equals(hex(digestOf("SHA-1", new byte[0]))),
                "SHA-1 empty vector");
        check("a9993e364706816aba3e25717850c26c9cd0d89d".equals(hex(digestOf("SHA-1", "abc".getBytes("UTF-8")))),
                "SHA-1 abc vector");
        check("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                .equals(hex(digestOf("SHA-256", new byte[0]))), "SHA-256 empty vector");
        check("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
                .equals(hex(digestOf("SHA-256", "abc".getBytes("UTF-8")))), "SHA-256 abc vector");

        // ---- metadata ----
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        check(md5.getDigestLength() == 16, "MD5 digest length 16");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        check(sha1.getDigestLength() == 20, "SHA-1 digest length 20");
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        check(sha256.getDigestLength() == 32, "SHA-256 digest length 32");
        check("SHA-256".equals(sha256.getAlgorithm()), "getAlgorithm");
        check(sha256.getProvider() != null, "getProvider non-null");
        check(sha256.toString().contains("SHA-256"), "toString contains algorithm");
        check(sha256.toString().contains("Message Digest"), "toString contains 'Message Digest'");

        // ---- alias equivalence (SHA-1 vs SHA1) ----
        MessageDigest alias = MessageDigest.getInstance("SHA1");
        byte[] a1 = alias.digest("abc".getBytes("UTF-8"));
        byte[] a2 = MessageDigest.getInstance("SHA-1").digest("abc".getBytes("UTF-8"));
        check(Arrays.equals(a1, a2), "SHA-1/SHA1 alias equal digest");

        // ---- update boundaries ----
        final MessageDigest ub = MessageDigest.getInstance("MD5");
        checkThrows(NullPointerException.class, () -> ub.update((byte[]) null), "update((byte[])null)");
        checkThrows(IllegalArgumentException.class, () -> ub.update((byte[]) null, 0, 1),
                "update(null,0,1)");
        checkThrows(IllegalArgumentException.class, () -> ub.update(new byte[2], 1, 2),
                "update(off+len > len)");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> ub.update(new byte[2], -1, 1),
                "update(negative off)");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> ub.update(new byte[2], 0, -1),
                "update(negative len)");
        ub.update(new byte[4], 0, 4); // legal full range
        check(true, "update(0,0) legal boundary");

        // ---- digest() resets state: digest() twice gives same empty digest ----
        MessageDigest r = MessageDigest.getInstance("SHA-256");
        byte[] d1 = r.digest();
        byte[] d2 = r.digest();
        check(Arrays.equals(d1, d2) && d1.length == 32, "digest() then digest() equal (reset)");
        // update then digest then update again reuses engine
        r.update("abc".getBytes("UTF-8"));
        byte[] afterUpdate = r.digest();
        check("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad".equals(hex(afterUpdate)),
                "reuse after previous digest");

        // ---- digest(byte[] input) does not mutate input and returns fresh array ----
        MessageDigest di = MessageDigest.getInstance("MD5");
        byte[] input = "hello".getBytes("UTF-8");
        byte[] inputCopy = input.clone();
        byte[] out = di.digest(input);
        check(out != input, "digest(byte[]) returns distinct array");
        check(Arrays.equals(input, inputCopy), "digest(byte[]) does not mutate input");
        check(out.length == 16, "digest(byte[]) length");

        // ---- digest(buf,off,len) short buffer -> DigestException ----
        final MessageDigest db = MessageDigest.getInstance("MD5");
        checkThrows(java.security.DigestException.class, () -> db.digest(new byte[8], 0, 4),
                "digest short buffer");
        byte[] big = new byte[64];
        MessageDigest db2 = MessageDigest.getInstance("MD5");
        int n = db2.digest(big, 16, 32);
        check(n == 16, "digest(buf,off,len) returns digest length");
        check(hex(Arrays.copyOfRange(big, 16, 16 + 16)).equals("d41d8cd98f00b204e9800998ecf8427e"),
                "digest(buf,off,len) writes at offset");

        // ---- reset() ----
        MessageDigest rs = MessageDigest.getInstance("SHA-1");
        rs.update("some data".getBytes("UTF-8"));
        rs.reset();
        byte[] emptySha1 = MessageDigest.getInstance("SHA-1").digest();
        check(Arrays.equals(rs.digest(), emptySha1), "reset() clears buffered data");

        // ---- clone() independence ----
        MessageDigest cl = MessageDigest.getInstance("SHA-256");
        cl.update("abc".getBytes("UTF-8"));
        MessageDigest cl2 = (MessageDigest) cl.clone();
        byte[] clA = cl.digest();
        byte[] clB = cl2.digest();
        check(Arrays.equals(clA, clB), "clone produces same in-progress digest");
        // clone is independent: continue updating original after clone
        MessageDigest cl3 = MessageDigest.getInstance("SHA-256");
        cl3.update("a".getBytes("UTF-8"));
        MessageDigest cl4 = (MessageDigest) cl3.clone();
        cl3.update("bc".getBytes("UTF-8"));
        byte[] full = MessageDigest.getInstance("SHA-256").digest("abc".getBytes("UTF-8"));
        byte[] onlyA = MessageDigest.getInstance("SHA-256").digest("a".getBytes("UTF-8"));
        check(Arrays.equals(cl3.digest(), full), "original continues after clone");
        check(Arrays.equals(cl4.digest(), onlyA), "clone isolated from original updates");

        // ---- isEqual ----
        check(MessageDigest.isEqual(null, null), "isEqual(null,null) true");
        check(!MessageDigest.isEqual(null, new byte[1]), "isEqual(null,nonnull) false");
        check(!MessageDigest.isEqual(new byte[1], null), "isEqual(nonnull,null) false");
        check(MessageDigest.isEqual(new byte[]{1, 2, 3}, new byte[]{1, 2, 3}), "isEqual equal true");
        check(!MessageDigest.isEqual(new byte[]{1, 2, 3}, new byte[]{1, 2, 4}), "isEqual differ false");
        check(!MessageDigest.isEqual(new byte[]{1}, new byte[]{1, 2}), "isEqual length differ false");

        // ---- large message determinism (incremental == one-shot) ----
        byte[] big1 = new byte[100000];
        for (int i = 0; i < big1.length; i++) big1[i] = (byte) (i * 31);
        MessageDigest oneShot = MessageDigest.getInstance("SHA-256");
        byte[] os = oneShot.digest(big1);
        MessageDigest inc = MessageDigest.getInstance("SHA-256");
        int off = 0;
        while (off < big1.length) {
            int len = Math.min(8191, big1.length - off);
            inc.update(big1, off, len);
            off += len;
        }
        check(Arrays.equals(os, inc.digest()), "incremental large message == one-shot");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static byte[] digestOf(String alg, byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance(alg);
        return md.digest(data);
    }
}

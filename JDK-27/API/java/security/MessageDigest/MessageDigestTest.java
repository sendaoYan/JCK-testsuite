/*
 * @test
 * @summary java.security.MessageDigest conformance: white-box structure + black-box behavior.
 * @run main MessageDigestTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.MessageDigest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.MessageDigestSpi"), "extends MessageDigestSpi");
        checkMethods(c, new String[]{"getInstance", "getProvider", "update", "digest", "isEqual",
            "reset", "getAlgorithm", "getDigestLength", "clone", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Fixed known digests asserted as hex (RFC vectors).
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        check("SHA-256".equals(sha.getAlgorithm()), "getAlgorithm SHA-256");
        check(sha.getProvider() != null, "getProvider non-null");
        check(sha.getDigestLength() == 32, "SHA-256 digest length 32");
        check(hex(sha.digest("abc".getBytes(StandardCharsets.UTF_8)))
            .equals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"),
            "SHA-256(\"abc\") known vector");
        check(hex(sha.digest(new byte[0]))
            .equals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
            "SHA-256(\"\") known vector");

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        check(md5.getDigestLength() == 16, "MD5 digest length 16");
        check(hex(md5.digest("abc".getBytes(StandardCharsets.UTF_8)))
            .equals("900150983cd24fb0d6963f7d28e17f72"), "MD5(\"abc\") known vector");

        // update fragmentation == one-shot digest
        byte[] data = "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8);
        MessageDigest whole = MessageDigest.getInstance("SHA-256");
        byte[] oneShot = whole.digest(data);
        MessageDigest frag = MessageDigest.getInstance("SHA-256");
        for (byte b : data) frag.update(b);
        check(MessageDigest.isEqual(oneShot, frag.digest()), "byte-wise update == one-shot");

        MessageDigest frag2 = MessageDigest.getInstance("SHA-256");
        frag2.update(data, 0, 9);
        frag2.update(data, 9, data.length - 9);
        check(MessageDigest.isEqual(oneShot, frag2.digest()), "offset update == one-shot");

        // digest(byte[]) returns digested copy; digest(byte[],int,int) writes into buffer
        MessageDigest d = MessageDigest.getInstance("SHA-256");
        d.update(data);
        byte[] out = new byte[32];
        int n = d.digest(out, 0, out.length);
        check(n == 32, "digest(byte[],int,int) returns 32");
        check(MessageDigest.isEqual(out, oneShot), "digest into buffer == one-shot");
        check(hex(d.digest(new byte[0])).equals(hex(MessageDigest.getInstance("SHA-256").digest(new byte[0]))),
            "digest() resets state (empty follow-up)");

        // reset
        MessageDigest r = MessageDigest.getInstance("SHA-256");
        r.update(data);
        r.reset();
        check(hex(r.digest(new byte[0]))
            .equals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
            "reset() then digest empty");

        // clone independence
        MessageDigest base = MessageDigest.getInstance("SHA-256");
        base.update(data, 0, 4);
        MessageDigest clone = (MessageDigest) base.clone();
        base.update(data, 4, data.length - 4);
        clone.update(data, 4, data.length - 4);
        check(MessageDigest.isEqual(base.digest(), clone.digest()), "clone digests equal");

        // update(ByteBuffer)
        MessageDigest bb = MessageDigest.getInstance("SHA-256");
        bb.update(ByteBuffer.wrap(data));
        check(MessageDigest.isEqual(bb.digest(), oneShot), "update(ByteBuffer) == one-shot");

        // isEqual edge cases
        check(MessageDigest.isEqual(null, null), "isEqual(null,null)");
        check(!MessageDigest.isEqual(new byte[]{1}, null), "isEqual(arr,null)");
        check(!MessageDigest.isEqual(new byte[]{1}, new byte[]{1, 2}), "isEqual different length");

        // getInstance with explicit provider
        MessageDigest shaViaProvider = MessageDigest.getInstance("SHA-256", "SUN");
        check("SHA-256".equals(shaViaProvider.getAlgorithm()), "getInstance(alg,provider)");
    }

    // ---- helpers ----
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static String hex(byte[] b) {
        char[] h = "0123456789abcdef".toCharArray();
        char[] out = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) { out[i * 2] = h[(b[i] >> 4) & 0xf]; out[i * 2 + 1] = h[b[i] & 0xf]; }
        return new String(out);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.security.DigestInputStream conformance: white-box structure + black-box behavior.
 * @run main DigestInputStreamTest
 */

import java.lang.reflect.Modifier;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class DigestInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DigestInputStream");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.io.FilterInputStream"), "extends FilterInputStream");
        checkMethods(c, new String[]{"getMessageDigest", "setMessageDigest", "read", "on", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] expected = md.digest(data);

        // read() one byte at a time (digest is on by default)
        MessageDigest m1 = MessageDigest.getInstance("SHA-256");
        DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(data), m1);
        check(m1 == dis.getMessageDigest(), "getMessageDigest returns the digest");
        int b;
        while ((b = dis.read()) != -1) { /* consume */ }
        check(Arrays.equals(expected, m1.digest()), "byte-wise read updates digest");

        // read(byte[],int,int) with on(false) does not update the digest
        MessageDigest m2 = MessageDigest.getInstance("SHA-256");
        DigestInputStream dis2 = new DigestInputStream(new ByteArrayInputStream(data), m2);
        dis2.on(false);
        byte[] buf = new byte[8];
        while (dis2.read(buf, 0, buf.length) != -1) { /* consume */ }
        check(hex(m2.digest()).equals(hex(MessageDigest.getInstance("SHA-256").digest(new byte[0]))),
            "on(false) skips digest update");

        // setMessageDigest replaces the active digest
        MessageDigest m3 = MessageDigest.getInstance("SHA-256");
        DigestInputStream dis3 = new DigestInputStream(new ByteArrayInputStream(data), MessageDigest.getInstance("MD5"));
        dis3.setMessageDigest(m3);
        check(m3 == dis3.getMessageDigest(), "setMessageDigest replaces digest");
        while (dis3.read() != -1) { /* consume */ }
        check(Arrays.equals(expected, m3.digest()), "replaced digest computes SHA-256");

        check(dis3.toString() != null, "toString non-null");
    }

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

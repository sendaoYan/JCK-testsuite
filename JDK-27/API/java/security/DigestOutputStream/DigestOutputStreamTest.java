/*
 * @test
 * @summary java.security.DigestOutputStream conformance: white-box structure + black-box behavior.
 * @run main DigestOutputStreamTest
 */

import java.lang.reflect.Modifier;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class DigestOutputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DigestOutputStream");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.io.FilterOutputStream"), "extends FilterOutputStream");
        checkMethods(c, new String[]{"getMessageDigest", "setMessageDigest", "write", "on", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] expected = md.digest(data);

        // write(int) one byte at a time (digest on by default)
        MessageDigest m1 = MessageDigest.getInstance("SHA-256");
        DigestOutputStream dos = new DigestOutputStream(new ByteArrayOutputStream(), m1);
        check(m1 == dos.getMessageDigest(), "getMessageDigest returns the digest");
        for (byte b : data) dos.write(b);
        check(Arrays.equals(expected, m1.digest()), "byte-wise write updates digest");

        // write(byte[],int,int) with on(false) does not update
        MessageDigest m2 = MessageDigest.getInstance("SHA-256");
        DigestOutputStream dos2 = new DigestOutputStream(new ByteArrayOutputStream(), m2);
        dos2.on(false);
        dos2.write(data, 0, data.length);
        check(hex(m2.digest()).equals(hex(MessageDigest.getInstance("SHA-256").digest(new byte[0]))),
            "on(false) skips digest update");

        // setMessageDigest replaces
        MessageDigest m3 = MessageDigest.getInstance("SHA-256");
        DigestOutputStream dos3 = new DigestOutputStream(new ByteArrayOutputStream(), MessageDigest.getInstance("MD5"));
        dos3.setMessageDigest(m3);
        check(m3 == dos3.getMessageDigest(), "setMessageDigest replaces digest");
        dos3.write(data);
        check(Arrays.equals(expected, m3.digest()), "replaced digest computes SHA-256");

        check(dos3.toString() != null, "toString non-null");
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

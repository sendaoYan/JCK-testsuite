/*
 * @test
 * @summary java.security.DigestInputStream conformance: construct and read, get/setMessageDigest.
 * @run main DigestInputStreamTest
 */

public class DigestInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream("abc".getBytes());
        java.security.DigestInputStream dis = new java.security.DigestInputStream(bin, md);
        check(md.equals(dis.getMessageDigest()), "getMessageDigest");
        byte[] buf = new byte[8];
        check(dis.read(buf) == 3, "read");
        dis.setMessageDigest(md);
        check(md.equals(dis.getMessageDigest()), "setMessageDigest");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

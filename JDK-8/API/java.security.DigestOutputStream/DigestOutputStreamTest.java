/*
 * @test
 * @summary java.security.DigestOutputStream conformance: construct and write, get/setMessageDigest.
 * @run main DigestOutputStreamTest
 */

public class DigestOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        java.security.DigestOutputStream dos = new java.security.DigestOutputStream(bout, md);
        check(md.equals(dos.getMessageDigest()), "getMessageDigest");
        dos.write("abc".getBytes());
        dos.flush();
        check("abc".equals(bout.toString()), "write");
        dos.setMessageDigest(md);
        check(md.equals(dos.getMessageDigest()), "setMessageDigest");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

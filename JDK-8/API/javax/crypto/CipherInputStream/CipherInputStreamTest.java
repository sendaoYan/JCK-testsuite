/*
 * @test
 * @summary javax.crypto.CipherInputStream conformance: construct and read.
 * @run main CipherInputStreamTest
 */

public class CipherInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.NullCipher c = new javax.crypto.NullCipher();
        java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream("data".getBytes());
        javax.crypto.CipherInputStream cin = new javax.crypto.CipherInputStream(bin, c);
        byte[] buf = new byte[10];
        int n = cin.read(buf);
        check(n == 4 && "data".equals(new String(buf, 0, n)), "read");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

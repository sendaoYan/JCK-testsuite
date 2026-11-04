/*
 * @test
 * @summary javax.crypto.CipherOutputStream conformance: construct and write.
 * @run main CipherOutputStreamTest
 */

public class CipherOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.NullCipher c = new javax.crypto.NullCipher();
        java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
        javax.crypto.CipherOutputStream cout = new javax.crypto.CipherOutputStream(bout, c);
        cout.write("data".getBytes());
        cout.close();
        check("data".equals(bout.toString()), "write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

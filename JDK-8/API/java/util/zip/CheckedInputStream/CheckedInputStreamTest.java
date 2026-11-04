/*
 * @test
 * @summary java.util.zip.CheckedInputStream conformance: checksum-verified read.
 * @run main CheckedInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.CRC32;

public class CheckedInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCheckedInputStream() throws Exception {
        byte[] data = "hello".getBytes("UTF-8");
        CheckedInputStream in = new CheckedInputStream(new ByteArrayInputStream(data), new CRC32());
        byte[] buf = new byte[10];
        int n = in.read(buf);
        check(n == 5, "read returns data length");
        check(in.getChecksum() instanceof CRC32, "getChecksum");
        check(in.getChecksum().getValue() != 0, "checksum computed");
        check(in.read() == -1, "read() returns -1 at end");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testCheckedInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

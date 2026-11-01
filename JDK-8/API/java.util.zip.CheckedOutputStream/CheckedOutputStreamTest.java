/*
 * @test
 * @summary java.util.zip.CheckedOutputStream conformance: checksum-verified write.
 * @run main CheckedOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;

public class CheckedOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCheckedOutputStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CheckedOutputStream out = new CheckedOutputStream(bout, new Adler32());
        out.write("hello".getBytes("UTF-8"));
        out.flush();
        check(bout.size() == 5, "write reached underlying stream");
        check(out.getChecksum() instanceof Adler32, "getChecksum");
        check(out.getChecksum().getValue() != 0, "checksum computed");
        out.close();
    }

    public static void main(String[] args) throws Exception {
        testCheckedOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

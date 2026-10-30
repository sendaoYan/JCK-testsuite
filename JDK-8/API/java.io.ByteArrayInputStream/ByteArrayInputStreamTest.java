/*
 * @test
 * @summary java.io.ByteArrayInputStream API conformance: available, read(), read(byte[]), and
 *          EOF returning -1.
 * @run main ByteArrayInputStreamTest
 */

import java.io.ByteArrayInputStream;

public class ByteArrayInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStream() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        check(in.available() == 5, "available");
        check(in.read() == 1 && in.read() == 2, "read()");
        byte[] buf = new byte[2];
        check(in.read(buf) == 2 && buf[0] == 3 && buf[1] == 4, "read(byte[])");
        check(in.read() == 5 && in.read() == -1, "read to end / EOF");
    }

    public static void main(String[] args) throws Exception {
        testStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

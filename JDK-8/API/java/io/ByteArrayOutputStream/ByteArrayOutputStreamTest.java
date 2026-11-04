/*
 * @test
 * @summary java.io.ByteArrayOutputStream API conformance: write, size, and toByteArray.
 * @run main ByteArrayOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ByteArrayOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(65);
        out.write(new byte[]{66, 67});
        check(out.size() == 3, "size");
        byte[] bytes = out.toByteArray();
        check(bytes.length == 3 && bytes[0] == 65, "toByteArray");
        check("ABC".equals(new String(bytes, StandardCharsets.UTF_8)), "bytes to String");
    }

    public static void main(String[] args) throws Exception {
        testStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

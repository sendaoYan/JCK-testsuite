/*
 * @test
 * @summary java.io.OutputStreamWriter API conformance: write/flush and UTF-8 byte encoding.
 * @run main OutputStreamWriterTest
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class OutputStreamWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOutputStreamWriter() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        osw.write("A");
        osw.flush();
        check(bos.size() == 1 && bos.toByteArray()[0] == 65, "UTF-8 encoding of 'A'");
    }

    public static void main(String[] args) throws Exception {
        testOutputStreamWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.io.InputStreamReader API conformance: UTF-8 roundtrip of non-ASCII chars.
 * @run main InputStreamReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class InputStreamReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInputStreamReader() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        osw.write("héllo");
        osw.flush();
        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()),
                                  StandardCharsets.UTF_8);
        char[] buf = new char[5];
        int n = isr.read(buf);
        check(n == 5 && "héllo".equals(new String(buf)), "UTF-8 roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testInputStreamReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

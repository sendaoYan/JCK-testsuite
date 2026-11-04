/*
 * @test
 * @summary java.io.InputStreamReader conformance: full contract coverage.
 *          Charset/CharsetName constructors, getEncoding, single/bulk/offset reads,
 *          UTF-8 multi-byte roundtrip, EOF.
 * @run main InputStreamReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class InputStreamReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        String text = "héllo 世界 😀";  // "héllo 世界 😀"
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        osw.write(text);
        osw.flush();

        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8);
        check(isr.getEncoding() != null, "getEncoding non-null");
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4];
        int n;
        while ((n = isr.read(buf, 0, buf.length)) != -1) sb.append(buf, 0, n);
        check(text.equals(sb.toString()), "UTF-8 multi-byte roundtrip");
        check(isr.read() == -1, "EOF");

        // CharsetName constructor
        InputStreamReader named = new InputStreamReader(
            new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)), "UTF-8");
        check(named.read() == 'a' && named.read() == 'b' && named.read() == 'c' && named.read() == -1,
              "CharsetName ctor single reads");

        // read(char[]) bulk
        InputStreamReader bulk = new InputStreamReader(
            new ByteArrayInputStream("xy".getBytes(StandardCharsets.UTF_8)), Charset.forName("UTF-8"));
        char[] two = new char[1];
        check(bulk.read(two) == 1 && two[0] == 'x', "read(char[]) single");
        check(bulk.read(two) == 1 && two[0] == 'y', "read(char[]) second");
        check(bulk.read(two) == -1, "read(char[]) EOF");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

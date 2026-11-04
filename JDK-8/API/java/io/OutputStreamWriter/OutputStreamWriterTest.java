/*
 * @test
 * @summary java.io.OutputStreamWriter conformance: full contract coverage.
 *          Charset/CharsetName constructors, getEncoding, write(String)/write(char[],off,len),
 *          append, flush, UTF-8 exact-byte encoding, roundtrip via InputStreamReader.
 * @run main OutputStreamWriterTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OutputStreamWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        String s = "Aé中";  // "Aé中"
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        check(osw.getEncoding() != null, "getEncoding non-null");
        osw.write(s);
        osw.flush();
        check(Arrays.equals(bos.toByteArray(), s.getBytes(StandardCharsets.UTF_8)),
              "UTF-8 exact bytes");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        OutputStreamWriter o2 = new OutputStreamWriter(b2, StandardCharsets.UTF_8);
        o2.write(new char[]{'x', 'y', 'z', 'w'}, 1, 2);
        o2.append('!');
        o2.flush();
        check("yz!".equals(new String(b2.toByteArray(), StandardCharsets.UTF_8)),
              "write(char[],off,len)+append");

        // CharsetName constructor + roundtrip
        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        OutputStreamWriter o3 = new OutputStreamWriter(b3, "UTF-8");
        o3.write("héllo 世界");  // "héllo 世界"
        o3.flush();
        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(b3.toByteArray()), Charset.forName("UTF-8"));
        char[] c = new char[100];
        int n = isr.read(c);
        check("héllo 世界".equals(new String(c, 0, n)), "roundtrip via InputStreamReader");

        // flush makes bytes visible; append(char) chain
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        OutputStreamWriter o4 = new OutputStreamWriter(b4, StandardCharsets.UTF_8);
        o4.append('a').append('b').append('c');
        o4.flush();
        check("abc".equals(new String(b4.toByteArray(), StandardCharsets.UTF_8)), "append chain");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

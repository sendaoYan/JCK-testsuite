/*
 * @test
 * @summary java.io.PrintWriter conformance: full contract coverage.
 *          print/println/printf/format with a fixed Locale, checkError, UTF-8 output
 *          via an explicit OutputStreamWriter, println platform line separator.
 * @run main PrintWriterTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PrintWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        check(pw.printf(Locale.US, "%d %.2f %s", 42, 3.14159, "x") == pw, "printf returns this");
        pw.println();
        pw.print(true);
        pw.print(' ');
        pw.print(7L);
        pw.flush();
        String out = sw.toString();
        check(out.startsWith("42 3.14 x"), "printf content");
        check(out.contains("true"), "print boolean");
        check(out.contains("7"), "print long");
        check(!pw.checkError(), "checkError false");

        StringWriter sw2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(sw2);
        pw2.format(Locale.US, "%05d", 42);
        pw2.flush();
        check("00042".equals(sw2.toString()), "format");

        // UTF-8 output via explicit OutputStreamWriter (JDK 8 has no Charset ctor)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter pw3 = new PrintWriter(new OutputStreamWriter(bos, StandardCharsets.UTF_8));
        pw3.println("héllo 世界");
        pw3.flush();
        InputStreamReader isr =
            new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), StandardCharsets.UTF_8);
        char[] buf = new char[100];
        int n = isr.read(buf);
        check(("héllo 世界" + System.lineSeparator()).equals(new String(buf, 0, n)),
              "UTF-8 println roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

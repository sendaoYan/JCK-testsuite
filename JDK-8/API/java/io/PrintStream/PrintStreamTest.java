/*
 * @test
 * @summary java.io.PrintStream conformance: full contract coverage.
 *          print/println/printf/format with a fixed Locale and explicit UTF-8
 *          encoding, checkError, append, println platform line separator.
 * @run main PrintStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class PrintStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos, false, "UTF-8");
        check(ps.printf(Locale.US, "%d %.2f", 42, 3.14159) == ps, "printf returns this");
        ps.println();
        ps.print(true);
        ps.append(" ok");
        ps.flush();
        String out = new String(bos.toByteArray(), StandardCharsets.UTF_8);
        check(out.startsWith("42 3.14"), "printf content");
        check(out.contains("true"), "print boolean");
        check(out.contains("ok"), "append");
        check(!ps.checkError(), "checkError false");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        PrintStream p2 = new PrintStream(b2, false, "UTF-8");
        p2.format(Locale.US, "%05d", 42);
        p2.flush();
        check("00042".equals(new String(b2.toByteArray(), StandardCharsets.UTF_8)), "format");

        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        PrintStream p3 = new PrintStream(b3, false, "UTF-8");
        p3.println("héllo 世界");
        p3.flush();
        check(("héllo 世界" + System.lineSeparator()).equals(new String(b3.toByteArray(), StandardCharsets.UTF_8)),
              "UTF-8 println roundtrip");

        // println() alone emits just the line separator
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        PrintStream p4 = new PrintStream(b4, false, "UTF-8");
        p4.println();
        p4.flush();
        check(System.lineSeparator().equals(new String(b4.toByteArray(), StandardCharsets.UTF_8)),
              "bare println emits the line separator");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

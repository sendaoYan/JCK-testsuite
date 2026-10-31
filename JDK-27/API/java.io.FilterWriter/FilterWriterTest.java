/*
 * @test
 * @summary java.io.FilterWriter API conformance: filters a Writer (via subclass; ctor is protected).
 * @run main FilterWriterTest
 */

import java.io.FilterWriter;
import java.io.StringWriter;
import java.io.Writer;

public class FilterWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Passthrough extends FilterWriter {
        Passthrough(Writer out) {
            super(out);
        }
    }

    static void testFilterWriter() throws Exception {
        StringWriter sink = new StringWriter();
        Passthrough w = new Passthrough(sink);
        w.write('a');
        w.write("bc");
        w.write("def", 0, 3);
        w.flush();
        check("abcdef".equals(sink.toString()), "write chars/strings");
        w.close();
    }

    public static void main(String[] args) throws Exception {
        testFilterWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

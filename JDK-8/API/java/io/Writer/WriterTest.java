/*
 * @test
 * @summary java.io.Writer API conformance: default write/append/flush/close semantics.
 * @run main WriterTest
 */

import java.io.Writer;

public class WriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class SinkWriter extends Writer {
        StringBuilder sb = new StringBuilder();

        public void write(char[] cbuf, int off, int len) {
            sb.append(cbuf, off, len);
        }

        public void flush() {}

        public void close() {}
    }

    static void testWriter() throws Exception {
        SinkWriter w = new SinkWriter();
        w.write('A');
        w.write(new char[] {'B', 'C'});
        w.write("DE");
        w.append("FG");
        check("ABCDEFG".equals(w.sb.toString()), "concatenated writes");
        check(w.append('H') == w, "append(char) returns this writer");
        check("ABCDEFGH".equals(w.sb.toString()), "append(char) writes the char");
        w.flush();
        w.close();
    }

    public static void main(String[] args) throws Exception {
        testWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

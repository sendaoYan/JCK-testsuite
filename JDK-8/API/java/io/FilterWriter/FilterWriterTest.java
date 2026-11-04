/*
 * @test
 * @summary java.io.FilterWriter conformance: full contract coverage.
 *          A minimal concrete subclass delegates write(int)/write(char[],int,int)/
 *          write(String,int,int)/flush/close to the wrapped writer; interception;
 *          protected out field reachable.
 * @run main FilterWriterTest
 */

import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class FilterWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class PassThrough extends FilterWriter {
        PassThrough(Writer out) { super(out); }
    }

    static class Recording extends Writer {
        final Writer delegate;
        int writes, flushes, closes;
        Recording(Writer d) { delegate = d; }
        public void write(char[] c, int o, int l) throws IOException { writes++; delegate.write(c, o, l); }
        public void write(int c) throws IOException { writes++; delegate.write(c); }
        public void write(String s, int o, int l) throws IOException { writes++; delegate.write(s, o, l); }
        public void flush() throws IOException { flushes++; delegate.flush(); }
        public void close() throws IOException { closes++; delegate.close(); }
    }

    static void testBehavior() throws Exception {
        // every write form delegates with region semantics
        StringWriter sink = new StringWriter();
        Recording rec = new Recording(sink);
        PassThrough fw = new PassThrough(rec);
        fw.write('a');
        fw.write("bcdef".toCharArray(), 1, 2);
        fw.write("XYZ", 1, 1);
        check("acdY".equals(sink.toString()), "write forms delegate");

        // append uses the Writer defaults (routes to write(char[],int,int))
        StringWriter s2 = new StringWriter();
        PassThrough f2 = new PassThrough(s2);
        f2.append('Q');
        f2.append("RS");
        f2.append("TUV", 1, 2);
        check("QRSU".contentEquals(s2.getBuffer()), "append through the decorator");

        // flush and close delegate
        Recording r3 = new Recording(new StringWriter());
        PassThrough f3 = new PassThrough(r3);
        f3.flush();
        f3.flush();
        check(r3.flushes == 2, "flush delegates");
        f3.close();
        check(r3.closes == 1, "close delegates");

        // interception: an uppercasing decorator
        class Upper extends FilterWriter {
            Upper(Writer o) { super(o); }
            @Override public void write(int c) throws IOException {
                super.write(Character.toUpperCase(c));
            }
        }
        StringWriter dst = new StringWriter();
        Upper u = new Upper(dst);
        u.write('a');
        u.write('b');
        u.close();
        check("AB".contentEquals(dst.getBuffer()), "subclass intercepted every char");

        // subclass can reach the protected out field
        StringWriter inner = new StringWriter();
        class Reaching extends FilterWriter {
            Reaching(Writer w) { super(w); }
            Writer inner() { return out; }
        }
        check(new Reaching(inner).inner() == inner, "protected out is the wrapped writer");

        // a failing sink surfaces with its message
        class Failing extends Writer {
            public void write(char[] c, int o, int l) throws IOException { throw new IOException("sink"); }
            public void flush() { }
            public void close() { }
        }
        try {
            new PassThrough(new Failing()).write('x');
            check(false, "sink failure must propagate");
        } catch (IOException ioe) {
            check("sink".equals(ioe.getMessage()), "original message kept");
        }
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

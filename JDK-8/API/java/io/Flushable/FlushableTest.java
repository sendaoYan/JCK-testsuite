/*
 * @test
 * @summary java.io.Flushable conformance (pure black-box): flush contract
 *          (force buffered bytes out); counting implementations; a failing flush
 *          surfaces; JDK buffered streams flushed through the interface
 *          observable in the underlying sink.
 * @run main FlushableTest
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

public class FlushableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Counting implements Flushable {
        int flushes;
        boolean throwOnFlush;
        public void flush() throws IOException {
            flushes++;
            if (throwOnFlush) throw new IOException("flush failed");
        }
    }

    /** Underlying sink that records when bytes actually arrive. */
    static class Recorder extends OutputStream {
        final ByteArrayOutputStream seen = new ByteArrayOutputStream();
        @Override public void write(int b) { seen.write(b); }
        @Override public void write(byte[] b, int off, int len) { seen.write(b, off, len); }
    }

    static void testBlackBox() throws Exception {
        // 1. lambda implementation
        int[] count = {0};
        Flushable lambda = () -> count[0]++;
        lambda.flush();
        lambda.flush();
        check(count[0] == 2, "lambda flush ran twice");

        // 2. a buffered stream does not deliver bytes until flushed
        Recorder rec = new Recorder();
        BufferedOutputStream bos = new BufferedOutputStream(rec, 8192);
        bos.write('a');
        check(rec.seen.size() == 0, "byte still buffered, not in the sink");
        Flushable f = bos;
        f.flush();
        check(rec.seen.size() == 1 && rec.seen.toByteArray()[0] == 'a',
              "flush pushed the buffered byte to the sink");

        // 3. a failing flush surfaces its IOException through the interface
        Counting c3 = new Counting();
        c3.throwOnFlush = true;
        try {
            c3.flush();
            check(false, "failing flush must throw");
        } catch (IOException ioe) {
            check("flush failed".equals(ioe.getMessage()), "flush failure propagated");
        }

        // 4. flush on an unbuffered stream is a harmless no-op
        Flushable baos = new ByteArrayOutputStream();
        baos.flush();
        baos.flush();
        passed++;

        // 5. close also flushes buffered data (contract of buffered implementors)
        Recorder rec2 = new Recorder();
        try (BufferedOutputStream b2 = new BufferedOutputStream(rec2, 8192)) {
            b2.write('z');
            check(rec2.seen.size() == 0, "still buffered before close");
        }
        check(rec2.seen.size() == 1, "close flushed the buffer");

        // 6. flush is repeatable; counting implementor sees every call
        Counting c6 = new Counting();
        for (int i = 0; i < 3; i++) c6.flush();
        check(c6.flushes == 3, "all flushes counted");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

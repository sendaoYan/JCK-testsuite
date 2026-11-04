/*
 * @test
 * @summary java.io.Flushable conformance (pure black-box): flush contract
 *          (force buffered bytes out); counting implementations; a failing flush
 *          surfaces; JDK buffered streams flushed through the interface
 *          observable in the underlying sink.
 *          Deepening: try-with-resources does NOT auto-flush, char-stream flush
 *          (BufferedWriter) observable in the sink, flush after close -> IOException,
 *          no-op implementors (StringWriter/CharArrayWriter/ByteArrayOutputStream),
 *          structure via reflection (root interface, throws IOException).
 * @run main FlushableTest
 */

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

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

    // ---- structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Flushable");
        check(c.isInterface(), "is an interface");
        check(c.getInterfaces().length == 0, "a root interface (extends nothing)");
        java.lang.reflect.Method m = c.getMethod("flush");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "flush() abstract");
        check(m.getExceptionTypes().length == 1 && m.getExceptionTypes()[0] == IOException.class,
              "flush() declares throws IOException");
        check(Flushable.class.isAssignableFrom(java.io.OutputStream.class),
              "OutputStream implements Flushable");
        check(Flushable.class.isAssignableFrom(java.io.Writer.class),
              "Writer implements Flushable");
        check(!(Flushable.class.isAssignableFrom(java.io.Closeable.class)),
              "Flushable is unrelated to Closeable");
    }

    /** Implements both interfaces with independent counters. */
    static class Sink implements Closeable, Flushable {
        int flushes, closes;
        public void flush() { flushes++; }
        public void close() { closes++; }
    }

    // ---- try-with-resources closes but never flushes ----
    static void testTwrDoesNotFlush() throws Exception {
        Sink s = new Sink();
        try (Sink r = s) {
            r.flush();                                  // only the explicit flush counts
        }
        check(s.closes == 1, "try-with-resources closed the resource");
        check(s.flushes == 1, "no implicit flush happened: only the explicit call");
        // with no explicit flush at all, close is still the only callback
        Sink plain = new Sink();
        try (Sink r = plain) { }
        check(plain.closes == 1 && plain.flushes == 0,
              "closing never triggers flush for a Flushable implementor");
    }

    /** Char sink that records when characters actually arrive. */
    static class CharRecorder extends Writer {
        final StringBuilder seen = new StringBuilder();
        @Override public void write(char[] c, int off, int len) { seen.append(c, off, len); }
        @Override public void flush() { }
        @Override public void close() { }
    }

    // ---- the same contract on the char side: BufferedWriter holds back until flush ----
    static void testBufferedWriterFlush() throws Exception {
        CharRecorder rec = new CharRecorder();
        BufferedWriter bw = new BufferedWriter(rec, 8192);
        bw.write("hold");
        check(rec.seen.length() == 0, "chars still buffered, not in the sink");
        Flushable f = bw;
        f.flush();
        check("hold".contentEquals(rec.seen), "flush pushed the buffered chars to the sink");
        // flush is repeatable
        f.flush();
        check("hold".contentEquals(rec.seen), "a second flush adds nothing");
        bw.close();
    }

    // ---- flush after close on a buffering implementor ----
    static void testFlushAfterClose() throws Exception {
        BufferedWriter bw = new BufferedWriter(new StringWriter());
        bw.close();
        boolean threw = false;
        try {
            ((Flushable) bw).flush();
        } catch (IOException e) {
            threw = true;
            check("Stream closed".equals(e.getMessage()),
                  "flush after close: \"Stream closed\" (verified on JDK 8 and 27)");
        }
        check(threw, "flush on a closed BufferedWriter must throw");
    }

    // ---- documented no-op implementors are safe through the interface ----
    static void testNoopImplementors() throws Exception {
        Flushable sw = new StringWriter();
        sw.flush();
        sw.flush();
        passed++;
        Flushable caw = new java.io.CharArrayWriter();
        caw.flush();
        caw.flush();
        passed++;
        ((StringWriter) sw).write("x");                 // still fully usable
        check("x".equals(sw.toString()), "StringWriter usable after interface flushes");
    }

    // ---- a failing flush propagates through a Writer reference too ----
    static void testFlushFailureThroughWriterRef() throws Exception {
        Writer w = new Writer() {
            public void write(char[] c, int o, int l) { }
            public void flush() throws IOException { throw new IOException("flush-boom"); }
            public void close() { }
        };
        Flushable f = w;
        boolean threw = false;
        try {
            f.flush();
        } catch (IOException e) {
            threw = true;
            check("flush-boom".equals(e.getMessage()), "original message kept");
        }
        check(threw, "a failing flush must propagate through the interface");
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testWhiteBox();
        testTwrDoesNotFlush();
        testBufferedWriterFlush();
        testFlushAfterClose();
        testNoopImplementors();
        testFlushFailureThroughWriterRef();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

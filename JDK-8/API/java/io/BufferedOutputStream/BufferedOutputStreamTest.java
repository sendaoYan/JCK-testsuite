/*
 * @test
 * @summary java.io.BufferedOutputStream API conformance: full contract coverage.
 *          Constructors (size<=0->IAE), write(int) low 8 bits + buffering, write(byte[],off,len)
 *          validation (NPE/IOBE) + direct-write path for large arrays, flush semantics,
 *          close flushes remaining bytes.
 *          Deepened (contract level): exact auto-flush boundary (8 writes of an 8-buffer
 *          stay buffered; the 9th flushes 8), array paths (len == buffer -> direct bulk
 *          write; len < buffer -> buffered; mixed flush+direct), default buffer size
 *          8192 observable, flush delegates exactly once, close = flush + close
 *          (measured non-idempotent on JDK-8), write-after-close via permissive and
 *          rejecting sinks, "Buffer size <= 0" message, arraycopy OOB type.
 * @run main BufferedOutputStreamTest
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class BufferedOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- 1. constructors ----
    static void testConstructors() throws Exception {
        BufferedOutputStream ok = new BufferedOutputStream(new ByteArrayOutputStream());
        ok.write(1);
        ok.flush();

        checkThrows(IllegalArgumentException.class,
                    () -> new BufferedOutputStream(new ByteArrayOutputStream(), 0),
                    "size 0 -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class,
                    () -> new BufferedOutputStream(new ByteArrayOutputStream(), -1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. write(int) + buffering ----
    static void testWriteIntAndBuffering() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(bos, 8);
        out.write(65);
        out.write(0x1FF);               // low 8 bits
        check(bos.size() == 0, "small writes stay buffered before flush");
        out.flush();
        byte[] b = bos.toByteArray();
        check(b.length == 2 && b[0] == 65 && (b[1] & 0xFF) == 0xFF, "flush writes buffered low 8 bits");
    }

    // ---- 3. write(byte[],off,len) validation ----
    static void testWriteArrayValidation() {
        final BufferedOutputStream out = new BufferedOutputStream(new ByteArrayOutputStream(), 4);
        checkThrows(NullPointerException.class, () -> out.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], -1, 1),
                    "write(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 0, 3),
                    "write(b,0,3) -> IndexOutOfBoundsException");
    }

    // ---- 4. large write goes direct; content preserved ----
    static void testLargeWrite() throws Exception {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(bos, 8);
        out.write(data, 0, data.length);    // >= buffer size -> direct write
        check(bos.size() == data.length, "large write bypasses buffer");
        check(Arrays.equals(bos.toByteArray(), data), "large write content");
    }

    // ---- 5. flush + close ----
    static void testFlushClose() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(bos, 8);
        out.write(new byte[]{1, 2, 3}, 0, 3);
        check(bos.size() == 0, "buffered before flush");
        out.flush();
        check(bos.size() == 3, "flush empties buffer");

        out.write(new byte[]{4, 5}, 0, 2);
        out.close();                        // close flushes remaining
        check(bos.size() == 5, "close flushes remaining bytes");
        check(Arrays.equals(bos.toByteArray(), new byte[]{1, 2, 3, 4, 5}), "final content");
    }

    /** Sink that records every call reaching it. */
    static class RecordingOut extends OutputStream {
        int singleWrites, bulkWrites, flushes, closes;
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        public void write(int b) { singleWrites++; data.write(b); }
        public void write(byte[] b, int off, int len) { bulkWrites++; data.write(b, off, len); }
        public void flush() { flushes++; }
        public void close() { closes++; }
    }

    // ---- 6. exact auto-flush boundary for write(int) (measured both JDKs) ----
    static void testExactFlushBoundary() throws Exception {
        RecordingOut sink = new RecordingOut();
        BufferedOutputStream out = new BufferedOutputStream(sink, 8);
        for (int i = 0; i < 8; i++) out.write(i);
        check(sink.data.size() == 0, "8 writes fill an 8-buffer exactly: still buffered");
        out.write(8);                    // the 9th write flushes the full buffer first
        check(sink.data.size() == 8, "9th write flushes the 8 buffered bytes to the sink");
        check(Arrays.equals(sink.data.toByteArray(), new byte[]{0, 1, 2, 3, 4, 5, 6, 7}),
              "flushed prefix content and order");
        check(sink.singleWrites == 0, "flushBuffer uses one bulk write, not per-byte writes");
        check(sink.bulkWrites == 1, "exactly one bulk write reached the sink");
        out.flush();
        check(sink.data.size() == 9 && sink.data.toByteArray()[8] == 8, "final byte flushed");
    }

    // ---- 7. write(byte[],off,len) buffer paths (measured both JDKs) ----
    static void testArrayWritePaths() throws Exception {
        // len == buffer size: existing buffered bytes flushed + direct bulk write
        RecordingOut direct = new RecordingOut();
        BufferedOutputStream d = new BufferedOutputStream(direct, 8);
        d.write(new byte[8], 0, 8);
        check(direct.data.size() == 8, "write(byte[8]) with 8-buffer reaches the sink immediately");
        check(direct.bulkWrites == 1 && direct.singleWrites == 0,
              "large array write goes directly via one bulk write");

        // len < buffer size: stays buffered
        RecordingOut small = new RecordingOut();
        BufferedOutputStream s = new BufferedOutputStream(small, 8);
        s.write(new byte[7], 0, 7);
        check(small.data.size() == 0, "write(byte[7]) stays buffered");

        // 3 buffered + a 10-byte array: flush 3 (bulk) + direct 10 (bulk) = 13
        RecordingOut mixed = new RecordingOut();
        BufferedOutputStream m = new BufferedOutputStream(mixed, 8);
        m.write(new byte[3], 0, 3);
        m.write(new byte[10], 0, 10);
        check(mixed.data.size() == 13, "mixed path flushes 3 then writes 10 directly");
        check(mixed.bulkWrites == 2, "mixed path is two bulk writes (flush + direct)");

        // len == 0: no delegate calls at all
        RecordingOut zero = new RecordingOut();
        new BufferedOutputStream(zero, 4).write(new byte[3], 1, 0);
        check(zero.data.size() == 0, "write(b,off,0) writes nothing");
    }

    // ---- 8. default buffer size is 8192 (observable via the flush boundary) ----
    static void testDefaultBufferSize() throws Exception {
        RecordingOut sink = new RecordingOut();
        BufferedOutputStream out = new BufferedOutputStream(sink);
        for (int i = 0; i < 8192; i++) out.write(1);
        check(sink.data.size() == 0, "8192 writes fill the default buffer exactly");
        out.write(1);
        check(sink.data.size() == 8192, "the 8193rd write flushes 8192 bytes (default size)");
    }

    // ---- 9. flush delegates exactly once, even with an empty buffer ----
    static void testFlushDelegation() throws Exception {
        RecordingOut sink = new RecordingOut();
        BufferedOutputStream out = new BufferedOutputStream(sink, 4);
        out.flush();
        check(sink.flushes == 1, "flush delegates to the sink exactly once");
        check(sink.data.size() == 0, "flush with empty buffer writes no data");
        out.flush();
        check(sink.flushes == 2, "second flush delegates again");
    }

    // ---- 10. close = flush buffered bytes, then close the sink ----
    static void testCloseDelegation() throws Exception {
        RecordingOut sink = new RecordingOut();
        BufferedOutputStream out = new BufferedOutputStream(sink, 8);
        out.write(42);
        out.close();
        check(sink.flushes == 1 && sink.closes == 1, "close flushes once then closes once");
        check(sink.data.size() == 1 && sink.data.toByteArray()[0] == 42,
              "close flushes the buffered byte to the sink");

        // JDK-8 FilterOutputStream.close is NOT idempotent: a second close re-flushes
        // and re-closes the sink (JDK-27 made it idempotent; measured difference)
        out.close();
        check(sink.closes == 2, "second close reaches the sink again (JDK-8 non-idempotent)");
        check(sink.flushes == 2, "second close flushes again (JDK-8)");
    }

    // ---- 11. write after close: not guarded by the buffer layer itself ----
    static void testWriteAfterClose() throws Exception {
        // permissive sink (BAOS close is a no-op): writes still reach the sink
        ByteArrayOutputStream permissive = new ByteArrayOutputStream();
        BufferedOutputStream a = new BufferedOutputStream(permissive, 4);
        a.write(1);
        a.close();
        a.write(2);
        a.flush();
        check(permissive.size() == 2, "write after close reaches a permissive sink");

        // rejecting sink: the buffered byte is flushed by close, a post-close write
        // stays buffered, and the next flush surfaces the sink's IOException
        final OutputStream rejecting = new OutputStream() {
            boolean closed;
            public void write(int b) throws IOException {
                if (closed) throw new IOException("Stream closed");
            }
            public void close() { closed = true; }
        };
        BufferedOutputStream b = new BufferedOutputStream(rejecting, 4);
        b.write(1);
        b.close();                       // flush(1) succeeds, then sink.close()
        b.write(2);                      // buffered silently
        checkThrows(IOException.class, b::flush, "flush after close surfaces sink IOException");
    }

    // ---- 12. constructor message (measured: "Buffer size <= 0") ----
    static void testConstructorMessage() {
        checkThrowsMsg(IllegalArgumentException.class, "Buffer size <= 0",
                       () -> new BufferedOutputStream(new ByteArrayOutputStream(), 0),
                       "size 0 message");
        checkThrowsMsg(IllegalArgumentException.class, "Buffer size <= 0",
                       () -> new BufferedOutputStream(new ByteArrayOutputStream(), -3),
                       "negative size message");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, String sub, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (String.valueOf(t.getMessage()).contains(sub)) { passed++; return; }
                throw new AssertionError(msg + ": message <" + t.getMessage()
                        + "> lacks substring <" + sub + ">");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- 13. OOB write surfaces as ArrayIndexOutOfBoundsException (measured both JDKs,
    //          thrown by System.arraycopy inside the buffer layer) ----
    static void testWriteOutOfBounds() {
        final BufferedOutputStream out = new BufferedOutputStream(new ByteArrayOutputStream(), 4);
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 0, -1),
                    "write(b,0,-1) -> ArrayIndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 3, 1),
                    "write(b,3,1) -> ArrayIndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], -1, 1),
                    "write(b,-1,1) -> ArrayIndexOutOfBoundsException");
        checkThrows(NullPointerException.class, () -> out.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
    }

    // ---- 14. content fidelity across many flush cycles ----
    static void testContentAcrossFlushes() throws Exception {
        byte[] data = new byte[10000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 7);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(sink, 64);
        for (int i = 0; i < data.length; i++) out.write(data[i]);
        out.flush();
        check(sink.size() == data.length, "10KB through a 64-buffer: size");
        check(Arrays.equals(sink.toByteArray(), data), "10KB content across flush cycles");
    }

    // ---- 15. partial-fit array path (measured on both JDKs):
    //          5 buffered + a 4-byte array with an 8-buffer flushes the 5 (bulk),
    //          then buffers the 4 ----
    static void testPartialFitArrayPath() throws Exception {
        RecordingOut sink = new RecordingOut();
        BufferedOutputStream out = new BufferedOutputStream(sink, 8);
        out.write(new byte[5], 0, 5);
        out.write(new byte[4], 0, 4);
        check(sink.data.size() == 5, "4-byte array that does not fit flushes the buffered 5");
        check(sink.bulkWrites == 1 && sink.singleWrites == 0, "flush of buffered bytes is one bulk write");
        out.flush();
        check(sink.data.size() == 9, "final flush writes the remaining 4");
        check(sink.bulkWrites == 2, "second bulk write carries the 4 buffered bytes");
    }

    // ---- 16. close order on the sink: data write (via flush), flush, close ----
    static void testCloseOrder() throws Exception {
        final java.util.ArrayList<String> order = new java.util.ArrayList<String>();
        class OrderedSink extends OutputStream {
            final ByteArrayOutputStream data = new ByteArrayOutputStream();
            public void write(int b) { data.write(b); order.add("w1"); }
            public void write(byte[] b, int off, int len) { data.write(b, off, len); order.add("W" + len); }
            public void flush() { order.add("F"); }
            public void close() { order.add("C"); }
        }
        OrderedSink sink = new OrderedSink();
        BufferedOutputStream out = new BufferedOutputStream(sink, 8);
        out.write(9);
        out.close();
        check(order.equals(java.util.Arrays.asList("W1", "F", "C")),
              "close produces W1 (flushBuffer), F (flush), C (close) in order");
        check(sink.data.size() == 1, "buffered byte flushed during close");
    }

    // ---- 17. exact sub-region of write(byte[],off,len) ----
    static void testWriteRegion() throws Exception {
        byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(sink, 8);
        out.write(data, 2, 5);           // region d[2..6] buffered, then flushed
        out.flush();
        check(Arrays.equals(sink.toByteArray(), new byte[]{3, 4, 5, 6, 7}),
              "write(b,off,len) writes exactly the requested region");
    }

    // ---- 18. inherited one-arg write(byte[]) ----
    static void testOneArgWriteInherited() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(sink, 4);
        out.write(new byte[]{1, 2, 3});          // buffered (len < size)
        check(sink.size() == 0, "write(byte[]) respects buffering");
        out.write(new byte[]{4, 5, 6, 7});       // len >= size: flush the 3, then direct 4
        check(sink.size() == 7, "write(byte[]) of len >= size flushes buffered bytes and goes direct");
        out.flush();
        check(Arrays.equals(sink.toByteArray(), new byte[]{1, 2, 3, 4, 5, 6, 7}),
              "write(byte[]) content order preserved");
        checkThrows(NullPointerException.class, () -> out.write((byte[]) null),
                    "write((byte[]) null) -> NPE");
    }

    // ---- 19. constructor surface: only (OutputStream) and (OutputStream,int) exist ----
    static void testConstructorSurface() throws Exception {
        java.lang.reflect.Constructor<?>[] ctors =
                BufferedOutputStream.class.getConstructors();
        check(ctors.length == 2, "exactly two public constructors (no no-arg variant)");
        boolean hasNoArg = false;
        for (java.lang.reflect.Constructor<?> c : ctors)
            if (c.getParameterTypes().length == 0) hasNoArg = true;
        check(!hasNoArg, "no public no-arg constructor (javap-confirmed on JDK-8 and JDK-27)");
    }

    // ---- 20. write(int) truncation values survive the buffer round trip ----
    static void testWriteIntTruncation() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(sink, 8);
        out.write(-1);                   // -> 0xFF
        out.write(-256);                 // -> 0x00
        out.write(0x100);                // -> 0x00
        out.write(0x141);                // -> 0x41
        out.flush();
        byte[] b = sink.toByteArray();
        check(b.length == 4, "truncation write count");
        check((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0x00 && (b[2] & 0xFF) == 0x00,
              "negative and >=256 values truncated to low 8 bits");
        check((b[3] & 0xFF) == 0x41, "0x141 truncated to 0x41");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testWriteIntAndBuffering();
        testWriteArrayValidation();
        testLargeWrite();
        testFlushClose();
        testExactFlushBoundary();
        testArrayWritePaths();
        testDefaultBufferSize();
        testFlushDelegation();
        testCloseDelegation();
        testWriteAfterClose();
        testConstructorMessage();
        testWriteOutOfBounds();
        testContentAcrossFlushes();
        testPartialFitArrayPath();
        testCloseOrder();
        testWriteRegion();
        testOneArgWriteInherited();
        testConstructorSurface();
        testWriteIntTruncation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

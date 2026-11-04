/*
 * @test
 * @summary java.io.FilterOutputStream API conformance: full contract coverage.
 *          Decorator base with protected out. write(int)/write(byte[])/
 *          write(byte[],off,len) delegate to the sink; close implies flush then close;
 *          flush delegates; null/bounds checking; subclass interception; protected out
 *          access; a failing sink surfaces; null delegate rejected on first write.
 *          Deepened (contract level): write(byte[],off,len) loops write(int) per byte
 *          (partial delivery before a mid-loop failure), one-arg write(byte[]) routes
 *          through the own three-arg write (subclass bulk override wins), exact close
 *          delegation (close = flush + close; measured NON-idempotent on JDK-8: a
 *          second close re-flushes and re-closes), two-layer close propagation,
 *          flush-failure during close still closes the sink, both-fail close keeps the
 *          flush exception primary with the close exception suppressed, write after
 *          close reaches a permissive sink, failing sink propagation for every
 *          operation, null delegate for every operation, write(int) truncation,
 *          protected out reassignment mid-stream.
 * @run main FilterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilterOutputStreamTest {
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

    /** Underlying sink that records calls. */
    static class Recording extends OutputStream {
        int singleWrites, bulkWrites, flushes, closes;
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        public void write(int b) { singleWrites++; data.write(b); }
        public void write(byte[] b, int off, int len) { bulkWrites++; data.write(b, off, len); }
        public void flush() { flushes++; }
        public void close() { closes++; }
    }

    // ---- 1. write(int) delegates ----
    static void testSingleWrite() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(65);
        check(rec.singleWrites == 1 && rec.data.size() == 1, "write(int) delegates");
    }

    // ---- 2. write(byte[]) and write(byte[],off,len) loop through write(int) ----
    static void testBulkWrite() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(new byte[]{66, 67});
        fos.write(new byte[]{88, 89, 90}, 1, 1);
        check(java.util.Arrays.equals(rec.data.toByteArray(),
                                      new byte[]{'B', 'C', 'Y'}),
              "bulk writes preserve region via write(int): " + rec.data);
        check(rec.singleWrites == 3 && rec.bulkWrites == 0,
              "FilterOutputStream.write(byte[],off,len) loops write(int)");
    }

    // ---- 3. flush delegates ----
    static void testFlush() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.flush();
        check(rec.flushes >= 1, "flush delegates");
    }

    // ---- 4. close implies flush then close ----
    static void testClose() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.close();
        check(rec.closes >= 1 && rec.flushes >= 1, "close flushes then closes");
    }

    // ---- 5. argument checking ----
    static void testArgumentChecking() throws Exception {
        final FilterOutputStream fos = new FilterOutputStream(new Recording());
        checkThrows(NullPointerException.class, () -> fos.write((byte[]) null),
                    "write((byte[])null) -> NPE");
        checkThrows(NullPointerException.class, () -> fos.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 2, 1),
                    "write(b,2,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], -1, 1),
                    "write(b,-1,1) -> IndexOutOfBoundsException");
    }

    // ---- 6. subclass interception ----
    static void testSubclassInterception() throws Exception {
        class Upper extends FilterOutputStream {
            Upper(OutputStream o) { super(o); }
            @Override public void write(int b) throws IOException {
                super.write(Character.toUpperCase(b));
            }
        }
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        Upper u = new Upper(dest);
        u.write('a');
        u.write('b');
        check("AB".equals(dest.toString()), "subclass intercepted every byte");
    }

    // ---- 7. protected out access ----
    static void testProtectedOut() throws Exception {
        class Reaching extends FilterOutputStream {
            Reaching(OutputStream o) { super(o); }
            OutputStream inner() { return out; }
        }
        ByteArrayOutputStream inner = new ByteArrayOutputStream();
        check(new Reaching(inner).inner() == inner, "protected out is the wrapped stream");
    }

    // ---- 8. failing sink surfaces ----
    static void testFailingSink() throws Exception {
        class Failing extends OutputStream {
            public void write(int b) throws IOException { throw new IOException("sink broken"); }
        }
        FilterOutputStream bad = new FilterOutputStream(new Failing());
        checkThrows(IOException.class, () -> bad.write(1), "sink failure propagates");
    }

    // ---- 9. null delegate rejected on first write ----
    static void testNullDelegate() throws Exception {
        FilterOutputStream nul = new FilterOutputStream(null);
        checkThrows(NullPointerException.class, () -> nul.write(1),
                    "null delegate -> NPE on first write");
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

    // ---- 10. bulk write loop details (measured both JDKs) ----
    static void testBulkWriteLoopDetails() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(new byte[0]);
        check(rec.singleWrites == 0 && rec.bulkWrites == 0, "write(byte[0]) performs no sink calls");

        fos.write(new byte[]{1, 2, 3});
        check(rec.singleWrites == 3 && rec.bulkWrites == 0,
              "write(byte[3]) loops write(int) three times (no bulk call on the sink)");

        fos.write(new byte[]{9, 8, 7, 6, 5}, 1, 2);
        check(rec.singleWrites == 5, "region write adds exactly len write(int) calls");
        byte[] all = rec.data.toByteArray();
        check(all.length == 5 && all[3] == 8 && all[4] == 7,
              "region bytes appended in order after the previous writes");
    }

    // ---- 11. one-arg write(byte[]) routes through the OWN three-arg write ----
    static void testOneArgRoutesOwnThreeArg() throws Exception {
        class BulkSink extends OutputStream {
            int singles, bulks;
            public void write(int b) { singles++; }
            public void write(byte[] b, int off, int len) { bulks++; }
        }
        final BulkSink sink = new BulkSink();
        class Efficient extends FilterOutputStream {
            Efficient(OutputStream o) { super(o); }
            @Override public void write(byte[] b, int off, int len) throws IOException {
                out.write(b, off, len);      // a real bulk implementation
            }
        }
        Efficient fos = new Efficient(sink);
        fos.write(new byte[]{1, 2, 3});
        check(sink.bulks == 1 && sink.singles == 0,
              "one-arg write(byte[]) dispatches to the overridden 3-arg write");
    }

    // ---- 12. OOB/null argument behavior (JDK-8: bare IOOBE/NPE with null message) ----
    static void testArgumentBehaviorJdk8() {
        final FilterOutputStream fos = new FilterOutputStream(new Recording());
        checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 3, 1),
                    "write(b,3,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 0, -1),
                    "write(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 1, 2),
                    "write(b,1,2) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> fos.write(new byte[2], 3, 0),
                    "write(b,3,0): off beyond length with len 0 still rejected");
        checkThrows(NullPointerException.class, () -> fos.write((byte[]) null, 0, 1),
                    "write(null,0,1) -> NPE");
    }

    // ---- 13. len==0 with off at the length boundary is legal ----
    static void testZeroLenBoundary() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(new byte[2], 2, 0);
        check(rec.singleWrites == 0, "write(b, b.length, 0) legal and writes nothing");
    }

    // ---- 14. exact close delegation (JDK-8 measured: non-idempotent) ----
    static void testCloseDelegationExact() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.close();
        check(rec.flushes == 1 && rec.closes == 1, "close = exactly one flush + one close");

        fos.close();
        check(rec.flushes == 2 && rec.closes == 2,
              "second close re-flushes and re-closes the sink (JDK-8, no closed flag)");
    }

    // ---- 15. close propagation through two filter layers ----
    static void testTwoLayerClose() throws Exception {
        Recording deep = new Recording();
        FilterOutputStream inner = new FilterOutputStream(deep);
        FilterOutputStream outer = new FilterOutputStream(inner);
        outer.close();
        check(deep.flushes == 2, "two-layer close flushes the sink twice (inner close re-flushes)");
        check(deep.closes == 1, "the sink is closed exactly once per outer close");

        outer.close();
        check(deep.flushes == 4 && deep.closes == 2,
              "second outer close repeats the whole chain (JDK-8)");
    }

    // ---- 16. flush failure during close still closes the sink ----
    static void testFlushFailDuringClose() throws Exception {
        class FlushFail extends OutputStream {
            int flushes, closes;
            public void write(int b) { }
            public void flush() throws IOException {
                if (++flushes == 2) throw new IOException("boom flush");
            }
            public void close() { closes++; }
        }
        final FlushFail sink = new FlushFail();
        FilterOutputStream fos = new FilterOutputStream(sink);
        fos.flush();
        checkThrowsMsg(IOException.class, "boom flush", fos::close, "close propagates the flush failure");
        check(sink.closes == 1, "sink is still closed after the flush failure");
        check(sink.flushes == 2, "the failing flush was the one issued by close");
    }

    // ---- 17. both flush and close fail during close (JDK-8: flush primary, close suppressed) ----
    static void testBothFailDuringClose() throws Exception {
        class BothFail extends OutputStream {
            public void write(int b) { }
            public void flush() throws IOException { throw new IOException("F"); }
            public void close() throws IOException { throw new IOException("C"); }
        }
        final BothFail sink = new BothFail();
        FilterOutputStream fos = new FilterOutputStream(sink);
        try {
            fos.close();
            check(false, "close must throw when both flush and close fail");
        } catch (IOException ioe) {
            check("F".equals(ioe.getMessage()),
                  "JDK-8: the flush exception is the primary one (try-with-resources body)");
            check(ioe.getSuppressed().length == 1 && "C".equals(ioe.getSuppressed()[0].getMessage()),
                  "the close exception is attached as suppressed");
        }
    }

    // ---- 18. write after close reaches a permissive sink (no guard in the base) ----
    static void testWriteAfterClosePermissive() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(1);
        fos.close();
        fos.write(2);
        fos.flush();
        check(rec.singleWrites == 2, "write after close still reaches a permissive sink");
        check(rec.flushes == 2, "flush after close still delegates (JDK-8 has no closed flag)");
    }

    // ---- 19. mid-loop failure delivers the earlier bytes ----
    static void testPartialWriteFailure() throws Exception {
        class MidFail extends OutputStream {
            int writes;
            final ByteArrayOutputStream data = new ByteArrayOutputStream();
            public void write(int b) throws IOException {
                if (++writes == 3) throw new IOException("mid boom");
                data.write(b);
            }
        }
        final MidFail sink = new MidFail();
        final FilterOutputStream fos = new FilterOutputStream(sink);
        checkThrowsMsg(IOException.class, "mid boom",
                       () -> fos.write(new byte[]{10, 20, 30, 40}), "mid-loop failure propagates");
        check(sink.writes == 3, "three write(int) calls reached the sink");
        check(java.util.Arrays.equals(sink.data.toByteArray(), new byte[]{10, 20}),
              "the two bytes before the failure were already delivered");
    }

    // ---- 20. every operation propagates a failing sink with its message ----
    static void testFailingSinkEveryOp() throws Exception {
        class FailAll extends OutputStream {
            public void write(int b) throws IOException { throw new IOException("sink broken"); }
            public void write(byte[] b, int o, int l) throws IOException { throw new IOException("sink broken"); }
            public void flush() throws IOException { throw new IOException("sink broken"); }
            public void close() throws IOException { throw new IOException("sink broken"); }
        }
        final FilterOutputStream fos = new FilterOutputStream(new FailAll());
        checkThrowsMsg(IOException.class, "sink broken", () -> fos.write(1), "write(int) propagates");
        checkThrowsMsg(IOException.class, "sink broken", () -> fos.write(new byte[1]),
                       "write(byte[]) propagates");
        checkThrowsMsg(IOException.class, "sink broken", () -> fos.write(new byte[1], 0, 1),
                       "write(b,off,len) propagates");
        checkThrowsMsg(IOException.class, "sink broken", fos::flush, "flush propagates");
        checkThrowsMsg(IOException.class, "sink broken", fos::close, "close propagates");
    }

    // ---- 21. null delegate rejected by every operation (JDK-8: message is null) ----
    static void testNullDelegateAllOps() {
        final FilterOutputStream fos = new FilterOutputStream(null);
        checkThrows(NullPointerException.class, () -> fos.write(1), "write(int) on null delegate");
        checkThrows(NullPointerException.class, () -> fos.write(new byte[1]),
                    "write(byte[]) on null delegate");
        checkThrows(NullPointerException.class, () -> fos.write(new byte[1], 0, 1),
                    "write(b,off,len) on null delegate");
        checkThrows(NullPointerException.class, fos::flush, "flush on null delegate");
        checkThrows(NullPointerException.class, fos::close, "close on null delegate");
    }

    // ---- 22. write(int) truncation to the low 8 bits ----
    static void testWriteIntTruncation() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(-1);
        fos.write(-256);
        fos.write(0x100);
        fos.write(0x141);
        byte[] b = rec.data.toByteArray();
        check(b.length == 4, "truncation write count");
        check((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0x00, "-1 -> 0xFF, -256 -> 0x00");
        check((b[2] & 0xFF) == 0x00 && (b[3] & 0xFF) == 0x41, "0x100 -> 0x00, 0x141 -> 0x41");
    }

    // ---- 23. flush delegates on every call, writes no data ----
    static void testFlushDelegationCount() throws Exception {
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.flush();
        fos.flush();
        check(rec.flushes == 2, "each flush() delegates exactly once");
        check(rec.data.size() == 0, "flush itself writes no data");
        check(rec.singleWrites == 0 && rec.bulkWrites == 0, "flush performs no writes on the sink");
    }

    // ---- 24. second close after a failed close (JDK-8: repeats the chain) ----
    static void testCloseAfterFailedClose() throws Exception {
        class FlakyFlush extends OutputStream {
            int flushes, closes;
            public void write(int b) { }
            public void flush() throws IOException {
                if (++flushes == 2) throw new IOException("one-time failure");
            }
            public void close() { closes++; }
        }
        final FlakyFlush sink = new FlakyFlush();
        FilterOutputStream fos = new FilterOutputStream(sink);
        fos.flush();
        checkThrows(IOException.class, fos::close, "first close fails at flush");
        fos.close();        // JDK-8: no closed flag, the chain runs again and now succeeds
        check(sink.flushes == 3 && sink.closes == 2,
              "close after a failed close retries flush and close (JDK-8)");
    }

    // ---- 25. protected out reassignment mid-stream ----
    static void testOutReassignment() throws Exception {
        class Reassigning extends FilterOutputStream {
            Reassigning(OutputStream o) { super(o); }
            void swap(OutputStream next) { out = next; }
        }
        ByteArrayOutputStream first = new ByteArrayOutputStream();
        Reassigning fos = new Reassigning(first);
        fos.write(1);
        ByteArrayOutputStream second = new ByteArrayOutputStream();
        fos.swap(second);
        fos.write(2);
        check(first.size() == 1 && first.toByteArray()[0] == 1, "pre-swap byte went to the first sink");
        check(second.size() == 1 && second.toByteArray()[0] == 2, "post-swap byte went to the new sink");
    }

    // ---- 26. content fidelity of a long per-byte loop ----
    static void testContentFidelityLoop() throws Exception {
        byte[] data = new byte[500];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 31);
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(data, 0, data.length);
        check(rec.singleWrites == data.length, "per-byte loop count");
        check(rec.bulkWrites == 0, "no bulk call despite a large array");
        check(java.util.Arrays.equals(rec.data.toByteArray(), data), "content preserved byte for byte");
    }

    public static void main(String[] args) throws Exception {
        testSingleWrite();
        testBulkWrite();
        testFlush();
        testClose();
        testArgumentChecking();
        testSubclassInterception();
        testProtectedOut();
        testFailingSink();
        testNullDelegate();
        testBulkWriteLoopDetails();
        testOneArgRoutesOwnThreeArg();
        testArgumentBehaviorJdk8();
        testZeroLenBoundary();
        testCloseDelegationExact();
        testTwoLayerClose();
        testFlushFailDuringClose();
        testBothFailDuringClose();
        testWriteAfterClosePermissive();
        testPartialWriteFailure();
        testFailingSinkEveryOp();
        testNullDelegateAllOps();
        testWriteIntTruncation();
        testFlushDelegationCount();
        testCloseAfterFailedClose();
        testOutReassignment();
        testContentFidelityLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

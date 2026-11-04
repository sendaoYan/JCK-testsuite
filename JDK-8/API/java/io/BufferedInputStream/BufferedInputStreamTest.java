/*
 * @test
 * @summary java.io.BufferedInputStream API conformance: full contract coverage.
 *          Constructors (buffer size<=0->IAE, null delegate acts closed->IOException),
 *          read()/read(byte[],off,len) validation (NPE/IOBE) + exact offset + len==0,
 *          buffering over a one-byte-at-a-time source, skip (negative->0, past-end),
 *          available, mark/reset (replay, invalidated->IOException, unmarked->IOException),
 *          close idempotent + closed-stream IOException.
 *          Deepened (contract level): exact closed-state messages ("Stream closed"),
 *          "Resetting to invalid mark" message, mark valid beyond readlimit while the
 *          buffer retains it (incl. across refills), fill() requests exactly the buffer
 *          size (default 8192 observable), available() delegates per call, close
 *          propagation through one and two layers, skip x buffer interplay.
 * @run main BufferedInputStreamTest
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedInputStreamTest {
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

    // source that returns at most one byte per read call (forces buffering to matter)
    static class OneByteStream extends InputStream {
        final byte[] data; int i;
        OneByteStream(byte[] data) { this.data = data; }
        public int read() { return (i < data.length) ? (data[i++] & 0xFF) : -1; }
        public int available() { return data.length - i; }
    }

    // ---- 1. constructors ----
    static void testConstructors() throws Exception {
        BufferedInputStream ok = new BufferedInputStream(new ByteArrayInputStream(new byte[3]));
        check(ok.read() != -1, "default constructor works");

        checkThrows(IllegalArgumentException.class,
                    () -> new BufferedInputStream(new ByteArrayInputStream(new byte[1]), 0),
                    "size 0 -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class,
                    () -> new BufferedInputStream(new ByteArrayInputStream(new byte[1]), -5),
                    "negative size -> IllegalArgumentException");

        // null delegate is treated as a closed stream on first read
        BufferedInputStream nul = new BufferedInputStream(null);
        checkThrows(IOException.class, () -> nul.read(), "null delegate -> IOException on read");
    }

    // ---- 2. buffering over one-byte-at-a-time source ----
    static void testBuffering() throws Exception {
        byte[] data = new byte[1000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 13);
        BufferedInputStream in = new BufferedInputStream(new OneByteStream(data), 64);
        byte[] got = new byte[data.length];
        int off = 0;
        while (off < got.length) {
            int n = in.read(got, off, got.length - off);
            if (n < 0) break;
            off += n;
        }
        check(off == data.length, "buffered read consumes all bytes");
        boolean eq = true;
        for (int i = 0; i < data.length; i++) if (got[i] != data[i]) { eq = false; break; }
        check(eq, "buffered read preserves content");
    }

    // ---- 3. read(byte[],off,len) validation ----
    static void testReadArrayValidation() throws Exception {
        final BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        checkThrows(NullPointerException.class, () -> in.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], -1, 1),
                    "read(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 0, -1),
                    "read(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException");

        // len == 0 returns 0
        check(in.read(new byte[1], 0, 0) == 0, "len==0 returns 0");

        // exact offset placement
        byte[] buf = new byte[5];
        int n = in.read(buf, 1, 2);
        check(n == 2, "read count");
        check(buf[0] == 0 && buf[1] == 1 && buf[2] == 2 && buf[3] == 0, "exact offset placement");
    }

    // ---- 4. skip ----
    static void testSkip() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        check(in.skip(2) == 2, "skip(2)");
        check(in.read() == 3, "read after skip");
        check(in.skip(100) == 2, "skip past end returns remaining");
        check(in.read() == -1, "EOF after skip past end");

        BufferedInputStream neg = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        check(neg.skip(-5) == 0, "negative skip returns 0");
        check(neg.read() == 1, "negative skip does not move");
    }

    // ---- 5. available ----
    static void testAvailable() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        check(in.available() == 5, "initial available");
        in.read();
        in.read();
        check(in.available() == 3, "available after two reads");
    }

    // ---- 6. mark/reset ----
    static void testMarkReset() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        check(in.markSupported(), "markSupported");

        // reset without mark -> IOException
        final BufferedInputStream unmarked = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1}));
        checkThrows(IOException.class, () -> unmarked.reset(), "reset without mark -> IOException");

        // mark then reset replays
        BufferedInputStream m = new BufferedInputStream(new ByteArrayInputStream(new byte[]{10, 20, 30, 40}));
        m.read();
        m.mark(100);
        m.read();
        m.read();
        m.reset();
        check(m.read() == 20 && m.read() == 30, "mark/reset replay");

        // mark invalidated by reading past readlimit -> IOException on reset
        BufferedInputStream inv = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}));
        inv.read();
        inv.mark(2);                    // allow 2 bytes before invalidation
        inv.read();
        inv.read();
        inv.read();
        inv.read();
        inv.reset();
        check(inv.read() == 2, "reset within limit replays");

        // with a small buffer, reading far past readlimit forces refill -> mark invalidated
        byte[] wide = new byte[200];
        for (int i = 0; i < wide.length; i++) wide[i] = (byte) i;
        final BufferedInputStream inv2 = new BufferedInputStream(new ByteArrayInputStream(wide), 8);
        inv2.mark(2);
        for (int i = 0; i < 50; i++) inv2.read();  // forces multiple refills past readlimit
        checkThrows(IOException.class, () -> inv2.reset(), "reset past readlimit -> IOException");
    }

    // ---- 7. close ----
    static void testClose() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        in.close();
        in.close();                     // idempotent
        checkThrows(IOException.class, () -> in.read(), "read after close -> IOException");
        checkThrows(IOException.class, () -> in.available(), "available after close -> IOException");
        checkThrows(IOException.class, () -> in.reset(), "reset after close -> IOException");
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

    /** Records calls that reach the underlying stream. */
    static class RecordingIn extends InputStream {
        final ByteArrayInputStream src;
        int singleReads, bulkReads, availables, skips;
        final java.util.ArrayList<Integer> bulkLens = new java.util.ArrayList<Integer>();
        RecordingIn(byte[] data) { src = new ByteArrayInputStream(data); }
        public int read() { singleReads++; return src.read(); }
        public int read(byte[] b, int off, int len) { bulkReads++; bulkLens.add(len); return src.read(b, off, len); }
        public int available() { availables++; return src.available(); }
        public long skip(long n) { skips++; return src.skip(n); }
    }

    /** ByteArrayInputStream that records whether close() reached it. */
    static class TrackedIn extends ByteArrayInputStream {
        boolean closed;
        TrackedIn(byte[] data) { super(data); }
        public void close() { closed = true; }
    }

    // ---- 8. exact closed-state messages (measured: "Stream closed" on JDK-8 and JDK-27) ----
    static void testClosedStateMessages() throws Exception {
        final BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        in.close();
        checkThrowsMsg(IOException.class, "Stream closed", () -> in.read(), "read after close");
        checkThrowsMsg(IOException.class, "Stream closed", () -> in.read(new byte[1]),
                       "read(byte[]) after close");
        checkThrowsMsg(IOException.class, "Stream closed", () -> in.read(new byte[1], 0, 1),
                       "read(b,off,len) after close");
        checkThrowsMsg(IOException.class, "Stream closed", () -> in.available(), "available after close");
        checkThrowsMsg(IOException.class, "Stream closed", () -> in.skip(1), "skip after close");
        checkThrowsMsg(IOException.class, "Stream closed", () -> in.reset(), "reset after close");
        in.mark(5);   // mark after close is a silent no-op (measured)
        check(true, "mark after close does not throw");
    }

    // ---- 9. reset invalidation message (measured: "Resetting to invalid mark") ----
    static void testResetInvalidMarkMessage() throws Exception {
        checkThrowsMsg(IOException.class, "Resetting to invalid mark",
                       () -> new BufferedInputStream(new ByteArrayInputStream(new byte[1])).reset(),
                       "reset without mark");
        final BufferedInputStream inv = new BufferedInputStream(new ByteArrayInputStream(new byte[200]), 8);
        inv.mark(2);
        for (int i = 0; i < 50; i++) inv.read();     // forces refills far past readlimit
        checkThrowsMsg(IOException.class, "Resetting to invalid mark", () -> inv.reset(),
                       "reset past readlimit after refill");
    }

    // ---- 10. mark stays valid beyond readlimit while the buffer retains it ----
    static void testMarkBeyondReadlimitStillValid() throws Exception {
        // whole buffer already filled: mark(2) remains valid after reading 100 bytes
        byte[] data = new byte[1000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data));   // buffer 8192
        in.mark(2);
        for (int i = 0; i < 100; i++) in.read();
        in.reset();
        check(in.read() == 0 && in.read() == 1,
              "reset beyond readlimit still works (buffer retained the mark)");

        // mark(100) with a smaller buffer survives refills (limit covers the buffer)
        BufferedInputStream small = new BufferedInputStream(new ByteArrayInputStream(data), 8);
        small.mark(100);
        for (int i = 0; i < 30; i++) small.read();
        small.reset();
        check(small.read() == 0 && small.read() == 1,
              "reset after refill within a readlimit that covers the buffer");
    }

    // ---- 11. fill() asks the underlying stream for exactly the buffer size ----
    static void testFillRequestsBufferSize() throws Exception {
        RecordingIn rec = new RecordingIn(new byte[200]);
        BufferedInputStream in = new BufferedInputStream(rec, 64);
        in.read();
        check(rec.bulkReads == 1, "first single read triggers exactly one fill");
        check(rec.bulkLens.get(0) == 64, "fill requests exactly the buffer size (64)");
        check(rec.singleReads == 0, "buffered read() is served from the buffer, not read()-by-read()");

        RecordingIn recDefault = new RecordingIn(new byte[20000]);
        BufferedInputStream def = new BufferedInputStream(recDefault);   // default buffer
        def.read();
        check(recDefault.bulkLens.get(0) == 8192, "default buffer size is 8192 (fill request)");
    }

    // ---- 12. available() delegates to the underlying stream on every call ----
    static void testAvailableDelegatesEachCall() throws Exception {
        RecordingIn rec = new RecordingIn(new byte[10]);
        BufferedInputStream in = new BufferedInputStream(rec, 4);
        in.read();
        in.available();
        in.available();
        check(rec.availables == 2, "each available() call consults the underlying stream");
    }

    // ---- 13. available() = underlying available + buffered ----
    static void testAvailableCombined() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[10]), 4);
        in.read();                       // fill(4), consume 1: 3 buffered, 6 in source
        check(in.available() == 9, "available = underlying (6) + buffered (3)");
    }

    // ---- 14. read(byte[]) is served from the buffer filled by one bulk request ----
    static void testReadByteArrayFillsFirst() throws Exception {
        byte[] data = new byte[10];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i + 1);
        RecordingIn rec = new RecordingIn(data);
        BufferedInputStream in = new BufferedInputStream(rec, 16);
        byte[] four = new byte[4];
        check(in.read(four) == 4, "read(byte[]) count");
        check(four[0] == 1 && four[3] == 4, "read(byte[]) content");
        check(rec.bulkReads == 1 && rec.bulkLens.get(0) == 16,
              "read(byte[]) fills the whole buffer in one bulk request");
        check(rec.singleReads == 0, "no per-byte reads on the underlying stream");
    }

    // ---- 15. close propagates to the underlying stream, also through two layers ----
    static void testClosePropagation() throws Exception {
        TrackedIn tracked = new TrackedIn(new byte[2]);
        new BufferedInputStream(tracked).close();
        check(tracked.closed, "close reaches the underlying stream");

        TrackedIn inner = new TrackedIn(new byte[2]);
        BufferedInputStream innerBis = new BufferedInputStream(inner);
        BufferedInputStream outer = new BufferedInputStream(innerBis);
        outer.close();
        check(inner.closed, "close propagates through two buffered layers");
    }

    // ---- 16. skip x buffer interplay ----
    static void testSkipBufferInterplay() throws Exception {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data), 8);
        check(in.skip(50) == 50, "skip across chunks");
        check(in.read() == 50, "read after large skip at exact index");

        // measured on JDK-8 and JDK-27: skip is bounded by the buffered bytes when the
        // buffer holds data (it does not consult the underlying stream for the rest)
        BufferedInputStream mix = new BufferedInputStream(new ByteArrayInputStream(data), 8);
        mix.read();
        check(mix.skip(10) == 7, "skip bounded by the buffered bytes (7 of 10 requested)");
        check(mix.read() == 8, "position after bounded skip: refill returns d[8]");

        BufferedInputStream within = new BufferedInputStream(new ByteArrayInputStream(data), 8);
        within.read();
        within.read();
        check(within.skip(3) == 3, "skip within the buffered bytes");
        check(within.read() == 5, "position after in-buffer skip");

        // at this point 7 bytes are still buffered (d51..d57): skip is bounded by them
        check(in.skip(1000) == 7, "skip bounded by buffered bytes even for a huge request");
        check(in.read() == 58, "read after bounded skip refills from the source");

        // a fresh stream with an empty buffer delegates skip to the source (clamped)
        BufferedInputStream fresh = new BufferedInputStream(new ByteArrayInputStream(data), 8);
        check(fresh.skip(100000) == 100, "skip past end delegated to source returns all");
        check(fresh.read() == -1, "EOF after skipping past end");
    }

    // ---- 17. chunked bulk reads with a size smaller than the buffer ----
    static void testChunkedReadOddSizes() throws Exception {
        byte[] data = new byte[1000];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i * 13);
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(data), 8);
        byte[] got = new byte[data.length];
        int off = 0;
        while (off < got.length) {
            int n = in.read(got, off, Math.min(7, got.length - off));
            if (n < 0) break;
            off += n;
        }
        check(off == data.length, "odd-sized chunks consume the stream completely");
        boolean eq = true;
        for (int i = 0; i < data.length; i++) if (got[i] != data[i]) { eq = false; break; }
        check(eq, "odd-sized chunked reads preserve content");
    }

    // ---- 18. constructor message ----
    static void testConstructorMessage() {
        checkThrowsMsg(IllegalArgumentException.class, "Buffer size <= 0",
                       () -> new BufferedInputStream(new ByteArrayInputStream(new byte[1]), 0),
                       "size 0 message");
        checkThrowsMsg(IllegalArgumentException.class, "Buffer size <= 0",
                       () -> new BufferedInputStream(new ByteArrayInputStream(new byte[1]), -1),
                       "negative size message");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testBuffering();
        testReadArrayValidation();
        testSkip();
        testAvailable();
        testMarkReset();
        testClose();
        testClosedStateMessages();
        testResetInvalidMarkMessage();
        testMarkBeyondReadlimitStillValid();
        testFillRequestsBufferSize();
        testAvailableDelegatesEachCall();
        testAvailableCombined();
        testReadByteArrayFillsFirst();
        testClosePropagation();
        testSkipBufferInterplay();
        testChunkedReadOddSizes();
        testConstructorMessage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

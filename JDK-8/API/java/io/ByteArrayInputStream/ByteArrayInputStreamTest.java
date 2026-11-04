/*
 * @test
 * @summary java.io.ByteArrayInputStream API conformance: full contract coverage.
 *          Constructors (buffer-not-copied, offset/length clamping, null->NPE),
 *          read() 0..255 range + EOF -1, read(byte[],off,len) parameter validation
 *          (NPE/IndexOutOfBoundsException) + exact-offset/partial/zero-length
 *          semantics, skip (negative->0, past-end), available, mark/reset
 *          (replay, unmarked reset, offset-constructor mark), close() no-op.
 * @run main ByteArrayInputStreamTest
 */

import java.io.ByteArrayInputStream;

public class ByteArrayInputStreamTest {
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
    static void testConstructors() {
        // (byte[]) uses the array as-is, pos=0, count=len
        byte[] data = {1, 2, 3};
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        check(in.available() == 3, "default available");
        // buffer is NOT copied: mutate source after construction, before reading that index
        data[1] = 99;
        check(in.read() == 1, "default first byte");
        check(in.read() == 99, "buffer not copied (source mutation visible)");

        // (byte[], off, len): reads start at off, count = min(off+len, len of buf)
        ByteArrayInputStream sub = new ByteArrayInputStream(new byte[]{9, 8, 7, 6, 5}, 1, 3);
        check(sub.available() == 3, "offset/length available");
        check(sub.read() == 8 && sub.read() == 7 && sub.read() == 6 && sub.read() == -1,
              "offset/length reads");

        // off+len > buf.length is clamped to buf.length (no exception)
        ByteArrayInputStream clamped = new ByteArrayInputStream(new byte[]{1, 2, 3}, 1, 100);
        check(clamped.available() == 2, "off+len clamped to buf.length");
        check(clamped.read() == 2 && clamped.read() == 3 && clamped.read() == -1, "clamped reads");

        // length == 0 yields an empty stream
        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[]{1, 2, 3}, 1, 0);
        check(empty.available() == 0 && empty.read() == -1, "zero length -> empty stream");

        // offset == buf.length yields an empty stream
        ByteArrayInputStream atEnd = new ByteArrayInputStream(new byte[]{1, 2, 3}, 3, 5);
        check(atEnd.available() == 0 && atEnd.read() == -1, "offset == buf.length -> empty");

        // null array -> NPE
        checkThrows(NullPointerException.class, () -> new ByteArrayInputStream(null),
                    "ByteArrayInputStream(null)");
    }

    // ---- 2. read() single byte ----
    static void testReadSingleByte() {
        // values are returned unsigned, in range 0..255 (0xFF -> 255, never -1)
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0x80, 0x7F});
        check(in.read() == 255, "0xFF -> 255");
        check(in.read() == 128, "0x80 -> 128");
        check(in.read() == 127, "0x7F -> 127");
        check(in.read() == -1, "read() at EOF returns -1");
        check(in.read() == -1, "read() repeatedly returns -1 at EOF");

        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
        check(empty.read() == -1, "empty stream read() == -1");
    }

    // ---- 3. read(byte[],off,len) parameter validation ----
    static void testReadArrayValidation() {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});

        checkThrows(NullPointerException.class, () -> in.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], -1, 1),
                    "read(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], 0, -1),
                    "read(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], 0, 5),
                    "read(b,0,5) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[4], 3, 2),
                    "read(b,3,2) -> IndexOutOfBoundsException");
    }

    // ---- 4. read(byte[],off,len) exact semantics ----
    static void testReadArrayContract() throws Exception {
        byte[] buf = new byte[6];
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{10, 20, 30, 40, 50});

        // exact offset placement
        int n = in.read(buf, 1, 2);
        check(n == 2, "read count");
        check(buf[0] == 0 && buf[1] == 10 && buf[2] == 20 && buf[3] == 0, "exact offset placement");

        // zero-length read before EOF returns 0
        check(in.read(buf, 0, 0) == 0, "zero-length read (not at EOF) returns 0");

        // partial read at end returns only remaining bytes
        byte[] tail = new byte[10];
        check(in.read(tail) == 3, "partial read at end returns remaining");
        check(tail[0] == 30 && tail[1] == 40 && tail[2] == 50, "tail contents");

        // at EOF: read(b,0,0) returns -1 (pos >= count short-circuits before len check)
        check(in.read(new byte[1], 0, 0) == -1, "zero-length read at EOF returns -1");
        check(in.read(new byte[1]) == -1, "read(byte[]) at EOF returns -1");
    }

    // ---- 5. skip ----
    static void testSkip() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
        check(in.skip(2) == 2, "skip(2) returns 2");
        check(in.read() == 3, "read after skip");
        check(in.skip(100) == 2, "skip past end returns remaining");
        check(in.read() == -1, "EOF after skip past end");

        ByteArrayInputStream neg = new ByteArrayInputStream(new byte[]{1, 2, 3});
        check(neg.skip(-5) == 0, "negative skip returns 0");
        check(neg.read() == 1, "negative skip does not move position");
        check(neg.skip(0) == 0, "skip(0) returns 0");
        check(neg.read() == 2, "skip(0) does not move position");
    }

    // ---- 6. available ----
    static void testAvailable() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5});
        check(in.available() == 5, "initial available");
        in.read();
        in.read();
        check(in.available() == 3, "available after two reads");
        in.skip(10);
        check(in.available() == 0, "available at EOF");
    }

    // ---- 7. mark/reset ----
    static void testMarkReset() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{10, 20, 30, 40});
        check(in.markSupported(), "markSupported");

        // unmarked reset returns to 0
        in.read();
        in.read();
        in.reset();
        check(in.read() == 10, "unmarked reset returns to position 0");

        // mark then reset replays
        ByteArrayInputStream m = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        m.read();                       // pos = 1
        m.mark(100);                    // mark = 1
        m.read();
        m.read();                       // pos = 3
        m.reset();                      // pos = 1
        check(m.read() == 2 && m.read() == 3, "mark/reset replay");

        // mark is recorded at the current position (readAheadLimit is ignored)
        ByteArrayInputStream ml = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        ml.read();
        ml.mark(0);
        ml.read();
        ml.reset();
        check(ml.read() == 2, "mark(0) at position 1 -> reset returns to 1");

        // offset constructor: initial mark is the offset
        ByteArrayInputStream off = new ByteArrayInputStream(new byte[]{0, 9, 8, 7}, 1, 3);
        off.read();                     // pos = 2
        off.reset();                    // mark = 1
        check(off.read() == 9, "offset constructor initial mark == offset");
    }

    // ---- 8. close is a no-op ----
    static void testCloseNoOp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        in.close();
        in.close();                     // idempotent
        check(in.available() == 3, "available after close");
        check(in.read() == 1, "read after close still works");
        check(in.skip(1) == 1, "skip after close still works");
        in.reset();
        check(in.read() == 1, "reset after close still works");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testReadSingleByte();
        testReadArrayValidation();
        testReadArrayContract();
        testSkip();
        testAvailable();
        testMarkReset();
        testCloseNoOp();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

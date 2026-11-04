/*
 * @test
 * @summary java.io.FilterInputStream API conformance: full contract coverage.
 *          Decorator base with protected in. read()/read(byte[],int,int)/skip/available/
 *          close/mark/reset/markSupported all pass through to the wrapped stream;
 *          subclass override interception; protected in access; a failing wrapped stream
 *          surfaces; null delegate rejected on first use.
 *          Deepened (contract level): one-arg read(byte[]) routes through the own
 *          three-arg read (delegate sees a single bulk call), reset over a
 *          mark-unsupported delegate -> IOException "mark/reset not supported",
 *          delegated parameter validation, close propagation through two layers,
 *          skip/available delegation counts, EOF semantics, protected in reassignment
 *          mid-stream, failing delegate propagation for every operation.
 * @run main FilterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilterInputStreamTest {
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

    /** Records every call that reaches the wrapped stream. */
    static class Recording extends InputStream {
        final InputStream delegate;
        int singleReads, bulkReads, skips, availables, marks, resets, closes;
        boolean markSupported = true;
        Recording(InputStream d) { delegate = d; }
        public int read() throws IOException { singleReads++; return delegate.read(); }
        public int read(byte[] b, int o, int l) throws IOException { bulkReads++; return delegate.read(b, o, l); }
        public long skip(long n) throws IOException { skips++; return delegate.skip(n); }
        public int available() throws IOException { availables++; return delegate.available(); }
        public void close() throws IOException { closes++; delegate.close(); }
        public synchronized void mark(int rl) { marks++; delegate.mark(rl); }
        public synchronized void reset() throws IOException { resets++; delegate.reset(); }
        public boolean markSupported() { return markSupported; }
    }

    // ---- 1. reads, skip, available and close pass through ----
    static void testDelegation() throws Exception {
        Recording rec = new Recording(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        FilterInputStream fis = new FilterInputStream(rec) { };
        check(fis.read() == 1 && rec.singleReads == 1, "read() delegates");
        byte[] two = new byte[2];
        check(fis.read(two) == 2 && two[0] == 2 && rec.bulkReads == 1, "read(byte[]) delegates");
        check(fis.available() == 2 && rec.availables == 1, "available delegates");
        check(fis.read() == 4, "state shared with wrapped stream");
        check(fis.skip(10) == 1 && rec.skips == 1, "skip delegates (clamped to remaining)");
        fis.close();
        check(rec.closes == 1, "close delegates");
    }

    // ---- 2. markSupported/mark/reset pass through ----
    static void testMarkReset() throws Exception {
        Recording mk = new Recording(new ByteArrayInputStream(new byte[]{9}));
        FilterInputStream fm = new FilterInputStream(mk) { };
        check(fm.markSupported(), "markSupported passes through (true)");
        mk.markSupported = false;
        check(!fm.markSupported(), "markSupported passes through (false)");
        fm.mark(4);
        check(mk.marks == 1, "mark delegates");
        fm.reset();
        check(mk.resets == 1, "reset delegates");
    }

    // ---- 3. subclass interception ----
    static void testSubclassInterception() throws Exception {
        class Counting extends FilterInputStream {
            int seen;
            Counting(InputStream in) { super(in); }
            @Override public int read() throws IOException {
                int v = super.read();
                if (v != -1) seen++;
                return v;
            }
        }
        Counting cnt = new Counting(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        while (cnt.read() != -1) { }
        check(cnt.seen == 3, "subclass saw every byte");
    }

    // ---- 4. protected in field access ----
    static void testProtectedIn() throws Exception {
        class Reaching extends FilterInputStream {
            Reaching(InputStream in) { super(in); }
            InputStream inner() { return in; }
        }
        ByteArrayInputStream inner = new ByteArrayInputStream(new byte[]{7});
        Reaching reach = new Reaching(inner);
        check(reach.inner() == inner, "protected in is the wrapped stream");
    }

    // ---- 5. failing wrapped stream surfaces ----
    static void testFailingDelegate() throws Exception {
        class Failing extends InputStream {
            public int read() throws IOException { throw new IOException("inner failure"); }
        }
        FilterInputStream bad = new FilterInputStream(new Failing()) { };
        checkThrows(IOException.class, () -> bad.read(), "wrapped failure propagates");
    }

    // ---- 6. null delegate rejected on first use ----
    static void testNullDelegate() throws Exception {
        FilterInputStream nul = new FilterInputStream(null) { };
        checkThrows(NullPointerException.class, () -> nul.read(),
                    "null delegate -> NPE on first read");
    }

    /** ByteArrayInputStream that counts close() calls reaching it. */
    static class TrackedSource extends ByteArrayInputStream {
        int closes;
        TrackedSource(byte[] data) { super(data); }
        public void close() { closes++; }
    }

    // ---- 7. one-arg read(byte[]) routes through the own three-arg read ----
    static void testReadByteArrRouting() throws Exception {
        Recording rec = new Recording(new ByteArrayInputStream(new byte[]{1, 2, 3, 4}));
        FilterInputStream fis = new FilterInputStream(rec) { };
        byte[] two = new byte[2];
        check(fis.read(two) == 2 && two[0] == 1 && two[1] == 2, "read(byte[]) content");
        check(rec.bulkReads == 1, "read(byte[]) performs exactly one bulk call on the delegate");
        check(rec.singleReads == 0, "read(byte[]) does not degrade to per-byte reads");

        check(fis.read(new byte[0]) == 0, "read(empty array) returns 0");
        check(rec.bulkReads == 2, "read(empty array) still delegates one bulk call");

        checkThrows(NullPointerException.class, () -> fis.read(null),
                    "read(null) -> NPE");
    }

    // ---- 8. reset over a mark-unsupported delegate ----
    static void testResetUnsupported() throws Exception {
        InputStream plain = new InputStream() {
            public int read() { return -1; }
        };
        final FilterInputStream fis = new FilterInputStream(plain) { };
        check(!fis.markSupported(), "markSupported passes through false");
        fis.mark(5);
        check(true, "mark on an unsupported delegate is a harmless no-op");
        checkThrowsMsg(IOException.class, "mark/reset not supported", fis::reset,
                       "reset over plain InputStream");
    }

    // ---- 9. parameter validation is delegated (delegate throws) ----
    static void testDelegatedValidation() throws Exception {
        final FilterInputStream fis =
                new FilterInputStream(new ByteArrayInputStream(new byte[]{1, 2})) { };
        checkThrows(IndexOutOfBoundsException.class, () -> fis.read(new byte[2], -1, 1),
                    "read(b,-1,1) -> IndexOutOfBoundsException (from the delegate)");
        checkThrows(IndexOutOfBoundsException.class, () -> fis.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException (from the delegate)");
        checkThrows(NullPointerException.class, () -> fis.read(null, 0, 1),
                    "read(null,0,1) -> NPE (from the delegate)");
        // valid call still works after the failed ones
        check(fis.read() == 1, "stream still usable after delegated validation errors");
    }

    // ---- 10. close propagates through one and two filter layers ----
    static void testCloseLayers() throws Exception {
        TrackedSource tracked = new TrackedSource(new byte[1]);
        new FilterInputStream(tracked) { }.close();
        check(tracked.closes == 1, "close reaches the wrapped stream once");

        TrackedSource deep = new TrackedSource(new byte[1]);
        FilterInputStream inner = new FilterInputStream(deep) { };
        FilterInputStream outer = new FilterInputStream(inner) { };
        outer.close();
        check(deep.closes == 1, "close propagates through two filter layers");
        outer.close();
        check(deep.closes == 2, "second close reaches the wrapped stream again (measured on both JDKs)");
    }

    // ---- 11. skip semantics ----
    static void testSkipSemantics() throws Exception {
        Recording rec = new Recording(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        FilterInputStream fis = new FilterInputStream(rec) { };
        check(fis.skip(0) == 0 && rec.skips == 1, "skip(0) delegates and returns 0");
        check(fis.skip(-5) == 0 && rec.skips == 2, "skip(-5) delegates and returns 0");
        check(fis.read() == 1, "position unchanged by zero/negative skips");
        check(fis.skip(2) == 2 && rec.skips == 3, "skip(2) delegates and skips 2");
        check(fis.read() == 4, "read after skip at the right position");
    }

    // ---- 12. available delegates on every call ----
    static void testAvailableDelegationCount() throws Exception {
        Recording rec = new Recording(new ByteArrayInputStream(new byte[5]));
        FilterInputStream fis = new FilterInputStream(rec) { };
        fis.available();
        fis.available();
        check(rec.availables == 2, "each available() call delegates exactly once");
        check(fis.available() == 5, "available value passes through");
    }

    // ---- 13. EOF semantics ----
    static void testEofSemantics() throws Exception {
        FilterInputStream fis = new FilterInputStream(new ByteArrayInputStream(new byte[0])) { };
        check(fis.read() == -1, "read() at EOF");
        check(fis.read(new byte[2]) == -1, "read(byte[]) at EOF");
        check(fis.available() == 0, "available 0 at EOF");
        check(fis.skip(10) == 0, "skip at EOF returns 0");
    }

    // ---- 14. protected in can be reassigned mid-stream ----
    static void testInReassignment() throws Exception {
        class Reassigning extends FilterInputStream {
            Reassigning(InputStream in) { super(in); }
            void swap(InputStream next) { in = next; }
        }
        Reassigning r = new Reassigning(new ByteArrayInputStream(new byte[]{1}));
        int first = r.read();
        r.swap(new ByteArrayInputStream(new byte[]{9}));
        check(first == 1 && r.read() == 9, "reads continue from the swapped delegate");
    }

    // ---- 15. failures propagate from every operation with the original message ----
    static void testFailingDelegateEveryOp() throws Exception {
        class FailingAll extends InputStream {
            private IOException boom() { return new IOException("inner failure"); }
            public int read() throws IOException { throw boom(); }
            public int read(byte[] b, int off, int len) throws IOException { throw boom(); }
            public long skip(long n) throws IOException { throw boom(); }
            public int available() throws IOException { throw boom(); }
            public void close() throws IOException { throw boom(); }
        }
        final FilterInputStream fis = new FilterInputStream(new FailingAll()) { };
        checkThrowsMsg(IOException.class, "inner failure", fis::read, "read propagates");
        checkThrowsMsg(IOException.class, "inner failure", () -> fis.read(new byte[1]),
                       "read(byte[]) propagates");
        checkThrowsMsg(IOException.class, "inner failure", () -> fis.read(new byte[1], 0, 1),
                       "read(b,off,len) propagates");
        checkThrowsMsg(IOException.class, "inner failure", () -> fis.skip(1), "skip propagates");
        checkThrowsMsg(IOException.class, "inner failure", fis::available, "available propagates");
        checkThrowsMsg(IOException.class, "inner failure", fis::close, "close propagates");
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

    // ---- 16. partial reads at EOF through the filter ----
    static void testPartialReadAtEof() throws Exception {
        FilterInputStream fis = new FilterInputStream(new ByteArrayInputStream(new byte[]{7, 8})) { };
        byte[] five = new byte[5];
        check(fis.read(five) == 2 && five[0] == 7 && five[1] == 8 && five[2] == 0,
              "read(byte[]) at stream end returns only the remaining bytes");
        check(fis.read(five) == -1, "subsequent read(byte[]) returns -1");
    }

    // ---- 17. mark/reset replay through the filter (mark-supporting delegate) ----
    static void testMarkResetReplay() throws Exception {
        Recording rec = new Recording(new ByteArrayInputStream(new byte[]{1, 2, 3, 4}));
        FilterInputStream fis = new FilterInputStream(rec) { };
        fis.read();                     // consume 1
        fis.mark(10);
        int a = fis.read();
        int b = fis.read();
        fis.reset();
        check(fis.read() == a && fis.read() == b, "mark/reset replays through the filter");
        check(rec.marks == 1 && rec.resets == 1, "mark and reset each delegate exactly once");
        check(fis.markSupported(), "markSupported passes through the filter");
    }

    // ---- 18. null delegate rejected by every operation ----
    static void testNullDelegateAllOps() {
        final FilterInputStream fis = new FilterInputStream(null) { };
        checkThrows(NullPointerException.class, fis::read, "read on null delegate");
        checkThrows(NullPointerException.class, () -> fis.read(new byte[1]),
                    "read(byte[]) on null delegate");
        checkThrows(NullPointerException.class, () -> fis.read(new byte[1], 0, 1),
                    "read(b,off,len) on null delegate");
        checkThrows(NullPointerException.class, () -> fis.skip(1), "skip on null delegate");
        checkThrows(NullPointerException.class, fis::available, "available on null delegate");
        checkThrows(NullPointerException.class, fis::close, "close on null delegate");
    }

    // ---- 19. read() returns unsigned 0..255 through the filter ----
    static void testUnsignedRead() throws Exception {
        FilterInputStream fis = new FilterInputStream(
                new ByteArrayInputStream(new byte[]{(byte) 0xFF, 0x00, 0x7F})) { };
        check(fis.read() == 255, "0xFF reads as 255");
        check(fis.read() == 0, "0x00 reads as 0");
        check(fis.read() == 127, "0x7F reads as 127");
        check(fis.read() == -1, "EOF reads as -1");
    }

    // ---- 20. read(byte[],off,len) exact placement ----
    static void testReadRegionPlacement() throws Exception {
        FilterInputStream fis = new FilterInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4})) { };
        byte[] buf = new byte[6];
        check(fis.read(buf, 2, 2) == 2, "region read count");
        check(buf[0] == 0 && buf[1] == 0 && buf[2] == 1 && buf[3] == 2 && buf[4] == 0,
              "region placed exactly at off, beyond untouched");
        check(fis.read() == 3, "stream position after region read");
    }

    public static void main(String[] args) throws Exception {
        testDelegation();
        testMarkReset();
        testSubclassInterception();
        testProtectedIn();
        testFailingDelegate();
        testNullDelegate();
        testReadByteArrRouting();
        testResetUnsupported();
        testDelegatedValidation();
        testCloseLayers();
        testSkipSemantics();
        testAvailableDelegationCount();
        testEofSemantics();
        testInReassignment();
        testFailingDelegateEveryOp();
        testPartialReadAtEof();
        testMarkResetReplay();
        testNullDelegateAllOps();
        testUnsignedRead();
        testReadRegionPlacement();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

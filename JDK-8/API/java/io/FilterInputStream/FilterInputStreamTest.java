/*
 * @test
 * @summary java.io.FilterInputStream API conformance: full contract coverage.
 *          Decorator base with protected in. read()/read(byte[],int,int)/skip/available/
 *          close/mark/reset/markSupported all pass through to the wrapped stream;
 *          subclass override interception; protected in access; a failing wrapped stream
 *          surfaces; null delegate rejected on first use.
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

    public static void main(String[] args) throws Exception {
        testDelegation();
        testMarkReset();
        testSubclassInterception();
        testProtectedIn();
        testFailingDelegate();
        testNullDelegate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

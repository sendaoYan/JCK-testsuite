/*
 * @test
 * @summary java.io.FilterOutputStream API conformance: full contract coverage.
 *          Decorator base with protected out. write(int)/write(byte[])/
 *          write(byte[],off,len) delegate to the sink; close implies flush then close;
 *          flush delegates; null/bounds checking; subclass interception; protected out
 *          access; a failing sink surfaces; null delegate rejected on first write.
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

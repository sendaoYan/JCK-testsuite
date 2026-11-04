/*
 * @test
 * @summary java.io.InputStream API conformance: full contract coverage.
 *          Abstract byte-input root. Default read(byte[]) / read(byte[],off,len) derived
 *          from read(), skip (negative->0), available/close/mark/reset defaults,
 *          markSupported false, argument checking (null->NPE, bounds->IOBE),
 *          polymorphic use through the base reference.
 * @run main InputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamTest {
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

    /** Minimal subclass: only read() is implemented, everything else inherited. */
    static class ArrayIn extends InputStream {
        final byte[] data;
        int pos;
        ArrayIn(byte[] data) { this.data = data; }
        @Override public int read() { return pos < data.length ? (data[pos++] & 0xFF) : -1; }
    }

    // ---- 1. default bulk reads derived from read() ----
    static void testBulkReadDefaults() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        byte[] three = new byte[3];
        check(in.read(three) == 3 && three[0] == 1 && three[2] == 3, "read(byte[])");
        byte[] two = new byte[2];
        check(in.read(two, 0, 2) == 2 && two[0] == 4 && two[1] == 5, "read(byte[],off,len)");
        check(in.read() == -1, "exhausted");
        check(in.read(new byte[2]) == -1, "bulk read at EOF returns -1");
        check(new ArrayIn(new byte[0]).read(new byte[1]) == -1, "empty source bulk read -1");

        in = new ArrayIn(new byte[]{9});
        check(in.read(new byte[0]) == 0, "read(empty) == 0");
        check(in.read(new byte[4], 1, 0) == 0, "read(b,off,0) == 0");
        check(in.read() == 9, "no byte consumed by zero-length reads");
    }

    // ---- 2. argument checking ----
    static void testArgumentChecking() throws Exception {
        final ArrayIn in = new ArrayIn(new byte[3]);
        checkThrows(NullPointerException.class, () -> in.read((byte[]) null),
                    "read((byte[])null) -> NPE");
        checkThrows(NullPointerException.class, () -> in.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], -1, 1),
                    "read(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 0, -1),
                    "read(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 2, 1),
                    "read(b,2,1) -> IndexOutOfBoundsException");
    }

    // ---- 3. skip ----
    static void testSkip() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        check(in.skip(-1) == 0, "skip(negative) == 0");
        check(in.read() == 1, "skip(negative) consumed nothing");
        check(in.skip(0) == 0, "skip(0) == 0");
        check(in.skip(2) == 2 && in.read() == 4, "skip(2) skips ahead");

        ArrayIn shortIn = new ArrayIn(new byte[]{1, 2});
        check(shortIn.skip(100) == 2, "skip past end returns remaining");
        check(shortIn.read() == -1, "exhausted after skip past end");
    }

    // ---- 4. defaults: available/close/mark/reset ----
    static void testDefaults() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1});
        check(in.available() == 0, "base available() is 0");
        in.close();
        in.close();
        check(in.read() == 1, "base close() does not close");
        check(!in.markSupported(), "base markSupported() is false");

        ArrayIn mk = new ArrayIn(new byte[]{7, 8});
        check(mk.read() == 7, "first byte before mark");
        mk.mark(10);                       // no-op, does not throw
        check(mk.read() == 8, "base mark() consumes nothing");
        checkThrows(IOException.class, () -> in.reset(), "base reset() -> IOException");
    }

    // ---- 5. polymorphic use ----
    static void testPolymorphicUse() throws Exception {
        InputStream bais = new ByteArrayInputStream(new byte[]{5, 6});
        check(bais.read() == 5 && bais.read() == 6, "ByteArrayInputStream via InputStream ref");
        check(bais.available() == 0, "available after drain");
        bais.close();
    }

    public static void main(String[] args) throws Exception {
        testBulkReadDefaults();
        testArgumentChecking();
        testSkip();
        testDefaults();
        testPolymorphicUse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

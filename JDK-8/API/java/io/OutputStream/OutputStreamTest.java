/*
 * @test
 * @summary java.io.OutputStream API conformance: full contract coverage.
 *          Abstract byte-output root. write(int) abstract; write(byte[]) delegates to
 *          write(byte[],int,int); default write loop; flush/close no-ops; null argument
 *          and bounds checking; polymorphic use through the base reference.
 * @run main OutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class OutputStreamTest {
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

    /** Only write(int): everything else inherited. */
    static class Bare extends OutputStream {
        final StringBuilder sb = new StringBuilder();
        @Override public void write(int b) { sb.append((char) (b & 0xFF)); }
    }

    static class Sink extends OutputStream {
        final ArrayList<Byte> bytes = new ArrayList<Byte>();
        int flushes, closes;
        @Override public void write(int b) { bytes.add((byte) b); }
        @Override public void flush() { flushes++; }
        @Override public void close() { closes++; }
    }

    // ---- 1. default write(byte[]) delegates to write(byte[],int,int) -> write(int) ----
    static void testWriteDelegation() throws Exception {
        Bare b = new Bare();
        b.write(new byte[]{'a', 'b', 'c'});
        check("abc".contentEquals(b.sb), "write(byte[]) reaches write(int)");
        b.write(new byte[]{'X', 'Y'}, 0, 0);
        check("abc".contentEquals(b.sb), "zero-length write writes nothing");
        b.write(new byte[]{'-', 'd', 'e', '-'}, 1, 2);
        check("abcde".contentEquals(b.sb), "write(byte[],off,len) region only");
    }

    // ---- 2. default write(byte[],int,int) loops one byte at a time ----
    static void testDefaultWriteLoop() throws Exception {
        class Counting extends OutputStream {
            int calls;
            @Override public void write(int x) { calls++; }
        }
        Counting c = new Counting();
        c.write(new byte[7], 2, 5);
        check(c.calls == 5, "default region write calls write(int) once per byte");
    }

    // ---- 3. argument checking ----
    static void testArgumentChecking() throws Exception {
        final Sink s = new Sink();
        checkThrows(NullPointerException.class, () -> s.write((byte[]) null),
                    "write((byte[])null) -> NPE");
        checkThrows(NullPointerException.class, () -> s.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> s.write(new byte[2], 2, 1),
                    "write(b,2,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> s.write(new byte[2], -1, 1),
                    "write(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> s.write(new byte[2], 0, -1),
                    "write(b,0,-1) -> IndexOutOfBoundsException");
    }

    // ---- 4. default flush()/close() are no-ops ----
    static void testDefaultFlushClose() throws Exception {
        class Defaults extends OutputStream {
            @Override public void write(int x) { }
        }
        Defaults d = new Defaults();
        d.flush();
        d.close();
        d.write(1);
        passed++;                       // none of the above interfered
    }

    // ---- 5. subclasses see overridden flush/close via base reference ----
    static void testOverrideDispatch() throws Exception {
        Sink s = new Sink();
        OutputStream os = s;
        os.write(65);
        os.flush();
        os.flush();
        os.close();
        check(s.bytes.size() == 1 && s.bytes.get(0) == 65, "write via base reference");
        check(s.flushes == 2, "flush invoked twice");
        check(s.closes == 1, "close invoked once");
    }

    // ---- 6. polymorphic use ----
    static void testPolymorphicUse() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream ref = baos;
        ref.write('h');
        ref.flush();
        check(baos.toByteArray().length == 1 && baos.toByteArray()[0] == 'h',
              "ByteArrayOutputStream via OutputStream ref");
    }

    public static void main(String[] args) throws Exception {
        testWriteDelegation();
        testDefaultWriteLoop();
        testArgumentChecking();
        testDefaultFlushClose();
        testOverrideDispatch();
        testPolymorphicUse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

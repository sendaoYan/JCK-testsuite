/*
 * @test
 * @summary java.io.BufferedInputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (size<=0->IAE, null delegate->IOException),
 *          read validation (NPE/IOBE), buffering over one-byte-at-a-time source, skip,
 *          available, mark/reset (replay/invalidated/unmarked/close), close idempotent.
 * @run main BufferedInputStreamTest
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    static class OneByteStream extends InputStream {
        final byte[] data; int i;
        OneByteStream(byte[] data) { this.data = data; }
        public int read() { return (i < data.length) ? (data[i++] & 0xFF) : -1; }
        public int available() { return data.length - i; }
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.BufferedInputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterInputStream.class, "extends FilterInputStream");
        hasMethod(c, "read");
        hasMethod(c, "mark", int.class);
        hasMethod(c, "reset");
        hasMethod(c, "markSupported");
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

        BufferedInputStream nul = new BufferedInputStream(null);
        checkThrows(IOException.class, () -> nul.read(), "null delegate -> IOException on read");
    }

    // ---- 2. buffering ----
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

        // readAllBytes over a buffered stream
        BufferedInputStream all = new BufferedInputStream(new ByteArrayInputStream(data), 32);
        check(Arrays.equals(all.readAllBytes(), data), "readAllBytes over buffered stream");
    }

    // ---- 3. read validation ----
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
        check(in.read(new byte[1], 0, 0) == 0, "len==0 returns 0");

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

        final BufferedInputStream unmarked = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1}));
        checkThrows(IOException.class, () -> unmarked.reset(), "reset without mark -> IOException");

        BufferedInputStream m = new BufferedInputStream(new ByteArrayInputStream(new byte[]{10, 20, 30, 40}));
        m.read();
        m.mark(100);
        m.read();
        m.read();
        m.reset();
        check(m.read() == 20 && m.read() == 30, "mark/reset replay");

        BufferedInputStream inv = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}));
        inv.read();
        inv.mark(2);
        inv.read();
        inv.read();
        inv.read();
        inv.read();
        inv.reset();
        check(inv.read() == 2, "reset within limit replays");

        byte[] wide = new byte[200];
        for (int i = 0; i < wide.length; i++) wide[i] = (byte) i;
        final BufferedInputStream inv2 = new BufferedInputStream(new ByteArrayInputStream(wide), 8);
        inv2.mark(2);
        for (int i = 0; i < 50; i++) inv2.read();
        checkThrows(IOException.class, () -> inv2.reset(), "reset past readlimit -> IOException");
    }

    // ---- 7. close ----
    static void testClose() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        in.close();
        in.close();
        checkThrows(IOException.class, () -> in.read(), "read after close -> IOException");
        checkThrows(IOException.class, () -> in.available(), "available after close -> IOException");
        checkThrows(IOException.class, () -> in.reset(), "reset after close -> IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testBuffering();
        testReadArrayValidation();
        testSkip();
        testAvailable();
        testMarkReset();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

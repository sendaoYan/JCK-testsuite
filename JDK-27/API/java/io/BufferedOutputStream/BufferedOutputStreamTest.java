/*
 * @test
 * @summary java.io.BufferedOutputStream conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (size<=0->IAE), write(int) low 8 bits +
 *          buffering, write(byte[],off,len) validation (NPE/IOBE), direct-write path,
 *          flush semantics, close flushes remaining bytes.
 * @run main BufferedOutputStreamTest
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
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

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.BufferedOutputStream");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.FilterOutputStream.class, "extends FilterOutputStream");
        hasMethod(c, "write", int.class);
        hasMethod(c, "flush");
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
        out.write(0x1FF);
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

    // ---- 4. large write direct + content ----
    static void testLargeWrite() throws Exception {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream out = new BufferedOutputStream(bos, 8);
        out.write(data, 0, data.length);
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
        out.close();
        check(bos.size() == 5, "close flushes remaining bytes");
        check(Arrays.equals(bos.toByteArray(), new byte[]{1, 2, 3, 4, 5}), "final content");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testWriteIntAndBuffering();
        testWriteArrayValidation();
        testLargeWrite();
        testFlushClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

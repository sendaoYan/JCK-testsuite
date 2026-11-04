/*
 * @test
 * @summary java.io.InputStream conformance: white-box structure (abstract byte input
 *          root) + black-box behavior (default implementations derived from read():
 *          bulk reads, skip/skipNBytes, readAllBytes/readNBytes, transferTo, mark/reset
 *          defaults, available/close defaults, nullInputStream, argument checking).
 * @run main InputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal subclass: only read() is implemented, everything else is inherited. */
    static class ArrayIn extends InputStream {
        final byte[] data;
        int pos;
        ArrayIn(byte[] data) { this.data = data; }
        @Override public int read() { return pos < data.length ? (data[pos++] & 0xFF) : -1; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.InputStream");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(Modifier.isAbstract(c.getMethod("read").getModifiers()), "read() abstract");
        check(!Modifier.isAbstract(c.getMethod("read", byte[].class, int.class, int.class)
                                      .getModifiers()),
              "read(byte[],int,int) is concrete in the base (loops over read())");
        check(Modifier.isStatic(c.getMethod("nullInputStream").getModifiers()),
              "static nullInputStream factory");
        for (String m : new String[]{"read", "readAllBytes", "readNBytes", "skip",
                                     "skipNBytes", "available", "close", "mark",
                                     "reset", "markSupported", "transferTo"})
            check(has(c, m), "has method " + m);
        boolean hasLock = false;
        for (java.lang.reflect.Field f : c.getDeclaredFields())
            if (f.getName().equals("lock")) hasLock = true;
        check(!hasLock, "InputStream has no lock field (that is Reader/Writer)");
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. default read(byte[]) and read(byte[],int,int) are derived from read()
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        byte[] three = new byte[3];
        check(in.read(three) == 3 && three[0] == 1 && three[2] == 3, "bulk read via read()");
        byte[] two = new byte[2];
        check(in.read(two, 0, 2) == 2 && two[0] == 4 && two[1] == 5, "offset bulk read");
        check(in.read() == -1, "exhausted");
        check(in.read(new byte[2]) == -1, "bulk read at EOF returns -1");
        check(new ArrayIn(new byte[0]).read(new byte[1]) == -1, "empty source bulk read -1");

        // 2. zero-length reads transfer nothing and never block
        in = new ArrayIn(new byte[]{9});
        check(in.read(new byte[0]) == 0, "read(empty) == 0");
        check(in.read(new byte[4], 1, 0) == 0, "read(b,off,0) == 0");
        check(in.read() == 9, "no byte consumed by zero-length reads");

        // 3. skip: forward skips, negative is a no-op returning 0
        in = new ArrayIn(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        check(in.skip(-1) == 0, "skip(negative) == 0");
        check(in.read() == 1, "skip(negative) consumed nothing");
        check(in.skip(2) == 2 && in.read() == 4, "skip(2) skips ahead");

        // 4. skipNBytes: exact skip, negative no-op, past EOF throws EOFException
        in = new ArrayIn(new byte[]{1, 2, 3, 4});
        in.skipNBytes(2);
        check(in.read() == 3, "skipNBytes(2)");
        in.skipNBytes(-5);
        check(in.read() == 4, "skipNBytes(negative) is a no-op");
        try {
            in.skipNBytes(10);                       // only 0 bytes remain
            check(false, "skipNBytes past EOF must throw");
        } catch (java.io.EOFException expected) {
            passed++;
        }

        // 5. readAllBytes consumes the remainder
        in = new ArrayIn(new byte[]{1, 2, 3});
        check(java.util.Arrays.equals(in.readAllBytes(), new byte[]{1, 2, 3}),
              "readAllBytes contents");
        check(in.read() == -1, "readAllBytes drains the stream");

        // 6. readNBytes: array form fills at most len; int form never returns more than asked
        in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        byte[] buf = new byte[2];
        check(in.readNBytes(buf, 0, 2) == 2 && buf[0] == 1, "readNBytes(b,off,len)");
        check(java.util.Arrays.equals(in.readNBytes(2), new byte[]{3, 4}), "readNBytes(int)");
        check(in.read() == 5, "position preserved across readNBytes forms");
        try {
            in.readNBytes(-1);
            check(false, "readNBytes(negative) must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 7. transferTo moves everything and reports the count
        in = new ArrayIn(new byte[]{9, 8, 7});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        check(in.transferTo(out) == 3 && java.util.Arrays.equals(out.toByteArray(), new byte[]{9, 8, 7}),
              "transferTo count and contents");
        try {
            new ArrayIn(new byte[1]).transferTo(null);
            check(false, "transferTo(null) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }

        // 8. defaults: available() == 0, close() no-op, markSupported() false,
        //    mark() no-op, reset() throws
        in = new ArrayIn(new byte[]{1});
        check(in.available() == 0, "base available() is 0");
        in.close();
        in.close();
        check(in.read() == 1, "base close() does not close");
        check(!in.markSupported(), "base markSupported() is false");
        ArrayIn mk = new ArrayIn(new byte[]{7, 8});
        check(mk.read() == 7, "first byte before mark");
        mk.mark(10);
        check(mk.read() == 8, "base mark() consumes nothing");
        try {
            in.reset();
            check(false, "base reset() must throw");
        } catch (IOException ioe) {
            check("mark/reset not supported".equals(ioe.getMessage()),
                  "reset message: " + ioe.getMessage());
        }

        // 9. argument checking is the base class's job too
        try {
            new ArrayIn(new byte[1]).read(null);
            check(false, "read(null) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new ArrayIn(new byte[3]).read(new byte[2], 2, 1);
            check(false, "out-of-bounds read must throw");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }

        // 10. nullInputStream: an always-empty, closed-on-arrival stream
        InputStream empty = InputStream.nullInputStream();
        check(empty.read() == -1 && empty.read(new byte[1]) == -1, "nullInputStream reads EOF");
        check(empty.readNBytes(3).length == 0 && empty.available() == 0, "no data ever");
        check(empty.skip(4) == 0, "skip on null stream is 0");
        empty.close();

        // 11. polymorphic use: a concrete JDK stream through the base reference
        try (InputStream bais = new ByteArrayInputStream(new byte[]{5, 6})) {
            check(bais.read() == 5 && bais.read() == 6, "ByteArrayInputStream via InputStream ref");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

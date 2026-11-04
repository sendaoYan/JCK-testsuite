/*
 * @test
 * @summary java.io.PushbackInputStream conformance: white-box structure + black-box
 *          behavior (unread of single bytes/arrays/regions, pushback buffer order
 *          LIFO, buffer exhaustion IOException, default buffer size 1, mark/reset
 *          unsupported, available counts pushed-back bytes, skip/transferTo interplay,
 *          null/bounds checks, close).
 * @run main PushbackInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class PushbackInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static PushbackInputStream pb(String s) {
        return new PushbackInputStream(new ByteArrayInputStream(s.getBytes()));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PushbackInputStream");
        check(c.getSuperclass().getName().equals("java.io.FilterInputStream"),
              "extends FilterInputStream");
        check(c.getConstructor(java.io.InputStream.class) != null, "ctor (InputStream)");
        check(c.getConstructor(java.io.InputStream.class, int.class) != null,
              "ctor (InputStream, int)");
        for (String m : new String[]{"read", "unread", "available", "skip",
                                     "markSupported", "mark", "reset", "close", "transferTo"})
            check(has(c, m), "has method " + m);
        check(Modifier.isProtected(c.getDeclaredField("buf").getModifiers()), "protected buf");
        check(Modifier.isProtected(c.getDeclaredField("pos").getModifiers()), "protected pos");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. unread(int): the byte is delivered before stream content (LIFO order)
        PushbackInputStream p = pb("bc");
        p.unread('a');
        check(p.read() == 'a', "unread byte delivered first");
        check(p.read() == 'b' && p.read() == 'c', "stream content follows");
        check(p.read() == -1, "EOF after all consumed");

        // 2. multiple unread calls deliver in reverse order
        p = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'z'}), 2);
        p.unread('y');
        p.unread('x');
        check(p.read() == 'x' && p.read() == 'y' && p.read() == 'z',
              "pushbacks unwind LIFO");

        // 3. default buffer size is exactly one byte
        p = pb("q");
        p.unread('1');
        try {
            p.unread('2');
            check(false, "second unread on a size-1 buffer must throw");
        } catch (IOException ioe) {
            check("Push back buffer is full".equals(ioe.getMessage()),
                  "overflow message: " + ioe.getMessage());
        }

        // 4. explicit buffer capacity; unread region of exact size fits
        PushbackInputStream p4 = new PushbackInputStream(
            new ByteArrayInputStream(new byte[]{9}), 2);
        p4.unread(new byte[]{1, 2}, 0, 2);
        check(p4.read() == 1 && p4.read() == 2 && p4.read() == 9, "region unread order");
        try {
            p4.unread(new byte[3], 0, 3);
            check(false, "oversized unread must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 5. unread(byte[]) region semantics
        p = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'d', 'e', 'f'}), 3);
        p.unread(new byte[]{0, 'a', 'b', 'c', 0}, 1, 3);
        byte[] out = new byte[6];
        int n = p.read(out, 0, 6);
        check(n == 6 && out[0] == 'a' && out[2] == 'c' && out[5] == 'f',
              "region unread then bulk read");

        // 6. available() counts the pushback buffer plus the stream
        p = pb("12");
        check(p.available() == 2, "available from stream only");
        p.unread('0');
        check(p.available() == 3, "available includes pushed-back byte");

        // 7. mark/reset unsupported even when the wrapped stream supports them
        java.io.ByteArrayInputStream marks = new ByteArrayInputStream(new byte[1]);
        PushbackInputStream p7 = new PushbackInputStream(marks, 4);
        check(!p7.markSupported(), "mark unsupported");
        try {
            p7.reset();
            check(false, "reset must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "reset message: " + ioe.getMessage());
        }

        // 8. skip consumes pushed-back bytes first
        p = pb("wxy");
        p.unread('v');
        p.skip(1);
        check(p.read() == 'w', "skip consumed the pushed-back byte");

        // 9. transferTo drains pushback + stream
        p = pb("rs");
        p.unread('t');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(p.transferTo(bos) == 3 && "trs".equals(bos.toString()),
              "transferTo includes pushback buffer");

        // 10. argument checking
        p = pb("a");
        try {
            p.unread((byte[]) null);
            check(false, "unread(null) must throw");
        } catch (NullPointerException | IOException expected) {
            passed++;
        }
        try {
            new PushbackInputStream(new ByteArrayInputStream(new byte[1]), -1);
            check(false, "negative buffer size must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 11. close releases both layers; reads after close throw
        p = pb("m");
        p.close();
        try {
            p.read();
            check(false, "read after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "close message: " + ioe.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

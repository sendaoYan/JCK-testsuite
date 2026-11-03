/*
 * @test
 * @summary java.io.PushbackReader conformance: white-box structure + black-box
 *          behavior (unread of char/char[]/regions, LIFO order, default size 1,
 *          buffer exhaustion IOException, mark unsupported, ready interplay, skip
 *          consumes pushback first, close behavior).
 * @run main PushbackReaderTest
 */

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class PushbackReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static PushbackReader pr(String s) {
        return new PushbackReader(new StringReader(s));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PushbackReader");
        check(c.getSuperclass().getName().equals("java.io.FilterReader"),
              "extends FilterReader");
        check(c.getConstructor(java.io.Reader.class) != null, "ctor (Reader)");
        check(c.getConstructor(java.io.Reader.class, int.class) != null, "ctor (Reader, int)");
        for (String m : new String[]{"read", "unread", "ready", "mark",
                                     "reset", "markSupported", "close", "skip"})
            check(has(c, m), "has method " + m);
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. unread(int): pushed char comes out first
        PushbackReader p = pr("bc");
        p.unread('a');
        check(p.read() == 'a', "unread char delivered first");
        check(p.read() == 'b' && p.read() == 'c', "source content follows");
        check(p.read() == -1, "EOF");

        // 2. LIFO across multiple unread calls
        p = new PushbackReader(new StringReader("z"), 2);
        p.unread('y');
        p.unread('x');
        check(p.read() == 'x' && p.read() == 'y' && p.read() == 'z', "LIFO order");

        // 3. default buffer is one char
        p = pr("q");
        p.unread('1');
        try {
            p.unread('2');
            check(false, "second unread on size-1 buffer must throw");
        } catch (IOException ioe) {
            check("Pushback buffer overflow".equals(ioe.getMessage()),
                  "overflow message: " + ioe.getMessage());
        }

        // 4. larger buffer, region unread
        p = new PushbackReader(new StringReader("def"), 3);
        p.unread(new char[]{'0', 'a', 'b', 'c', '0'}, 1, 3);
        char[] out = new char[6];
        check(p.read(out) == 6 && out[0] == 'a' && out[5] == 'f', "region unread + bulk read");

        // 5. exact-fit unread then oversized unread
        p = new PushbackReader(new StringReader("9"), 2);
        p.unread(new char[]{'x', 'y'});
        try {
            p.unread('z');
            check(false, "overflowing a full buffer must throw");
        } catch (IOException expected) {
            passed++;
        }
        check(p.read() == 'x' && p.read() == 'y' && p.read() == '9', "buffer intact after failed push");

        // 6. ready() true while pushback buffer holds data
        p = new PushbackReader(new StringReader(""), 1);
        check(p.ready(), "ready follows the wrapped StringReader (always ready)");
        p.unread('k');
        check(p.read() == 'k', "pushed char readable");

        // 7. mark/reset unsupported
        p = pr("a");
        check(!p.markSupported(), "mark unsupported");
        try {
            p.reset();
            check(false, "reset must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "reset message: " + ioe.getMessage());
        }

        // 8. skip consumes the pushback buffer first
        p = pr("wxy");
        p.unread('v');
        p.skip(1);
        check(p.read() == 'w', "skip ate the pushed-back char");

        // 9. negative/invalid buffer size
        try {
            new PushbackReader(new StringReader("a"), -1);
            check(false, "negative size must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 10. close: reads after close throw
        p = pr("m");
        p.close();
        try {
            p.read();
            check(false, "read after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "close message: " + ioe.getMessage());
        }
        try {
            p.unread('u');
            check(false, "unread after close must throw");
        } catch (IOException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.io.CharArrayReader conformance: white-box structure + black-box
 *          behavior (full-array and (off,len) constructors, read forms, skip, ready,
 *          mark/reset supported, close behavior, no defensive copy of the caller's
 *          array, empty reader).
 * @run main CharArrayReaderTest
 */

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CharArrayReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.CharArrayReader");
        check(c.getSuperclass().getName().equals("java.io.Reader"), "extends Reader");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getConstructor(char[].class) != null, "ctor (char[])");
        check(c.getConstructor(char[].class, int.class, int.class) != null,
              "ctor (char[], int, int)");
        for (String m : new String[]{"read", "skip", "ready", "markSupported",
                                     "mark", "reset", "close"})
            check(has(c, m), "has method " + m);
        check(Modifier.isProtected(c.getDeclaredField("buf").getModifiers()), "protected buf");
        check(Modifier.isProtected(c.getDeclaredField("pos").getModifiers()), "protected pos");
        check(Modifier.isProtected(c.getDeclaredField("markedPos").getModifiers()),
              "protected markedPos");
        check(Modifier.isProtected(c.getDeclaredField("count").getModifiers()),
              "protected count");
    }

    static boolean has(Class<?> c, String m) {
        try {
            for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
            return false;
        } catch (Exception e) { return false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. full-array constructor: every read form
        CharArrayReader r = new CharArrayReader("abc".toCharArray());
        check(r.read() == 'a', "single read");
        char[] two = new char[2];
        check(r.read(two) == 2 && two[0] == 'b' && two[1] == 'c', "bulk read");
        check(r.read() == -1, "EOF");
        check(!r.ready(), "not ready at EOF");

        // 2. ready() reflects buffered content
        CharArrayReader rr = new CharArrayReader("xy".toCharArray());
        check(rr.ready(), "ready when data remains");
        rr.read();
        rr.read();
        check(!rr.ready(), "not ready at EOF");

        // 3. (off,len) constructor reads only the window
        CharArrayReader w = new CharArrayReader("abcdef".toCharArray(), 1, 3);
        check(w.read() == 'b', "window starts at offset");
        char[] rest = new char[4];
        check(w.read(rest) == 2 && rest[0] == 'c' && rest[1] == 'd', "window limited to len");
        check(w.read() == -1, "window EOF");

        // 4. the reader does NOT copy the array: later writes are visible
        char[] live = {'1', '2', '3'};
        CharArrayReader lr = new CharArrayReader(live);
        check(lr.read() == '1', "first char");
        live[2] = 'Z';
        lr.read();
        check(lr.read() == 'Z', "mutation of the source array is observed");

        // 5. skip forward and clamped
        CharArrayReader s = new CharArrayReader("12345".toCharArray());
        check(s.skip(2) == 2 && s.read() == '3', "skip(2)");
        check(s.skip(10) == 2, "skip clamps to remaining");
        check(s.read() == -1, "EOF after clamped skip");

        // 6. mark/reset round-trip (mark IS supported)
        CharArrayReader m = new CharArrayReader("hello".toCharArray());
        check(m.markSupported(), "mark supported");
        m.skip(2);
        m.mark(10);
        check(m.read() == 'l' && m.read() == 'l', "read past mark");
        m.reset();
        check(m.read() == 'l', "reset rewinds to the mark");
        m.mark(0);                                    // readAheadLimit ignored for arrays
        m.reset();
        passed++;

        // 7. zero-length reads
        CharArrayReader z = new CharArrayReader("q".toCharArray());
        check(z.read(new char[0]) == 0, "zero-length read");
        check(z.read() == 'q', "no consumption");

        // 8. close: further reads throw, close is idempotent
        CharArrayReader c8 = new CharArrayReader("x".toCharArray());
        c8.close();
        try {
            c8.read();
            check(false, "read after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }
        c8.close();
        try {
            c8.ready();
            check(false, "ready after close must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 9. empty source: immediate EOF through every form
        CharArrayReader e = new CharArrayReader(new char[0]);
        check(e.read() == -1 && e.read(new char[1]) == -1 && !e.ready(), "empty reader EOF");

        // 10. polymorphic use as Reader
        Reader asReader = new CharArrayReader("pr".toCharArray());
        check(asReader.read() == 'p' && asReader.read() == 'r', "via Reader reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

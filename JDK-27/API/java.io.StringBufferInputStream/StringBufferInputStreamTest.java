/*
 * @test
 * @summary java.io.StringBufferInputStream conformance: white-box structure (deprecated
 *          byte view of a String, low 8 bits per char) + black-box behavior (reads,
 *          skip, available, reset to start, no close semantics, live view of the
 *          string, non-ASCII truncation to low byte).
 * @run main StringBufferInputStreamTest
 */

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class StringBufferInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.StringBufferInputStream");
        check(c.getSuperclass().getName().equals("java.io.InputStream"), "extends InputStream");
        check(c.getConstructor(String.class) != null, "ctor (String)");
        check(c.isAnnotationPresent(Deprecated.class), "@Deprecated (replaced by StringReader)");
        for (String m : new String[]{"read", "skip", "available", "reset"})
            check(has(c, m), "has method " + m);
        check(Modifier.isProtected(c.getDeclaredField("buffer").getModifiers()), "protected buffer");
        check(Modifier.isProtected(c.getDeclaredField("pos").getModifiers()), "protected pos");
        check(Modifier.isProtected(c.getDeclaredField("count").getModifiers()), "protected count");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    static boolean hasNot(Class<?> c, String m) {
        return !has(c, m);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. reads deliver chars as bytes
        StringBufferInputStream in = new StringBufferInputStream("abc");
        check(in.read() == 'a' && in.read() == 'b' && in.read() == 'c', "sequential reads");
        check(in.read() == -1, "EOF");

        // 2. bulk read and region
        in = new StringBufferInputStream("abcdef");
        byte[] buf = new byte[3];
        check(in.read(buf, 0, 3) == 3 && buf[0] == 'a', "region read");
        check(in.read(buf) == 3 && new String(buf, 0, 3).equals("def"), "remaining read");
        check(in.read(buf) == -1, "EOF bulk");

        // 3. non-ASCII: only the low 8 bits of each char survive
        in = new StringBufferInputStream("é");
        int v = in.read();
        check(v == 0xE9, "low 8 bits of 'é' delivered: " + Integer.toHexString(v));

        // 4. skip and available
        in = new StringBufferInputStream("12345");
        check(in.available() == 5, "available is the full length");
        check(in.skip(2) == 2 && in.available() == 3, "skip(2)");
        check(in.read() == '3', "position after skip");
        check(in.skip(99) == 2, "skip clamps to remaining");

        // 5. reset rewinds to the start (no mark needed)
        in = new StringBufferInputStream("xy");
        in.skip(2);
        in.reset();
        check(in.read() == 'x', "reset rewinds to position 0");

        // 6. length snapshot taken at construction (Strings are immutable anyway)
        StringBufferInputStream lv = new StringBufferInputStream("12");
        check(lv.read() == '1' && lv.read() == '2' && lv.read() == -1, "exact length read");

        // 7. close() does not prevent reads (base InputStream no-op)
        in = new StringBufferInputStream("q");
        in.close();
        check(in.read() == 'q', "close is a no-op here");

        // 8. empty string
        StringBufferInputStream e = new StringBufferInputStream("");
        check(e.read() == -1 && e.available() == 0, "empty source");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

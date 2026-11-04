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

    // ---- 深化追加（契约级），行为全部经 JDK-27 实测锁定 ----

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

    /** 构造 null → NPE；低 8 位截断矩阵。 */
    static void testConstructorAndLowByte() throws Exception {
        checkThrows(NullPointerException.class, () -> new StringBufferInputStream(null),
                    "ctor(null) -> NPE");
        check(new StringBufferInputStream("ሴ").read() == 0x34,
              "\\u1234 truncated to low 8 bits (0x34)");
        check(new StringBufferInputStream("ÿ").read() == 0xFF, "\\u00FF survives as 0xFF");
        check(new StringBufferInputStream("Ā").read() == 0x00, "\\u0100 becomes 0x00");
        check(new StringBufferInputStream("￿").read() == 0xFF, "\\uFFFF becomes 0xFF");
        StringBufferInputStream az = new StringBufferInputStream("Az09");
        check(az.read() == 65 && az.read() == 122 && az.read() == 48 && az.read() == 57,
              "ASCII values 65,122,48,57");
    }

    /** read(b,off,len) 参数矩阵、部分填充与 SBIS 特有的 EOF 优先语义。 */
    static void testReadBoundsAndPartial() throws Exception {
        StringBufferInputStream in = new StringBufferInputStream("abcde");
        byte[] buf = new byte[8];
        check(in.read(buf, 1, 5) == 5 && buf[1] == 'a' && buf[5] == 'e',
              "region read fills off..off+len");
        check(buf[0] == 0 && buf[6] == 0, "slots outside region untouched");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(buf, 1, 8),
                    "read(b,1,8) overflow -> IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(buf, -1, 0),
                    "read(b,-1,0) -> IOOBE");
        checkThrows(NullPointerException.class, () -> in.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(NullPointerException.class, () -> in.read((byte[]) null),
                    "read(null) -> NPE");
        check(in.read(buf, 1, 0) == -1,
              "SBIS bulk read at EOF returns -1 even for len==0 (EOF checked first)");
        check(in.read(new byte[0]) == -1, "read(empty array) at EOF also -1");
        StringBufferInputStream notEof = new StringBufferInputStream("q");
        check(notEof.read(buf, 1, 0) == 0, "read(b,1,0) == 0 while data remains");

        StringBufferInputStream part = new StringBufferInputStream("ab");
        byte[] big = new byte[10];
        check(part.read(big) == 2 && big[0] == 'a' && big[1] == 'b',
              "partial fill returns available count");
        check(big[2] == 0, "unfilled slot untouched");
        check(part.read(big) == -1, "EOF bulk read -1");
        check(part.read() == -1 && part.read() == -1, "EOF single read stays -1");
    }

    /** skip 语义与 mark/reset。 */
    static void testSkipMarkReset() throws Exception {
        StringBufferInputStream in = new StringBufferInputStream("abcdef");
        check(in.skip(-1) == 0 && in.skip(0) == 0, "negative/zero skip return 0");
        check(in.read() == 'a', "no movement from no-op skips");
        check(in.skip(2) == 2 && in.read() == 'd', "skip(2) precise");
        check(in.skip(99) == 2, "skip past end clamps to remaining");
        check(in.read() == -1, "EOF after clamped skip");
        check(new StringBufferInputStream("").skip(3) == 0, "empty source skip 0");

        StringBufferInputStream mk = new StringBufferInputStream("xyz");
        check(!mk.markSupported(), "markSupported false");
        mk.read();
        mk.mark(4);                                       // 基类 no-op
        check(mk.read() == 'y', "mark consumes nothing");
        mk.reset();
        check(mk.read() == 'x', "reset rewinds to 0 regardless of mark");
        mk.close();
        mk.close();
        passed++;
        check(mk.read() == 'y', "reads still served after close (no-op semantics)");
    }

    /** available 与位置、reset 后 available 恢复。 */
    static void testAvailableLifecycle() throws Exception {
        StringBufferInputStream in = new StringBufferInputStream("12345");
        check(in.available() == 5, "initial available == full length");
        in.read();
        check(in.available() == 4, "available decrements");
        in.skip(2);
        check(in.available() == 2, "available after skip (pos 3 of 5)");
        in.reset();
        check(in.available() == 5, "available restored after reset");
        in.skip(5);
        check(in.available() == 0, "available 0 at EOF");
        check(in.read() == -1, "EOF read -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorAndLowByte();
        testReadBoundsAndPartial();
        testSkipMarkReset();
        testAvailableLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

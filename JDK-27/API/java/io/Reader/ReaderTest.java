/*
 * @test
 * @summary java.io.Reader conformance: white-box structure (abstract character input
 *          root: Readable + Closeable) + black-box behavior (default read()/read(char[])
 *          derived from read(char[],int,int); skip/ready/mark/reset defaults;
 *          read(CharBuffer); readAllLines/readAllAsString/transferTo; Reader.of;
 *          nullReader; skip argument checks).
 * @run main ReaderTest
 */

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.CharBuffer;
import java.util.List;

public class ReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal subclass implementing only read(char[],int,int) and close(). */
    static class ArrayReader extends Reader {
        final char[] data;
        int pos;
        ArrayReader(char[] data) { this.data = data; }
        @Override public int read(char[] cbuf, int off, int len) {
            if (pos >= data.length) return -1;
            int n = 0;
            while (n < len && pos < data.length) cbuf[off + n++] = data[pos++];
            return n;
        }
        @Override public void close() { }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Reader");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.Readable.class.isAssignableFrom(c), "implements Readable");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(Modifier.isAbstract(c.getMethod("read", char[].class, int.class, int.class)
                                     .getModifiers()), "read(char[],int,int) abstract");
        check(Modifier.isAbstract(c.getMethod("close").getModifiers()), "close abstract");
        for (String m : new String[]{"read", "skip", "ready", "markSupported",
                                     "mark", "reset", "transferTo", "readAllLines",
                                     "readAllAsString"})
            check(has(c, m), "has method " + m);
        check(Modifier.isProtected(c.getDeclaredField("lock").getModifiers()),
              "protected lock field for subclass synchronization");
        check(Modifier.isStatic(c.getMethod("nullReader").getModifiers()), "nullReader factory");
        check(Modifier.isStatic(c.getMethod("of", CharSequence.class).getModifiers()),
              "Reader.of(CharSequence) factory");
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. default single-char read and bulk read derive from read(char[],int,int)
        ArrayReader r = new ArrayReader(new char[]{'a', 'b', 'c', 'd'});
        check(r.read() == 'a', "read() single char");
        char[] two = new char[2];
        check(r.read(two) == 2 && two[0] == 'b' && two[1] == 'c', "read(char[])");
        check(r.read() == 'd', "position shared across forms");
        check(r.read() == -1, "EOF via read()");
        check(r.read(new char[2]) == -1, "EOF via read(char[])");
        check(new ArrayReader(new char[0]).read(new char[1]) == -1, "empty reader EOF");
        check(new ArrayReader(new char[]{'z'}).read(new char[0]) == 0, "zero-length read 0");

        // 2. read(CharBuffer) via the Readable interface
        r = new ArrayReader(new char[]{'x', 'y', 'z'});
        CharBuffer cb = CharBuffer.allocate(10);
        check(r.read(cb) == 3, "read(CharBuffer) count");
        cb.flip();
        check("xyz".contentEquals(cb), "read(CharBuffer) contents");
        check(r.read(CharBuffer.allocate(2)) == -1, "read(CharBuffer) at EOF");

        // 3. skip: forward works, negative rejected
        r = new ArrayReader(new char[]{'a', 'b', 'c', 'd'});
        check(r.skip(2) == 2 && r.read() == 'c', "skip(2)");
        try {
            r.skip(-1);
            check(false, "skip(negative) must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 4. defaults: markSupported false, reset throws, ready false, mark no-op
        check(!r.markSupported(), "base markSupported false");
        try {
            r.reset();
            check(false, "base reset must throw");
        } catch (IOException ioe) {
            check("reset() not supported".equals(ioe.getMessage()),
                  "reset message: " + ioe.getMessage());
        }
        check(!r.ready(), "base ready false");
        try {
            r.mark(5);
            check(false, "base mark() must throw when unsupported");
        } catch (IOException ioe) {
            check("mark() not supported".equals(ioe.getMessage()),
                  "mark message: " + ioe.getMessage());
        }
        check(r.read() == 'd', "position unchanged by the failed mark");

        // 5. readAllLines / readAllAsString / transferTo
        Reader of = Reader.of("ab\ncd\n");
        check(of.readAllLines().equals(List.of("ab", "cd")), "readAllLines");
        check(Reader.of("ab\ncd").readAllAsString().equals("ab\ncd"), "readAllAsString");
        r = new ArrayReader(new char[]{'p', 'q', 'r'});
        StringWriter sw = new StringWriter();
        check(r.transferTo(sw) == 3 && "pqr".contentEquals(sw.getBuffer()),
              "transferTo count and contents");
        check(r.read() == -1, "transferTo drains the reader");

        // 6. Reader.of(CharSequence): non-mutating view of the sequence
        StringBuilder sb = new StringBuilder("live");
        Reader live = Reader.of(sb);
        check(live.read() == 'l', "Reader.of reads first char");
        sb.append("Z");                               // Reader.of sees later mutations
        live.skip(3);
        check(live.read() == 'Z', "Reader.of reflects subsequent CharSequence edits");

        // 7. nullReader: permanently empty
        Reader nr = Reader.nullReader();
        check(nr.read() == -1 && nr.read(new char[2]) == -1, "nullReader EOF");
        check(!nr.ready() && nr.skip(1) == 0, "nullReader not ready, skip 0");
        check(nr.readAllAsString().isEmpty(), "nullReader readAllAsString empty");
        nr.close();
        nr.close();

        // 8. polymorphic use: JDK readers through the base reference
        try (Reader sr = new java.io.StringReader("hi")) {
            check(sr.read() == 'h' && sr.read() == 'i', "StringReader via Reader ref");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

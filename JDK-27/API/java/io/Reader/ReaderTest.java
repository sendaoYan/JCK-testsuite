/*
 * @test
 * @summary java.io.Reader conformance: white-box structure (abstract character input
 *          root: Readable + Closeable) + black-box behavior (default read()/read(char[])
 *          derived from read(char[],int,int); skip/ready/mark/reset defaults;
 *          read(CharBuffer); readAllLines/readAllAsString/transferTo; Reader.of;
 *          nullReader; skip argument checks).
 *          Deepening: exact delegation shapes (read()/read(char[]) derive from
 *          read(char[],int,int) with len 1 / len cbuf.length, no internal loop),
 *          default skip loop-until-EOF semantics with negative -> IAE "skip value
 *          is negative" (verified message), read(CharBuffer) default details,
 *          nullReader full contract (incl. skip(-n) == 0, closed matrix), Reader.of
 *          deep (markSupported true, live CharSequence, null NPE), readAllLines
 *          line-terminator matrix, readAllAsString exactness, transferTo edges.
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

    /** Probe that records every read(char[],int,int) delegation. */
    static class Counting extends Reader {
        final char[] data;
        int pos;
        int calls;
        final StringBuilder lens = new StringBuilder();
        Counting(String s) { data = s.toCharArray(); }
        @Override public int read(char[] cbuf, int off, int len) {
            calls++;
            lens.append(len).append(',');
            if (len == 0) return 0;
            int n = 0;
            while (n < len && pos < data.length) cbuf[off + n++] = data[pos++];
            return n == 0 ? -1 : n;
        }
        @Override public void close() { }
    }

    interface Op { void run() throws Exception; }

    /** Runs r and returns the thrown Throwable, or null if nothing was thrown. */
    static Throwable capture(Op r) {
        try { r.run(); } catch (Throwable t) { return t; }
        return null;
    }

    // ---- exact delegation shapes of the derived read forms ----
    static void testDelegationShapes() throws Exception {
        Counting r = new Counting("abcde");
        check(r.read() == 'a', "read() returns the first char");
        check(r.calls == 1 && "1,".equals(r.lens.toString()),
              "read() delegates exactly one call with len 1");
        char[] three = new char[3];
        check(r.read(three) == 3 && "bcd".equals(new String(three)), "read(char[]) content");
        check(r.calls == 2 && "1,3,".equals(r.lens.toString()),
              "read(char[]) delegates one call with len == cbuf.length");
        check(r.read() == 'e', "position shared across all forms");
        check(r.read() == -1, "read() at EOF delegates and returns -1");
        check(r.calls == 4, "EOF read() still delegates");
        // the default read(char[]) does not loop over short reads
        final int[] tCalls = {0};
        Reader throttled = new Reader() {
            int round;
            @Override public int read(char[] cbuf, int off, int len) {
                tCalls[0]++;
                cbuf[off] = (char) ('A' + round++);
                return Math.min(len, 2);        // always reports a short read
            }
            @Override public void close() { }
        };
        char[] buf = new char[5];
        check(throttled.read(buf) == 2 && tCalls[0] == 1,
              "read(char[]) delegates once and returns the short count as-is");
        check(buf[2] == 0 && buf[3] == 0 && buf[4] == 0, "tail beyond the short read untouched");
    }

    // ---- default skip: loop reads until n satisfied or EOF; negative -> exact IAE ----
    static void testSkipLoopDeep() throws Exception {
        Counting r = new Counting("abcdefghij");
        check(r.skip(7) == 7, "skip(7) skips 7");
        check(r.calls == 1 && "7,".equals(r.lens.toString()),
              "one delegate call satisfies the skip: no extra EOF probe");
        check(r.read() == 'h', "position after skip");
        Counting e = new Counting("abc");
        check(e.skip(10) == 3, "skip past EOF returns only what was there");
        check(e.calls == 2, "default skip loops and calls read again after a short read");
        check("10,7,".equals(e.lens.toString()),
              "loop asks min(n, buffer) first, then the remainder");
        Counting z = new Counting("abc");
        check(z.skip(0) == 0 && z.calls == 0, "skip(0) delegates nothing");
        Counting neg = new Counting("abc");
        Throwable t = capture(() -> neg.skip(-1));
        check(t instanceof IllegalArgumentException, "skip(-1) -> IllegalArgumentException");
        check("skip value is negative".equals(t.getMessage()),
              "exact message \"skip value is negative\" (verified on JDK 8 and 27)");
        check(neg.calls == 0 && neg.read() == 'a', "rejected skip consumes nothing");
    }

    // ---- read(CharBuffer) default implementation details ----
    static void testCharBufferDeep() throws Exception {
        Counting r = new Counting("abcdef");
        CharBuffer cb = CharBuffer.allocate(4);
        check(r.read(cb) == 4, "reads exactly remaining() chars");
        cb.flip();
        check("abcd".contentEquals(cb), "content placed at the buffer position");
        check(r.calls == 1 && "4,".equals(r.lens.toString()),
              "one delegate call with len == remaining()");
        check(r.read(CharBuffer.allocate(4)) == 2, "short read near EOF passes through");
        check(r.read(CharBuffer.allocate(4)) == -1, "EOF -> -1");
        Counting z = new Counting("q");
        CharBuffer full = CharBuffer.allocate(1);
        full.put('x'); full.flip(); full.position(1);
        check(z.read(full) == 0, "zero remaining returns the len==0 result (0)");
        check(z.calls == 1 && "0,".equals(z.lens.toString()), "delegates once with len 0");
        checkThrowsRO(() -> new Counting("a").read(CharBuffer.wrap("x").asReadOnlyBuffer()));
        checkThrowsNP(() -> new Counting("a").read((CharBuffer) null));
    }

    static void checkThrowsRO(Op r) throws Exception {
        try { r.run(); check(false, "read-only buffer must throw"); }
        catch (java.nio.ReadOnlyBufferException expected) { passed++; }
    }

    static void checkThrowsNP(Op r) throws Exception {
        try { r.run(); check(false, "null buffer must throw"); }
        catch (NullPointerException expected) { passed++; }
    }

    // ---- nullReader full contract ----
    static void testNullReaderDeep() throws Exception {
        Reader nr = Reader.nullReader();
        check(nr.read(new char[3]) == -1, "nullReader read(char[]) -> -1");
        check(nr.read(CharBuffer.allocate(2)) == -1, "nullReader read(CharBuffer) -> -1");
        check(nr.skip(0) == 0, "nullReader skip(0) == 0");
        // nullReader does NOT use the base skip: a negative skip returns 0, no IAE
        check(nr.skip(-5) == 0, "nullReader skip(-5) == 0 (no IllegalArgumentException)");
        check(nr.readAllLines().isEmpty(), "nullReader readAllLines empty");
        check(nr.transferTo(new StringWriter()) == 0, "nullReader transferTo 0");
        Throwable npe = capture(() -> Reader.nullReader().read((CharBuffer) null));
        check(npe instanceof NullPointerException, "nullReader read(null CharBuffer) -> NPE");
        // closed: "Stream closed" for every op, close idempotent
        Reader c = Reader.nullReader();
        c.close();
        c.close();
        String closed = "Stream closed";
        Throwable t1 = capture(() -> c.read());
        check(t1 instanceof IOException && closed.equals(t1.getMessage()), "closed read(): \"Stream closed\"");
        Throwable t2 = capture(() -> c.ready());
        check(t2 instanceof IOException && closed.equals(t2.getMessage()), "closed ready(): \"Stream closed\"");
        Throwable t3 = capture(() -> c.skip(1));
        check(t3 instanceof IOException && closed.equals(t3.getMessage()), "closed skip(): \"Stream closed\"");
        Throwable t4 = capture(() -> c.readAllAsString());
        check(t4 instanceof IOException && closed.equals(t4.getMessage()), "closed readAllAsString(): \"Stream closed\"");
    }

    // ---- Reader.of deep ----
    static void testOfDeep() throws Exception {
        Throwable npe = capture(() -> Reader.of((CharSequence) null));
        check(npe instanceof NullPointerException, "Reader.of(null) -> NPE");
        // live CharSequence view: later edits are visible
        StringBuilder sb = new StringBuilder("abc");
        Reader live = Reader.of(sb);
        check(live.read() == 'a' && live.ready(), "of() reads and is ready");
        live.mark(3);
        sb.append("XY");                            // mutation after construction
        live.skip(2);                               // over 'b','c'
        check(live.read() == 'X' && live.read() == 'Y', "of() reflects later CharSequence edits");
        live.reset();
        check(live.read() == 'b', "of() supports mark/reset replay");
        check(live.markSupported(), "of() reports markSupported");
        // of("") is a permanently empty reader
        Reader empty = Reader.of("");
        check(empty.read() == -1 && empty.readAllAsString().isEmpty(), "of(\"\") always EOF");
        // close reports "Stream closed"
        Reader c = Reader.of("q");
        c.close();
        Throwable t = capture(() -> c.read());
        check(t instanceof IOException && "Stream closed".equals(t.getMessage()),
              "of() closed read(): \"Stream closed\"");
    }

    // ---- readAllLines line-terminator matrix / readAllAsString exactness ----
    static void testReadAllLinesDeep() throws Exception {
        check(Reader.of("a\r\nb\rc\nd").readAllLines().equals(List.of("a", "b", "c", "d")),
              "\\r\\n, \\r and \\n all terminate lines");
        check(Reader.of("no-newline").readAllLines().equals(List.of("no-newline")),
              "a missing trailing terminator still yields the last line");
        check(Reader.of("a\n").readAllLines().equals(List.of("a")),
              "a single trailing terminator does not create an empty last line");
        check(Reader.of("a\n\n").readAllLines().equals(List.of("a", "")),
              "an extra blank line yields an empty final line");
        check(Reader.of("").readAllLines().isEmpty(), "empty input yields no lines");
        // readAllLines is also a default method: works through a minimal subclass
        check(new Counting("l1\nl2\n").readAllLines().equals(List.of("l1", "l2")),
              "readAllLines default impl reads via read(char[],int,int)");
        // readAllAsString preserves every char including terminators and surrogates
        check(Reader.of("a\r\nb").readAllAsString().equals("a\r\nb"),
              "readAllAsString keeps \\r\\n verbatim");
        String sup = new String(Character.toChars(0x1F600));
        check(Reader.of(sup + "!").readAllAsString().equals(sup + "!"),
              "readAllAsString keeps surrogate pairs");
        check(new Counting("xy").readAllAsString().equals("xy"),
              "readAllAsString default impl via read(char[],int,int)");
        Reader c = Reader.of("q");
        c.close();
        Throwable t = capture(() -> c.readAllLines());
        check(t instanceof IOException, "readAllLines on a closed reader -> IOException");
    }

    // ---- transferTo edges ----
    static void testTransferToDeep() throws Exception {
        Throwable npe = capture(() -> Reader.of("a").transferTo(null));
        check(npe instanceof NullPointerException, "transferTo(null) -> NPE");
        check(Reader.of("").transferTo(new StringWriter()) == 0, "empty source transfers 0");
        // a failing sink's IOException propagates out of transferTo with its message
        java.io.Writer boom = new java.io.Writer() {
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("sink-boom");
            }
            public void flush() { }
            public void close() { }
        };
        Throwable t = capture(() -> new Counting("abcdef").transferTo(boom));
        check(t instanceof IOException && "sink-boom".equals(t.getMessage()),
              "sink failure propagates with its message");
        // transferTo into nullWriter discards but still drains
        Reader src = Reader.of("abc");
        check(src.transferTo(java.io.Writer.nullWriter()) == 3, "transferTo(nullWriter) returns the count");
        check(src.read() == -1, "source fully drained");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDelegationShapes();
        testSkipLoopDeep();
        testCharBufferDeep();
        testNullReaderDeep();
        testOfDeep();
        testReadAllLinesDeep();
        testTransferToDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

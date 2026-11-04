/*
 * @test
 * @summary java.io.FilterReader conformance: white-box structure (abstract decorator
 *          base with protected in) + black-box behavior (a minimal concrete subclass
 *          delegates reads/skip/ready/mark/reset/close to the wrapped reader;
 *          interception; access to the protected field).
 *          Deepening: exact 1:1 delegation targets (read() -> in.read() directly,
 *          read(char[]) -> in.read(c,0,len)), null ctor -> NPE, closed underlying
 *          reader surfaces "Stream closed" through the filter, bounds propagation,
 *          functional mark/reset replay through the decorator.
 * @run main FilterReaderTest
 */

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FilterReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Straight pass-through subclass (the base methods delegate to in). */
    static class PassThrough extends FilterReader {
        PassThrough(Reader in) { super(in); }
    }

    static class Recording extends Reader {
        final Reader delegate;
        int singles, bulks, skips, readies, marks, resets, closes;
        Recording(Reader d) { delegate = d; }
        public int read(char[] c, int o, int l) throws IOException { bulks++; return delegate.read(c, o, l); }
        public long skip(long n) throws IOException { skips++; return delegate.skip(n); }
        public boolean ready() throws IOException { readies++; return delegate.ready(); }
        public boolean markSupported() { return true; }
        public void mark(int rl) throws IOException { marks++; delegate.mark(rl); }
        public void reset() throws IOException { resets++; delegate.reset(); }
        public void close() throws IOException { closes++; delegate.close(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FilterReader");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.io.Reader"), "extends Reader");
        Constructor<?> k = c.getDeclaredConstructor(Reader.class);
        check(Modifier.isProtected(k.getModifiers()), "protected (Reader) ctor");
        check(Modifier.isProtected(c.getDeclaredField("in").getModifiers()), "protected in field");
        for (String m : new String[]{"read", "skip", "ready", "markSupported",
                                     "mark", "reset", "close"})
            check(has(c, m), "has method " + m);
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. pass-through reads share position with the wrapped reader
        Recording rec = new Recording(new StringReader("abcde"));
        PassThrough fr = new PassThrough(rec);
        check(fr.read() == 'a' && rec.bulks >= 1, "read() delegates (default reads 1-char buffer)");
        char[] two = new char[2];
        check(fr.read(two) == 2 && new String(two).startsWith("bc"), "read(char[]) delegates");
        check(fr.read() == 'd', "shared position");
        check(fr.ready() && rec.readies == 1, "ready delegates");
        check(fr.read() == 'e', "last char");

        // 2. mark/reset/markSupported pass through
        Recording mk = new Recording(new StringReader("xy"));
        PassThrough fm = new PassThrough(mk);
        check(fm.markSupported(), "markSupported delegates (true)");
        fm.mark(3);
        check(mk.marks == 1, "mark delegates");
        fm.reset();
        check(mk.resets == 1, "reset delegates");

        // 3. skip delegates
        Recording sk = new Recording(new StringReader("12345"));
        PassThrough fs = new PassThrough(sk);
        check(fs.skip(3) == 3 && sk.skips == 1, "skip delegates");
        check(fs.read() == '4', "position after skip");

        // 4. close delegates
        Recording cl = new Recording(new StringReader("z"));
        PassThrough fc = new PassThrough(cl);
        fc.close();
        check(cl.closes == 1, "close delegates");

        // 5. interception: a filtering subclass drops unwanted characters
        class VowelDropper extends FilterReader {
            VowelDropper(Reader in) { super(in); }
            @Override public int read(char[] cbuf, int off, int len) throws IOException {
                int n = super.read(cbuf, off, len);
                if (n <= 0) return n;
                int kept = 0;
                for (int i = 0; i < n; i++) {
                    char ch = cbuf[off + i];
                    if (ch != 'a' && ch != 'e' && ch != 'i' && ch != 'o' && ch != 'u') {
                        cbuf[off + kept++] = ch;
                    }
                }
                return kept;
            }
        }
        VowelDropper vd = new VowelDropper(new StringReader("banana"));
        char[] buf = new char[6];
        int total = 0;
        int n;
        while ((n = vd.read(buf, total, 6 - total)) > 0) total += n;
        check("bnn".equals(new String(buf, 0, total)), "vowels dropped: " + new String(buf, 0, total));

        // 6. subclass can reach the protected in field
        StringReader inner = new StringReader("q");
        class Reaching extends FilterReader {
            Reaching(Reader r) { super(r); }
            Reader inner() { return in; }
        }
        check(new Reaching(inner).inner() == inner, "protected in is the wrapped reader");

        // 7. a failing wrapped reader surfaces with its message
        class Failing extends Reader {
            public int read(char[] c, int o, int l) throws IOException { throw new IOException("inner"); }
            public void close() { }
        }
        try {
            new PassThrough(new Failing()).read();
            check(false, "wrapped failure must propagate");
        } catch (IOException ioe) {
            check("inner".equals(ioe.getMessage()), "original message kept");
        }
    }

    /** Probe with separate counters for every underlying method. */
    static class Counting extends Reader {
        int singles, bulks, skips, readies, marks, resets, closes;
        public int read() { singles++; return 's'; }
        public int read(char[] c, int o, int l) { bulks++; return l == 0 ? 0 : -1; }
        public long skip(long n) { skips++; return n; }
        public boolean ready() { readies++; return true; }
        public boolean markSupported() { return true; }
        public void mark(int rl) { marks++; }
        public void reset() { resets++; }
        public void close() { closes++; }
    }

    // ---- exact delegation targets, verified 1:1 ----
    static void testDelegationExact() throws Exception {
        Counting c = new Counting();
        PassThrough fr = new PassThrough(c);
        fr.read();
        check(c.singles == 1 && c.bulks == 0,
              "FilterReader.read() delegates to in.read() directly (not a 1-char buffer)");
        fr.read(new char[3], 0, 2);
        check(c.bulks == 1, "read(char[],off,len) delegates to the same-named method");
        fr.read(new char[3]);
        check(c.bulks == 2 && c.singles == 1,
              "read(char[]) uses the Reader default: in.read(cbuf,0,len)");
        fr.skip(5);
        check(c.skips == 1, "skip delegates");
        fr.ready();
        check(c.readies == 1, "ready delegates");
        fr.markSupported();
        check(c.marks + c.resets == 0, "markSupported does not touch mark/reset state");
        fr.mark(2);
        check(c.marks == 1, "mark delegates");
        fr.reset();
        check(c.resets == 1, "reset delegates");
        fr.close();
        check(c.closes == 1, "close delegates");
        // exactly one delegation per call, no extras
        check(c.singles == 1 && c.bulks == 2 && c.skips == 1 && c.readies == 1,
              "no hidden extra calls");
    }

    // ---- null constructor argument ----
    static void testNullConstructor() throws Exception {
        boolean npe = false;
        try {
            PassThrough p = new PassThrough(null);   // Reader(Reader lock) rejects null
            p.read();
        } catch (NullPointerException expected) {
            npe = true;
        }
        check(npe, "FilterReader(null) -> NPE (from the base Reader lock check)");
    }

    // ---- a closed underlying reader surfaces through the filter ----
    static void testClosedUnderlying() throws Exception {
        PassThrough fr = new PassThrough(new StringReader("q"));
        fr.close();                                   // delegates close to the StringReader
        try {
            fr.read();
            check(false, "read on a closed filter must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()),
                  "underlying message surfaces: " + ioe.getMessage());
        }
        try {
            fr.skip(1);
            check(false, "skip on a closed filter must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "skip surfaces the same message");
        }
        try {
            fr.ready();
            check(false, "ready on a closed filter must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "ready surfaces the same message");
        }
        try {
            fr.reset();
            check(false, "reset on a closed filter must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "reset surfaces the same message");
        }
    }

    // ---- bounds validation propagates through the decorator ----
    static void testBoundsPropagation() throws Exception {
        final PassThrough fr = new PassThrough(new StringReader("abc"));
        boolean ioobe1 = false;
        try { fr.read(new char[2], 0, 5); } catch (IndexOutOfBoundsException expected) { ioobe1 = true; }
        check(ioobe1, "read(cbuf,off,len) bounds are enforced by the underlying reader");
        boolean ioobe2 = false;
        try { fr.read(new char[2], -1, 1); } catch (IndexOutOfBoundsException expected) { ioobe2 = true; }
        check(ioobe2, "negative off propagates");
        // the stream is still usable after the failed calls
        check(fr.read() == 'a', "filter still usable after failed bounds checks");
    }

    // ---- functional mark/reset replay through the decorator ----
    static void testMarkResetThroughFilter() throws Exception {
        PassThrough fr = new PassThrough(new StringReader("abcdef"));
        check(fr.read() == 'a', "read through the filter");
        fr.mark(10);
        check(fr.read(new char[2]) == 2, "bulk read through the filter");
        fr.reset();
        check(fr.read() == 'b' && fr.read() == 'c', "reset replays through the filter");
        check(fr.ready(), "ready passthrough on a StringReader (always true)");
        // skip + reset interplay through the decorator
        fr.mark(10);
        fr.skip(2);
        fr.reset();
        check(fr.read() == 'd', "position after skip+reset through the filter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDelegationExact();
        testNullConstructor();
        testClosedUnderlying();
        testBoundsPropagation();
        testMarkResetThroughFilter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.io.FilterReader conformance: white-box structure (abstract decorator
 *          base with protected in) + black-box behavior (a minimal concrete subclass
 *          delegates reads/skip/ready/mark/reset/close to the wrapped reader;
 *          interception; access to the protected field).
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

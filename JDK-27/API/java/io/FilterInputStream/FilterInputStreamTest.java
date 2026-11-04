/*
 * @test
 * @summary java.io.FilterInputStream conformance: white-box structure (decorator base
 *          with protected in) + black-box behavior (every operation delegates to the
 *          wrapped stream: reads, bulk reads, skip, available, close, mark/reset
 *          pass-through; subclass override interception; null stream rejected).
 * @run main FilterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FilterInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Records every call that reaches the wrapped stream. */
    static class Recording extends InputStream {
        final InputStream delegate;
        int singleReads, bulkReads, skips, availables, marks, resets, closes;
        boolean markSupported = true;
        Recording(InputStream d) { delegate = d; }
        public int read() throws IOException { singleReads++; return delegate.read(); }
        public int read(byte[] b, int o, int l) throws IOException { bulkReads++; return delegate.read(b, o, l); }
        public long skip(long n) throws IOException { skips++; return delegate.skip(n); }
        public int available() throws IOException { availables++; return delegate.available(); }
        public void close() throws IOException { closes++; delegate.close(); }
        public synchronized void mark(int rl) { marks++; delegate.mark(rl); }
        public synchronized void reset() throws IOException { resets++; delegate.reset(); }
        public boolean markSupported() { return markSupported; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FilterInputStream");
        check(c.getSuperclass().getName().equals("java.io.InputStream"), "extends InputStream");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        Constructor<?> k = c.getDeclaredConstructor(InputStream.class);
        check(Modifier.isProtected(k.getModifiers()), "protected (InputStream) ctor");
        Field in = c.getDeclaredField("in");
        check(Modifier.isProtected(in.getModifiers()), "protected in field");
        check(Modifier.isVolatile(in.getModifiers()), "in is volatile (subclass-mutable)");
        for (String m : new String[]{"read", "skip", "available", "close", "mark",
                                     "reset", "markSupported"})
            check(has(c, m), "has method " + m);
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. reads, skip, available and close all pass through
        Recording rec = new Recording(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        FilterInputStream fis = new FilterInputStream(rec) { };
        check(fis.read() == 1 && rec.singleReads == 1, "read() delegates");
        byte[] two = new byte[2];
        check(fis.read(two) == 2 && two[0] == 2 && rec.bulkReads == 1, "read(byte[]) delegates");
        check(fis.available() == 2 && rec.availables == 1, "available delegates");
        check(fis.read() == 4, "state shared with wrapped stream");
        check(fis.skip(10) == 1 && rec.skips == 1, "skip delegates (clamped to remaining 1)");
        fis.close();
        check(rec.closes == 1, "close delegates");

        // 2. markSupported/mark/reset pass through
        Recording mk = new Recording(new ByteArrayInputStream(new byte[]{9}));
        FilterInputStream fm = new FilterInputStream(mk) { };
        check(fm.markSupported(), "markSupported passes through (true)");
        mk.markSupported = false;
        check(!fm.markSupported(), "markSupported passes through (false)");
        fm.mark(4);
        check(mk.marks == 1, "mark delegates");
        fm.reset();
        check(mk.resets == 1, "reset delegates");

        // 3. subclass interception: a counting decorator wraps its parent chain
        class Counting extends FilterInputStream {
            int seen;
            Counting(InputStream in) { super(in); }
            @Override public int read() throws IOException {
                int v = super.read();
                if (v != -1) seen++;
                return v;
            }
        }
        Counting cnt = new Counting(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        while (cnt.read() != -1) { }
        check(cnt.seen == 3, "subclass saw every byte");

        // 4. subclass can see the protected in field directly
        class Reaching extends FilterInputStream {
            Reaching(InputStream in) { super(in); }
            InputStream inner() { return in; }
        }
        ByteArrayInputStream inner = new ByteArrayInputStream(new byte[]{7});
        Reaching reach = new Reaching(inner);
        check(reach.inner() == inner, "protected in is the wrapped stream");

        // 5. JDK subclasses built on it keep behavior (PushbackInputStream)
        java.io.PushbackInputStream pb =
            new java.io.PushbackInputStream(new ByteArrayInputStream(new byte[]{5}), 1);
        check(pb.read() == 5, "PushbackInputStream works over FilterInputStream");

        // 6. a failing wrapped stream surfaces through the filter
        class Failing extends InputStream {
            public int read() throws IOException { throw new IOException("inner failure"); }
            public int read(byte[] b, int o, int l) throws IOException { throw new IOException("inner failure"); }
        }
        FilterInputStream bad = new FilterInputStream(new Failing()) { };
        try {
            bad.read();
            check(false, "wrapped failure must propagate");
        } catch (IOException ioe) {
            check("inner failure".equals(ioe.getMessage()), "original message kept");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

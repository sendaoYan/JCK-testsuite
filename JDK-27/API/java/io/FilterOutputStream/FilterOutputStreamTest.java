/*
 * @test
 * @summary java.io.FilterOutputStream conformance: white-box structure (decorator base
 *          with protected out) + black-box behavior (write delegation semantics,
 *          flush/close contract of the base (close implies flush), a failing
 *          underlying stream surfaces, subclass interception).
 * @run main FilterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FilterOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Underlying sink that records calls. */
    static class Recording extends OutputStream {
        int singleWrites, bulkWrites, flushes, closes;
        boolean closed;
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        public void write(int b) { singleWrites++; data.write(b); }
        public void write(byte[] b, int off, int len) { bulkWrites++; data.write(b, off, len); }
        public void flush() { flushes++; }
        public void close() { closes++; closed = true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FilterOutputStream");
        check(c.getSuperclass().getName().equals("java.io.OutputStream"), "extends OutputStream");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        Constructor<?> k = c.getDeclaredConstructor(OutputStream.class);
        check(Modifier.isPublic(k.getModifiers()), "public (OutputStream) ctor");
        check(Modifier.isProtected(c.getDeclaredField("out").getModifiers()), "protected out");
        for (String m : new String[]{"write", "flush", "close"})
            check(has(c, m), "has method " + m);
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. single writes pass through
        Recording rec = new Recording();
        FilterOutputStream fos = new FilterOutputStream(rec);
        fos.write(65);
        check(rec.singleWrites == 1 && rec.data.size() == 1, "write(int) delegates");

        // 2. write(byte[]) and write(byte[],off,len) reach the sink with exact contents
        fos.write(new byte[]{66, 67});
        fos.write(new byte[]{88, 89, 90}, 1, 1);
        check(java.util.Arrays.equals(rec.data.toByteArray(),
                                      new byte[]{'A', 'B', 'C', 'Y'}),
              "bulk writes preserved region: " + rec.data);

        // 3. flush delegates
        fos.flush();
        check(rec.flushes >= 1, "flush delegates");

        // 4. close(): flushes then closes (FilterOutputStream close = flush + close)
        fos.close();
        check(rec.closes >= 1 && rec.flushes >= 2, "close implies a final flush");

        // 5. subclass interception (a classic logging decorator)
        class Upper extends FilterOutputStream {
            Upper(OutputStream o) { super(o); }
            @Override public void write(int b) throws IOException {
                super.write(Character.toUpperCase(b));
            }
        }
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        try (Upper u = new Upper(dest)) {
            u.write('a');
            u.write('b');
        }
        check("AB".equals(dest.toString()), "subclass intercepted every byte");

        // 6. subclass can reach the protected out field
        class Reaching extends FilterOutputStream {
            Reaching(OutputStream o) { super(o); }
            OutputStream inner() { return out; }
        }
        ByteArrayOutputStream inner = new ByteArrayOutputStream();
        check(new Reaching(inner).inner() == inner, "protected out is the wrapped stream");

        // 7. a failing sink surfaces with its message
        class Failing extends OutputStream {
            public void write(int b) throws IOException { throw new IOException("sink broken"); }
        }
        FilterOutputStream bad = new FilterOutputStream(new Failing());
        try {
            bad.write(1);
            check(false, "sink failure must propagate");
        } catch (IOException ioe) {
            check("sink broken".equals(ioe.getMessage()), "original message kept");
        }

        // 8. JDK subclass PrintStream builds on this base
        java.io.PrintStream ps = new java.io.PrintStream(new ByteArrayOutputStream());
        ps.print("ok");
        ps.flush();
        check(!ps.checkError(), "PrintStream (a FilterOutputStream) works");
        ps.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

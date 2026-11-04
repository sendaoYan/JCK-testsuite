/*
 * @test
 * @summary java.io.FilterWriter conformance: white-box structure (abstract decorator
 *          base with protected out) + black-box behavior (a minimal concrete subclass
 *          delegates write(int)/write(char[],int,int)/write(String,int,int)/flush/close
 *          to the wrapped writer; interception; protected field access).
 *          Deepening: exact delegation targets per overload (write(int) -> out.write(int),
 *          inherited write(char[])/write(String) -> the 3-arg forms, append routes
 *          through write(int)/write(String)), null ctor -> NPE, closed underlying
 *          writer surfaces through the filter, bounds propagation.
 * @run main FilterWriterTest
 */

import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FilterWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class PassThrough extends FilterWriter {
        PassThrough(Writer out) { super(out); }
    }

    static class Recording extends Writer {
        final Writer delegate;
        int writes, flushes, closes;
        Recording(Writer d) { delegate = d; }
        public void write(char[] c, int o, int l) throws IOException { writes++; delegate.write(c, o, l); }
        public void write(int c) throws IOException { writes++; delegate.write(c); }
        public void write(String s, int o, int l) throws IOException { writes++; delegate.write(s, o, l); }
        public void flush() throws IOException { flushes++; delegate.flush(); }
        public void close() throws IOException { closes++; delegate.close(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FilterWriter");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.io.Writer"), "extends Writer");
        Constructor<?> k = c.getDeclaredConstructor(Writer.class);
        check(Modifier.isProtected(k.getModifiers()), "protected (Writer) ctor");
        check(Modifier.isProtected(c.getDeclaredField("out").getModifiers()), "protected out field");
        for (String m : new String[]{"write", "flush", "close"})
            check(has(c, m), "has method " + m);
        check(hasOverload(c, "write", char[].class, int.class, int.class),
              "write(char[],int,int) abstract");
        check(hasOverload(c, "write", int.class), "write(int) declared here");
        check(hasOverload(c, "write", String.class, int.class, int.class),
              "write(String,int,int) declared here");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    static boolean hasOverload(Class<?> c, String m, Class<?>... ps) {
        try { c.getMethod(m, ps); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. every write form delegates with region semantics
        StringWriter sink = new StringWriter();
        Recording rec = new Recording(sink);
        PassThrough fw = new PassThrough(rec);
        fw.write('a');
        fw.write("bcdef".toCharArray(), 1, 2);
        fw.write("XYZ", 1, 1);
        check("acdY".equals(sink.toString()), "write forms delegate: " + sink);

        // 2. append uses the Writer defaults (routes to write(char[],int,int))
        StringWriter s2 = new StringWriter();
        PassThrough f2 = new PassThrough(s2);
        f2.append('Q');
        f2.append("RS");
        f2.append("TUV", 1, 2);
        check("QRSU".contentEquals(s2.getBuffer()), "append through the decorator");

        // 3. flush and close delegate
        Recording r3 = new Recording(new StringWriter());
        PassThrough f3 = new PassThrough(r3);
        f3.flush();
        f3.flush();
        check(r3.flushes == 2, "flush delegates");
        f3.close();
        check(r3.closes == 1, "close delegates");

        // 4. interception: an uppercasing decorator
        class Upper extends FilterWriter {
            Upper(Writer o) { super(o); }
            @Override public void write(int c) throws IOException {
                super.write(Character.toUpperCase(c));
            }
        }
        StringWriter dst = new StringWriter();
        try (Upper u = new Upper(dst)) {
            u.write('a');
            u.write('b');
        }
        check("AB".contentEquals(dst.getBuffer()), "subclass intercepted every char");

        // 5. subclass can reach the protected out field
        StringWriter inner = new StringWriter();
        class Reaching extends FilterWriter {
            Reaching(Writer w) { super(w); }
            Writer inner() { return out; }
        }
        check(new Reaching(inner).inner() == inner, "protected out is the wrapped writer");

        // 6. a failing sink surfaces with its message
        class Failing extends Writer {
            public void write(char[] c, int o, int l) throws IOException { throw new IOException("sink"); }
            public void flush() { }
            public void close() { }
        }
        try {
            new PassThrough(new Failing()).write('x');
            check(false, "sink failure must propagate");
        } catch (IOException ioe) {
            check("sink".equals(ioe.getMessage()), "original message kept");
        }
    }

    /** Probe with separate counters for every underlying write form. */
    static class Counting extends Writer {
        int intWrites, bulkWrites, strWrites, flushes, closes;
        public void write(int c) { intWrites++; }
        public void write(char[] c, int o, int l) { bulkWrites++; }
        public void write(String s, int o, int l) { strWrites++; }
        public void flush() { flushes++; }
        public void close() { closes++; }
    }

    // ---- exact delegation targets per overload (verified 1:1) ----
    static void testDelegationExact() throws Exception {
        Counting c = new Counting();
        PassThrough fw = new PassThrough(c);
        fw.write('x');
        check(c.intWrites == 1 && c.bulkWrites == 0 && c.strWrites == 0,
              "write(int) delegates to out.write(int) directly");
        fw.write(new char[3], 0, 2);
        check(c.bulkWrites == 1, "write(char[],off,len) delegates to the same-named form");
        fw.write("ab", 0, 1);
        check(c.strWrites == 1, "write(String,off,len) delegates to the same-named form");
        fw.write("full".toCharArray());                 // inherited Writer.write(char[])
        check(c.bulkWrites == 2 && c.strWrites == 1, "inherited write(char[]) -> out.write(c,0,len)");
        fw.write("str");                                // inherited Writer.write(String)
        check(c.strWrites == 2 && c.bulkWrites == 2, "inherited write(String) -> out.write(s,0,len)");
        // append uses the Writer defaults and routes through the FilterWriter overrides
        fw.append('A');
        check(c.intWrites == 2 && c.bulkWrites == 2,
              "append(char) routes through the write(int) override");
        fw.append("BC");
        check(c.strWrites == 3 && c.intWrites == 2,
              "append(CharSequence) routes through write(String) -> out.write(String,off,len)");
        fw.append("DEF", 1, 2);
        check(c.strWrites == 4, "append(csq,s,e) routes through the String region form");
        check(c.flushes == 0 && c.closes == 0, "no writes imply flush/close");
        fw.flush();
        fw.flush();
        check(c.flushes == 2, "flush delegates 1:1");
        fw.close();
        check(c.closes == 1, "close delegates 1:1");
    }

    // ---- null constructor argument ----
    static void testNullConstructor() throws Exception {
        boolean npe = false;
        try {
            PassThrough p = new PassThrough(null);      // Writer(Object lock) rejects null
            p.write('x');
        } catch (NullPointerException expected) {
            npe = true;
        }
        check(npe, "FilterWriter(null) -> NPE (from the base Writer lock check)");
    }

    // ---- a closed/failing underlying writer surfaces through the filter ----
    static class ClosedSink extends Writer {
        boolean closed;
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (closed) throw new IOException("Stream closed");
        }
        public void flush() throws IOException {
            if (closed) throw new IOException("Stream closed");
        }
        public void close() { closed = true; }
    }

    static void testClosedUnderlying() throws Exception {
        ClosedSink sink = new ClosedSink();
        PassThrough fw = new PassThrough(sink);
        fw.write('a');                                  // fine while open
        fw.close();                                     // delegates close to the sink
        try {
            fw.write('b');
            check(false, "write on a closed filter must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()),
                  "underlying message surfaces: " + ioe.getMessage());
        }
        try {
            fw.write("xy".toCharArray(), 0, 2);
            check(false, "bulk write on a closed filter must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "bulk write surfaces the message");
        }
        try {
            fw.flush();
            check(false, "flush on a closed filter must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "flush surfaces the message");
        }
    }

    // ---- bounds validation propagates through the decorator ----
    static void testBoundsPropagation() throws Exception {
        StringWriter sink = new StringWriter();
        PassThrough fw = new PassThrough(sink);
        boolean e1 = false;
        try { fw.write(new char[2], 0, 5); } catch (IndexOutOfBoundsException expected) { e1 = true; }
        check(e1, "write(char[],off,len) bounds propagate");
        boolean e2 = false;
        try { fw.write("abc", 0, 9); } catch (IndexOutOfBoundsException expected) { e2 = true; }
        check(e2, "write(String,off,len) bounds propagate");
        boolean e3 = false;
        try { fw.write((char[]) null, 0, 1); } catch (NullPointerException expected) { e3 = true; }
        check(e3, "write((char[])null,0,1) NPE propagates");
        // still usable after the failed calls
        fw.write('k');
        check("k".equals(sink.toString()), "filter still usable after failed writes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDelegationExact();
        testNullConstructor();
        testClosedUnderlying();
        testBoundsPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.io.FilterWriter conformance: white-box structure (abstract decorator
 *          base with protected out) + black-box behavior (a minimal concrete subclass
 *          delegates write(int)/write(char[],int,int)/write(String,int,int)/flush/close
 *          to the wrapped writer; interception; protected field access).
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

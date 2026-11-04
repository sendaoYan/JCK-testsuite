/*
 * @test
 * @summary java.io.OutputStream conformance: white-box structure (abstract byte output
 *          root) + black-box behavior (write(int) abstract; write(byte[]) delegating to
 *          write(byte[],int,int); default write loop; flush/close no-ops; null
 *          argument and bounds checking; nullOutputStream).
 * @run main OutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class OutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal subclass implementing only write(int). */
    static class Sink extends OutputStream {
        final java.util.ArrayList<Byte> bytes = new java.util.ArrayList<>();
        int flushes, closes;
        @Override public void write(int b) { bytes.add((byte) b); }
        @Override public void flush() { flushes++; }
        @Override public void close() { closes++; }
    }

    /** Only write(int): everything else inherited. */
    static class Bare extends OutputStream {
        final StringBuilder sb = new StringBuilder();
        @Override public void write(int b) { sb.append((char) (b & 0xFF)); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.OutputStream");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.io.Closeable.class.isAssignableFrom(c), "Closeable");
        check(java.io.Flushable.class.isAssignableFrom(c), "Flushable");
        check(Modifier.isAbstract(c.getMethod("write", int.class).getModifiers()),
              "write(int) abstract");
        for (String m : new String[]{"write", "write", "flush", "close"})
            check(has(c, m), "has method " + m);
        check(Modifier.isStatic(c.getMethod("nullOutputStream").getModifiers()),
              "static nullOutputStream factory");
        int ctors = c.getDeclaredConstructors().length;
        check(ctors == 1 && Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()),
              "single public no-arg constructor");
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. default write(byte[]) delegates to write(byte[],int,int) -> write(int)
        Bare b = new Bare();
        b.write(new byte[]{'a', 'b', 'c'});
        check("abc".contentEquals(b.sb), "write(byte[]) reaches write(int)");
        b.write(new byte[]{'X', 'Y'}, 0, 0);
        check("abc".contentEquals(b.sb), "zero-length write(byte[]) writes nothing");
        b.write(new byte[]{'-', 'd', 'e', '-'}, 1, 2);
        check("abcde".contentEquals(b.sb), "write(byte[],off,len) region only");

        // 2. the default write(byte[],int,int) loops one byte at a time
        class Counting extends OutputStream {
            int calls;
            @Override public void write(int x) { calls++; }
        }
        Counting c2 = new Counting();
        c2.write(new byte[7], 2, 5);
        check(c2.calls == 5, "default region write calls write(int) once per byte");

        // 3. argument checking on the base class
        try {
            new Sink().write((byte[]) null);
            check(false, "write(null) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new Sink().write(null, 0, 1);
            check(false, "write(null,off,len) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new Sink().write(new byte[2], 2, 1);
            check(false, "out-of-bounds write must throw");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }

        // 4. default flush()/close() are no-ops (overridable)
        class Defaults extends OutputStream {
            @Override public void write(int x) { }
        }
        Defaults d = new Defaults();
        d.flush();
        d.close();
        d.write(1);
        passed++;                                     // none of the above interfered

        // 5. subclasses see overridden flush/close invoked through the base reference
        Sink s = new Sink();
        OutputStream os = s;
        os.write(65);
        os.flush();
        os.flush();
        os.close();
        check(s.bytes.size() == 1 && s.bytes.get(0) == 65, "write via base reference");
        check(s.flushes == 2, "flush invoked twice");
        check(s.closes == 1, "close invoked once");

        // 6. nullOutputStream: accepts and discards everything, close is final
        OutputStream nullOs = OutputStream.nullOutputStream();
        nullOs.write(7);
        nullOs.write(new byte[]{1, 2, 3});
        nullOs.write(new byte[]{9, 9, 9}, 1, 1);
        nullOs.flush();
        nullOs.close();
        passed++;                                     // all discarded silently
        try {
            nullOs.write(1);
            check(false, "nullOutputStream after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()),
                  "message: " + ioe.getMessage());
        }

        // 7. flush/close idempotence through a concrete JDK stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write('h');
        baos.flush();
        baos.flush();
        baos.close();
        baos.write('i');                              // BAOS tolerates post-close writes
        check(java.util.Arrays.equals(baos.toByteArray(), new byte[]{'h', 'i'}),
              "ByteArrayOutputStream unaffected (subclass semantics)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

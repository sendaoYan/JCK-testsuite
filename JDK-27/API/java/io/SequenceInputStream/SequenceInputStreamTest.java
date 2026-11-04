/*
 * @test
 * @summary java.io.SequenceInputStream conformance: white-box structure + black-box
 *          behavior (two-stream and Enumeration constructors, sequential exhaustion,
 *          bulk reads do not cross stream boundaries, available reflects only the
 *          current stream, close closes every stream, empty/EOF transitions).
 * @run main SequenceInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class SequenceInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Tracked extends ByteArrayInputStream {
        final String name;
        boolean closed;
        Tracked(String name, byte[] data) { super(data); this.name = name; }
        @Override public void close() throws IOException { closed = true; super.close(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.SequenceInputStream");
        check(c.getSuperclass().getName().equals("java.io.InputStream"), "extends InputStream");
        check(c.getConstructor(InputStream.class, InputStream.class) != null,
              "ctor (InputStream, InputStream)");
        check(c.getConstructor(Enumeration.class) != null, "ctor (Enumeration)");
        for (String m : new String[]{"read", "available", "close", "transferTo"})
            check(has(c, m), "has method " + m);
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. two-stream constructor: reads exhaust the first then the second
        try (SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3, 4}))) {
            check(sis.read() == 1 && sis.read() == 2 && sis.read() == 3 && sis.read() == 4,
                  "sequential reads across the boundary");
            check(sis.read() == -1, "EOF after both drained");
        }

        // 2. bulk read does NOT cross the stream boundary in one call
        try (SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3, 4}))) {
            byte[] four = new byte[4];
            int first = sis.read(four);
            check(first == 2 && four[0] == 1 && four[1] == 2,
                  "bulk read stops at the first stream's end: " + first);
            int second = sis.read(four);
            check(second == 2 && four[0] == 3 && four[1] == 4, "next bulk read drains the second");
            check(sis.read(four) == -1, "EOF");
        }

        // 3. available() reports only the current stream
        try (SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3}),
                new ByteArrayInputStream(new byte[]{4, 5}))) {
            check(sis.available() == 3, "available of the active stream");
            sis.read(new byte[3]);
            check(sis.available() == 0, "first drained");
            check(sis.read() == 4, "transition to the second");
            check(sis.available() == 1, "available of the second stream");
        }

        // 4. Enumeration constructor with three streams (Vector/Enumeration path)
        Vector<InputStream> v = new Vector<>(List.of(
                new ByteArrayInputStream(new byte[]{'a'}),
                new ByteArrayInputStream(new byte[]{'b'}),
                new ByteArrayInputStream(new byte[]{'c'})));
        try (SequenceInputStream sis = new SequenceInputStream(v.elements())) {
            check(sis.read() == 'a' && sis.read() == 'b' && sis.read() == 'c' && sis.read() == -1,
                  "Enumeration of three streams");
        }

        // 5. empty enumeration yields immediate EOF
        try (SequenceInputStream sis = new SequenceInputStream(
                Collections.<InputStream>emptyEnumeration())) {
            check(sis.read() == -1, "empty enumeration EOF");
            check(sis.read(new byte[2]) == -1, "bulk EOF");
        }

        // 6. an empty first stream transitions silently
        try (SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[0]),
                new ByteArrayInputStream(new byte[]{7}))) {
            check(sis.read() == 7, "empty head skipped");
        }

        // 7. close closes every stream that was ever current
        Tracked t1 = new Tracked("one", new byte[]{1});
        Tracked t2 = new Tracked("two", new byte[]{2});
        Tracked t3 = new Tracked("three", new byte[]{3});
        SequenceInputStream sis = new SequenceInputStream(
            java.util.Collections.enumeration(List.of(t1, t2, t3)));
        check(sis.read() == 1 && sis.read() == 2 && sis.read() == 3, "read all three");
        sis.close();
        check(t1.closed && t2.closed && t3.closed, "close reached every stream");

        // 8. a stream that was never reached is still closed
        Tracked unreached = new Tracked("unreached", new byte[]{9});
        try (SequenceInputStream s2 = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}), unreached)) {
            s2.read();                                 // never consumed the second
        }
        check(unreached.closed, "try-with-resources closed the unreached stream too");

        // 9. transferTo spans the concatenation
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (SequenceInputStream s3 = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3}))) {
            check(s3.transferTo(bos) == 3
                      && java.util.Arrays.equals(bos.toByteArray(), new byte[]{1, 2, 3}),
                  "transferTo concatenates");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

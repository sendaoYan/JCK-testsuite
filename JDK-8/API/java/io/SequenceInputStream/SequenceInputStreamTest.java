/*
 * @test
 * @summary java.io.SequenceInputStream API conformance: full contract coverage.
 *          Two-stream and Enumeration constructors, sequential exhaustion, bulk reads
 *          do not cross stream boundaries, available reflects only the current stream,
 *          empty enumeration/empty head transitions, close closes every stream.
 * @run main SequenceInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

public class SequenceInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class Tracked extends ByteArrayInputStream {
        final String name;
        boolean closed;
        Tracked(String name, byte[] data) { super(data); this.name = name; }
        @Override public void close() throws IOException { closed = true; super.close(); }
    }

    // ---- 1. two-stream constructor: sequential exhaustion ----
    static void testTwoStreamConstructor() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3, 4}));
        check(sis.read() == 1 && sis.read() == 2 && sis.read() == 3 && sis.read() == 4,
              "sequential reads across the boundary");
        check(sis.read() == -1, "EOF after both drained");
    }

    // ---- 2. bulk read does not cross the boundary ----
    static void testBulkReadBoundary() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3, 4}));
        byte[] four = new byte[4];
        int first = sis.read(four);
        check(first == 2 && four[0] == 1 && four[1] == 2,
              "bulk read stops at first stream's end");
        int second = sis.read(four);
        check(second == 2 && four[0] == 3 && four[1] == 4, "next bulk read drains second");
        check(sis.read(four) == -1, "EOF");

        // argument checking requires a stream that is NOT already exhausted (the
        // EOF shortcut returns -1 before null/bounds validation).
        final SequenceInputStream fresh = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}), new ByteArrayInputStream(new byte[]{2}));
        checkThrows(NullPointerException.class, () -> fresh.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> fresh.read(new byte[2], 0, -1),
                    "read(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> fresh.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException");
        check(fresh.read(new byte[1], 0, 0) == 0, "len==0 returns 0");
    }

    // ---- 3. available reflects only current stream ----
    static void testAvailable() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3}),
                new ByteArrayInputStream(new byte[]{4, 5}));
        check(sis.available() == 3, "available of active stream");
        sis.read(new byte[3]);
        check(sis.available() == 0, "first drained");
        check(sis.read() == 4, "transition to second");
        check(sis.available() == 1, "available of second stream");
    }

    // ---- 4. Enumeration constructor ----
    static void testEnumerationConstructor() throws Exception {
        Vector<InputStream> v = new Vector<InputStream>();
        v.add(new ByteArrayInputStream(new byte[]{'a'}));
        v.add(new ByteArrayInputStream(new byte[]{'b'}));
        v.add(new ByteArrayInputStream(new byte[]{'c'}));
        SequenceInputStream sis = new SequenceInputStream(v.elements());
        check(sis.read() == 'a' && sis.read() == 'b' && sis.read() == 'c' && sis.read() == -1,
              "Enumeration of three streams");
    }

    // ---- 5. empty enumeration / empty head ----
    static void testEmptyTransitions() throws Exception {
        SequenceInputStream empty = new SequenceInputStream(
                Collections.<InputStream>emptyEnumeration());
        check(empty.read() == -1, "empty enumeration EOF");
        check(empty.read(new byte[2]) == -1, "empty enumeration bulk EOF");

        SequenceInputStream head = new SequenceInputStream(
                new ByteArrayInputStream(new byte[0]),
                new ByteArrayInputStream(new byte[]{7}));
        check(head.read() == 7, "empty head skipped silently");
    }

    // ---- 6. close closes every stream ----
    static void testClose() throws Exception {
        Tracked t1 = new Tracked("one", new byte[]{1});
        Tracked t2 = new Tracked("two", new byte[]{2});
        Tracked t3 = new Tracked("three", new byte[]{3});
        SequenceInputStream sis = new SequenceInputStream(
                Collections.enumeration(java.util.Arrays.asList(t1, t2, t3)));
        check(sis.read() == 1 && sis.read() == 2 && sis.read() == 3, "read all three");
        sis.close();
        check(t1.closed && t2.closed && t3.closed, "close reached every stream");

        Tracked unreached = new Tracked("unreached", new byte[]{9});
        SequenceInputStream s2 = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}), unreached);
        s2.read();                        // never consumed the second
        s2.close();
        check(unreached.closed, "close reaches a stream that was never current");
    }

    public static void main(String[] args) throws Exception {
        testTwoStreamConstructor();
        testBulkReadBoundary();
        testAvailable();
        testEnumerationConstructor();
        testEmptyTransitions();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

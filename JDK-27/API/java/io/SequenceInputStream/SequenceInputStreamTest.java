/*
 * @test
 * @summary java.io.SequenceInputStream conformance: white-box structure + black-box
 *          behavior (two-stream and Enumeration constructors, sequential exhaustion,
 *          bulk reads do not cross stream boundaries, available reflects only the
 *          current stream, close closes every stream, empty/EOF transitions).
 *          Deepened (contract level): transition closes each drained substream at the
 *          boundary, inherited skip semantics (across boundary, past end, 0/negative,
 *          after close), null-constructor timing (ctor((Enumeration)null) fails at
 *          construction with the helpful "this.e" NPE message, ctor(null,s2) at
 *          construction, ctor(s1,null) at the transition read), state after close,
 *          inherited mark/reset ("mark/reset not supported"), len==0 short-circuits
 *          before the EOF check, Objects.checkFromIndexSize messages for read OOB,
 *          Enumeration RuntimeException propagation from read() and close(), a failing
 *          substream close does NOT abort the chain on JDK-27 (all remaining streams
 *          are closed, later failures suppressed onto the first), transferTo full
 *          contract (concatenation, partial remainder, null out, after close).
 * @run main SequenceInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    static void checkThrowsMsg(Class<? extends Throwable> expected, String sub, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (String.valueOf(t.getMessage()).contains(sub)) { passed++; return; }
                throw new AssertionError(msg + ": message <" + t.getMessage()
                        + "> lacks substring <" + sub + ">");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- deepened: transition closes the drained substream at the boundary ----
    static void testTransitionCloseTiming() throws Exception {
        Tracked a = new Tracked("a", new byte[]{1});
        Tracked b = new Tracked("b", new byte[]{2});
        SequenceInputStream sis = new SequenceInputStream(a, b);
        check(!a.closed && !b.closed, "nothing closed before any read");
        sis.read();                       // 1 from a
        check(sis.read() == 2, "the boundary read returns the successor's first byte");
        check(a.closed, "the drained substream is closed at the transition");
        check(!b.closed, "the successor is still open while it is current");
        check(sis.read() == -1, "EOF after draining b");
        check(b.closed, "the last substream is closed on its EOF transition");
    }

    // ---- deepened: inherited skip semantics (measured on both JDKs) ----
    static void testSkipSemantics() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3, 4}));
        check(sis.skip(3) == 3, "skip(3) crosses the boundary");
        check(sis.read() == 4, "read resumes after the cross-boundary skip");

        SequenceInputStream all = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3, 4}));
        check(all.skip(100) == 4, "skip past the end skips exactly the remaining 4");
        check(all.read() == -1, "EOF after skipping past the end");

        SequenceInputStream zero = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}),
                new ByteArrayInputStream(new byte[0]));
        check(zero.skip(0) == 0 && zero.read() == 1, "skip(0) is a no-op");
        check(zero.skip(-1) == 0, "negative skip returns 0");
        check(zero.read() == -1, "EOF after the skips");

        SequenceInputStream closed = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}),
                new ByteArrayInputStream(new byte[0]));
        closed.close();
        check(closed.skip(7) == 0, "skip after close returns 0");
    }

    // ---- deepened: null-constructor timing (JDK-27 ctor NPE has a helpful message) ----
    static void testNullConstructorTiming() throws Exception {
        checkThrowsMsg(NullPointerException.class, "this.e",
                       () -> new SequenceInputStream((Enumeration<InputStream>) null),
                       "ctor((Enumeration)null) message");
        checkThrows(NullPointerException.class,
                    () -> new SequenceInputStream(null,
                            new ByteArrayInputStream(new byte[1])),
                    "ctor(null, s2) -> NPE at construction");

        Tracked s1 = new Tracked("s1", new byte[]{1});
        SequenceInputStream late = new SequenceInputStream(s1, null);
        check(late.read() == 1, "ctor(s1, null) succeeds and reads s1");
        checkThrows(NullPointerException.class, () -> late.read(),
                    "null successor -> NPE at the transition read");
        check(s1.closed, "s1 was closed before the null successor surfaced");
        check(late.available() == 0, "available after the transition NPE is 0");
        late.close();
        check(true, "close after the transition NPE completes");
    }

    // ---- deepened: state after close (measured on both JDKs) ----
    static void testStateAfterClose() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[0]));
        sis.close();
        check(sis.read() == -1, "read() after close");
        check(sis.available() == 0, "available() after close");
        byte[] buf = new byte[2];
        check(sis.read(buf, 0, 0) == -1, "read(b,0,0) after close (in == null shortcut)");
        check(sis.read(buf, 0, 1) == -1, "read(b,0,1) after close");
        sis.close();
        check(true, "second close is a no-op");
    }

    // ---- deepened: inherited mark/reset ----
    static void testMarkReset() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}),
                new ByteArrayInputStream(new byte[0]));
        check(!sis.markSupported(), "markSupported is false");
        sis.mark(5);
        check(sis.read() == 1, "mark is a harmless no-op");
        checkThrowsMsg(IOException.class, "mark/reset not supported", sis::reset,
                       "reset -> IOException");
    }

    // ---- deepened: read OOB messages come from Objects.checkFromIndexSize ----
    static void testReadValidationMessages() {
        final SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}),
                new ByteArrayInputStream(new byte[0]));
        checkThrowsMsg(IndexOutOfBoundsException.class, "out of bounds for length 2",
                       () -> sis.read(new byte[2], 0, 3), "read(b,0,3) message");
        checkThrowsMsg(IndexOutOfBoundsException.class, "out of bounds for length 2",
                       () -> sis.read(new byte[2], -1, 1), "read(b,-1,1) message");
        checkThrowsMsg(IndexOutOfBoundsException.class, "out of bounds for length 2",
                       () -> sis.read(new byte[2], 0, -1), "read(b,0,-1) message");
        checkThrows(NullPointerException.class, () -> sis.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
    }

    // ---- deepened: len==0 short-circuits before the EOF check (measured) ----
    static void testZeroLenAtEof() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}),
                new ByteArrayInputStream(new byte[0]));
        check(sis.read(new byte[1], 0, 0) == 0, "len==0 mid-stream returns 0");
        check(sis.read() == 1, "read the only byte");
        check(sis.read(new byte[1], 0, 0) == 0,
              "len==0 on a drained (but not transitioned) stream still returns 0");
    }

    // ---- deepened: empty middle stream is skipped silently ----
    static void testEmptyMiddleStream() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                Collections.enumeration(List.of(
                        new ByteArrayInputStream(new byte[]{1}),
                        new ByteArrayInputStream(new byte[0]),
                        new ByteArrayInputStream(new byte[]{2}))));
        check(sis.read() == 1 && sis.read() == 2, "reads span the empty middle");
        check(sis.read() == -1, "EOF after the third stream");
    }

    // ---- deepened: Enumeration RuntimeException propagates from read() and close() ----
    static void testEnumRuntimeException() throws Exception {
        class ThrowingEnum implements Enumeration<InputStream> {
            boolean served;
            public boolean hasMoreElements() { return true; }
            public InputStream nextElement() {
                if (!served) { served = true; return new ByteArrayInputStream(new byte[]{5}); }
                throw new IllegalStateException("enum boom");
            }
        }
        ThrowingEnum te = new ThrowingEnum();
        SequenceInputStream sis = new SequenceInputStream(te);
        check(sis.read() == 5, "first element reads fine");
        checkThrowsMsg(IllegalStateException.class, "enum boom", sis::read,
                       "read at the throwing transition");

        ThrowingEnum te2 = new ThrowingEnum();
        SequenceInputStream tc = new SequenceInputStream(te2);
        tc.read();
        checkThrowsMsg(IllegalStateException.class, "enum boom", tc::close,
                       "close pulls enumeration elements and propagates");
    }

    // ---- deepened: failing substream close does NOT abort the chain on JDK-27 ----
    static void testCloseFailureContinuesJdk27() throws Exception {
        class FailClose extends ByteArrayInputStream {
            boolean closed;
            FailClose(byte[] d) { super(d); }
            @Override public void close() throws IOException {
                closed = true;
                throw new IOException("close boom");
            }
        }
        FailClose first = new FailClose(new byte[]{1});
        Tracked second = new Tracked("second", new byte[]{2});
        SequenceInputStream sis = new SequenceInputStream(first, second);
        try {
            sis.close();
            check(false, "close must propagate the substream IOException");
        } catch (IOException ioe) {
            check("close boom".equals(ioe.getMessage()), "first failure propagates");
        }
        check(first.closed, "the failing substream was attempted");
        check(second.closed, "JDK-27 continues the chain: the successor is closed anyway");

        FailClose f2 = new FailClose(new byte[]{1});
        FailClose f3 = new FailClose(new byte[]{2});
        SequenceInputStream both = new SequenceInputStream(
                Collections.enumeration(List.of(f2, f3)));
        try {
            both.close();
            check(false, "double failure must also propagate");
        } catch (IOException ioe) {
            check("close boom".equals(ioe.getMessage()), "the first failure is the primary one");
            check(ioe.getSuppressed().length == 1, "the second failure is suppressed (measured)");
        }
        check(f2.closed && f3.closed, "every remaining stream was closed despite failures");
    }

    // ---- deepened: failing substream read/available propagate ----
    static void testFailingSubstream() throws Exception {
        class FailRead extends InputStream {
            public int read() throws IOException { throw new IOException("read boom"); }
        }
        class AvailFail extends InputStream {
            public int read() { return -1; }
            public int available() throws IOException { throw new IOException("avail boom"); }
        }
        final SequenceInputStream fr = new SequenceInputStream(
                new FailRead(), new ByteArrayInputStream(new byte[]{1}));
        checkThrowsMsg(IOException.class, "read boom", fr::read, "failing substream read");
        checkThrowsMsg(IOException.class, "read boom", fr::read,
                       "the failing substream stays current: no transition happens");

        final SequenceInputStream fa = new SequenceInputStream(
                new AvailFail(), new ByteArrayInputStream(new byte[0]));
        checkThrowsMsg(IOException.class, "avail boom", fa::available,
                       "failing substream available");
    }

    // ---- deepened: single-element enumeration ----
    static void testSingleStreamEnumeration() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                Collections.enumeration(List.of(new ByteArrayInputStream(new byte[]{9}))));
        check(sis.read() == 9, "the only stream reads");
        check(sis.read() == -1, "EOF with no successor");
        check(sis.available() == 0, "available 0 after exhaustion");
    }

    // ---- deepened: ctor eagerly pulls the first element ----
    static void testEagerFirstElement() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3}),
                new ByteArrayInputStream(new byte[]{4}));
        check(sis.available() == 3, "the first stream is current right after construction");
    }

    // ---- deepened: content fidelity across many small streams ----
    static void testContentAcrossManyStreams() throws Exception {
        byte[] expected = new byte[300];
        for (int i = 0; i < expected.length; i++) expected[i] = (byte) (i * 7);
        Vector<InputStream> v = new Vector<>();
        for (int i = 0; i < expected.length; i += 10)
            v.add(new ByteArrayInputStream(java.util.Arrays.copyOfRange(
                    expected, i, Math.min(i + 10, expected.length))));
        SequenceInputStream sis = new SequenceInputStream(v.elements());
        byte[] got = new byte[expected.length];
        int off = 0;
        while (off < got.length) {
            int n = sis.read(got, off, got.length - off);
            if (n < 0) break;
            off += n;
        }
        check(off == expected.length, "300 bytes over 30 ten-byte streams");
        check(java.util.Arrays.equals(got, expected), "content preserved across all boundaries");
    }

    // ---- deepened: region placement never crosses the boundary in one call ----
    static void testRegionPlacement() throws Exception {
        SequenceInputStream sis = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}),
                new ByteArrayInputStream(new byte[]{3, 4}));
        byte[] buf = new byte[6];
        check(sis.read(buf, 2, 4) == 2 && buf[2] == 1 && buf[3] == 2,
              "first region read stops at the boundary");
        check(buf[0] == 0 && buf[4] == 0, "bytes outside the region untouched");
        check(sis.read(buf, 2, 4) == 2 && buf[2] == 3 && buf[3] == 4,
              "second region read drains the successor");
    }

    // ---- deepened: transferTo full contract (JDK-27 override) ----
    static void testTransferToContract() throws Exception {
        // partial remainder across the boundary (measured)
        SequenceInputStream tt = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3}),
                new ByteArrayInputStream(new byte[]{4, 5}));
        tt.read();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(tt.transferTo(bos) == 4, "transferTo transfers the remaining bytes");
        check(java.util.Arrays.equals(bos.toByteArray(), new byte[]{2, 3, 4, 5}),
              "transferTo content spans the boundary");
        check(tt.read() == -1, "transferTo leaves the stream at EOF");

        // empty enumeration transfers nothing
        ByteArrayOutputStream emptyOut = new ByteArrayOutputStream();
        check(new SequenceInputStream(Collections.<InputStream>emptyEnumeration())
                      .transferTo(emptyOut) == 0, "transferTo on an empty sequence");

        // null out -> NPE with the documented name
        checkThrowsMsg(NullPointerException.class, "out",
                       () -> new SequenceInputStream(new ByteArrayInputStream(new byte[1]),
                                                     new ByteArrayInputStream(new byte[0]))
                               .transferTo(null),
                       "transferTo(null) message");

        // a failing sink propagates
        checkThrowsMsg(IOException.class, "sink boom",
                       () -> new SequenceInputStream(
                               new ByteArrayInputStream(new byte[]{1, 2, 3}),
                               new ByteArrayInputStream(new byte[0]))
                               .transferTo(new OutputStream() {
                                   public void write(int b) throws IOException {
                                       throw new IOException("sink boom");
                                   }
                               }),
                       "transferTo propagates a sink failure");

        // after close: in == null, nothing transferred, no throw
        SequenceInputStream closed = new SequenceInputStream(
                new ByteArrayInputStream(new byte[]{1}),
                new ByteArrayInputStream(new byte[0]));
        closed.close();
        ByteArrayOutputStream co = new ByteArrayOutputStream();
        check(closed.transferTo(co) == 0, "transferTo after close returns 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTransitionCloseTiming();
        testSkipSemantics();
        testNullConstructorTiming();
        testStateAfterClose();
        testMarkReset();
        testReadValidationMessages();
        testZeroLenAtEof();
        testEmptyMiddleStream();
        testEnumRuntimeException();
        testCloseFailureContinuesJdk27();
        testFailingSubstream();
        testSingleStreamEnumeration();
        testEagerFirstElement();
        testContentAcrossManyStreams();
        testRegionPlacement();
        testTransferToContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

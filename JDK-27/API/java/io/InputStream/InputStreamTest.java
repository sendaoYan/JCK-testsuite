/*
 * @test
 * @summary java.io.InputStream conformance: white-box structure (abstract byte input
 *          root) + black-box behavior (default implementations derived from read():
 *          bulk reads, skip/skipNBytes, readAllBytes/readNBytes, transferTo, mark/reset
 *          defaults, available/close defaults, nullInputStream, argument checking).
 * @run main InputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal subclass: only read() is implemented, everything else is inherited. */
    static class ArrayIn extends InputStream {
        final byte[] data;
        int pos;
        ArrayIn(byte[] data) { this.data = data; }
        @Override public int read() { return pos < data.length ? (data[pos++] & 0xFF) : -1; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.InputStream");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(Modifier.isAbstract(c.getMethod("read").getModifiers()), "read() abstract");
        check(!Modifier.isAbstract(c.getMethod("read", byte[].class, int.class, int.class)
                                      .getModifiers()),
              "read(byte[],int,int) is concrete in the base (loops over read())");
        check(Modifier.isStatic(c.getMethod("nullInputStream").getModifiers()),
              "static nullInputStream factory");
        for (String m : new String[]{"read", "readAllBytes", "readNBytes", "skip",
                                     "skipNBytes", "available", "close", "mark",
                                     "reset", "markSupported", "transferTo"})
            check(has(c, m), "has method " + m);
        boolean hasLock = false;
        for (java.lang.reflect.Field f : c.getDeclaredFields())
            if (f.getName().equals("lock")) hasLock = true;
        check(!hasLock, "InputStream has no lock field (that is Reader/Writer)");
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. default read(byte[]) and read(byte[],int,int) are derived from read()
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        byte[] three = new byte[3];
        check(in.read(three) == 3 && three[0] == 1 && three[2] == 3, "bulk read via read()");
        byte[] two = new byte[2];
        check(in.read(two, 0, 2) == 2 && two[0] == 4 && two[1] == 5, "offset bulk read");
        check(in.read() == -1, "exhausted");
        check(in.read(new byte[2]) == -1, "bulk read at EOF returns -1");
        check(new ArrayIn(new byte[0]).read(new byte[1]) == -1, "empty source bulk read -1");

        // 2. zero-length reads transfer nothing and never block
        in = new ArrayIn(new byte[]{9});
        check(in.read(new byte[0]) == 0, "read(empty) == 0");
        check(in.read(new byte[4], 1, 0) == 0, "read(b,off,0) == 0");
        check(in.read() == 9, "no byte consumed by zero-length reads");

        // 3. skip: forward skips, negative is a no-op returning 0
        in = new ArrayIn(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        check(in.skip(-1) == 0, "skip(negative) == 0");
        check(in.read() == 1, "skip(negative) consumed nothing");
        check(in.skip(2) == 2 && in.read() == 4, "skip(2) skips ahead");

        // 4. skipNBytes: exact skip, negative no-op, past EOF throws EOFException
        in = new ArrayIn(new byte[]{1, 2, 3, 4});
        in.skipNBytes(2);
        check(in.read() == 3, "skipNBytes(2)");
        in.skipNBytes(-5);
        check(in.read() == 4, "skipNBytes(negative) is a no-op");
        try {
            in.skipNBytes(10);                       // only 0 bytes remain
            check(false, "skipNBytes past EOF must throw");
        } catch (java.io.EOFException expected) {
            passed++;
        }

        // 5. readAllBytes consumes the remainder
        in = new ArrayIn(new byte[]{1, 2, 3});
        check(java.util.Arrays.equals(in.readAllBytes(), new byte[]{1, 2, 3}),
              "readAllBytes contents");
        check(in.read() == -1, "readAllBytes drains the stream");

        // 6. readNBytes: array form fills at most len; int form never returns more than asked
        in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        byte[] buf = new byte[2];
        check(in.readNBytes(buf, 0, 2) == 2 && buf[0] == 1, "readNBytes(b,off,len)");
        check(java.util.Arrays.equals(in.readNBytes(2), new byte[]{3, 4}), "readNBytes(int)");
        check(in.read() == 5, "position preserved across readNBytes forms");
        try {
            in.readNBytes(-1);
            check(false, "readNBytes(negative) must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 7. transferTo moves everything and reports the count
        in = new ArrayIn(new byte[]{9, 8, 7});
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        check(in.transferTo(out) == 3 && java.util.Arrays.equals(out.toByteArray(), new byte[]{9, 8, 7}),
              "transferTo count and contents");
        try {
            new ArrayIn(new byte[1]).transferTo(null);
            check(false, "transferTo(null) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }

        // 8. defaults: available() == 0, close() no-op, markSupported() false,
        //    mark() no-op, reset() throws
        in = new ArrayIn(new byte[]{1});
        check(in.available() == 0, "base available() is 0");
        in.close();
        in.close();
        check(in.read() == 1, "base close() does not close");
        check(!in.markSupported(), "base markSupported() is false");
        ArrayIn mk = new ArrayIn(new byte[]{7, 8});
        check(mk.read() == 7, "first byte before mark");
        mk.mark(10);
        check(mk.read() == 8, "base mark() consumes nothing");
        try {
            in.reset();
            check(false, "base reset() must throw");
        } catch (IOException ioe) {
            check("mark/reset not supported".equals(ioe.getMessage()),
                  "reset message: " + ioe.getMessage());
        }

        // 9. argument checking is the base class's job too
        try {
            new ArrayIn(new byte[1]).read(null);
            check(false, "read(null) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new ArrayIn(new byte[3]).read(new byte[2], 2, 1);
            check(false, "out-of-bounds read must throw");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }

        // 10. nullInputStream: an always-empty, closed-on-arrival stream
        InputStream empty = InputStream.nullInputStream();
        check(empty.read() == -1 && empty.read(new byte[1]) == -1, "nullInputStream reads EOF");
        check(empty.readNBytes(3).length == 0 && empty.available() == 0, "no data ever");
        check(empty.skip(4) == 0, "skip on null stream is 0");
        empty.close();

        // 11. polymorphic use: a concrete JDK stream through the base reference
        try (InputStream bais = new ByteArrayInputStream(new byte[]{5, 6})) {
            check(bais.read() == 5 && bais.read() == 6, "ByteArrayInputStream via InputStream ref");
        }
    }

    // ---- 深化追加（契约级），行为全部经 JDK-27 实测锁定 ----

    /** 子类覆写 read(byte[],int,int) 时，read(byte[]) 委托给它。 */
    static class DelegatingBulk extends InputStream {
        boolean threeArgCalled;
        public int read() { return 1; }
        @Override public int read(byte[] b, int off, int len) {
            threeArgCalled = true;
            if (len == 0) return 0;
            b[off] = 42;
            return 1;
        }
    }

    static void testReadByteDelegation() throws Exception {
        DelegatingBulk d = new DelegatingBulk();
        byte[] buf = new byte[2];
        check(d.read(buf) == 1 && d.threeArgCalled,
              "read(byte[]) delegates to read(byte[],int,int)");
        check(buf[0] == 42 && buf[1] == 0, "delegated call fills through 3-arg form");
        d.threeArgCalled = false;
        d.read(new byte[0]);
        check(d.threeArgCalled, "zero-length read(byte[]) still delegates");
    }

    /** 参数校验完整矩阵 + JDK-27 的 Range/NPE 精确消息。 */
    static void testArgumentMatrixDeep() throws Exception {
        ArrayIn in = new ArrayIn(new byte[3]);
        check(in.read(new byte[2], 2, 0) == 0, "read(b,b.length,0) == 0 legal boundary");
        try {
            in.read(new byte[2], 3, 0);
            check(false, "read(b,3,0) must throw");
        } catch (IndexOutOfBoundsException e) {
            check(e.getMessage() != null && e.getMessage().contains("out of bounds for length 2"),
                  "JDK-27 Range message: " + e.getMessage());
        }
        try {
            in.read(new byte[2], 1, Integer.MAX_VALUE);
            check(false, "read(b,1,MAX) must throw");
        } catch (IndexOutOfBoundsException e) {
            check(e.getMessage() != null && e.getMessage().contains("out of bounds for length 2"),
                  "overflow Range message: " + e.getMessage());
        }
        try {
            in.read((byte[]) null);
            check(false, "read(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("\"b\" is null"),
                  "JDK-27 NPE message names b: " + e.getMessage());
        }
    }

    /** skip 默认实现矩阵（含大跳与 Long.MIN_VALUE）。 */
    static void testSkipDefaultsDeep() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        check(in.skip(Long.MIN_VALUE) == 0, "skip(Long.MIN_VALUE) == 0");
        check(in.read() == 1, "Long.MIN skip consumed nothing");
        check(in.skip(0) == 0 && in.read() == 2, "skip(0) no-op");
        check(in.skip(100) == 3, "skip past end returns remaining");
        check(in.skip(1) == 0, "skip on exhausted stream == 0");
        check(in.read() == -1, "read after exhausted skip");

        ArrayIn empty = new ArrayIn(new byte[0]);
        check(empty.skip(5) == 0, "empty source skip(5) == 0");

        byte[] big = new byte[6000];
        for (int i = 0; i < big.length; i++) big[i] = (byte) i;
        ArrayIn bigIn = new ArrayIn(big);
        check(bigIn.skip(5000) == 5000, "skip(5000) on 6000-byte source");
        check(bigIn.read() == (5000 & 0xFF), "position exact after large skip");
    }

    /** reset 精确消息（JDK-27 实测同为 "mark/reset not supported"）。 */
    static void testResetMessageExact() throws Exception {
        try {
            new ArrayIn(new byte[1]).reset();
            check(false, "reset must throw");
        } catch (IOException ioe) {
            check("mark/reset not supported".equals(ioe.getMessage()),
                  "JDK-27 reset message exact: " + ioe.getMessage());
        }
    }

    /** EOF 与零长度交叉矩阵。 */
    static void testEofZeroLengthMatrix() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{7});
        check(in.read() == 7, "consume the only byte");
        check(in.read() == -1 && in.read(new byte[3]) == -1, "EOF reads -1");
        check(in.read(new byte[3], 0, 3) == -1, "region read EOF == -1");
        check(in.read(new byte[0]) == 0, "zero-length read at EOF == 0");
        check(in.read(new byte[3], 1, 0) == 0, "zero-length region read at EOF == 0");

        ArrayIn part = new ArrayIn(new byte[]{1, 2, 3});
        byte[] ten = new byte[10];
        check(part.read(ten) == 3, "partial fill returns available count only");
        check(ten[3] == 0 && ten[9] == 0, "bytes beyond returned count untouched");
    }

    /** close 默认不关闭底层（幂等、不影响后续读取）。 */
    static void testCloseNoopDeep() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{9});
        in.close();
        in.close();
        in.close();
        check(in.available() == 0, "available still 0 after closes");
        check(in.read() == 9, "read still served after repeated no-op closes");
    }

    /** readNBytes(int)：部分可用、EOF、0、MAX 不抛。 */
    static void testReadNBytesIntDeep() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        check(in.readNBytes(0).length == 0, "readNBytes(0) empty array");
        byte[] two = in.readNBytes(2);
        check(two.length == 2 && two[0] == 1 && two[1] == 2, "readNBytes(2) contents");
        byte[] partial = in.readNBytes(10);                       // only 3 remain
        check(partial.length == 3 && partial[2] == 5, "readNBytes(10) partial");
        check(in.read() == -1, "drained after partial readNBytes");
        check(in.readNBytes(4).length == 0, "readNBytes at EOF empty array");
        ArrayIn e = new ArrayIn(new byte[0]);
        byte[] none = e.readNBytes(Integer.MAX_VALUE);            // grows lazily, no throw
        check(none.length == 0, "readNBytes(MAX_VALUE) on empty: no throw, empty");
        try {
            new ArrayIn(new byte[1]).readNBytes(-1);
            check(false, "readNBytes(-1) must throw");
        } catch (IllegalArgumentException iae) {
            check("len < 0".equals(iae.getMessage()), "IAE message: " + iae.getMessage());
        }
    }

    /** readNBytes(byte[],off,len)：参数矩阵、部分填充、EOF 返回 0（非 -1）。 */
    static void testReadNBytesArrayDeep() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3});
        byte[] buf = new byte[2];
        check(in.readNBytes(buf, 0, 0) == 0, "readNBytes(b,0,0) == 0");
        check(in.readNBytes(buf, 0, 2) == 2 && buf[0] == 1 && buf[1] == 2, "full fill");
        byte[] big = new byte[10];
        check(in.readNBytes(big, 0, 10) == 1 && big[0] == 3, "partial: only remaining 1");
        check(big[1] == 0, "untouched slot stays 0");
        check(in.readNBytes(new byte[4], 0, 4) == 0, "readNBytes at EOF == 0 (not -1)");
        try { new ArrayIn(new byte[1]).readNBytes(new byte[2], 3, 0); check(false, "must throw"); }
        catch (IndexOutOfBoundsException e) {
            check(e.getMessage().contains("out of bounds for length 2"), "Range msg");
        }
        try { new ArrayIn(new byte[1]).readNBytes(new byte[2], 0, -1); check(false, "must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { new ArrayIn(new byte[1]).readNBytes(null, 0, 1); check(false, "must throw"); }
        catch (NullPointerException expected) { passed++; }
    }

    /** skipNBytes 完整矩阵。 */
    static void testSkipNBytesMatrix() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4});
        in.skipNBytes(-5);
        check(in.read() == 1, "skipNBytes(negative) is a no-op (delegates skip<0)");
        in.skipNBytes(0);
        check(in.read() == 2, "skipNBytes(0) no-op");
        in.skipNBytes(2);
        check(in.read() == -1, "skipNBytes exact remaining reaches EOF");
        try {
            in.skipNBytes(1);
            check(false, "skipNBytes at EOF must throw EOFException");
        } catch (java.io.EOFException expected) {
            passed++;
        }
        in.skipNBytes(0);
        passed++;                                          // zero at EOF is a no-op
        in.skipNBytes(Long.MIN_VALUE);
        passed++;                                          // negative long is a no-op
        try {
            new ArrayIn(new byte[0]).skipNBytes(2);
            check(false, "skipNBytes on empty source must throw");
        } catch (java.io.EOFException expected) {
            passed++;
        }
    }

    /** transferTo：null NPE("out")、IOE 透传、空流、write(b,off,len) 通道。 */
    static class BoomReadIn extends InputStream {
        int calls;
        public int read() throws IOException {
            if (calls++ == 0) return 7;
            throw new IOException("boom-read");
        }
    }
    static class BoomWriteOut extends OutputStream {
        public void write(int b) throws IOException { throw new IOException("boom-write"); }
    }
    static class SpyBulkOut extends OutputStream {
        boolean bulk;
        public void write(int x) { }
        @Override public void write(byte[] b, int off, int len) { bulk = true; }
    }

    static void testTransferToDeep() throws Exception {
        try {
            new ArrayIn(new byte[1]).transferTo(null);
            check(false, "transferTo(null) must throw");
        } catch (NullPointerException npe) {
            check("out".equals(npe.getMessage()), "NPE message is out: " + npe.getMessage());
        }
        try {
            new BoomReadIn().transferTo(new ByteArrayOutputStream());
            check(false, "read failure must propagate");
        } catch (IOException ioe) {
            check("boom-read".equals(ioe.getMessage()), "read IOE passed through verbatim");
        }
        try {
            new ArrayIn(new byte[1]).transferTo(new BoomWriteOut());
            check(false, "write failure must propagate");
        } catch (IOException ioe) {
            check("boom-write".equals(ioe.getMessage()), "write IOE passed through verbatim");
        }
        check(new ArrayIn(new byte[0]).transferTo(new ByteArrayOutputStream()) == 0,
              "empty source transfers 0");
        SpyBulkOut spy = new SpyBulkOut();
        check(new ArrayIn(new byte[]{1, 2, 3}).transferTo(spy) == 3, "count reported");
        check(spy.bulk, "transferTo prefers write(byte[],int,int) channel");
    }

    /** nullInputStream 全矩阵（含关闭后各方法 "Stream closed"）。 */
    static void testNullInputStreamMatrix() throws Exception {
        InputStream n = InputStream.nullInputStream();
        check(n.read() == -1 && n.read(new byte[2]) == -1 && n.read(new byte[2], 0, 2) == -1,
              "all read forms EOF");
        check(n.available() == 0, "available 0");
        check(n.skip(4) == 0, "skip 0");
        check(!n.markSupported(), "markSupported false");
        check(n.readNBytes(3).length == 0, "readNBytes(int) empty array");
        check(n.readNBytes(new byte[3], 0, 3) == 0, "readNBytes(b,off,len) 0 (not -1)");
        check(n.readAllBytes().length == 0, "readAllBytes empty");
        try {
            n.skipNBytes(2);
            check(false, "nullInputStream skipNBytes(positive) must throw EOF");
        } catch (java.io.EOFException expected) {
            passed++;
        }
        n.mark(1);
        passed++;                                          // mark allowed
        try {
            n.reset();
            check(false, "reset must throw");
        } catch (IOException ioe) {
            check("mark/reset not supported".equals(ioe.getMessage()), "reset message");
        }
        try {
            n.transferTo(null);
            check(false, "transferTo(null) NPE checked before closed state");
        } catch (NullPointerException npe) {
            // nullInputStream 覆写版用单参 requireNonNull（无消息），区别于基类的 "out"
            check(npe.getMessage() == null,
                  "nullInputStream NPE carries no message: " + npe.getMessage());
        }
        check(n.transferTo(new ByteArrayOutputStream()) == 0, "transferTo 0");
        n.close();
        n.close();
        passed++;                                          // close idempotent
        for (int i = 0; i < 4; i++) {
            InputStream closed = InputStream.nullInputStream();
            closed.close();
            try {
                switch (i) {
                    case 0: closed.read(); check(false, "read after close"); break;
                    case 1: closed.readNBytes(2); check(false, "readNBytes after close"); break;
                    case 2: closed.readAllBytes(); check(false, "readAllBytes after close"); break;
                    default: closed.skip(1); check(false, "skip after close"); break;
                }
            } catch (IOException ioe) {
                check("Stream closed".equals(ioe.getMessage()),
                      "message Stream closed: " + ioe.getMessage());
            }
        }
        InputStream closedAvail = InputStream.nullInputStream();
        closedAvail.close();
        try {
            closedAvail.available();
            check(false, "available after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "available closed msg");
        }
        InputStream closedMark = InputStream.nullInputStream();
        closedMark.close();
        closedMark.mark(1);
        passed++;                                          // mark after close is permitted
    }

    /** readAllBytes 消费剩余 + 空。 */
    static void testReadAllBytesDeep() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{9, 8, 7, 6});
        check(in.read() == 9, "pre-consume");
        byte[] rest = in.readAllBytes();
        check(rest.length == 3 && rest[0] == 8 && rest[2] == 6, "readAllBytes remainder");
        check(in.readAllBytes().length == 0, "readAllBytes on drained == empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReadByteDelegation();
        testArgumentMatrixDeep();
        testSkipDefaultsDeep();
        testResetMessageExact();
        testEofZeroLengthMatrix();
        testCloseNoopDeep();
        testReadNBytesIntDeep();
        testReadNBytesArrayDeep();
        testSkipNBytesMatrix();
        testTransferToDeep();
        testNullInputStreamMatrix();
        testReadAllBytesDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

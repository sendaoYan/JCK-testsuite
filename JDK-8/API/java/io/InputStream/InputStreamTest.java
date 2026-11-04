/*
 * @test
 * @summary java.io.InputStream API conformance: full contract coverage.
 *          Abstract byte-input root. Default read(byte[]) / read(byte[],off,len) derived
 *          from read(), skip (negative->0), available/close/mark/reset defaults,
 *          markSupported false, argument checking (null->NPE, bounds->IOBE),
 *          polymorphic use through the base reference.
 * @run main InputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamTest {
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

    /** Minimal subclass: only read() is implemented, everything else inherited. */
    static class ArrayIn extends InputStream {
        final byte[] data;
        int pos;
        ArrayIn(byte[] data) { this.data = data; }
        @Override public int read() { return pos < data.length ? (data[pos++] & 0xFF) : -1; }
    }

    // ---- 1. default bulk reads derived from read() ----
    static void testBulkReadDefaults() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        byte[] three = new byte[3];
        check(in.read(three) == 3 && three[0] == 1 && three[2] == 3, "read(byte[])");
        byte[] two = new byte[2];
        check(in.read(two, 0, 2) == 2 && two[0] == 4 && two[1] == 5, "read(byte[],off,len)");
        check(in.read() == -1, "exhausted");
        check(in.read(new byte[2]) == -1, "bulk read at EOF returns -1");
        check(new ArrayIn(new byte[0]).read(new byte[1]) == -1, "empty source bulk read -1");

        in = new ArrayIn(new byte[]{9});
        check(in.read(new byte[0]) == 0, "read(empty) == 0");
        check(in.read(new byte[4], 1, 0) == 0, "read(b,off,0) == 0");
        check(in.read() == 9, "no byte consumed by zero-length reads");
    }

    // ---- 2. argument checking ----
    static void testArgumentChecking() throws Exception {
        final ArrayIn in = new ArrayIn(new byte[3]);
        checkThrows(NullPointerException.class, () -> in.read((byte[]) null),
                    "read((byte[])null) -> NPE");
        checkThrows(NullPointerException.class, () -> in.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], -1, 1),
                    "read(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 0, -1),
                    "read(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 2, 1),
                    "read(b,2,1) -> IndexOutOfBoundsException");
    }

    // ---- 3. skip ----
    static void testSkip() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        check(in.skip(-1) == 0, "skip(negative) == 0");
        check(in.read() == 1, "skip(negative) consumed nothing");
        check(in.skip(0) == 0, "skip(0) == 0");
        check(in.skip(2) == 2 && in.read() == 4, "skip(2) skips ahead");

        ArrayIn shortIn = new ArrayIn(new byte[]{1, 2});
        check(shortIn.skip(100) == 2, "skip past end returns remaining");
        check(shortIn.read() == -1, "exhausted after skip past end");
    }

    // ---- 4. defaults: available/close/mark/reset ----
    static void testDefaults() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1});
        check(in.available() == 0, "base available() is 0");
        in.close();
        in.close();
        check(in.read() == 1, "base close() does not close");
        check(!in.markSupported(), "base markSupported() is false");

        ArrayIn mk = new ArrayIn(new byte[]{7, 8});
        check(mk.read() == 7, "first byte before mark");
        mk.mark(10);                       // no-op, does not throw
        check(mk.read() == 8, "base mark() consumes nothing");
        checkThrows(IOException.class, () -> in.reset(), "base reset() -> IOException");
    }

    // ---- 5. polymorphic use ----
    static void testPolymorphicUse() throws Exception {
        InputStream bais = new ByteArrayInputStream(new byte[]{5, 6});
        check(bais.read() == 5 && bais.read() == 6, "ByteArrayInputStream via InputStream ref");
        check(bais.available() == 0, "available after drain");
        bais.close();
    }

    // ---- 深化追加（契约级），以下均为双侧实测后锁定的行为 ----

    /** 子类覆写 read(byte[],int,int) 时，read(byte[]) 委托给它（JLS 层派生关系）。 */
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

    /** 参数校验矩阵各分支（异常类型级；JDK-8 消息为 null，断言消息为 null 这一点本身）。 */
    static void testArgumentMatrixDeep() throws Exception {
        final ArrayIn in = new ArrayIn(new byte[3]);
        // (b,2,0): off == b.length 且 len == 0 —— JDK-8 条件 off > b.length 不含等号，合法
        check(in.read(new byte[2], 2, 0) == 0, "read(b,b.length,0) == 0 legal boundary");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[2], 3, 0),
                    "read(b,3,0) off beyond array -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> in.read(new byte[2], 1, Integer.MAX_VALUE),
                    "read(b,1,MAX) off+len overflow -> IndexOutOfBoundsException");
        try {
            in.read(new byte[2], 0, 1);
        } catch (IndexOutOfBoundsException ioobe) {
            check(ioobe.getMessage() == null,
                  "JDK-8 base IOOBE carries no message: " + ioobe.getMessage());
        }
        try {
            in.read((byte[]) null, 0, 1);
        } catch (NullPointerException npe) {
            check(npe.getMessage() == null,
                  "JDK-8 base NPE carries no message: " + npe.getMessage());
        }
    }

    /** skip 默认实现（丢弃式 read）完整矩阵。 */
    static void testSkipDefaultsDeep() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{1, 2, 3, 4, 5});
        check(in.skip(Long.MIN_VALUE) == 0, "skip(Long.MIN_VALUE) == 0");
        check(in.read() == 1, "Long.MIN skip consumed nothing");
        check(in.skip(0) == 0 && in.read() == 2, "skip(0) no-op");
        check(in.skip(100) == 3, "skip past end returns remaining (3)");
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

    /** reset 精确消息（JDK-8 实测："mark/reset not supported"）。 */
    static void testResetMessageExact() throws Exception {
        try {
            new ArrayIn(new byte[1]).reset();
            check(false, "reset must throw");
        } catch (IOException ioe) {
            check("mark/reset not supported".equals(ioe.getMessage()),
                  "JDK-8 reset message exact: " + ioe.getMessage());
        }
    }

    /** EOF 与零长度交叉矩阵。 */
    static void testEofZeroLengthMatrix() throws Exception {
        ArrayIn in = new ArrayIn(new byte[]{7});
        check(in.read() == 7, "consume the only byte");
        check(in.read() == -1, "read() EOF");
        check(in.read(new byte[3]) == -1, "read(byte[]) EOF == -1");
        check(in.read(new byte[3], 0, 3) == -1, "read(b,off,len) EOF == -1");
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

    public static void main(String[] args) throws Exception {
        testBulkReadDefaults();
        testArgumentChecking();
        testSkip();
        testDefaults();
        testPolymorphicUse();
        testReadByteDelegation();
        testArgumentMatrixDeep();
        testSkipDefaultsDeep();
        testResetMessageExact();
        testEofZeroLengthMatrix();
        testCloseNoopDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

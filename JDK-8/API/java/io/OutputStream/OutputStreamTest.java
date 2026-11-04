/*
 * @test
 * @summary java.io.OutputStream API conformance: full contract coverage.
 *          Abstract byte-output root. write(int) abstract; write(byte[]) delegates to
 *          write(byte[],int,int); default write loop; flush/close no-ops; null argument
 *          and bounds checking; polymorphic use through the base reference.
 * @run main OutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class OutputStreamTest {
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

    /** Only write(int): everything else inherited. */
    static class Bare extends OutputStream {
        final StringBuilder sb = new StringBuilder();
        @Override public void write(int b) { sb.append((char) (b & 0xFF)); }
    }

    static class Sink extends OutputStream {
        final ArrayList<Byte> bytes = new ArrayList<Byte>();
        int flushes, closes;
        @Override public void write(int b) { bytes.add((byte) b); }
        @Override public void flush() { flushes++; }
        @Override public void close() { closes++; }
    }

    // ---- 1. default write(byte[]) delegates to write(byte[],int,int) -> write(int) ----
    static void testWriteDelegation() throws Exception {
        Bare b = new Bare();
        b.write(new byte[]{'a', 'b', 'c'});
        check("abc".contentEquals(b.sb), "write(byte[]) reaches write(int)");
        b.write(new byte[]{'X', 'Y'}, 0, 0);
        check("abc".contentEquals(b.sb), "zero-length write writes nothing");
        b.write(new byte[]{'-', 'd', 'e', '-'}, 1, 2);
        check("abcde".contentEquals(b.sb), "write(byte[],off,len) region only");
    }

    // ---- 2. default write(byte[],int,int) loops one byte at a time ----
    static void testDefaultWriteLoop() throws Exception {
        class Counting extends OutputStream {
            int calls;
            @Override public void write(int x) { calls++; }
        }
        Counting c = new Counting();
        c.write(new byte[7], 2, 5);
        check(c.calls == 5, "default region write calls write(int) once per byte");
    }

    // ---- 3. argument checking ----
    static void testArgumentChecking() throws Exception {
        final Sink s = new Sink();
        checkThrows(NullPointerException.class, () -> s.write((byte[]) null),
                    "write((byte[])null) -> NPE");
        checkThrows(NullPointerException.class, () -> s.write(null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> s.write(new byte[2], 2, 1),
                    "write(b,2,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> s.write(new byte[2], -1, 1),
                    "write(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> s.write(new byte[2], 0, -1),
                    "write(b,0,-1) -> IndexOutOfBoundsException");
    }

    // ---- 4. default flush()/close() are no-ops ----
    static void testDefaultFlushClose() throws Exception {
        class Defaults extends OutputStream {
            @Override public void write(int x) { }
        }
        Defaults d = new Defaults();
        d.flush();
        d.close();
        d.write(1);
        passed++;                       // none of the above interfered
    }

    // ---- 5. subclasses see overridden flush/close via base reference ----
    static void testOverrideDispatch() throws Exception {
        Sink s = new Sink();
        OutputStream os = s;
        os.write(65);
        os.flush();
        os.flush();
        os.close();
        check(s.bytes.size() == 1 && s.bytes.get(0) == 65, "write via base reference");
        check(s.flushes == 2, "flush invoked twice");
        check(s.closes == 1, "close invoked once");
    }

    // ---- 6. polymorphic use ----
    static void testPolymorphicUse() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream ref = baos;
        ref.write('h');
        ref.flush();
        check(baos.toByteArray().length == 1 && baos.toByteArray()[0] == 'h',
              "ByteArrayOutputStream via OutputStream ref");
    }

    // ---- 深化追加（契约级），行为全部经 JDK-8 实测锁定 ----

    /** 子类覆写 write(byte[],int,int) 时，write(byte[]) 委托给它。 */
    static class DelegatingBulk extends OutputStream {
        boolean threeArgCalled;
        final java.util.ArrayList<Byte> seen = new java.util.ArrayList<Byte>();
        @Override public void write(int b) { seen.add((byte) b); }
        @Override public void write(byte[] b, int off, int len) {
            threeArgCalled = true;
            for (int i = 0; i < len; i++) seen.add(b[off + i]);
        }
    }

    static void testWriteByteDelegation() throws Exception {
        DelegatingBulk d = new DelegatingBulk();
        d.write(new byte[]{1, 2, 3});
        check(d.threeArgCalled, "write(byte[]) delegates to write(byte[],int,int)");
        check(d.seen.size() == 3 && d.seen.get(2) == 3, "delegated write receives region");
        d.threeArgCalled = false;
        d.write(new byte[0]);
        check(d.threeArgCalled, "zero-length write(byte[]) still delegates");
        check(d.seen.size() == 3, "empty array writes nothing");
    }

    static void testArgumentMatrixDeep() throws Exception {
        final Sink s = new Sink();
        // 合法边界：off == b.length 且 len == 0（JDK-8 条件 off > b.length 不含等号）
        s.write(new byte[2], 2, 0);
        passed++;
        check(s.bytes.isEmpty(), "boundary write writes nothing");
        checkThrows(IndexOutOfBoundsException.class, () -> s.write(new byte[2], 3, 0),
                    "write(b,3,0) off beyond array -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> s.write(new byte[2], 1, Integer.MAX_VALUE),
                    "write(b,1,MAX) overflow -> IndexOutOfBoundsException");
        s.write(new byte[0]);
        check(s.bytes.isEmpty(), "write(empty array) is a no-op");
        try {
            s.write(new byte[2], 0, 1);
        } catch (IndexOutOfBoundsException ioobe) {
            check(ioobe.getMessage() == null, "JDK-8 base IOOBE carries no message");
        }
        try {
            s.write((byte[]) null, 0, 1);
        } catch (NullPointerException npe) {
            check(npe.getMessage() == null, "JDK-8 base NPE carries no message");
        }
    }

    /** write(int) 的 int 原样传给子类——截断到低 8 位是子类契约，不是基类行为。 */
    static void testWriteIntPassthrough() throws Exception {
        final java.util.List<Integer> seen = new java.util.ArrayList<Integer>();
        class Recorder extends OutputStream {
            @Override public void write(int b) { seen.add(b); }
        }
        Recorder r = new Recorder();
        r.write(0x141);                                   // 321，超出一个字节
        r.write(255);
        r.write(0);
        check(seen.size() == 3, "three write(int) calls delivered");
        check(seen.get(0) == 0x141, "value passed through un-truncated (321)");
        check(seen.get(1) == 255 && seen.get(2) == 0, "0 and 255 delivered verbatim");
        r.write(new byte[]{(byte) 0x80, 0x7F});
        check(seen.get(3) == -128 && seen.get(4) == 127,
              "default loop delivers sign-extended byte (-128), masking is subclass's job");
    }

    /** 默认 write(byte[],int,int) 循环的顺序与内容保真。 */
    static void testDefaultLoopFidelity() throws Exception {
        final StringBuilder order = new StringBuilder();
        class Ordered extends OutputStream {
            @Override public void write(int b) { order.append((char) (b & 0xFF)); }
        }
        Ordered o = new Ordered();
        o.write(new byte[]{'a', 'b', 'c'}, 0, 3);
        check("abc".contentEquals(order), "in-order byte delivery");
        o.write(new byte[]{'x', 'y'}, 1, 1);
        check("abcy".contentEquals(order), "off/len region honored in loop");
        o.write(new byte[3], 0, 0);
        check("abcy".contentEquals(order), "zero-length region skips loop");
    }

    /** 默认 close()/flush() 多次调用互不影响后续 write。 */
    static void testCloseFlushNoopDeep() throws Exception {
        class Defaults extends OutputStream {
            int writes;
            @Override public void write(int b) { writes++; }
        }
        Defaults d = new Defaults();
        d.close();
        d.close();
        d.flush();
        d.flush();
        d.write(1);
        d.write(new byte[]{2, 3});
        check(d.writes == 3, "writes unaffected by repeated no-op close/flush");
    }

    /** 白盒：JDK-8 的 OutputStream 没有静态工厂（nullInputStream 是 JDK 11+ 的事，OS 同）。 */
    static void testWhiteBoxNoNullStream() throws Exception {
        Class<?> c = java.io.OutputStream.class;
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.reflect.Modifier.isAbstract(
                  c.getMethod("write", int.class).getModifiers()), "write(int) abstract");
        check(!java.lang.reflect.Modifier.isAbstract(
                  c.getMethod("write", byte[].class, int.class, int.class).getModifiers()),
              "write(byte[],int,int) is concrete in JDK-8 (loops over write(int))");
        boolean hasNullStream = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals("nullOutputStream")) hasNullStream = true;
        check(!hasNullStream, "JDK-8 has no nullOutputStream factory");
        try {
            c.getDeclaredConstructor().newInstance();
            check(false, "abstract class must refuse instantiation");
        } catch (InstantiationException expected) {
            passed++;
        }
    }

    /** 默认循环中 write(int) 抛出的 IOException 原样透传，已写部分保留。 */
    static class BoomThirdWrite extends OutputStream {
        int calls;
        final java.util.List<Integer> seen = new java.util.ArrayList<Integer>();
        @Override public void write(int b) throws IOException {
            if (++calls == 3) throw new IOException("third-write-boom");
            seen.add(b);
        }
    }

    static void testExceptionPropagation() throws Exception {
        BoomThirdWrite b = new BoomThirdWrite();
        try {
            b.write(new byte[]{1, 2, 3, 4});
            check(false, "IOException must propagate out of default loop");
        } catch (IOException ioe) {
            check("third-write-boom".equals(ioe.getMessage()), "verbatim message");
        }
        check(b.seen.size() == 2 && b.seen.get(0) == 1 && b.seen.get(1) == 2,
              "partial writes kept before failure");
        BoomThirdWrite b2 = new BoomThirdWrite();
        try {
            b2.write(9);
            b2.write(9);
            b2.write(9);
            check(false, "third single write must throw");
        } catch (IOException expected) {
            passed++;
        }
        check(b2.seen.size() == 2, "third call not recorded");
        // 子类的 flush/close 允许抛 IOException（基类不吞）
        class Flusher extends OutputStream {
            @Override public void write(int x) { }
            @Override public void flush() throws IOException { throw new IOException("f"); }
            @Override public void close() throws IOException { throw new IOException("c"); }
        }
        Flusher f = new Flusher();
        checkThrows(IOException.class, () -> f.flush(), "subclass flush may throw");
        checkThrows(IOException.class, () -> f.close(), "subclass close may throw");
    }

    /** off/len 区域矩阵：两端与内部区域只写请求部分。 */
    static void testRegionMatrix() throws Exception {
        final java.util.List<String> regions = new java.util.ArrayList<String>();
        class Reg extends OutputStream {
            @Override public void write(int b) { }
            @Override public void write(byte[] b, int off, int len) {
                regions.add(new String(b, off, len, java.nio.charset.StandardCharsets.US_ASCII));
            }
        }
        Reg r = new Reg();
        byte[] data = new byte[]{'a', 'b', 'c', 'd'};
        r.write(data, 0, 4);
        r.write(data, 0, 1);
        r.write(data, 3, 1);
        r.write(data, 1, 2);
        check(regions.get(0).equals("abcd"), "full region");
        check(regions.get(1).equals("a") && regions.get(2).equals("d"), "both ends");
        check(regions.get(3).equals("bc"), "interior region");
        r.write(data, 4, 0);
        check(regions.size() == 5 && regions.get(4).isEmpty(),
              "empty tail region dispatches to subclass override with empty range");
        // 经默认循环（子类只实现 write(int)）的区域取值
        final StringBuilder viaLoop = new StringBuilder();
        class Loop extends OutputStream {
            @Override public void write(int x) { viaLoop.append((char) (x & 0xFF)); }
        }
        Loop lp = new Loop();
        lp.write(data, 2, 2);
        check("cd".contentEquals(viaLoop), "default loop honors region tail");
        lp.write(data, 0, 0);
        check("cd".contentEquals(viaLoop), "zero length through loop");
    }

    /** OutputStream 是 Closeable：try-with-resources 保证 close 恰好一次。 */
    static void testTryWithResourcesClose() throws Exception {
        class CountingClose extends OutputStream {
            int closes, writes;
            @Override public void write(int b) { writes++; }
            @Override public void close() { closes++; }
        }
        CountingClose c = new CountingClose();
        try (OutputStream os = c) {
            os.write(1);
            os.write(2);
        }
        check(c.writes == 2, "both writes landed");
        check(c.closes == 1, "try-with-resources closes exactly once");
    }

    public static void main(String[] args) throws Exception {
        testWriteDelegation();
        testDefaultWriteLoop();
        testArgumentChecking();
        testDefaultFlushClose();
        testOverrideDispatch();
        testPolymorphicUse();
        testWriteByteDelegation();
        testArgumentMatrixDeep();
        testWriteIntPassthrough();
        testDefaultLoopFidelity();
        testCloseFlushNoopDeep();
        testWhiteBoxNoNullStream();
        testExceptionPropagation();
        testRegionMatrix();
        testTryWithResourcesClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

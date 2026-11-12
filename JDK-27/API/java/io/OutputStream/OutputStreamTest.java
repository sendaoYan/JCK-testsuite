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

    // ---- 深化追加（契约级），行为全部经 JDK-27 实测锁定 ----

    /** 子类覆写 write(byte[],int,int) 时，write(byte[]) 委托给它。 */
    static class DelegatingBulk extends OutputStream {
        boolean threeArgCalled;
        final java.util.ArrayList<Byte> seen = new java.util.ArrayList<>();
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
        Sink s = new Sink();
        s.write(new byte[2], 2, 0);
        passed++;                                          // off == b.length, len == 0 legal
        check(s.bytes.isEmpty(), "boundary write writes nothing");
        try {
            s.write(new byte[2], 3, 0);
            check(false, "write(b,3,0) must throw");
        } catch (IndexOutOfBoundsException e) {
            check(e.getMessage() != null && e.getMessage().contains("out of bounds for length 2"),
                  "JDK-27 Range message: " + e.getMessage());
        }
        try {
            s.write(new byte[2], 1, Integer.MAX_VALUE);
            check(false, "write(b,1,MAX) must throw");
        } catch (IndexOutOfBoundsException e) {
            check(e.getMessage() != null && e.getMessage().contains("out of bounds for length 2"),
                  "overflow Range message: " + e.getMessage());
        }
        try {
            s.write((byte[]) null);
            check(false, "write(null) must throw");
        } catch (NullPointerException npe) {
            // -Xcomp 快路径下 helpful NPE message 可能为 null，仅类型是契约。
            check(npe.getMessage() == null || npe.getMessage().contains("\"b\" is null"),
                  "JDK-27 NPE message names b: " + npe.getMessage());
        }
        s.write(new byte[0]);
        check(s.bytes.isEmpty(), "write(empty array) is a no-op");
    }

    /** write(int) 的 int 原样传给子类——截断到低 8 位是子类契约，不是基类行为。 */
    static void testWriteIntPassthrough() throws Exception {
        java.util.List<Integer> seen = new java.util.ArrayList<>();
        class Recorder extends OutputStream {
            @Override public void write(int b) { seen.add(b); }
        }
        Recorder r = new Recorder();
        r.write(0x141);
        r.write(255);
        r.write(0);
        check(seen.size() == 3 && seen.get(0) == 0x141, "value passed through un-truncated");
        check(seen.get(1) == 255 && seen.get(2) == 0, "0 and 255 delivered verbatim");
    }

    /** 默认 write(byte[],int,int) 循环的顺序与内容保真。 */
    static void testDefaultLoopFidelity() throws Exception {
        StringBuilder order = new StringBuilder();
        class Ordered extends OutputStream {
            @Override public void write(int b) { order.append((char) (b & 0xFF)); }
        }
        Ordered o = new Ordered();
        o.write(new byte[]{'a', 'b', 'c'}, 0, 3);
        check("abc".contentEquals(order), "in-order byte delivery");
        o.write(new byte[]{'x', 'y'}, 1, 1);
        check("abcy".contentEquals(order), "off/len region honored in loop");
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

    /** nullOutputStream 全矩阵（含关闭后各 write 形式 "Stream closed"、flush 例外）。 */
    static void testNullOutputStreamMatrix() throws Exception {
        OutputStream n = OutputStream.nullOutputStream();
        n.write(1);
        n.write(0xFF);
        n.write(new byte[]{1, 2, 3});
        n.write(new byte[]{9, 9, 9, 9}, 1, 2);
        n.write(new byte[0]);
        n.write(new byte[2], 0, 0);
        n.flush();
        n.flush();
        passed++;                                          // everything silently discarded
        try {
            n.write((byte[]) null);
            check(false, "nullOutputStream write(null) must throw");
        } catch (NullPointerException npe) {
            // -Xcomp 快路径下 helpful NPE message 可能为 null，仅类型是契约。
            check(npe.getMessage() == null || npe.getMessage().contains("\"b\" is null"),
                  "write(null) NPE: " + npe.getMessage());
        }
        try {
            n.write(null, 0, 1);
            check(false, "write(null,0,1) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            n.write(new byte[2], 0, 3);
            check(false, "out-of-bounds write must throw");
        } catch (IndexOutOfBoundsException e) {
            check(e.getMessage().contains("out of bounds for length 2"), "Range message");
        }
        n.close();
        n.close();
        passed++;                                          // close idempotent
        for (int i = 0; i < 3; i++) {
            OutputStream closed = OutputStream.nullOutputStream();
            closed.close();
            try {
                switch (i) {
                    case 0: closed.write(1); check(false, "write(int) after close"); break;
                    case 1: closed.write(new byte[1]); check(false, "write(byte[]) after close"); break;
                    default:
                        closed.write(new byte[1], 0, 1);
                        check(false, "write(b,off,len) after close"); break;
                }
            } catch (IOException ioe) {
                check("Stream closed".equals(ioe.getMessage()),
                      "message Stream closed: " + ioe.getMessage());
            }
        }
        OutputStream closedFlush = OutputStream.nullOutputStream();
        closedFlush.close();
        closedFlush.flush();
        passed++;                                          // flush after close is allowed (discards)
    }

    /** 白盒补充：write(byte[]) 在 JDK-27 同样派生自 write(byte[],int,int)。 */
    static void testWhiteBoxDeep() throws Exception {
        Class<?> c = java.io.OutputStream.class;
        check(!Modifier.isAbstract(c.getMethod("write", byte[].class, int.class, int.class)
                                       .getModifiers()),
              "write(byte[],int,int) concrete (loops over write(int))");
        boolean hasNullStream = false;
        for (Method m : c.getMethods())
            if (m.getName().equals("nullOutputStream")) hasNullStream = true;
        check(hasNullStream, "JDK-27 has nullOutputStream factory");
    }

    /** 默认循环中 write(int) 抛出的 IOException 原样透传，已写部分保留。 */
    static class BoomThirdWrite extends OutputStream {
        int calls;
        final java.util.List<Integer> seen = new java.util.ArrayList<>();
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
    }

    /** off/len 区域矩阵：两端与内部区域只写请求部分（JDK-27 虚分派同 JDK-8）。 */
    static void testRegionMatrix() throws Exception {
        java.util.List<String> regions = new java.util.ArrayList<>();
        class Reg extends OutputStream {
            @Override public void write(int b) { }
            @Override public void write(byte[] bb, int off, int len) {
                regions.add(new String(bb, off, len, java.nio.charset.StandardCharsets.US_ASCII));
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
              "empty tail region dispatches to subclass override");
        // 经默认循环（子类只实现 write(int)）的区域取值：0x80 符号扩展为 -128
        java.util.List<Integer> viaLoop = new java.util.ArrayList<>();
        class Loop extends OutputStream {
            @Override public void write(int x) { viaLoop.add(x); }
        }
        Loop lp = new Loop();
        lp.write(new byte[]{(byte) 0x80, 0x7F}, 0, 2);
        check(viaLoop.size() == 2 && viaLoop.get(0) == -128 && viaLoop.get(1) == 127,
              "default loop delivers sign-extended bytes");
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
        testWhiteBox();
        testBlackBox();
        testWriteByteDelegation();
        testArgumentMatrixDeep();
        testWriteIntPassthrough();
        testDefaultLoopFidelity();
        testCloseFlushNoopDeep();
        testNullOutputStreamMatrix();
        testWhiteBoxDeep();
        testExceptionPropagation();
        testRegionMatrix();
        testTryWithResourcesClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

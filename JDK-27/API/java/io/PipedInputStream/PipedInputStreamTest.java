/*
 * @test
 * @summary java.io.PipedInputStream conformance: white-box structure + black-box
 *          behavior (both constructors and connect(), round-trip with a single
 *          deterministic writer thread, available() reporting, buffer wraparound past
 *          the pipe capacity, reader-closed write failure, writer-closed EOF,
 *          unconnected read failure).
 * @run main PipedInputStreamTest
 */

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PipedInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    // 深化辅助：断言 op 抛出指定类型（含子类）且消息含精确子串（双侧实测过）
    static void expect(Class<? extends Throwable> type, String msgSub, String label,
                       ThrowingRunnable op) {
        try {
            op.run();
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(label + ": wrong type " + t.getClass().getName()
                        + " (" + t.getMessage() + ")");
            if (msgSub != null && (t.getMessage() == null || !t.getMessage().contains(msgSub)))
                throw new AssertionError(label + ": message \"" + t.getMessage()
                        + "\" lacks \"" + msgSub + "\"");
            passed++;
            return;
        }
        throw new AssertionError(label + ": expected " + type.getName() + " containing \""
                + msgSub + "\", but returned normally");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PipedInputStream");
        check(c.getSuperclass().getName().equals("java.io.InputStream"), "extends InputStream");
        check(c.getConstructor().getModifiers() == Modifier.PUBLIC, "no-arg ctor");
        check(c.getConstructor(int.class) != null, "ctor (int pipeSize)");
        check(c.getConstructor(PipedOutputStream.class) != null, "ctor (PipedOutputStream)");
        check(c.getConstructor(PipedOutputStream.class, int.class) != null,
              "ctor (PipedOutputStream, int)");
        check(c.getMethod("connect", PipedOutputStream.class) != null, "connect method");
        check(Modifier.isProtected(c.getDeclaredField("buffer").getModifiers()), "protected buffer");
        check(Modifier.isProtected(c.getDeclaredField("in").getModifiers())
              && Modifier.isProtected(c.getDeclaredField("out").getModifiers()),
              "protected in/out cursors");
        check(Modifier.isStatic(c.getDeclaredField("PIPE_SIZE").getModifiers())
              && c.getDeclaredField("PIPE_SIZE").getType() == int.class,
              "protected static final int PIPE_SIZE");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. reading before connecting fails
        try {
            new PipedInputStream().read();
            check(false, "read before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 2. constructor wiring + deterministic round-trip: one daemon writer,
        //    the main thread drains it fully (writer closes to signal EOF)
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        Thread writer = new Thread(null, () -> {
            try {
                byte[] payload = new byte[600];
                for (int i = 0; i < payload.length; i++) payload[i] = (byte) i;
                out.write(payload);
                out.write('Z');
                out.flush();
                out.close();                          // signals EOF to the reader
            } catch (IOException ignored) { }
        }, "pis-writer", 0);
        writer.setDaemon(true);
        writer.start();
        int total = 0, last = -1, v;
        while ((v = in.read()) != -1) { total++; last = v; }
        writer.join(5000);
        check(total == 601, "all bytes delivered: " + total);
        check(last == 'Z', "final byte is the sentinel");
        check(!writer.isAlive(), "writer finished");

        // 3. available() reflects buffered bytes after a write
        PipedInputStream in3 = new PipedInputStream(1024);
        PipedOutputStream out3 = new PipedOutputStream();
        out3.connect(in3);
        out3.write(new byte[]{1, 2, 3});
        out3.flush();
        check(in3.available() == 3, "available counts 3");
        check(in3.read() == 1, "consume one");
        check(in3.available() == 2, "available now 2");

        // 4. the circular buffer wraps: write 5000 bytes (> default capacity) with
        //    a concurrent reader; no data lost or reordered
        PipedInputStream in4 = new PipedInputStream();
        PipedOutputStream out4 = new PipedOutputStream(in4);
        Thread w4 = new Thread(null, () -> {
            try {
                for (int i = 0; i < 5000; i++) out4.write(i & 0x7F);
                out4.close();
            } catch (IOException ignored) { }
        }, "pis-wrap", 0);
        w4.setDaemon(true);
        w4.start();
        boolean ordered = true;
        for (int i = 0; i < 5000; i++) {
            int b = in4.read();
            if (b != (i & 0x7F)) { ordered = false; break; }
        }
        check(ordered, "5000 bytes in exact order across buffer wraparound");
        check(in4.read() == -1, "EOF after writer closed");
        w4.join(5000);

        // 5. write after the reader closed fails with "Pipe closed"
        PipedInputStream in5 = new PipedInputStream();
        PipedOutputStream out5 = new PipedOutputStream(in5);
        in5.close();
        try {
            out5.write(1);
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "failure message: " + ioe.getMessage());
        }

        // 6. read after the reader itself closed throws
        try {
            in5.read();
            check(false, "read after own close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 7. double connect is refused
        PipedInputStream in7 = new PipedInputStream();
        PipedOutputStream first = new PipedOutputStream(in7);
        try {
            in7.connect(new PipedOutputStream());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }
        first.close();

        // 8. bulk read drains the pipe
        PipedInputStream in8 = new PipedInputStream();
        PipedOutputStream out8 = new PipedOutputStream(in8);
        Thread w8 = new Thread(null, () -> {
            try {
                out8.write(new byte[]{10, 20, 30, 40});
                out8.close();
            } catch (IOException ignored) { }
        }, "pis-bulk", 0);
        w8.setDaemon(true);
        w8.start();
        byte[] buf = new byte[4];
        int n = 0;
        while (n < 4) {
            int got = in8.read(buf, n, 4 - n);
            if (got < 0) break;
            n += got;
        }
        check(n == 4 && buf[0] == 10 && buf[3] == 40, "bulk read drained in order");
        check(in8.read() == -1, "EOF follows");
        w8.join(5000);
    }

    // ==================== 深化追加（契约级，批次 ioD） ====================

    // 构造矩阵：pipeSize 边界 IAE、PIPE_SIZE 常量、connect(null)、重复 connect 精确消息、
    // closed-unconnected 端 connect 成功（字节侧 connect 无 closed 检查，实测 B13）
    static void testCtorAndConnectMatrix() throws Exception {
        expect(IllegalArgumentException.class, "Pipe Size <= 0", "pipeSize -1",
                () -> new PipedInputStream(-1));
        expect(IllegalArgumentException.class, "Pipe Size <= 0", "pipeSize 0",
                () -> new PipedInputStream(0));
        // JDK-27 模块系统禁止 setAccessible(java.base)；PIPE_SIZE 值改用行为探针：
        // 默认管道单线程写恰好 1024 字节不阻塞且 available()==1024（首次 receive 不会 awaitSpace）
        Field f = PipedInputStream.class.getDeclaredField("PIPE_SIZE");
        check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                && Modifier.isProtected(f.getModifiers()), "PIPE_SIZE protected static final");
        PipedInputStream def = new PipedInputStream();
        PipedOutputStream defOut = new PipedOutputStream(def);
        defOut.write(new byte[1024], 0, 1024);
        check(def.available() == 1024, "PIPE_SIZE == 1024 (exactly-full default buffer)");
        check(def.read(new byte[1024], 0, 1024) == 1024, "default buffer drains 1024");

        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        // JDK-27 NPE 带 helpful message（JDK-8 消息为 null，双侧类型一致，差异已记录）
        expect(NullPointerException.class, "because \"src\" is null", "connect(null)",
                () -> in.connect(null));
        expect(IOException.class, "Already connected", "in.connect(other) after connected",
                () -> in.connect(new PipedOutputStream()));
        expect(IOException.class, "Already connected", "out.connect(other) after connected",
                () -> out.connect(new PipedInputStream()));
        expect(IOException.class, "Already connected", "third party connect(connected in)",
                () -> new PipedOutputStream().connect(in));
        expect(IOException.class, "Already connected", "ctor with already-connected src",
                () -> new PipedInputStream(out));

        // (src, pipeSize) 构造器连接且缓冲生效
        PipedOutputStream out2 = new PipedOutputStream();
        PipedInputStream in2 = new PipedInputStream(out2, 4);
        out2.write(new byte[]{1, 2, 3, 4}, 0, 4);
        check(in2.available() == 4, "(src,4) sized buffer exactly full");
        check(in2.read() == 1 && in2.read() == 2 && in2.read() == 3 && in2.read() == 4,
                "(src,pipeSize) round-trip");
        check(in2.available() == 0, "drained");

        // 未连接 close 后仍可 connect（字节侧无 closed 检查，实测 B13），
        // 但之后读/写都报 "Pipe closed"（closedByReader 已置位）
        PipedInputStream in3 = new PipedInputStream();
        in3.close();
        PipedOutputStream out3 = new PipedOutputStream();
        out3.connect(in3);
        passed++; // connect succeeded without exception
        expect(IOException.class, "Pipe closed", "write into closed-unconnected peer",
                () -> out3.write(9));
        expect(IOException.class, "Pipe closed", "read from closed-unconnected self",
                () -> in3.read());
    }

    // read(byte[],off,len) 参数校验顺序契约（实测：bounds 与 len==0 先于连接检查）
    static void testReadBoundsAndZero() throws Exception {
        final byte[] b = new byte[4];
        check(new PipedInputStream().read(b, 0, 0) == 0,
                "len==0 unconnected returns 0 (bounds/len precede connection check)");
        // JDK-27 IOOBE 带 Objects.checkFromIndexSize 消息（JDK-8 为 null 消息）
        expect(IndexOutOfBoundsException.class, "out of bounds", "read(b,-1,0) unconnected is IOOBE not IOE",
                () -> new PipedInputStream().read(b, -1, 0));
        expect(IndexOutOfBoundsException.class, "out of bounds", "read(b,0,-1) unconnected",
                () -> new PipedInputStream().read(b, 0, -1));
        expect(NullPointerException.class, null, "read(null,0,1)",
                () -> new PipedInputStream().read(null, 0, 1));
        expect(IOException.class, "Pipe not connected", "read(b,0,1) unconnected",
                () -> new PipedInputStream().read(b, 0, 1));

        PipedInputStream in = new PipedInputStream();
        new PipedOutputStream(in);
        expect(IndexOutOfBoundsException.class, "out of bounds", "read(b,2,3) len>remaining",
                () -> in.read(b, 2, 3));
        in.close();
        check(in.read(b, 0, 0) == 0, "len==0 after own close still 0");
        expect(IOException.class, "Pipe closed", "read(b,0,1) after own close",
                () -> in.read(b, 0, 1));

        PipedInputStream in2 = new PipedInputStream();
        PipedOutputStream out2 = new PipedOutputStream(in2);
        out2.close();
        check(in2.read(b, 0, 0) == 0, "len==0 after writer close still 0");
        check(in2.read(b, 0, 1) == -1, "read(b,0,1) after writer close is EOF");
    }

    // available()：未连接/closed → 0；缓冲满 → pipeSize；排空 → 0
    static void testAvailableContract() throws Exception {
        check(new PipedInputStream().available() == 0, "unconnected available == 0");
        PipedInputStream closed = new PipedInputStream();
        closed.close();
        check(closed.available() == 0, "closed available == 0");
        PipedInputStream in = new PipedInputStream(4);
        PipedOutputStream out = new PipedOutputStream(in);
        out.write(new byte[]{9, 9, 9, 9}, 0, 4);
        check(in.available() == 4, "exactly-full buffer reports pipeSize");
        check(in.read(new byte[4], 0, 4) == 4, "drain 4");
        check(in.available() == 0, "empty after drain");
    }

    // protected receive(int) 契约（子类暴露）
    static class ExposingPipedInputStream extends PipedInputStream {
        void rx(int b) throws IOException { receive(b); }
    }

    static void testReceiveProtected() throws Exception {
        final ExposingPipedInputStream ex = new ExposingPipedInputStream();
        expect(IOException.class, "Pipe not connected", "receive(int) unconnected",
                () -> ex.rx(1));
        ExposingPipedInputStream ex2 = new ExposingPipedInputStream();
        PipedOutputStream p2 = new PipedOutputStream(ex2);
        p2.close();
        expect(IOException.class, "Pipe closed", "receive(int) after writer close",
                () -> ex2.rx(1));
        ExposingPipedInputStream ex3 = new ExposingPipedInputStream();
        new PipedOutputStream(ex3);
        ex3.rx(7);
        check(ex3.available() == 1, "receive(int) buffers one byte");
        check(ex3.read() == 7, "received byte readable");
    }

    // broken pipe 双向检测（受控 daemon 线程 + latch/join 超时，无时序依赖）：
    // 写端死亡 → 读端 "Write end dead"；读端死亡 → 写端 "Read end dead"（双侧实测）
    static void testBrokenPipeDetection() throws Exception {
        // Write end dead：writer 线程写 1 字节后死亡（不 close），排空后再读立即抛
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        Thread w = new Thread(() -> {
            try { out.write(42); } catch (IOException ignored) { }
        }, "pis-d-writer");
        w.setDaemon(true);
        w.start();
        w.join(15000);
        check(!w.isAlive(), "writer thread died within bound");
        check(in.read() == 42, "buffered byte still delivered after writer death");
        expect(IOException.class, "Write end dead", "read after writer thread death",
                () -> in.read());

        // Read end dead：reader 线程读 1 字节后死亡（不 close），主线程再写立即抛
        final PipedInputStream in2 = new PipedInputStream();
        final PipedOutputStream out2 = new PipedOutputStream(in2);
        out2.write(43);
        final CountDownLatch done = new CountDownLatch(1);
        Thread r = new Thread(() -> {
            try { in2.read(); } catch (IOException ignored) { }
            finally { done.countDown(); }
        }, "pis-d-reader");
        r.setDaemon(true);
        r.start();
        check(done.await(15, TimeUnit.SECONDS), "reader consumed within bound");
        r.join(15000);
        check(!r.isAlive(), "reader thread died within bound");
        expect(IOException.class, "Read end dead", "write after reader thread death",
                () -> out2.write(44));
    }

    // markSupported false；InputStream.skip 语义：负/0 → 0、跳过、EOF 后 0
    static void testSkipAndMark() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        check(!in.markSupported(), "markSupported is false");
        out.write(new byte[]{1, 2, 3, 4, 5, 6}, 0, 6);
        check(in.skip(2) == 2, "skip 2");
        check(in.read() == 3, "read resumes after skip");
        check(in.skip(-5) == 0, "negative skip returns 0");
        check(in.skip(0) == 0, "skip(0) returns 0");
        out.close();
        check(in.skip(4) == 3, "skip past remaining returns what is left (3)");
        check(in.skip(3) == 0, "skip at EOF returns 0");
        check(in.read() == -1, "EOF confirmed");
    }

    // 单线程写后读（数据在缓冲内时允许）：unsigned 字节、UTF-8 拆批、计数递减
    static void testSingleThreadAndUtf8() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        out.write(0xCA);
        check(in.read() == 202, "byte delivered unsigned (0xCA -> 202)");

        byte[] utf8 = "héllo".getBytes("UTF-8");
        out.write(utf8, 0, 1);
        out.write(utf8, 1, utf8.length - 1);
        byte[] buf = new byte[32];
        int n = in.read(buf, 0, buf.length);
        check(new String(buf, 0, n, "UTF-8").equals("héllo"),
                "multi-byte UTF-8 across split writes reassembles");

        out.write(new byte[8], 0, 8);
        check(in.available() == 8, "8 buffered");
        check(in.read() == 0, "consume one");
        check(in.available() == 7, "7 left");
    }

    // 跨线程 latch 同步往返：256 缓冲 × 4096 字节，强制多次回绕与写端背压
    static void testCrossThreadLatchedRoundTrip() throws Exception {
        final int n = 4096;
        final PipedInputStream in = new PipedInputStream(256);
        final PipedOutputStream out = new PipedOutputStream(in);
        final CountDownLatch written = new CountDownLatch(1);
        Thread w = new Thread(() -> {
            try {
                byte[] payload = new byte[n];
                for (int i = 0; i < n; i++) payload[i] = (byte) i;
                out.write(payload, 0, n);
                out.close();
            } catch (IOException ignored) { }
            finally { written.countDown(); }
        }, "pis-x-writer");
        w.setDaemon(true);
        w.start();
        int got = 0;
        boolean ordered = true;
        int v;
        while ((v = in.read()) != -1) {
            if (v != (got & 0xFF)) ordered = false;
            got++;
        }
        check(written.await(15, TimeUnit.SECONDS), "writer finished within bound");
        w.join(15000);
        check(!w.isAlive(), "writer joined");
        check(got == n, "all 4096 bytes delivered: " + got);
        check(ordered, "order preserved across wraparounds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorAndConnectMatrix();
        testReadBoundsAndZero();
        testAvailableContract();
        testReceiveProtected();
        testBrokenPipeDetection();
        testSkipAndMark();
        testSingleThreadAndUtf8();
        testCrossThreadLatchedRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

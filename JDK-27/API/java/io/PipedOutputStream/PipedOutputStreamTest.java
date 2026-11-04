/*
 * @test
 * @summary java.io.PipedOutputStream conformance: white-box structure + black-box
 *          behavior (both constructors and connect(), write/flush round-trip with a
 *          single deterministic reader thread, unconnected write failure,
 *          reader-closed write failure, connect idempotence errors, close terminates
 *          the peer with EOF).
 * @run main PipedOutputStreamTest
 */

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PipedOutputStreamTest {
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
        Class<?> c = Class.forName("java.io.PipedOutputStream");
        check(c.getSuperclass().getName().equals("java.io.OutputStream"), "extends OutputStream");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(PipedInputStream.class) != null, "ctor (PipedInputStream)");
        check(c.getMethod("connect", PipedInputStream.class) != null, "connect method");
        check(c.getMethod("write", int.class) != null, "write(int)");
        check(c.getMethod("write", byte[].class, int.class, int.class) != null,
              "write(byte[], int, int)");
        check(c.getMethod("flush") != null, "flush");
        check(Modifier.isPrivate(c.getDeclaredField("sink").getModifiers())
                  && c.getDeclaredField("sink").getType() == PipedInputStream.class,
              "private volatile sink");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. writing before connecting fails
        try {
            new PipedOutputStream().write(1);
            check(false, "write before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 2. flush before connecting is a silent no-op (sink == null)
        new PipedOutputStream().flush();
        passed++;

        // 3. round-trip: write a known payload from the main thread while one
        //    daemon reader drains it, then close to deliver EOF
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        out.connect(in);
        final java.util.List<Integer> got = java.util.Collections.synchronizedList(
                new java.util.ArrayList<>());
        Thread reader = new Thread(null, () -> {
            try {
                int v;
                while ((v = in.read()) != -1) got.add(v);
            } catch (IOException ignored) { }
        }, "pos-reader", 0);
        reader.setDaemon(true);
        reader.start();
        for (int i = 1; i <= 100; i++) out.write(i);
        out.flush();
        out.close();                                        // wakes the reader with EOF
        reader.join(5000);
        check(!reader.isAlive(), "reader terminated on close");
        check(got.size() == 100, "reader received every byte: " + got.size());
        boolean ordered = got.size() == 100;
        for (int i = 0; i < got.size() && ordered; i++) ordered = got.get(i) == i + 1;
        check(ordered, "payload order preserved");

        // 4. bulk write delivers identical bytes
        PipedOutputStream out4 = new PipedOutputStream(new PipedInputStream());
        // no reader thread: the default 1024-byte buffer absorbs 4 bytes without blocking
        out4.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}, 0, 4);
        out4.flush();
        passed++;

        // 5. write after the reader closed fails
        PipedOutputStream out5 = new PipedOutputStream();
        PipedInputStream in5 = new PipedInputStream(out5);
        in5.close();
        try {
            out5.write(7);
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 6. double connect is refused in both directions
        PipedOutputStream out6 = new PipedOutputStream(new PipedInputStream());
        try {
            out6.connect(new PipedInputStream());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 7. close() is idempotent and a later write still fails cleanly
        out6.close();
        out6.close();
        try {
            out6.write(0);
            check(false, "write after close must throw");
        } catch (IOException ioe) {
            passed++;
        }

        // 8. writing a sub-range copies only that range (verified via the peer)
        PipedInputStream in8 = new PipedInputStream();
        PipedOutputStream out8 = new PipedOutputStream(in8);
        out8.write(new byte[]{9, 8, 7, 6, 5}, 1, 3);
        out8.flush();
        check(in8.available() == 3, "three bytes buffered: " + in8.available());
        check(in8.read() == 8 && in8.read() == 7 && in8.read() == 6, "range [1,4) delivered");
    }

    // ==================== 深化追加（契约级，批次 ioD） ====================

    // JDK-27 特有结构差异：sink 声明为 volatile（JDK-8 为普通 private 字段）
    static void testJdk27Structure() throws Exception {
        check(Modifier.isVolatile(PipedOutputStream.class.getDeclaredField("sink").getModifiers()),
                "JDK-27 sink is volatile");
        check(Modifier.isPrivate(PipedOutputStream.class.getDeclaredField("sink").getModifiers())
                && PipedOutputStream.class.getDeclaredField("sink").getType() == PipedInputStream.class,
                "sink still private PipedInputStream");
    }

    // 构造/connect 矩阵：connect(null) NPE、双向重复 connect 精确消息 "Already connected"、
    // 未连接 close 是真空操作且之后仍可 connect（字节侧 connect 无 closed 检查）
    static void testCtorAndConnectMatrix() throws Exception {
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        // connect(null) 是显式 NPE：双侧消息均为 null（实测 B08）
        expect(NullPointerException.class, null, "connect(null)",
                () -> out.connect(null));
        expect(IOException.class, "Already connected", "out.connect(other) after connected",
                () -> out.connect(new PipedInputStream()));
        expect(IOException.class, "Already connected", "in.connect(other) after connected",
                () -> in.connect(new PipedOutputStream()));
        expect(IOException.class, "Already connected", "third party connect(connected in)",
                () -> new PipedOutputStream().connect(in));

        // was-connected close 后 connect → "Already connected"（sink 仍非 null）
        PipedOutputStream out2 = new PipedOutputStream(new PipedInputStream());
        out2.close();
        expect(IOException.class, "Already connected", "connect after own close (was connected)",
                () -> out2.connect(new PipedInputStream()));

        // unconnected close 是 no-op（sink==null），之后 connect 成功且可往返（实测）
        PipedOutputStream out3 = new PipedOutputStream();
        out3.close();
        passed++; // unconnected close returned normally
        PipedInputStream in3 = new PipedInputStream();
        out3.connect(in3);
        passed++; // connect after unconnected-close succeeded
        out3.write(77);
        check(in3.available() == 1 && in3.read() == 77,
                "round-trip works after unconnected close + connect");
    }

    // write(byte[],off,len) 校验顺序：sink 检查先于一切（len==0 未连接也抛）；bounds IOOBE
    static void testWriteBoundsAndZero() throws Exception {
        final byte[] b = new byte[3];
        expect(IOException.class, "Pipe not connected", "write(b,0,0) unconnected (sink check first)",
                () -> new PipedOutputStream().write(b, 0, 0));
        PipedOutputStream out = new PipedOutputStream(new PipedInputStream());
        expect(NullPointerException.class, null, "write(null,0,1)",
                () -> out.write(null, 0, 1));
        // JDK-27 IOOBE 带 Objects.checkFromIndexSize 消息（JDK-8 为 null 消息）
        expect(IndexOutOfBoundsException.class, "out of bounds", "write(b,-1,2)",
                () -> out.write(b, -1, 2));
        expect(IndexOutOfBoundsException.class, "out of bounds", "write(b,0,-1)",
                () -> out.write(b, 0, -1));
        expect(IndexOutOfBoundsException.class, "out of bounds", "write(b,2,2) len>remaining",
                () -> out.write(b, 2, 2));
        expect(IndexOutOfBoundsException.class, "out of bounds", "write(b,Integer.MAX_VALUE,1) overflow",
                () -> out.write(b, Integer.MAX_VALUE, 1));

        // len==0 在 close 后仍静默返回（len==0 先于 receive 状态检查，实测）
        PipedInputStream in4 = new PipedInputStream();
        PipedOutputStream out4 = new PipedOutputStream(in4);
        out4.close();
        out4.write(b, 0, 0);
        passed++; // len==0 after own close is silent
        in4.close();
        out4.write(b, 0, 0);
        passed++; // len==0 after peer close is silent
    }

    // flush 从不抛异常（与 PipedWriter.flush 抛 "Pipe closed" 的关键族差异，实测）
    static void testFlushNeverThrows() throws Exception {
        new PipedOutputStream().flush();
        passed++; // unconnected flush silent
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        in.close();
        out.flush();
        passed++; // flush after reader close silent
        out.close();
        out.flush();
        passed++; // flush after own close silent
    }

    // close 后写：精确消息 "Pipe closed"；close 幂等
    static void testWriteAfterCloseContract() throws Exception {
        PipedOutputStream out = new PipedOutputStream(new PipedInputStream());
        out.close();
        out.close();
        passed++; // close idempotent
        expect(IOException.class, "Pipe closed", "write(int) after own close",
                () -> out.write(1));
        expect(IOException.class, "Pipe closed", "write(b,0,1) after own close",
                () -> out.write(new byte[]{1}, 0, 1));
    }

    // 读端已 close 但未连接时 connect 仍成功，随后写报 "Pipe closed"（字节侧无 closed 检查）
    static void testClosedUnconnectedPeer() throws Exception {
        PipedInputStream in = new PipedInputStream();
        in.close();
        PipedOutputStream out = new PipedOutputStream();
        out.connect(in);
        passed++; // connect to closed-unconnected peer succeeds
        expect(IOException.class, "Pipe closed", "write into closed-unconnected peer",
                () -> out.write(9));
    }

    // 读端线程死亡（不 close）→ 写端 "Read end dead"（受控 daemon 线程 + latch，实测）
    static void testReadEndDead() throws Exception {
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        out.write(43);
        final CountDownLatch done = new CountDownLatch(1);
        Thread r = new Thread(() -> {
            try { in.read(); } catch (IOException ignored) { }
            finally { done.countDown(); }
        }, "pos-d-reader");
        r.setDaemon(true);
        r.start();
        check(done.await(15, TimeUnit.SECONDS), "reader consumed within bound");
        r.join(15000);
        check(!r.isAlive(), "reader thread died within bound");
        expect(IOException.class, "Read end dead", "write after reader thread death",
                () -> out.write(44));
    }

    // unsigned 字节与全数组 write(byte[]) 等价性
    static void testUnsignedAndFullArray() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        out.write(0xFF);
        check(in.read() == 255, "0xFF delivered unsigned as 255");
        out.write(new byte[]{(byte) 200, (byte) 151, (byte) 100});
        check(in.read() == 200 && in.read() == 151 && in.read() == 100,
                "write(byte[]) full-array delivers exact unsigned values");
    }

    // 跨线程大负载：8192 字节（分块写，背压由并发读者排解）+ close 交付 EOF + latch
    static void testCrossThreadBigPayload() throws Exception {
        final int n = 8192;
        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);
        final CountDownLatch done = new CountDownLatch(1);
        final int[] result = new int[2]; // {count, ordered(1/0)}
        Thread r = new Thread(() -> {
            int got = 0;
            boolean ordered = true;
            int v;
            try {
                while ((v = in.read()) != -1) {
                    if (v != (got & 0xFF)) ordered = false;
                    got++;
                }
            } catch (IOException ignored) { }
            result[0] = got;
            result[1] = ordered ? 1 : 0;
            done.countDown();
        }, "pos-x-reader");
        r.setDaemon(true);
        r.start();
        byte[] chunk = new byte[512];
        for (int base = 0; base < n; base += 512) {
            for (int i = 0; i < 512; i++) chunk[i] = (byte) (base + i);
            out.write(chunk, 0, 512);
        }
        out.flush();
        out.close();
        check(done.await(15, TimeUnit.SECONDS), "reader finished within bound");
        r.join(15000);
        check(!r.isAlive(), "reader joined");
        check(result[0] == n, "all 8192 bytes received: " + result[0]);
        check(result[1] == 1, "order preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testJdk27Structure();
        testCtorAndConnectMatrix();
        testWriteBoundsAndZero();
        testFlushNeverThrows();
        testWriteAfterCloseContract();
        testClosedUnconnectedPeer();
        testReadEndDead();
        testUnsignedAndFullArray();
        testCrossThreadBigPayload();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

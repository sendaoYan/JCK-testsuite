/*
 * @test
 * @summary java.io.PipedReader conformance: white-box structure + black-box behavior
 *          (both constructors and connect(), round-trip with a single deterministic
 *          writer thread, ready() semantics, unconnected read failure, reader-closed
 *          read/write failures, writer-closed EOF, small-buffer constructor).
 * @run main PipedReaderTest
 */

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PipedReaderTest {
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
        Class<?> c = Class.forName("java.io.PipedReader");
        check(c.getSuperclass().getName().equals("java.io.Reader"), "extends Reader");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(int.class) != null, "ctor (int pipeSize)");
        check(c.getConstructor(PipedWriter.class) != null, "ctor (PipedWriter)");
        check(c.getConstructor(PipedWriter.class, int.class) != null,
              "ctor (PipedWriter, int)");
        check(c.getMethod("connect", PipedWriter.class) != null, "connect method");
        check(c.getDeclaredField("buffer").getType() == char[].class
                  && !Modifier.isPrivate(c.getDeclaredField("buffer").getModifiers()),
              "package-visible char[] buffer");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. reading before connecting fails
        try {
            new PipedReader().read();
            check(false, "read before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 2. deterministic round-trip: one daemon writer emits a known char
        //    sequence and closes; the main thread drains to EOF
        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);
        Thread writer = new Thread(null, () -> {
            try {
                for (char ch = 'a'; ch <= 'z'; ch++) out.write(ch);
                out.flush();
                out.close();                              // signals EOF
            } catch (IOException ignored) { }
        }, "pr-writer", 0);
        writer.setDaemon(true);
        writer.start();
        StringBuilder sb = new StringBuilder();
        int v;
        while ((v = in.read()) != -1) sb.append((char) v);
        writer.join(5000);
        check("abcdefghijklmnopqrstuvwxyz".contentEquals(sb), "payload: " + sb);
        check(!writer.isAlive(), "writer finished");

        // 3. bulk read into a char[] region
        PipedReader in3 = new PipedReader(1024);
        PipedWriter out3 = new PipedWriter();
        out3.connect(in3);
        out3.write("hello".toCharArray(), 1, 3);          // "ell"
        out3.flush();
        char[] buf = new char[5];
        int n = in3.read(buf, 1, 3);
        check(n == 3, "three chars read: " + n);
        check(buf[1] == 'e' && buf[2] == 'l' && buf[3] == 'l' && buf[0] == 0 && buf[4] == 0,
              "region filled, untouched slots intact");

        // 4. ready() is true exactly when data is buffered
        PipedReader in4 = new PipedReader();
        PipedWriter out4 = new PipedWriter(in4);
        check(!in4.ready(), "empty pipe is not ready");
        out4.write('x');
        out4.flush();
        check(in4.ready(), "buffered data makes it ready");
        check(in4.read() == 'x', "consume the char");
        check(!in4.ready(), "drained again");

        // 5. writer closing delivers EOF; read-after-EOF keeps returning -1
        PipedReader in5 = new PipedReader();
        PipedWriter out5 = new PipedWriter(in5);
        out5.write('Q');
        out5.close();
        check(in5.read() == 'Q', "byte before close");
        check(in5.read() == -1, "EOF after writer close");

        // 6. closing the reader makes subsequent reads fail
        PipedReader in6 = new PipedReader();
        PipedWriter out6 = new PipedWriter(in6);
        in6.close();
        try {
            in6.read();
            check(false, "read after close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 7. writing into a closed reader fails from the writer side
        try {
            out6.write('z');
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "failure reported");
        }

        // 8. double connect refused
        PipedReader in8 = new PipedReader();
        PipedWriter first = new PipedWriter(in8);
        try {
            in8.connect(new PipedWriter());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }
        first.close();

        // 9. skip() forwards to the character stream
        PipedReader in9 = new PipedReader();
        PipedWriter out9 = new PipedWriter(in9);
        out9.write("abcdef");
        out9.close();
        check(in9.skip(2) == 2, "skip two chars");
        check(in9.read() == 'c', "read resumes after skip");

        // 10. tiny pipe (size 1) round-trips once a reader is draining it
        PipedReader in10 = new PipedReader(1);
        PipedWriter out10 = new PipedWriter(in10);
        Thread w10 = new Thread(null, () -> {
            try {
                out10.write("AB");
                out10.close();
            } catch (IOException ignored) { }
        }, "pr-small", 0);
        w10.setDaemon(true);
        w10.start();
        check(in10.read() == 'A' && in10.read() == 'B' && in10.read() == -1,
              "size-1 pipe delivers both chars");
        w10.join(5000);
    }

    // ==================== 深化追加（契约级，批次 ioD） ====================

    // 构造/connect 矩阵：pipeSize IAE 消息与字节版大小写不同（"Pipe size <= 0"）；
    // 字符版 connect 检查 closed 状态（与字节版差异）：closed-unconnected 端 connect → "Pipe closed"
    static void testCtorAndConnectMatrix() throws Exception {
        expect(IllegalArgumentException.class, "Pipe size <= 0", "pipeSize -1",
                () -> new PipedReader(-1));
        expect(IllegalArgumentException.class, "Pipe size <= 0", "pipeSize 0",
                () -> new PipedReader(0));
        final PipedReader in = new PipedReader();
        final PipedWriter out = new PipedWriter(in);
        // JDK-27 connect(null) NPE 带 helpful message（JDK-8 为 null 消息）
        expect(NullPointerException.class, "because \"src\" is null", "connect(null)",
                () -> in.connect(null));
        expect(IOException.class, "Already connected", "in.connect(other) after connected",
                () -> in.connect(new PipedWriter()));
        expect(IOException.class, "Already connected", "out.connect(other) after connected",
                () -> out.connect(new PipedReader()));
        expect(IOException.class, "Already connected", "third party connect(connected in)",
                () -> new PipedWriter().connect(in));
        expect(IOException.class, "Already connected", "ctor with already-connected src",
                () -> new PipedReader(out));

        // 字符版关键差异：未连接 close 后 connect → "Pipe closed"（字节版是 connect 成功）
        PipedReader closedReader = new PipedReader();
        closedReader.close();
        expect(IOException.class, "Pipe closed", "connect after own close (unconnected)",
                () -> closedReader.connect(new PipedWriter()));
        expect(IOException.class, "Pipe closed", "writer ctor with closed unconnected reader",
                () -> new PipedWriter(closedReader));
        expect(IOException.class, "Pipe closed", "writer connect to closed unconnected reader",
                () -> new PipedWriter().connect(closedReader));

        // was-connected writer close 后 connect(other) → "Already connected"（sink 先判）
        PipedWriter out2 = new PipedWriter(new PipedReader());
        out2.close();
        expect(IOException.class, "Already connected", "connect other after own close (was connected)",
                () -> out2.connect(new PipedReader()));

        // (src, pipeSize) 构造器连接且缓冲生效
        PipedWriter out3 = new PipedWriter();
        PipedReader in3 = new PipedReader(out3, 4);
        out3.write("abcd", 0, 4);
        check(in3.ready(), "(src,4) sized buffer full and ready");
        check(in3.read(new char[4], 0, 4) == 4, "(src,pipeSize) round-trip");
    }

    // read(char[],off,len) 校验顺序契约：连接/关闭检查先于 bounds 与 len==0
    //（与 PipedInputStream 相反：字节版 bounds/len==0 先于连接检查）
    static void testReadBoundsAndOrder() throws Exception {
        final char[] c = new char[4];
        expect(IOException.class, "Pipe not connected", "read(c,0,0) unconnected throws (order!)",
                () -> new PipedReader().read(c, 0, 0));
        expect(IOException.class, "Pipe not connected", "read(c,-1,0) unconnected throws (order!)",
                () -> new PipedReader().read(c, -1, 0));

        PipedReader in = new PipedReader();
        new PipedWriter(in);
        expect(NullPointerException.class, null, "read(null,0,1)",
                () -> in.read(null, 0, 1));
        // JDK-27 IOOBE 带 Objects.checkFromIndexSize 消息（JDK-8 为 null 消息）
        expect(IndexOutOfBoundsException.class, "out of bounds", "read(c,-1,0) connected",
                () -> in.read(c, -1, 0));
        expect(IndexOutOfBoundsException.class, "out of bounds", "read(c,0,-1)",
                () -> in.read(c, 0, -1));
        expect(IndexOutOfBoundsException.class, "out of bounds", "read(c,3,2) len>remaining",
                () -> in.read(c, 3, 2));
        check(in.read(c, 0, 0) == 0, "len==0 connected returns 0");

        in.close();
        expect(IOException.class, "Pipe closed", "read(c,0,0) after own close (order!)",
                () -> in.read(c, 0, 0));
        expect(IOException.class, "Pipe closed", "read(c,-1,0) after own close (order!)",
                () -> in.read(c, -1, 0));

        PipedReader in2 = new PipedReader();
        PipedWriter out2 = new PipedWriter(in2);
        out2.close();
        check(in2.read(c, 0, 1) == -1, "read(c,0,1) after writer close is EOF");
    }

    // ready() 契约：未连接/closed 抛、空 false、有数据 true、排空 false、写端关闭后 false
    static void testReadyContract() throws Exception {
        expect(IOException.class, "Pipe not connected", "ready() unconnected",
                () -> new PipedReader().ready());
        PipedReader closed = new PipedReader();
        new PipedWriter(closed);
        closed.close();
        expect(IOException.class, "Pipe closed", "ready() after own close",
                () -> closed.ready());

        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);
        check(!in.ready(), "empty pipe not ready (writer alive)");
        out.write('x');
        check(in.ready(), "buffered char makes it ready");
        check(in.read() == 'x', "consume");
        check(!in.ready(), "drained again");
        out.close();
        check(!in.ready(), "not ready at EOF");
    }

    // broken pipe 双向（受控 daemon 线程 + join/latch 超时）：写端死 → read/read(c)/ready
    // 三处入口检查均抛 "Write end dead"；读端死 → 写端 "Read end dead"（双侧实测）
    static void testBrokenPipeDetection() throws Exception {
        final PipedReader in = new PipedReader();
        final PipedWriter out = new PipedWriter(in);
        Thread w = new Thread(() -> {
            try { out.write('w'); } catch (IOException ignored) { }
        }, "pr-d-writer");
        w.setDaemon(true);
        w.start();
        w.join(15000);
        check(!w.isAlive(), "writer thread died within bound");
        check(in.read() == 'w', "buffered char still delivered after writer death");
        expect(IOException.class, "Write end dead", "read() after writer thread death",
                () -> in.read());
        expect(IOException.class, "Write end dead", "read(c,0,1) after writer thread death",
                () -> in.read(new char[1], 0, 1));
        expect(IOException.class, "Write end dead", "ready() after writer thread death",
                () -> in.ready());

        final PipedReader in2 = new PipedReader();
        final PipedWriter out2 = new PipedWriter(in2);
        out2.write('q');
        final CountDownLatch done = new CountDownLatch(1);
        Thread r = new Thread(() -> {
            try { in2.read(); } catch (IOException ignored) { }
            finally { done.countDown(); }
        }, "pr-d-reader");
        r.setDaemon(true);
        r.start();
        check(done.await(15, TimeUnit.SECONDS), "reader consumed within bound");
        r.join(15000);
        check(!r.isAlive(), "reader thread died within bound");
        expect(IOException.class, "Read end dead", "write after reader thread death",
                () -> out2.write('r'));
    }

    // skip 契约（Reader.skip，非覆写）：负数 → IAE（与 InputStream.skip 返回 0 的族差异）、
    // 0 → 0、跳过、EOF 后 0、越余量返回实际值（双侧实测）
    static void testSkipContract() throws Exception {
        expect(IOException.class, "Pipe not connected", "skip(1) unconnected",
                () -> new PipedReader().skip(1));
        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);
        expect(IllegalArgumentException.class, "skip value is negative", "skip(-3)",
                () -> in.skip(-3));
        check(in.skip(0) == 0, "skip(0) returns 0");
        out.write("abcdef", 0, 6);
        check(in.skip(2) == 2, "skip 2");
        check(in.read() == 'c', "read resumes after skip");
        out.close();
        check(in.skip(99) == 3, "skip past remaining (3 of d,e,f)");
        check(in.skip(3) == 0, "skip at EOF returns 0");
        check(in.read() == -1, "EOF confirmed");
    }

    // char 全域 16 位传递（字节版只有 0-255 unsigned）
    static void testCharRange() throws Exception {
        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);
        out.write('ሴ');
        out.write('￿');
        check(in.read() == 0x1234, "BMP char 0x1234 preserved");
        check(in.read() == 0xFFFF, "char 0xFFFF preserved");
    }

    // 跨线程 latch 同步往返：64 缓冲 × 2048 字符，强制多次回绕与写端背压
    static void testCrossThreadLatchedRoundTrip() throws Exception {
        final int n = 2048;
        final PipedReader in = new PipedReader(64);
        final PipedWriter out = new PipedWriter(in);
        final CountDownLatch written = new CountDownLatch(1);
        Thread w = new Thread(() -> {
            try {
                char[] payload = new char[n];
                for (int i = 0; i < n; i++) payload[i] = (char) ('A' + (i % 26));
                out.write(payload, 0, n);
                out.close();
            } catch (IOException ignored) { }
            finally { written.countDown(); }
        }, "pr-x-writer");
        w.setDaemon(true);
        w.start();
        int got = 0;
        boolean ordered = true;
        int v;
        while ((v = in.read()) != -1) {
            if (v != ('A' + (got % 26))) ordered = false;
            got++;
        }
        check(written.await(15, TimeUnit.SECONDS), "writer finished within bound");
        w.join(15000);
        check(!w.isAlive(), "writer joined");
        check(got == n, "all 2048 chars delivered: " + got);
        check(ordered, "order preserved across wraparounds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorAndConnectMatrix();
        testReadBoundsAndOrder();
        testReadyContract();
        testBrokenPipeDetection();
        testSkipContract();
        testCharRange();
        testCrossThreadLatchedRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

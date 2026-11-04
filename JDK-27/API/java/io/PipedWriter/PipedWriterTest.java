/*
 * @test
 * @summary java.io.PipedWriter conformance: white-box structure + black-box behavior
 *          (both constructors and connect(), round-trip with a single deterministic
 *          reader thread, unconnected write failure, reader-closed write failure,
 *          close delivers EOF, double connect refused, append/flush inherited API).
 * @run main PipedWriterTest
 */

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PipedWriterTest {
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
        Class<?> c = Class.forName("java.io.PipedWriter");
        check(c.getSuperclass().getName().equals("java.io.Writer"), "extends Writer");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(PipedReader.class) != null, "ctor (PipedReader)");
        check(c.getMethod("connect", PipedReader.class) != null, "connect method");
        check(c.getMethod("write", int.class) != null, "write(int)");
        check(c.getMethod("write", char[].class, int.class, int.class) != null,
              "write(char[], int, int)");
        check(c.getMethod("write", String.class, int.class, int.class) != null,
              "write(String, int, int)");
        check(c.getMethod("flush") != null, "flush");
        check(Modifier.isPrivate(c.getDeclaredField("sink").getModifiers())
                  && c.getDeclaredField("sink").getType() == PipedReader.class,
              "private sink");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. writing before connecting fails
        try {
            new PipedWriter().write('x');
            check(false, "write before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 2. flush before connecting is a silent no-op (sink == null)
        new PipedWriter().flush();
        passed++;

        // 3. deterministic round-trip: one daemon reader drains while the main
        //    thread writes; closing the writer ends the reader with EOF
        PipedWriter out = new PipedWriter();
        PipedReader in = new PipedReader(out);
        final java.util.List<Character> got = java.util.Collections.synchronizedList(
                new java.util.ArrayList<>());
        Thread reader = new Thread(null, () -> {
            try {
                int v;
                while ((v = in.read()) != -1) got.add((char) v);
            } catch (IOException ignored) { }
        }, "pw-reader", 0);
        reader.setDaemon(true);
        reader.start();
        for (char ch = '0'; ch <= '9'; ch++) out.write(ch);
        out.write("suffix", 0, 6);
        out.flush();
        out.close();
        reader.join(5000);
        check(!reader.isAlive(), "reader terminated on writer close");
        check(got.size() == 16, "every char received: " + got.size());
        check(got.size() == 16 && got.get(0) == '0' && got.get(9) == '9' && got.get(15) == 'x',
              "payload order preserved");

        // 4. write(String) sub-range copies exactly that slice
        PipedWriter out4 = new PipedWriter();
        PipedReader in4 = new PipedReader(out4);
        out4.write("abcdef", 2, 2);                       // "cd"
        out4.flush();
        char[] buf = new char[2];
        check(in4.read(buf) == 2 && buf[0] == 'c' && buf[1] == 'd', "slice [2,4) delivered");

        // 5. append(CharSequence) routes through write
        PipedWriter out5 = new PipedWriter(new PipedReader());
        out5.append("ok");                                 // buffered, no reader needed
        out5.flush();
        passed++;

        // 6. write after the reader closed fails
        PipedWriter out6 = new PipedWriter();
        PipedReader in6 = new PipedReader(out6);
        in6.close();
        try {
            out6.write('z');
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 7. double connect refused
        PipedWriter out7 = new PipedWriter(new PipedReader());
        try {
            out7.connect(new PipedReader());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 8. close is idempotent; writing afterwards still fails
        out7.close();
        out7.close();
        try {
            out7.write('q');
            check(false, "write after close must throw");
        } catch (IOException ioe) {
            passed++;
        }

        // 9. flush after the reader closed reports the closed pipe (JDK 27 raises
        //    "Pipe closed" from flush; an unconnected flush stays a silent no-op)
        try {
            out6.flush();
            check(false, "flush after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "flush message: " + ioe.getMessage());
        }
    }

    // ==================== 深化追加（契约级，批次 ioD） ====================

    // 构造/connect 矩阵（字符版特有 closed 检查，与 PipedOutputStream 差异，实测 C13-C15/C29/C30）：
    // connect(null) NPE；双向重复 → "Already connected"；unconnected close 后 connect → "Pipe closed"；
    // connect 到 closed unconnected reader → "Pipe closed"；ctor 同
    static void testCtorAndConnectMatrix() throws Exception {
        final PipedReader in = new PipedReader();
        final PipedWriter out = new PipedWriter(in);
        // connect(null) 是显式 NPE：双侧消息均为 null（实测 C08）
        expect(NullPointerException.class, null, "connect(null)",
                () -> out.connect(null));
        expect(IOException.class, "Already connected", "out.connect(other) after connected",
                () -> out.connect(new PipedReader()));
        expect(IOException.class, "Already connected", "in.connect(other) after connected",
                () -> in.connect(new PipedWriter()));
        expect(IOException.class, "Already connected", "third party connect(connected in)",
                () -> new PipedWriter().connect(in));

        // 未连接 close 置位 closed 标志 → 之后 connect → "Pipe closed"（字节版是成功）
        PipedWriter closedWriter = new PipedWriter();
        closedWriter.close();
        expect(IOException.class, "Pipe closed", "connect after own close (unconnected)",
                () -> closedWriter.connect(new PipedReader()));

        // 对端 reader 已 close（未连接）→ "Pipe closed"（构造器与 connect 皆抛）
        PipedReader closedReader = new PipedReader();
        closedReader.close();
        expect(IOException.class, "Pipe closed", "connect to closed unconnected reader",
                () -> new PipedWriter().connect(closedReader));
        expect(IOException.class, "Pipe closed", "ctor with closed unconnected reader",
                () -> new PipedWriter(closedReader));

        // was-connected close 后 connect(other) → "Already connected"（sink 先判，实测 C15）
        PipedWriter out2 = new PipedWriter(new PipedReader());
        out2.close();
        expect(IOException.class, "Already connected", "connect other after own close (was connected)",
                () -> out2.connect(new PipedReader()));
    }

    // write(char[],off,len)：sink 检查先于一切；bounds IOOBE；len==0 close 后静默（实测 C18/C22/C33）
    static void testWriteBoundsAndZero() throws Exception {
        final char[] c = new char[3];
        expect(IOException.class, "Pipe not connected", "write(c,0,0) unconnected (sink check first)",
                () -> new PipedWriter().write(c, 0, 0));
        PipedWriter out = new PipedWriter(new PipedReader());
        // JDK-27 NPE 带 helpful message（JDK-8 为 null 消息）
        expect(NullPointerException.class, "Cannot read the array length", "write(null cbuf,0,1)",
                () -> out.write((char[]) null, 0, 1));
        // JDK-27 IOOBE 带 Objects.checkFromIndexSize 消息（JDK-8 为 null 消息）
        expect(IndexOutOfBoundsException.class, "out of bounds", "write(c,-1,2)",
                () -> out.write(c, -1, 2));
        expect(IndexOutOfBoundsException.class, "out of bounds", "write(c,0,-1)",
                () -> out.write(c, 0, -1));
        expect(IndexOutOfBoundsException.class, "out of bounds", "write(c,2,2) len>remaining",
                () -> out.write(c, 2, 2));

        // len==0 经 receive(char[],off,0) 循环不执行 → 不触发状态检查 → close 后静默
        PipedReader in4 = new PipedReader();
        PipedWriter out4 = new PipedWriter(in4);
        out4.close();
        out4.write(c, 0, 0);
        passed++; // len==0 after own close is silent
        in4.close();
        out4.write(c, 0, 0);
        passed++; // len==0 after peer close is silent
    }

    // write(String)/append 契约：String 越界 StringIndexOutOfBoundsException（IOOBE 子类，
    // 消息双侧不同故只断言类型）、null String NPE、append(null) 写出 "null"、append(null,s,e) 子段
    static void testStringAndAppend() throws Exception {
        PipedWriter out = new PipedWriter(new PipedReader());
        expect(IndexOutOfBoundsException.class, null, "write(String,-1,1)",
                () -> out.write("abc", -1, 1));
        expect(IndexOutOfBoundsException.class, null, "write(String,2,2)",
                () -> out.write("abc", 2, 2));
        expect(IndexOutOfBoundsException.class, null, "write(String,1,-1)",
                () -> out.write("abc", 1, -1));
        // JDK-27 null String NPE 带 helpful message（JDK-8 为 null 消息）
        expect(NullPointerException.class, "\"str\" is null", "write((String)null)",
                () -> out.write((String) null));
        expect(NullPointerException.class, null, "write((String)null,0,1)",
                () -> out.write((String) null, 0, 1));

        PipedReader retIn = new PipedReader();
        PipedWriter retOut = new PipedWriter(retIn);
        check(retOut.append("ok") == retOut, "append(CharSequence) returns this Writer");
        check(retIn.read() == 'o' && retIn.read() == 'k', "append(CharSequence) routed to write");

        PipedReader in = new PipedReader();
        PipedWriter out2 = new PipedWriter(in);
        out2.append((CharSequence) null);
        out2.append((CharSequence) null, 1, 3);
        char[] buf = new char[8];
        int n = in.read(buf, 0, 8);
        check(n == 6, "append(null)+append(null,1,3) write 6 chars: " + n);
        check(buf[0] == 'n' && buf[1] == 'u' && buf[2] == 'l' && buf[3] == 'l'
                && buf[4] == 'u' && buf[5] == 'l',
                "null-CharSequence literalized then sliced (\"null\"+\"ul\")");
    }

    // flush/close 契约（与 PipedOutputStream.flush 从不抛的关键族差异，实测 C16/C17/C23）：
    // 自身 close 后 flush → "Pipe closed"；reader close 后 flush → "Pipe closed"；close 幂等
    static void testCloseFlushContract() throws Exception {
        PipedWriter out = new PipedWriter(new PipedReader());
        out.close();
        out.close();
        passed++; // close idempotent
        expect(IOException.class, "Pipe closed", "write after own close",
                () -> out.write('a'));
        expect(IOException.class, "Pipe closed", "flush after own close",
                () -> out.flush());

        PipedReader in = new PipedReader();
        PipedWriter out2 = new PipedWriter(in);
        in.close();
        expect(IOException.class, "Pipe closed", "write after reader close (exact message)",
                () -> out2.write('z'));
        expect(IOException.class, "Pipe closed", "flush after reader close",
                () -> out2.flush());
    }

    // 读端线程死亡（不 close）→ 写端 "Read end dead"（受控 daemon 线程 + latch，实测 C42）
    static void testReadEndDead() throws Exception {
        final PipedReader in = new PipedReader();
        final PipedWriter out = new PipedWriter(in);
        out.write('q');
        final CountDownLatch done = new CountDownLatch(1);
        Thread r = new Thread(() -> {
            try { in.read(); } catch (IOException ignored) { }
            finally { done.countDown(); }
        }, "pw-d-reader");
        r.setDaemon(true);
        r.start();
        check(done.await(15, TimeUnit.SECONDS), "reader consumed within bound");
        r.join(15000);
        check(!r.isAlive(), "reader thread died within bound");
        expect(IOException.class, "Read end dead", "write after reader thread death",
                () -> out.write('r'));
    }

    // 跨线程 latch 同步往返：1024 字符分块写 + close 交付 EOF + 读端校验
    static void testCrossThreadLatchedRoundTrip() throws Exception {
        final int n = 1024;
        final PipedReader in = new PipedReader();
        final PipedWriter out = new PipedWriter(in);
        final CountDownLatch done = new CountDownLatch(1);
        final int[] result = new int[2];
        Thread r = new Thread(() -> {
            int got = 0;
            boolean ordered = true;
            int v;
            try {
                while ((v = in.read()) != -1) {
                    if (v != ('A' + (got % 26))) ordered = false;
                    got++;
                }
            } catch (IOException ignored) { }
            result[0] = got;
            result[1] = ordered ? 1 : 0;
            done.countDown();
        }, "pw-x-reader");
        r.setDaemon(true);
        r.start();
        char[] chunk = new char[256];
        for (int base = 0; base < n; base += 256) {
            for (int i = 0; i < 256; i++) chunk[i] = (char) ('A' + ((base + i) % 26));
            out.write(chunk, 0, 256);
        }
        out.flush();
        out.close();
        check(done.await(15, TimeUnit.SECONDS), "reader finished within bound");
        r.join(15000);
        check(!r.isAlive(), "reader joined");
        check(result[0] == n, "all 1024 chars received: " + result[0]);
        check(result[1] == 1, "order preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorAndConnectMatrix();
        testWriteBoundsAndZero();
        testStringAndAppend();
        testCloseFlushContract();
        testReadEndDead();
        testCrossThreadLatchedRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

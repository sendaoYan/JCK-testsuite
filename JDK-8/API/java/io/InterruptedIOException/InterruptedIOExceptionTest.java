/*
 * @test
 * @summary java.io.InterruptedIOException conformance (pure black-box):
 *          constructors and the public bytesTransferred field; a real trigger
 *          interrupting a thread blocked in PipedInputStream.read; interrupt status
 *          of the reading thread after the throw.
 * @run main InterruptedIOExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class InterruptedIOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructors and field default
        InterruptedIOException e0 = new InterruptedIOException();
        check(e0.getMessage() == null && e0.bytesTransferred == 0,
              "no-arg: null message, bytesTransferred 0");
        InterruptedIOException e1 = new InterruptedIOException("read timed out");
        check("read timed out".equals(e1.getMessage()) && e1.bytesTransferred == 0,
              "(String): message set, bytesTransferred still 0");

        // 2. bytesTransferred is a writable data field (documents partial transfer)
        e1.bytesTransferred = 512;
        check(e1.bytesTransferred == 512, "bytesTransferred assignable");
        e1.bytesTransferred = 0;

        // 3. real trigger: interrupt a thread blocked reading an empty pipe
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        final InterruptedIOException[] hit = new InterruptedIOException[1];
        final int[] partial = new int[]{-1};
        Thread reader = new Thread(null, () -> {
            try {
                in.read();
            } catch (InterruptedIOException iio) {
                hit[0] = iio;
                partial[0] = iio.bytesTransferred;
            } catch (IOException io) {
                // unexpected
            }
        }, "iio-reader", 0);
        reader.setDaemon(true);
        reader.start();
        for (int i = 0; i < 400; i++) {
            Thread.State st = reader.getState();
            if (st == Thread.State.WAITING || st == Thread.State.TIMED_WAITING) break;
            Thread.sleep(5);
        }
        check(reader.getState() == Thread.State.WAITING
              || reader.getState() == Thread.State.TIMED_WAITING,
              "reader parked on the empty pipe: " + reader.getState());
        reader.interrupt();
        reader.join(5000);
        check(hit[0] != null, "blocked read threw InterruptedIOException");
        check(partial[0] == 0, "bytesTransferred 0 (no byte had arrived)");
        check(!reader.isInterrupted(),
              "interrupt is consumed by the throw (PipedInputStream does not re-set it)");
        out.close();
        in.close();

        // 4. contrast: interrupting is not required for ordinary reads
        check(new DataInputStream(new ByteArrayInputStream(new byte[]{5})).read() == 5,
              "normal read unaffected");

        // 5. caught as IOException by API callers
        try {
            throw new InterruptedIOException("x");
        } catch (IOException io) {
            check(io instanceof InterruptedIOException, "catchable as IOException");
        }
    }

    // ================= 深化追加（契约级） =================

    static byte[] serOf(Object o) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(b)) {
            oos.writeObject(o);
        }
        return b.toByteArray();
    }

    static Object back(byte[] b) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return ois.readObject();
        }
    }

    // ---- Throwable protocol + bytesTransferred public-field semantics ----
    static void testThrowableProtocolAndField() {
        InterruptedIOException e = new InterruptedIOException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.InterruptedIOException: m"),
              "toString = FQCN + \": \" + message");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        // bytesTransferred: public mutable data field defaulting to 0
        check(new InterruptedIOException().bytesTransferred == 0, "default 0 (no-arg)");
        check(new InterruptedIOException("m").bytesTransferred == 0, "default 0 ((String))");
        e.bytesTransferred = 65535;
        check(e.bytesTransferred == 65535, "assignable to max int value");
        e.bytesTransferred = -1;
        check(e.bytesTransferred == -1, "negative values are not rejected (plain field)");
        e.bytesTransferred = 0;
        check(e.toString().equals("java.io.InterruptedIOException: m"),
              "toString does not expose bytesTransferred");
        StackTraceElement[] st = {new StackTraceElement("I", "read", "I.java", 9)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1 && "read".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        Exception cause = new IllegalStateException("c");
        check(new InterruptedIOException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new InterruptedIOException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new InterruptedIOException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        InterruptedIOException sup = new InterruptedIOException("s");
        sup.addSuppressed(new Exception("x"));
        check(sup.getSuppressed().length == 1, "suppressed kept");
        try {
            sup.addSuppressed(sup);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            sup.addSuppressed(null);
            check(false, "addSuppressed(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        check(InterruptedIOException.class.getSuperclass() == IOException.class,
              "direct child of IOException");
    }

    // ---- serialization: bytesTransferred is a serialized public field ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(InterruptedIOException.class).getSerialVersionUID()
                  == 4020568460727500567L, "serialVersionUID == 4020568460727500567");
        InterruptedIOException sent = new InterruptedIOException("mid");
        sent.bytesTransferred = 512;
        InterruptedIOException r = (InterruptedIOException) back(serOf(sent));
        check("mid".equals(r.getMessage()), "message survives round-trip");
        check(r.bytesTransferred == 512, "bytesTransferred survives round-trip (non-transient field)");
        check(r.toString().equals("java.io.InterruptedIOException: mid"),
              "toString survives round-trip");
        check(((InterruptedIOException) back(serOf(new InterruptedIOException()))).bytesTransferred == 0,
              "default 0 survives round-trip");
        InterruptedIOException chained = new InterruptedIOException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((InterruptedIOException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
    }

    // ---- deterministic single-threaded trigger: pre-set interrupt flag ----
    static void testDeterministicTrigger() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        // 数据可用时：读取成功，不触发（中断标志不影响非阻塞读）
        out.write(new byte[]{1, 2, 3});
        Thread.currentThread().interrupt();
        try {
            int n = in.read(new byte[8], 0, 8);
            check(n == 3, "available data still readable under an interrupt flag: " + n);
        } finally {
            Thread.interrupted();                       // clear for the next section
        }
        // 无数据且已置中断标志：阻塞读立即以 InterruptedIOException 失败（实测双侧一致）
        Thread.currentThread().interrupt();
        try {
            in.read();
            check(false, "interrupted blocked read must throw");
        } catch (InterruptedIOException iio) {
            check(iio.bytesTransferred == 0, "bytesTransferred 0 (nothing arrived)");
            check(iio.getMessage() == null, "pipe-triggered instance has a null message");
            check(!Thread.currentThread().isInterrupted(),
                  "interrupt is consumed by the throw");
        }
        check(!Thread.currentThread().isInterrupted(), "flag cleared after the throw");
        // 相同触发路径对字节批量读也成立
        Thread.currentThread().interrupt();
        try {
            in.read(new byte[4], 0, 4);
            check(false, "interrupted bulk read must throw");
        } catch (InterruptedIOException iio) {
            check(iio.bytesTransferred == 0, "bulk trigger: bytesTransferred 0");
            passed++;
        }
        check(!Thread.currentThread().isInterrupted(), "flag cleared again");
        out.close();
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testThrowableProtocolAndField();
        testSerializationContract();
        testDeterministicTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

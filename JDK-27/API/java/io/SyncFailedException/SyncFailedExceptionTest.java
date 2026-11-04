/*
 * @test
 * @summary java.io.SyncFailedException conformance: white-box structure + black-box
 *          behavior (the single String constructor; real trigger: FileDescriptor.sync()
 *          on an unbound descriptor; contrast: sync succeeding on a real file).
 * @run main SyncFailedExceptionTest
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;

public class SyncFailedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.SyncFailedException");
        check(c.getSuperclass().getName().equals("java.io.IOException"), "extends IOException");
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) ctors++;
        check(ctors == 1 && c.getConstructor(String.class) != null,
              "single public (String) constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructor keeps the message (the reason sync failed)
        SyncFailedException sfe = new SyncFailedException("sync failed");
        check("sync failed".equals(sfe.getMessage()), "(String) message kept");
        check(sfe.getCause() == null, "no cause");

        // 2. real trigger: syncing a descriptor that is not backed by a real fd
        try {
            new FileDescriptor().sync();
            check(false, "sync on an unbound descriptor must throw");
        } catch (SyncFailedException expected) {
            check(expected.getMessage() != null,
                  "sync failure reports a reason: " + expected.getMessage());
        }

        // 3. contrast: sync succeeds on a descriptor belonging to a real open file
        File tmp = File.createTempFile("sfe", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(42);
            FileDescriptor fd = raf.getFD();
            check(fd.valid(), "descriptor valid while open");
            fd.sync();
            passed++;                                  // returned normally
        } finally {
            tmp.delete();
        }

        // 4. caught as IOException by callers that only know about IOException
        try {
            throw new SyncFailedException("m");
        } catch (IOException io) {
            check(io instanceof SyncFailedException, "catchable as IOException");
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

    // ---- Throwable protocol matrix ----
    static void testThrowableProtocol() {
        SyncFailedException e = new SyncFailedException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.SyncFailedException: m"),
              "toString = FQCN + \": \" + message");
        check(new SyncFailedException("x").toString().endsWith(": x"),
              "single-arg ctor always renders a message segment");
        check(new SyncFailedException((String) null).getMessage() == null,
              "null message accepted, getMessage stays null");
        check(new SyncFailedException("").getMessage().isEmpty(),
              "empty message distinct from null");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("F", "sync", "F.java", 9)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "sync".equals(e.getStackTrace()[0].getMethodName()),
              "setStackTrace/getStackTrace round-trip");
        e.setStackTrace(new StackTraceElement[0]);
        check(e.getStackTrace().length == 0, "setStackTrace(empty) yields empty view");
        try {
            e.setStackTrace(null);
            check(false, "setStackTrace(null) must throw NPE");
        } catch (NullPointerException expected) {
            passed++;
        }
        Exception cause = new IllegalStateException("c");
        check(new SyncFailedException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new SyncFailedException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new SyncFailedException("n").initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        SyncFailedException sup = new SyncFailedException("s");
        sup.addSuppressed(new Exception("x"));
        sup.addSuppressed(new Exception("y"));
        check(sup.getSuppressed().length == 2, "suppressed kept in order");
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
        check(SyncFailedException.class.getSuperclass() == IOException.class,
              "direct child of IOException (no ObjectStreamException in the chain)");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(SyncFailedException.class).getSerialVersionUID()
                  == -2353342684412443330L, "serialVersionUID == -2353342684412443330");
        SyncFailedException r = (SyncFailedException) back(serOf(new SyncFailedException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.SyncFailedException: kept"),
              "toString survives round-trip");
        check(((SyncFailedException) back(serOf(new SyncFailedException((String) null)))).getMessage() == null,
              "null-message instance round-trips with null message");
        SyncFailedException chained = new SyncFailedException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((SyncFailedException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
        SyncFailedException sup = new SyncFailedException("s");
        sup.addSuppressed(new IllegalStateException("z"));
        check(((SyncFailedException) back(serOf(sup))).getSuppressed().length == 1,
              "suppressed survive round-trip");
    }

    // ---- exact trigger diagnostics + sync family matrix (实测双侧一致) ----
    static void testTriggerPrecision() throws Exception {
        // unbound descriptor: exact fixed message, no errno decoration
        try {
            new FileDescriptor().sync();
            check(false, "sync on an unbound descriptor must throw");
        } catch (SyncFailedException sfe) {
            check("sync failed".equals(sfe.getMessage()),
                  "exact unbound message: " + sfe.getMessage());
            check(sfe.toString().equals("java.io.SyncFailedException: sync failed"),
                  "toString composes the fixed reason");
            check(sfe.getCause() == null, "triggered instance carries no cause");
        }
        // in/out descriptors answer to sync() on this platform
        FileDescriptor in = FileDescriptor.in;
        try {
            in.sync();
            passed++;
        } catch (SyncFailedException sfe) {
            check(sfe.getMessage() != null, "stdin sync failure still names a reason");
        }
        // read-only and write-only file descriptors both sync successfully here
        File tmp = File.createTempFile("sfeDeepen", ".dat");
        try {
            try (RandomAccessFile raf = new RandomAccessFile(tmp, "r")) {
                raf.getFD().sync();
                passed++;                       // read-only fd sync is a no-op success
            } catch (SyncFailedException sfe) {
                check(sfe.getMessage() != null, "read-only sync failure names a reason");
            }
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp, true)) {
                fos.write(7);
                fos.getFD().sync();
                passed++;                       // appended write + sync succeeds
            }
            // the stream exception is distinct from the family root: not an ObjectStreamException
            check(!java.io.ObjectStreamException.class.isInstance(new SyncFailedException("m")),
                  "sync failure is a plain IOException, unrelated to object streams");
        } finally {
            tmp.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testTriggerPrecision();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

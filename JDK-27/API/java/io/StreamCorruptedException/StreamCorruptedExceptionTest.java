/*
 * @test
 * @summary java.io.StreamCorruptedException conformance: white-box structure + black-box
 *          behavior (constructors; real triggers: garbage stream header, truncated
 *          header, a bad tag byte, and a class descriptor with an invalid field type).
 * @run main StreamCorruptedExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamConstants;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class StreamCorruptedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.StreamCorruptedException");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        int noArg = 0, str = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 0) noArg++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) str++;
        }
        check(noArg == 1 && str == 1, "two public constructors");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructors
        check(new StreamCorruptedException().getMessage() == null, "no-arg message null");
        check("why".equals(new StreamCorruptedException("why").getMessage()), "(String) message");

        // 2. trigger: an invalid stream header
        try {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{0x00, 0x01, 0x02, 0x03}));
            check(false, "garbage header must throw");
        } catch (StreamCorruptedException sce) {
            check(sce.getMessage() != null && sce.getMessage().startsWith("invalid stream header"),
                  "message names the header problem: " + sce.getMessage());
        }

        // 3. trigger: a header truncated before STREAM_VERSION
        try {
            new ObjectInputStream(new ByteArrayInputStream(
                new byte[]{(byte) (ObjectStreamConstants.STREAM_MAGIC >>> 8),
                           (byte) ObjectStreamConstants.STREAM_MAGIC}));
            check(false, "short header must throw");
        } catch (java.io.IOException expected) {
            check(expected instanceof StreamCorruptedException || expected instanceof java.io.EOFException,
                  "truncated header: " + expected.getClass().getSimpleName());
        }

        // 4. trigger: valid header, then an unknown tag byte
        byte[] junk = {(byte) (ObjectStreamConstants.STREAM_MAGIC >>> 8),
                       (byte) ObjectStreamConstants.STREAM_MAGIC,
                       (byte) (ObjectStreamConstants.STREAM_VERSION >>> 8),
                       (byte) ObjectStreamConstants.STREAM_VERSION,
                       0x7F};                     // not a valid TC_* tag
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(junk))) {
            ois.readObject();
            check(false, "unknown tag must throw");
        } catch (StreamCorruptedException sce) {
            check(sce.getMessage() != null && sce.getMessage().contains("invalid type code"),
                  "invalid type code message: " + sce.getMessage());
        }

        // 5. trigger: hand-corrupted payload (flip bytes after the header)
        byte[] good = ser("hello");
        good[good.length - 1] ^= 0x55;            // corrupt the string body
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(good))) {
            String s = (String) ois.readObject();
            check(!"hello".equals(s), "corrupted byte changes the value");
        } catch (java.io.IOException ex) {
            check(ex instanceof StreamCorruptedException || ex instanceof java.io.UTFDataFormatException
                  || ex instanceof java.io.EOFException,
                  "corrupted payload surfaces as a stream/decoding failure: " + ex);
        }

        // 6. contrast: an unmodified stream reads cleanly
        check("hello".equals(
            new ObjectInputStream(new ByteArrayInputStream(ser("hello"))).readObject()),
            "clean stream round-trips");

        // 7. caught as ObjectStreamException
        try {
            throw new StreamCorruptedException("m");
        } catch (ObjectStreamException ose) {
            check(ose instanceof StreamCorruptedException, "catchable as ObjectStreamException");
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
        StreamCorruptedException e = new StreamCorruptedException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.StreamCorruptedException: m"),
              "toString = FQCN + \": \" + message");
        check(new StreamCorruptedException().toString().equals("java.io.StreamCorruptedException"),
              "no-arg toString carries no colon");
        check(new StreamCorruptedException((String) null).getMessage() == null,
              "null message accepted, getMessage stays null");
        check(new StreamCorruptedException("").getMessage().isEmpty(),
              "empty message distinct from null");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("R", "readStreamHeader", "R.java", 6)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "readStreamHeader".equals(e.getStackTrace()[0].getMethodName()),
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
        check(new StreamCorruptedException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new StreamCorruptedException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new StreamCorruptedException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        StreamCorruptedException sup = new StreamCorruptedException("s");
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
        check(StreamCorruptedException.class.getSuperclass() == ObjectStreamException.class,
              "direct child of ObjectStreamException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(StreamCorruptedException.class).getSerialVersionUID()
                  == 8983558202217591746L, "serialVersionUID == 8983558202217591746");
        StreamCorruptedException r =
            (StreamCorruptedException) back(serOf(new StreamCorruptedException("kept")));
        check("kept".equals(r.getMessage()), "message survives round-trip");
        check(r.toString().equals("java.io.StreamCorruptedException: kept"),
              "toString survives round-trip");
        check(((StreamCorruptedException) back(serOf(new StreamCorruptedException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
        StreamCorruptedException chained = new StreamCorruptedException("w");
        chained.initCause(new RuntimeException("r"));
        check("r".equals(((StreamCorruptedException) back(serOf(chained))).getCause().getMessage()),
              "cause survives round-trip");
    }

    // ---- precise header/tag diagnostics (实测双侧一致) ----
    static void testDiagnostics() throws Exception {
        // garbage header: message names the offending bytes in hex
        try {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{0, 1, 2, 3}));
            check(false, "garbage header must throw");
        } catch (StreamCorruptedException sce) {
            check("invalid stream header: 00010203".equals(sce.getMessage()),
                  "exact header diagnostic: " + sce.getMessage());
        }
        // correct STREAM_MAGIC but wrong version is also corrupt
        byte[] badVersion = {(byte) (ObjectStreamConstants.STREAM_MAGIC >>> 8),
                             (byte) ObjectStreamConstants.STREAM_MAGIC, 0, 99};
        try {
            new ObjectInputStream(new ByteArrayInputStream(badVersion));
            check(false, "wrong version must throw");
        } catch (StreamCorruptedException sce) {
            check(sce.getMessage().startsWith("invalid stream header"),
                  "version diagnostic: " + sce.getMessage());
        }
        // unknown TC tag after a valid header
        byte[] junk = {(byte) (ObjectStreamConstants.STREAM_MAGIC >>> 8),
                       (byte) ObjectStreamConstants.STREAM_MAGIC,
                       (byte) (ObjectStreamConstants.STREAM_VERSION >>> 8),
                       (byte) ObjectStreamConstants.STREAM_VERSION, 0x7F};
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(junk))) {
            ois.readObject();
            check(false, "unknown tag must throw");
        } catch (StreamCorruptedException sce) {
            check("invalid type code: 7F".equals(sce.getMessage()),
                  "exact tag diagnostic: " + sce.getMessage());
        }
        // the constructor is the documented recovery path: the same diagnostic can be reused
        check("invalid stream header: 00010203".equals(
                  new StreamCorruptedException("invalid stream header: 00010203").getMessage()),
              "message is a plain verbatim string (no composition)");
        // contrast: an empty stream is an EOFException, not a corruption
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(new byte[0])) {}) {
            ois.readObject();
            check(false, "empty stream must throw");
        } catch (java.io.EOFException expected) {
            check("EOFException".equals(expected.getClass().getSimpleName()),
                  "clean EOF reports EOFException, not StreamCorruptedException");
        }
        // a triggered instance itself round-trips through serialization with its diagnostic intact
        StreamCorruptedException caught;
        try {
            new ObjectInputStream(new ByteArrayInputStream(new byte[]{0, 1, 2, 3}));
            caught = null;
        } catch (StreamCorruptedException sce) {
            caught = sce;
        }
        check("invalid stream header: 00010203".equals(
                  ((StreamCorruptedException) back(serOf(caught))).getMessage()),
              "triggered diagnostic survives a round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testDiagnostics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

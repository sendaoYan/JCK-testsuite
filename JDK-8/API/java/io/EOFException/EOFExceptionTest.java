/*
 * @test
 * @summary java.io.EOFException conformance (pure black-box): constructors; real
 *          triggers from DataInputStream primitive reads, RandomAccessFile reads
 *          past EOF, ObjectInputStream.readUTF past the end.
 * @run main EOFExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.RandomAccessFile;

public class EOFExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface Throwing { void run() throws java.io.IOException; }

    static void testBlackBox() throws Exception {
        // 1. constructors
        EOFException e0 = new EOFException();
        check(e0.getMessage() == null && e0.getCause() == null, "no-arg: nulls");
        check("unexpected end".equals(new EOFException("unexpected end").getMessage()),
              "(String) message");

        // 2. trigger: multi-byte primitive read hits EOF mid-value
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(new byte[]{1, 2}));
        try {
            d.readInt();
            check(false, "readInt with 2 bytes must throw EOFException");
        } catch (EOFException expected) {
            passed++;
        }

        // 3. trigger: every wide primitive type on a fully empty stream
        byte[] empty = new byte[0];
        for (Throwing r : new Throwing[]{
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readLong(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readDouble(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readChar(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readUTF(),
                () -> new DataInputStream(new ByteArrayInputStream(empty)).readFully(new byte[1])}) {
            try {
                r.run();
                check(false, "expected EOFException");
            } catch (java.io.IOException ex) {
                check(ex instanceof EOFException, "EOFException from " + ex.getClass().getSimpleName());
            }
        }

        // 3b. contrast: readLine and skipBytes signal EOF without an exception
        check(new DataInputStream(new ByteArrayInputStream(empty)).readLine() == null,
              "readLine returns null at EOF");
        check(new DataInputStream(new ByteArrayInputStream(empty)).skipBytes(4) == 0,
              "skipBytes returns 0 at EOF");

        // 4. trigger: readFully consumes what it can, then reports EOF
        byte[] buf = new byte[4];
        java.util.Arrays.fill(buf, (byte) 7);
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[]{9})).readFully(buf);
            check(false, "readFully short stream must throw");
        } catch (EOFException expected) {
            check(buf[0] == 9 && buf[3] == 7,
                  "readFully filled available bytes, left the rest untouched");
        }

        // 5. trigger: RandomAccessFile.readByte/readInt past end of file
        File tmp = File.createTempFile("eofx", ".dat");
        try {
            try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
                raf.writeShort(0x1234);
                raf.seek(0);                           // pointer sits at EOF after the write
                check(raf.readShort() == 0x1234, "short reads back");
                try {
                    raf.readInt();                     // needs 4 bytes, only 0 left
                    check(false, "readInt at EOF must throw");
                } catch (EOFException expected) {
                    passed++;
                }
                try {
                    raf.readByte();
                    check(false, "readByte at EOF must throw");
                } catch (EOFException expected) {
                    passed++;
                }
                check(raf.readLine() == null, "RandomAccessFile.readLine null at EOF");
                raf.seek(0);
                try {
                    raf.readInt();                     // only 2 bytes available
                    check(false, "partially available readInt must throw");
                } catch (EOFException expected) {
                    passed++;
                }
            }
        } finally {
            tmp.delete();
        }

        // 6. trigger: ObjectInputStream.readUTF after the payload object is exhausted
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeUTF("only");
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("only".equals(ois.readUTF()), "payload first");
            try {
                ois.readInt();
                check(false, "readInt past stream end must throw");
            } catch (EOFException expected) {
                passed++;
            }
        }

        // 7. negative contrast: -1 is returned by InputStream.read at EOF, not an exception
        java.io.File tmp2 = File.createTempFile("eofx2", ".dat");
        try (FileInputStream fin = new FileInputStream(tmp2)) {
            check(fin.read() == -1, "raw InputStream.read returns -1 at EOF");
            check(fin.read(new byte[1]) == -1, "raw bulk read returns -1 at EOF");
        } finally {
            tmp2.delete();
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
        EOFException e = new EOFException("m");
        check("m".equals(e.getLocalizedMessage()), "getLocalizedMessage defaults to getMessage");
        check(e.toString().equals("java.io.EOFException: m"), "toString = FQCN + \": \" + message");
        check(new EOFException().toString().equals("java.io.EOFException"),
              "no-arg toString carries no colon");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace().length > 0, "stack trace captured");
        StackTraceElement[] st = {new StackTraceElement("E", "readInt", "E.java", 5)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1
                  && "readInt".equals(e.getStackTrace()[0].getMethodName()),
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
        check(new EOFException("w").initCause(cause).getCause() == cause,
              "initCause injects a cause on a class with no cause ctor");
        try {
            new EOFException("w").initCause(cause).initCause(cause);
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        check(new EOFException().initCause(null).getCause() == null,
              "initCause(null) permitted while the cause is unset");
        EOFException sup = new EOFException("s");
        sup.addSuppressed(new Exception("x"));
        sup.addSuppressed(new Exception("y"));
        check(sup.getSuppressed().length == 2, "suppressed kept in insertion order");
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
        check(EOFException.class.getSuperclass() == java.io.IOException.class,
              "direct child of IOException");
    }

    // ---- serialization contract ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(EOFException.class).getSerialVersionUID()
                  == 6433858223774886977L, "serialVersionUID == 6433858223774886977");
        EOFException r = (EOFException) back(serOf(new EOFException("kept")));
        check("kept".equals(r.getMessage()) && r.toString().equals("java.io.EOFException: kept"),
              "message and toString survive round-trip");
        EOFException chained = new EOFException("wrap");
        chained.initCause(new java.io.FileNotFoundException("missing"));
        EOFException rc = (EOFException) back(serOf(chained));
        check(rc.getCause() instanceof java.io.FileNotFoundException,
              "cause type survives round-trip");
        check(rc.getCause().getMessage().equals("missing"), "cause message survives round-trip");
        check(back(serOf(new EOFException())) instanceof EOFException
                  && ((EOFException) back(serOf(new EOFException()))).getMessage() == null,
              "no-arg instance round-trips with null message");
    }

    // ---- triggered instances: precise type/length semantics across the DataInput surface ----
    static void testTriggeredSemantics() throws Exception {
        // every triggered EOFException from DataInputStream carries a null message
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[0])).readInt();
        } catch (EOFException e) {
            check(e.getMessage() == null, "triggered readInt EOFException has a null message");
        }
        // single-byte primitives on an empty stream
        for (Throwing r : new Throwing[]{
                () -> new DataInputStream(new ByteArrayInputStream(new byte[0])).readByte(),
                () -> new DataInputStream(new ByteArrayInputStream(new byte[0])).readBoolean(),
                () -> new DataInputStream(new ByteArrayInputStream(new byte[0])).readUnsignedByte(),
                () -> new DataInputStream(new ByteArrayInputStream(new byte[0])).readUnsignedShort(),
                () -> new DataInputStream(new ByteArrayInputStream(new byte[0])).readFloat(),
                () -> new DataInputStream(new ByteArrayInputStream(new byte[1])).readShort(),
                () -> new DataInputStream(new ByteArrayInputStream(new byte[3])).readLong()}) {
            try {
                r.run();
                check(false, "expected EOFException");
            } catch (java.io.IOException ex) {
                check(ex instanceof EOFException,
                      "EOFException from " + ex.getClass().getSimpleName());
            }
        }
        // readFully(b, off, len) reports EOF when len exceeds the remainder
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[]{1, 2})).readFully(new byte[4], 0, 4);
            check(false, "short readFully must throw");
        } catch (EOFException expected) {
            passed++;
        }
        // zero-length readFully at EOF succeeds (asks for nothing)
        new DataInputStream(new ByteArrayInputStream(new byte[0])).readFully(new byte[0]);
        passed++;
        // readUTF with a truncated 2-byte length prefix
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[]{0})).readUTF();
            check(false, "truncated UTF length must throw");
        } catch (EOFException expected) {
            passed++;
        }
        // contrast: Reader-based EOF signalling is a -1 return, never EOFException
        java.io.StringReader sr = new java.io.StringReader("");
        check(sr.read() == -1, "Reader.read signals EOF with -1");
        sr.close();
        // the triggered instance is catchable through the IOException family root
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[0])).readInt();
        } catch (java.io.IOException io) {
            check(io instanceof EOFException, "family-root catch sees the exact subtype");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testThrowableProtocol();
        testSerializationContract();
        testTriggeredSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

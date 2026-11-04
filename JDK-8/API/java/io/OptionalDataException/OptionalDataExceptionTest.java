/*
 * @test
 * @summary java.io.OptionalDataException conformance (pure black-box): real
 *          triggers — readObject over primitive block data reports length and
 *          eof=false; the length field counts unread bytes; a class without
 *          readObject discards trailing custom data.
 * @run main OptionalDataExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.Serializable;

public class OptionalDataExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. trigger: readObject where the stream holds only primitive data
        //    (writeObject wrote an int; the reader expects an object)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeInt(42);
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            check(false, "readObject over primitive data must throw");
        } catch (OptionalDataException ode) {
            check(!ode.eof, "eof=false: primitive data is present");
            check(ode.length == 4, "length counts the unread primitive bytes (int = 4): " + ode.length);
        }

        // 2. length scales with the amount of buffered primitive data
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeLong(1L);
            oos.writeInt(2);
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
        } catch (OptionalDataException ode) {
            check(ode.length == 12, "length = 8 (long) + 4 (int): " + ode.length);
        }

        // 3. the data named by the exception is still readable via primitive reads
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeInt(42);
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            try {
                ois.readObject();
            } catch (OptionalDataException expected) {
                check(ois.readInt() == 42, "primitive recoverable after OptionalDataException");
            }
        }

        // 4. contrast: consuming the primitive data first lets the object read proceed
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeInt(42);
            oos.writeObject("after");
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check(ois.readInt() == 42, "read the int first");
            check("after".equals(ois.readObject()), "then the object reads fine");
        }

        // 5. a custom writeObject appending data for a class with no readObject:
        //    the deserializer skips the optional data and still delivers the object
        class WriterOnly implements Serializable {
            private static final long serialVersionUID = 741L;
            int v = 7;
            private void writeObject(ObjectOutputStream out) throws java.io.IOException {
                out.defaultWriteObject();
                out.writeInt(999);
            }
        }
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new WriterOnly());
            oos.writeObject("next");
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            WriterOnly back = (WriterOnly) ois.readObject();
            check(back.v == 7, "object restored, optional data skipped");
            check("next".equals(ois.readObject()), "stream positioned after the object");
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

    static OptionalDataException trigger(int... writes) throws Exception {
        java.io.DataOutputStream unused;                 // keep imports honest
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            for (int w : writes) oos.writeInt(w);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            return null;
        } catch (OptionalDataException ode) {
            return ode;
        }
    }

    // ---- reflective surface: no public ctor, public fields, JDK-8 class NOT final ----
    static void testReflectiveSurface() throws Exception {
        check(OptionalDataException.class.getConstructors().length == 0,
              "no public constructors: only the stream can create instances");
        check(OptionalDataException.class.getField("eof").getType() == boolean.class,
              "public boolean eof");
        check(OptionalDataException.class.getField("length").getType() == int.class,
              "public int length");
        check(!java.lang.reflect.Modifier.isFinal(OptionalDataException.class.getModifiers()),
              "JDK-8 class is not final");
        check(OptionalDataException.class.getSuperclass() == ObjectStreamException.class,
              "direct child of ObjectStreamException");
    }

    // ---- public field semantics + Throwable protocol on a caught instance ----
    static void testFieldAndThrowableProtocol() throws Exception {
        OptionalDataException ode = trigger(42);
        check(ode != null, "trigger produced an instance");
        check(ode.getMessage() == null, "triggered instance carries a null message");
        check(ode.toString().equals("java.io.OptionalDataException"),
              "toString has no message segment");
        check(ode.getStackTrace().length > 0, "stack trace captured");
        check(ode.fillInStackTrace() == ode, "fillInStackTrace returns this");
        // eof/length are public mutable data fields
        ode.eof = true;
        ode.length = 77;
        check(ode.eof && ode.length == 77, "eof/length assignable in place");
        ode.initCause(new RuntimeException("c"));
        check("c".equals(ode.getCause().getMessage()),
              "initCause injects a cause on the caught instance");
        check(ode.getSuppressed().length == 0, "suppressed empty by default");
        ode.addSuppressed(new Exception("s"));
        check(ode.getSuppressed().length == 1, "addSuppressed appends");
        try {
            ode.addSuppressed(ode);
            check(false, "self-suppression must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- serialization: the caught exception itself round-trips keeping eof/length ----
    static void testSerializationContract() throws Exception {
        check(ObjectStreamClass.lookup(OptionalDataException.class).getSerialVersionUID()
                  == -8011121865681257820L, "serialVersionUID == -8011121865681257820");
        OptionalDataException ode = trigger(7, 8, 9);
        OptionalDataException r = (OptionalDataException) back(serOf(ode));
        check(r.eof == ode.eof, "eof survives round-trip");
        check(r.length == ode.length, "length survives round-trip: " + r.length);
        check(r.toString().equals("java.io.OptionalDataException"), "toString after round-trip");
    }

    // ---- trigger matrix: length arithmetic and repeatability (实测双侧一致) ----
    static void testTriggerMatrix() throws Exception {
        // one int = 4 bytes
        OptionalDataException ode = trigger(42);
        check(!ode.eof && ode.length == 4, "single int: length 4, eof false");
        // the exception is repeatable: the primitive data is not consumed by readObject
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeInt(7);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            for (int i = 0; i < 3; i++) {
                try {
                    ois.readObject();
                    check(false, "readObject over primitive data must keep throwing");
                } catch (OptionalDataException again) {
                    check(again.length == 4, "attempt " + i + ": length still 4");
                }
            }
        }
        // boolean + long = 1 + 8 bytes
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeBoolean(true);
            oos.writeLong(5L);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            try {
                ois.readObject();
            } catch (OptionalDataException mixed) {
                check(mixed.length == 9, "boolean+long: length 9: " + mixed.length);
            }
        }
        // a UTF string: 2 length bytes + the body
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeUTF("hello world");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            try {
                ois.readObject();
            } catch (OptionalDataException utf) {
                check(utf.length == 13, "writeUTF body: length 13 (2 + 11): " + utf.length);
            }
        }
        // contrast: a fully consumed stream reports EOFException, not OptionalDataException
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("x");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("x".equals(ois.readObject()), "object first");
            try {
                ois.readObject();
                check(false, "readObject at clean EOF must throw");
            } catch (OptionalDataException wrong) {
                check(false, "clean EOF must not report optional data");
            } catch (java.io.EOFException expected) {
                passed++;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        testReflectiveSurface();
        testFieldAndThrowableProtocol();
        testSerializationContract();
        testTriggerMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

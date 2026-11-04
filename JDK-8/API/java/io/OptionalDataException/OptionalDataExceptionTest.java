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

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

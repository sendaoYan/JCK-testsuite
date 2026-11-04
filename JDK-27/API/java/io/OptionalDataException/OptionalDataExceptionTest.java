/*
 * @test
 * @summary java.io.OptionalDataException conformance: white-box structure (final class,
 *          package-private constructors, public length/eof fields) + black-box behavior
 *          (real triggers: readObject over primitive block data reports length and
 *          eof=false; a class without readObject discarding trailing custom data; the
 *          length field counting unread bytes).
 * @run main OptionalDataExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class OptionalDataExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class NoReader implements Serializable {
        @Serial
        private static final long serialVersionUID = 740L;
        int v = 1;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.OptionalDataException");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        check(c.getField("length").getType() == int.class, "public int length");
        check(c.getField("eof").getType() == boolean.class, "public boolean eof");
        for (Constructor<?> k : c.getDeclaredConstructors())
            check(!Modifier.isPublic(k.getModifiers()) && !Modifier.isProtected(k.getModifiers()),
                  "package-private constructor (JDK-internal only): " + k);
    }

    // ---- Part 2: black-box (behavioral) ----
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
            @Serial
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

        // 6. JDK-internal only: the package-private (int) constructor is not accessible
        //    to application code (java.base does not open java.io)
        Constructor<OptionalDataException> k =
            OptionalDataException.class.getDeclaredConstructor(int.class);
        try {
            k.setAccessible(true);
            check(false, "setAccessible must be refused under strong encapsulation");
        } catch (RuntimeException expected) {
            check(expected instanceof java.lang.reflect.InaccessibleObjectException,
                  "constructor stays inaccessible: " + expected);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
